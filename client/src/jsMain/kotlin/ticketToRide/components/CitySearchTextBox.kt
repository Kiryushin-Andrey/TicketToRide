package ticketToRide.components

import mui.material.*
import react.FC
import react.Props
import react.ReactNode
import react.dom.onChange
import react.useMemo
import ticketToRide.Locale
import ticketToRide.LocalizedStrings

external interface CitySearchTextBoxProps : Props {
    var text: String
    var locale: Locale
    var onTextChanged: (String) -> Unit
    var onEnter: () -> Unit
}

val CitySearchTextBox = FC<CitySearchTextBoxProps> { props ->
    val str = useMemo(props.locale) { strings(props.locale) }

    Tooltip {
        title = ReactNode(str.tooltip)
        placement = TooltipPlacement.topStart

        TextField {
            label = ReactNode(str.header)
            value = props.text
            onChange = { e ->
                val inputValue = e.target.asDynamic().value.unsafeCast<String>()
                props.onTextChanged(inputValue)
            }
            onKeyDown = { e ->
                when (e.key) {
                    "Enter" -> props.onEnter() // Enter
                    "Escape" -> props.onTextChanged("")   // Esc
                }
            }
            size = Size.small
            fullWidth = true
            variant = FormControlVariant.outlined
            margin = FormControlMargin.dense
        }
    }
}

private fun strings(locale: Locale) = object : LocalizedStrings({ locale }) {
    val tooltip by loc(
        Locale.En to "Enter - pick city, Esc - clear input",
        Locale.Ru to "Enter - выбрать город, Esc - сбросить ввод"
    )

    val header by loc(
        Locale.En to "Search city",
        Locale.Ru to "Поиск города"
    )
}
