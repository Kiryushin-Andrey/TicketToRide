package ticketToRide.components.welcomeScreen

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.*
import kotlinx.css.*
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import org.w3c.files.get
import react.*
import react.dom.attrs
import styled.*
import ticketToRide.*

data class CustomGameMap(val filename: String, val map: GameMap)
data class CustomGameMapParseErrors(val filename: String, val errors: List<GameMapParseError>)

external interface ChooseGameMapComponentProps : Props {
    var locale: Locale
    var customMap: CustomGameMap?
    var onCustomMapChanged: (CustomGameMap?) -> Unit
    var onShowParseErrors: (CustomGameMapParseErrors) -> Unit
}

external interface ChooseGameMapComponentState : State {
    var fileTooLarge: Boolean
    var errors: CustomGameMapParseErrors?
}

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
class ChooseGameMapComponent : RComponent<ChooseGameMapComponentProps, ChooseGameMapComponentState>() {

    override fun RBuilder.render() {

        styledDiv {
            css {
                display = Display.flex
                flexDirection = FlexDirection.row
                justifyContent = JustifyContent.spaceBetween
                alignItems = Align.center
            }
            styledLabel {
                css {
                    cursor = Cursor.pointer
                    display = Display.inlineFlex
                    alignItems = Align.center
                    marginLeft = (-11).px
                }
                mRadio {
                    attrs {
                        color = MOptionColor.primary
                        size = MIconButtonSize.small
                        checked = props.customMap == null
                        onChange = { _, value -> if (value) props.onCustomMapChanged(null) }
                    }
                }
                mTypography(str.builtinMap, MTypographyVariant.body1)
                mTooltip(str.downloadSample) {
                    mIconButton("cloud_download") {
                        attrs {
                            href = "/default.map"
                        }
                    }
                }
            }
            styledInput(InputType.file) {
                attrs {
                    id = "map-file-input"
                    css { display = Display.none }
                    onChangeFunction = { e ->
                        (e.target as HTMLInputElement).files?.get(0)?.let { file ->
                            if (file.size.toInt() > maxFileSizeBytes)
                                setState {
                                    errors = null
                                    fileTooLarge = true
                                }
                            else
                                FileReader().apply {
                                    onload = { e ->
                                        (e.target.asDynamic().result as? String)?.let {
                                            when (val map = GameMap.parse(it)) {
                                                is Try.Success -> {
                                                    props.onCustomMapChanged(
                                                        CustomGameMap(
                                                            file.name,
                                                            map.value
                                                        )
                                                    )
                                                    setState {
                                                        fileTooLarge = false
                                                        errors = null
                                                    }
                                                }
                                                is Try.Error -> {
                                                    props.onCustomMapChanged(null)
                                                    setState {
                                                        fileTooLarge = false
                                                        errors =
                                                            CustomGameMapParseErrors(
                                                                file.name,
                                                                map.errors
                                                            )
                                                    }
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
            styledLabel {
                attrs {
                    htmlFor = "map-file-input"
                    css {
                        display = Display.flex
                        flexDirection = FlexDirection.row
                        alignItems = Align.center
                    }
                }
                props.customMap?.let {
                    mRadioWithLabel(it.filename, true, MOptionColor.primary, size = MIconButtonSize.small)
                    mIconButton("cloud_upload") {
                        attrs { component = "span" }
                    }
                } ?: mButton(str.uploadMap) {
                    attrs {
                        color = MColor.primary
                        variant = MButtonVariant.contained
                        size = MButtonSize.small
                        component = "span"
                        startIcon = buildElement { mIcon("cloud_upload") }
                    }
                }
            }
        }

        if (!state.fileTooLarge && state.errors == null) {
            mTypography(str.customMapHint, MTypographyVariant.caption)
        }

        if (state.fileTooLarge) {
            mTypography(str.fileTooLarge, color = MTypographyColor.error)
        }

        state.errors?.let { err ->
            styledDiv {
                css {
                    display = Display.flex
                    flexDirection = FlexDirection.row
                    justifyContent = JustifyContent.spaceBetween
                    alignItems = Align.center
                }
                mTypography(str.parsingFailed(err.filename), color = MTypographyColor.error)
                mButton(str.viewParseErrors) {
                    attrs {
                        color = MColor.secondary
                        variant = MButtonVariant.contained
                        size = MButtonSize.small
                        onClick = { props.onShowParseErrors(err) }
                    }
                }
            }
        }
    }

    private inner class Strings : LocalizedStrings({ props.locale }) {

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

    private val str = Strings()

    private val maxFileSizeBytes = 1024 * 1024
}

fun RBuilder.chooseGameMap(locale: Locale, builder: ChooseGameMapComponentProps.() -> Unit) =
    child(ChooseGameMapComponent::class) {
        attrs {
            this.locale = locale
            builder()
        }
    }