package ticketToRide.components.building

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import kotlinx.css.*
import react.RBuilder
import react.RComponent
import react.Props
import react.State
import styled.*
import ticketToRide.*
import ticketToRide.components.cards.myCard

external interface OptionsForCardsToDropComponentProps : Props {
    var locale: Locale
    var options: List<OptionForCardsToDrop>
    var chosenCardsToDropIx: Int?
    var confirmBtnTitle: String
    var onChooseCards: (Int) -> Unit
    var onConfirm: () -> Unit
}

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
class OptionsForCardsToDropComponent : RComponent<OptionsForCardsToDropComponentProps, State>() {

    override fun RBuilder.render() {
        when {
            props.options.isEmpty() ->
                mTypography(str.notEnoughCardsOnHand, MTypographyVariant.body1) {
                    css { marginTop = 10.px }
                }

            props.options.size == 1 -> {
                styledDiv {
                    css {
                        marginTop = 10.px
                        display = Display.flex
                        justifyContent = JustifyContent.spaceBetween
                    }
                    styledDiv {
                        css {
                            display = Display.inlineFlex
                            justifyContent = JustifyContent.left
                        }
                        props.options[0].cards.forEach { myCard(it, props.locale) }
                    }
                    confirmButton()
                }
            }

            else -> {
                styledDiv {
                    css {
                        marginTop = 10.px
                        display = Display.flex
                        flexDirection = FlexDirection.column
                        flexWrap = FlexWrap.nowrap
                    }

                    for ((ix, option) in props.options.withIndex()) {
                        styledLabel {
                            mPaper {
                                attrs {
                                    elevation = 4
                                }
                                css {
                                    borderRadius = 4.px
                                    paddingLeft = 10.px
                                    marginBottom = 8.px
                                }

                                val fitsSeveralSegments = props.options.any { option2 ->
                                    option.hasSameCardsAs(option2) && option.segmentColor != option2.segmentColor
                                }
                                if (fitsSeveralSegments) {
                                    css {
                                        display = Display.flex
                                        flexWrap = FlexWrap.nowrap
                                        flexDirection = FlexDirection.column
                                        alignItems = Align.flexStart
                                        backgroundColor = Color(toSegmentRgb(option.segmentColor)).withAlpha(0.4)
                                    }
                                    cardsToDrop(option, ix)
                                    mTypography(
                                            str.forSegmentColor(getSegmentColorName(option.segmentColor, props.locale)),
                                            variant = MTypographyVariant.body1) {
                                        css {
                                            paddingLeft = 40.px
                                        }
                                    }
                                } else {
                                    cardsToDrop(option, ix)
                                }
                            }
                        }
                    }
                    confirmButton()
                }
            }
        }
    }

    private fun RBuilder.confirmButton() {
        mButton(props.confirmBtnTitle, MColor.primary, MButtonVariant.contained) {
            if (props.options.size > 1) {
                css { marginTop = 10.px }
            }
            attrs {
                disabled = props.options.size > 1 && props.chosenCardsToDropIx == null
                onClick = { props.onConfirm() }
            }
        }
    }

    private fun StyledElementBuilder<*>.cardsToDrop(option: OptionForCardsToDrop, ix: Int) {
        styledDiv {
            css {
                display = Display.flex
                flexWrap = FlexWrap.nowrap
                flexDirection = FlexDirection.row
                alignItems = Align.center
            }

            mRadio {
                attrs {
                    checked = ix == props.chosenCardsToDropIx
                    onClick = { props.onChooseCards(ix) }
                }
            }
            option.cards.forEach { myCard(it, props.locale) }
        }
    }

    private inner class Strings : LocalizedStrings({ props.locale }) {
        val notEnoughCardsOnHand by loc(
                Locale.En to "Not enough cards on hand \uD83D\uDE1E",
                Locale.Ru to "Не хватает карт \uD83D\uDE1E"
        )
        val forSegmentColor by locWithParam<String>(
                Locale.En to { segmentColor -> "for $segmentColor segment" },
                Locale.Ru to { segmentColor -> "на $segmentColor путь" }
        )
    }

    private val str = Strings()
}

fun RBuilder.optionsForCardsToDrop(locale: Locale, builder: OptionsForCardsToDropComponentProps.() -> Unit) {
    child(OptionsForCardsToDropComponent::class) {
        attrs {
            this.locale = locale
            builder()
        }
    }
}