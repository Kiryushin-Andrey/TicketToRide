package ticketToRide.components

import com.ccfraser.muirwik.components.TooltipPlacement
import com.ccfraser.muirwik.components.form.MFormControlMargin
import com.ccfraser.muirwik.components.form.MFormControlVariant
import com.ccfraser.muirwik.components.form.margin
import com.ccfraser.muirwik.components.form.variant
import com.ccfraser.muirwik.components.input.MInputProps
import com.ccfraser.muirwik.components.mTextField
import com.ccfraser.muirwik.components.mTooltip
import com.ccfraser.muirwik.components.targetInputValue
import kotlinext.js.jsObject
import kotlinx.css.*
import react.*
import react.dom.input
import styled.*

interface SearchTextBoxProps : RProps {
    var text: String
    var onTextChanged: (String) -> Unit
    var onEnter: () -> Unit
}

class SearchTextBox : RComponent<SearchTextBoxProps, RState>() {
    override fun RBuilder.render() {
        mTooltip("Enter - выбрать город, Esc - сбросить ввод", TooltipPlacement.topStart) {
            mTextField("Поиск") {
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

    private object ComponentStyles : StyleSheet("SearchTextBox", isStatic = true) {
        val input by css {
            padding = 12.px.toString()
        }
    }
}

fun RBuilder.searchTextBox(builder: SearchTextBoxProps.() -> Unit) = child(SearchTextBox::class) {
    attrs(builder)
}