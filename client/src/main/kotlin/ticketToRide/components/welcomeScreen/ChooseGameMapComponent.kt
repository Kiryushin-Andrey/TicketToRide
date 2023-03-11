package ticketToRide.components.welcomeScreen

import csstype.*
import emotion.react.css
import js.core.get
import mui.icons.material.CloudDownload
import mui.icons.material.CloudUpload
import mui.material.*
import mui.material.Size
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import ticketToRide.*
import web.file.FileReader
import web.html.HTMLInputElement
import web.html.InputType

data class CustomGameMap(val filename: String, val map: GameMap)
data class CustomGameMapParseErrors(val filename: String, val errors: List<GameMapParseError>)

external interface ChooseGameMapComponentProps : Props {
    var locale: Locale
    var customMap: CustomGameMap?
    var onCustomMapChanged: (CustomGameMap?) -> Unit
    var onShowParseErrors: (CustomGameMapParseErrors) -> Unit
}

val ChooseGameMapComponent = FC<ChooseGameMapComponentProps> { props ->
    val str = useMemo(props.locale) { strings(props.locale) }
    var fileTooLarge by useState(false)
    var errors by useState<CustomGameMapParseErrors?>(null)

    div {
        css {
            display = Display.flex
            flexDirection = FlexDirection.row
            justifyContent = JustifyContent.spaceBetween
            alignItems = AlignItems.center
        }
        label {
            css {
                cursor = Cursor.pointer
                display = Display.inlineFlex
                alignItems = AlignItems.center
                marginLeft = (-11).px
            }
            Radio {
                color = RadioColor.primary
                size = Size.small
                checked = props.customMap == null
                onChange = { _, value -> if (value) props.onCustomMapChanged(null) }
            }
            Typography {
                variant = TypographyVariant.body1
                sx { paddingRight = 8.px }
                +str.builtinMap
            }
            Tooltip {
                title = ReactNode(str.downloadSample)
                a {
                    href = "/default.map"
                    CloudDownload()
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
                                            props.onCustomMapChanged(CustomGameMap(file.name, map.value))
                                            fileTooLarge = false
                                            errors = null
                                        }

                                        is Try.Error -> {
                                            props.onCustomMapChanged(null)
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
            props.customMap?.let {
                label {
                    Radio {
                        color = RadioColor.primary
                        size = Size.small
                        checked = true
                    }
                    +it.filename
                }
                IconButton {
                    asDynamic().component = "span"
                    CloudUpload()
                }
            } ?: Button {
                +str.uploadMap
                color = ButtonColor.primary
                variant = ButtonVariant.contained
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

private val maxFileSizeBytes = 1024 * 1024

private fun strings(locale: Locale) = object : LocalizedStrings({ locale }) {

    val builtinMap by loc(
        Locale.En to "Built-in map of Russia",
        Locale.Ru to "Карта России"
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
