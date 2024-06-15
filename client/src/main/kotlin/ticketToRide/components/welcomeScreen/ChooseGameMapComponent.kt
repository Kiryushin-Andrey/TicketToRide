package ticketToRide.components.welcomeScreen

import csstype.*
import emotion.react.css
import js.core.get
import js.uri.encodeURIComponent
import kotlinx.browser.window
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import mui.base.ClickAwayListener
import mui.icons.material.*
import mui.material.*
import mui.material.Size
import mui.material.styles.TypographyVariant
import mui.system.sx
import popper.core.Placement
import react.*
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import ticketToRide.*
import web.file.FileReader
import web.html.HTMLElement
import web.html.InputType

data class CustomGameMapParseErrors(val filename: String, val errors: List<GameMapParseError>)

external interface ChooseGameMapComponentProps : Props {
    var locale: Locale
    var map: ConnectRequest.StartGameMap?
    var onMapChanged: (ConnectRequest.StartGameMap?) -> Unit
    var onShowParseErrors: (CustomGameMapParseErrors) -> Unit
}

val ChooseGameMapComponent = FC<ChooseGameMapComponentProps> { props ->
    val str = useMemo(props.locale) { strings(props.locale) }
    var fileTooLarge by useState(false)
    var errors by useState<CustomGameMapParseErrors?>(null)

    var isChooseMapMenuOpen by useState(false)
    val chooseMapButtonRef = useRef<HTMLElement>()
    var selectedMap by useState { listOf("default") }
    var mapsTree by useState<MapsTreeItem.Folder?> { null }
    var currentFolder by useState<List<MapsTreeItem.Folder>?> { null }
    useEffect(Unit) {
        window.fetch("/maps").then { response ->
            if (response.ok) {
                response.text().then { json ->
                    Json.decodeFromString<MapsTreeItem.Folder>(json).let {
                        mapsTree = it
                        currentFolder = listOf(it)
                    }
                }
            }
        }
    }

    InputLabel {
        +str.gameMap
    }

    div {
        css {
            display = Display.flex
            flexDirection = FlexDirection.row
            justifyContent = JustifyContent.spaceBetween
            alignItems = AlignItems.center
            marginBottom = 8.px
        }
        label {
            css {
                cursor = Cursor.pointer
                display = Display.inlineFlex
                alignItems = AlignItems.center
            }
            Typography {
                variant = TypographyVariant.body1
                if (props.map == null) {
                    asDynamic().color = "text.error"
                }
                sx { paddingRight = 8.px }
                +(when (val map = props.map) {
                    is ConnectRequest.StartGameMap.BuiltIn -> map.path.last()
                    is ConnectRequest.StartGameMap.Custom -> map.filename
                    null -> "not selected"
                })
            }

            mapsTree?.takeUnless { it.children.isEmpty() }?.let { mapsTree ->
                Tooltip {
                    title = ReactNode(str.chooseMap)
                    a {
                        css { paddingRight = 8.px }
                        ref = chooseMapButtonRef
                        onClick = { e ->
                            e.preventDefault()
                            isChooseMapMenuOpen = true
                            currentFolder = listOf(mapsTree)
                        }
                        FolderOpen()
                    }
                }

                Popper {
                    open = isChooseMapMenuOpen
                    anchorEl = chooseMapButtonRef.current
                    placement = Placement.rightStart
                    sx { zIndex = integer(1600) }

                    Paper {
                        sx {
                            height = 420.px
                            width = 360.px
                            overflow = Auto.auto
                        }

                        ClickAwayListener {
                            onClickAway = { isChooseMapMenuOpen = false }

                            MenuList {
                                if ((currentFolder?.size ?: 0) > 1) {
                                    MenuItem {
                                        divider = true
                                        onClick = {
                                            currentFolder = currentFolder.orEmpty().dropLast(1)
                                        }
                                        ListItemIcon {
                                            ChevronLeft()
                                        }
                                        ListItemText {
                                            primary = ReactNode(str.back)
                                        }
                                    }
                                }
                                currentFolder?.lastOrNull()?.children?.filterIsInstance<MapsTreeItem.Folder>()
                                    ?.forEach { folder ->
                                        MenuItem {
                                            onClick = {
                                                currentFolder = currentFolder.orEmpty() + folder
                                            }

                                            ListItemIcon { Folder() }
                                            ListItemText {
                                                primary = ReactNode(folder.name)
                                            }
                                            ListItemIcon {
                                                sx { justifyContent = JustifyContent.end }
                                                ChevronRight()
                                            }
                                        }
                                    }

                                currentFolder?.lastOrNull()?.children?.filterIsInstance<MapsTreeItem.Map>()
                                    ?.forEach { map ->
                                        MenuItem {
                                            onClick = {
                                                isChooseMapMenuOpen = false
                                                (currentFolder!!.drop(1).map { it.name } + map.name).let {
                                                    selectedMap = it
                                                    props.onMapChanged(ConnectRequest.StartGameMap.BuiltIn(it))
                                                }
                                            }
                                            ListItemIcon { Map() }
                                            ListItemText {
                                                primary = ReactNode(map.name)
                                            }
                                        }
                                    }
                            }
                        }
                    }
                }
            }

            Tooltip {
                title = ReactNode(str.downloadSample)
                a {
                    href = "/maps/${selectedMap.joinToString("/") { encodeURIComponent(it) }}"
                    Download()
                }
            }
        }

        input {
            type = InputType.file
            id = "map-file-input"
            css { display = None.none }
            onChange = { e ->
                e.target.files?.get(0)?.let { file ->
                    if (file.size.toInt() > maxFileSizeBytes) {
                        errors = null
                        fileTooLarge = true
                    } else {
                        FileReader().apply {
                            onload = { e ->
                                (e.target.asDynamic().result as? String)?.let {
                                    when (val map = GameMap.parse(it)) {
                                        is Try.Success -> {
                                            props.onMapChanged(ConnectRequest.StartGameMap.Custom(file.name, map.value))
                                            fileTooLarge = false
                                            errors = null
                                        }

                                        is Try.Error -> {
                                            props.onMapChanged(null)
                                            fileTooLarge = false
                                            errors = CustomGameMapParseErrors(file.name, map.errors)
                                        }
                                    }
                                }
                            }
                            readAsText(file)
                        }
                    }
                }
            }
        }
        label {
            htmlFor = "map-file-input"
            css {
                display = Display.flex
                flexDirection = FlexDirection.row
                alignItems = AlignItems.center
            }
            Button {
                +str.uploadMap
                color = ButtonColor.primary
                variant = ButtonVariant.outlined
                size = Size.small
                asDynamic().component = "span"
                startIcon = createElement(CloudUpload)
            }
        }
    }

    if (!fileTooLarge && errors == null) {
        Typography {
            variant = TypographyVariant.caption
            +str.customMapHint
        }
    }

    if (fileTooLarge) {
        Typography {
            asDynamic().color = "text.error"
            +str.fileTooLarge
        }
    }

    errors?.let { err ->
        div {
            css {
                display = Display.flex
                flexDirection = FlexDirection.row
                justifyContent = JustifyContent.spaceBetween
                alignItems = AlignItems.center
            }

            Typography {
                +str.parsingFailed(err.filename)
                asDynamic().color = "text.error"
            }
            Button {
                +str.viewParseErrors
                color = ButtonColor.secondary
                variant = ButtonVariant.contained
                size = Size.small
                onClick = { props.onShowParseErrors(err) }
            }
        }
    }
}

