package ticketToRide.components.building

import csstype.*
import emotion.react.css
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.label
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

val OptionsForCardsToDropComponent = FC<OptionsForCardsToDropComponentProps> { props ->
    val str = useMemo(props.locale) { strings(props.locale) }

    when {
        props.options.isEmpty() ->
            Typography {
                sx { marginTop = 10.px }
                variant = TypographyVariant.body1
                +str.notEnoughCardsOnHand
            }

        props.options.size == 1 -> {
            div {
                css {
                    marginTop = 10.px
                    display = Display.flex
                    justifyContent = JustifyContent.spaceBetween
                }
                div {
                    css {
                        display = Display.inlineFlex
                        justifyContent = JustifyContent.left
                    }
                    props.options[0].cards.forEach { myCard(it, props.locale) }
                }
                confirmButton(props)
            }
        }

        else -> {
            div {
                css {
                    marginTop = 10.px
                    display = Display.flex
                    flexDirection = FlexDirection.column
                    flexWrap = FlexWrap.nowrap
                }

                for ((ix, option) in props.options.withIndex()) {
                    label {
                        Paper {
                            sx {
                                borderRadius = 4.px
                                paddingLeft = 10.px
                                marginBottom = 8.px
                            }
                            elevation = 4

                            val fitsSeveralSegments = props.options.any { option2 ->
                                option.hasSameCardsAs(option2) && option.segmentColor != option2.segmentColor
                            }
                            if (fitsSeveralSegments) {
                                sx {
                                    display = Display.flex
                                    flexWrap = FlexWrap.nowrap
                                    flexDirection = FlexDirection.column
                                    alignItems = AlignItems.flexStart
                                    backgroundColor = Color(toSegmentRgb(option.segmentColor) + "66")
                                }
                                cardsToDrop(option, ix, props)
                                Typography {
                                    sx {
                                        paddingLeft = 40.px
                                    }
                                    variant = TypographyVariant.body1
                                    +str.forSegmentColor(getSegmentColorName(option.segmentColor, props.locale))
                                }
                            } else {
                                cardsToDrop(option, ix, props)
                            }
                        }
                    }
                }
                confirmButton(props)
            }
        }
    }
}

private fun strings(locale: Locale) = object : LocalizedStrings({ locale }) {
    val notEnoughCardsOnHand by loc(
        Locale.En to "Not enough cards on hand \uD83D\uDE1E",
        Locale.Ru to "Не хватает карт \uD83D\uDE1E"
    )
    val forSegmentColor by locWithParam<String>(
        Locale.En to { segmentColor -> "for $segmentColor segment" },
        Locale.Ru to { segmentColor -> "на $segmentColor путь" }
    )
}

private fun ChildrenBuilder.confirmButton(props: OptionsForCardsToDropComponentProps) {
    Button {
        +props.confirmBtnTitle

        color = ButtonColor.primary
        variant = ButtonVariant.contained
        disabled = props.options.size > 1 && props.chosenCardsToDropIx == null
        onClick = { props.onConfirm() }
        if (props.options.size > 1) {
            css { marginTop = 10.px }
        }
    }
}

private fun ChildrenBuilder.cardsToDrop(option: OptionForCardsToDrop, ix: Int, props: OptionsForCardsToDropComponentProps) {
    div {
        css {
            display = Display.flex
            flexWrap = FlexWrap.nowrap
            flexDirection = FlexDirection.row
            alignItems = AlignItems.center
        }

        Radio {
            checked = ix == props.chosenCardsToDropIx
            onClick = { props.onChooseCards(ix) }
        }
        option.cards.forEach { myCard(it, props.locale) }
    }
}
