package ticketToRide.components.building

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv
import styled.styledLabel
import ticketToRide.Card
import ticketToRide.Locale
import ticketToRide.LocalizedStrings
import ticketToRide.components.cards.myCard

class OptionsForCardsToDropComponent : RComponent<OptionsForCardsToDropComponent.Props, RState>() {

    interface Props : RProps {
        var locale: Locale
        var options: List<List<Card>>
        var chosenCardsToDropIx: Int?
        var confirmBtnTitle: String
        var onChooseCards: (Int) -> Unit
        var onConfirm: () -> Unit
    }

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
                        props.options[0].forEach { myCard(it, props.locale) }
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
                    for ((ix, cards) in props.options.withIndex()) {
                        styledLabel {
                            mPaper {
                                attrs {
                                    elevation = 4
                                }
                                css {
                                    display = Display.flex
                                    flexDirection = FlexDirection.row
                                    flexWrap = FlexWrap.nowrap
                                    alignItems = Align.center
                                    borderRadius = 4.px
                                    paddingLeft = 10.px
                                    marginBottom = 8.px
                                }
                                mRadio {
                                    attrs {
                                        checked = ix == props.chosenCardsToDropIx
                                        onClick = { props.onChooseCards(ix) }
                                    }
                                }
                                cards.forEach { myCard(it, props.locale) }
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

    private inner class Strings : LocalizedStrings({ props.locale }) {
        val notEnoughCardsOnHand by loc(
            Locale.En to "Not enough cards on hand \uD83D\uDE1E",
            Locale.Ru to "Не хватает карт \uD83D\uDE1E"
        )
    }

    private val str = Strings()
}

fun RBuilder.optionsForCardsToDrop(locale: Locale, builder: OptionsForCardsToDropComponent.Props.() -> Unit) {
    child(OptionsForCardsToDropComponent::class) {
        attrs {
            this.locale = locale
            builder()
        }
    }
}