private const val maxFileSizeBytes = 1024 * 1024

private fun strings(locale: Locale) = object : LocalizedStrings({ locale }) {

    val gameMap by loc(
        Locale.En to "Game map",
        Locale.Ru to "Карта для игры"
    )

    val chooseMap by loc(
        Locale.En to "Choose another game map",
        Locale.Ru to "Выбрать карту для игры"
    )

    val back by loc(
        Locale.En to "Back",
        Locale.Ru to "Назад"
    )

    val downloadSample by loc(
        Locale.En to "Download as file",
        Locale.Ru to "Скачать файл"
    )

    val uploadMap by loc(
        Locale.En to "Upload my map",
        Locale.Ru to "Загрузить"
    )

    val customMapHint by loc(
        Locale.En to "You can create and upload your own game map. Download the built-in map file to see a sample",
        Locale.Ru to "Вы можете создать и загрузить свою карту для игры. Скачайте готовую карту чтобы посмотреть формат файла"
    )

    val parsingFailed by locWithParam<String>(
        Locale.En to { filename -> "Failed to load map from \"$filename\"" },
        Locale.Ru to { filename -> "Не удалось загрузить карту из файла \"$filename\"" }
    )

    val viewParseErrors by loc(
        Locale.En to "View details",
        Locale.Ru to "Подробнее"
    )

    val fileTooLarge by loc(
        Locale.En to "File exceeds the maximum allowed size of 1 Mb",
        Locale.Ru to "Файл превышает максимальный размер в 1 Мб"
    )
}
