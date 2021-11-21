package ticketToRide.components

import com.ccfraser.muirwik.components.TooltipPlacement
import com.ccfraser.muirwik.components.form.MFormControlMargin
import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ccfraser.muirwik.components.form.margin
import com.ccfraser.muirwik.components.form.variant
import com.ccfraser.muirwik.components.mTextField
import com.ccfraser.muirwik.components.mTooltip
import com.ccfraser.muirwik.components.targetInputValue
import react.RBuilder
import react.RComponent
import react.Props
import react.State
import ticketToRide.Locale
import ticketToRide.LocalizedStrings

external interface CitySearchTextBoxProps : Props {
    var text: String
    var locale: Locale
    var onTextChanged: (String) -> Unit
    var onEnter: () -> Unit
}

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
class CitySearchTextBox : RComponent<CitySearchTextBoxProps, State>() {

    override fun RBuilder.render() {
        mTooltip(str.tooltip, TooltipPlacement.topStart) {
            mTextField(str.header) {
                attrs {
                    value = props.text
                    onChange = { e ->
                        props.onTextChanged(e.targetInputValue.trim())
                    }
                    onKeyDown = { e ->
                        when (e.keyCode) {
                            13 -> props.onEnter() // Enter
                            27 -> props.onTextChanged("")   // Esc
                        }
                    }
                    asDynamic().size = "small"
                    fullWidth = true
                    variant = MFormControlVariant.outlined
                    margin = MFormControlMargin.dense
                }
            }
        }
    }

    private inner class Strings : LocalizedStrings({ props.locale }) {

        val tooltip by loc(
            Locale.En to "Enter - pick city, Esc - clear input",
            Locale.Ru to "Enter - выбрать город, Esc - сбросить ввод"
        )

        val header by loc(
            Locale.En to "Search city",
            Locale.Ru to "Поиск города"
        )
    }

    private val str = Strings()
}

fun RBuilder.searchTextBox(locale: Locale, builder: CitySearchTextBoxProps.() -> Unit) = child(CitySearchTextBox::class) {
    attrs {
        this.locale = locale
        builder()
    }
}