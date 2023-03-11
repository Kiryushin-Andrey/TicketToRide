package ticketToRide.components.cards

import csstype.*
import emotion.react.css
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.FC
import react.dom.html.ReactHTML.div
import react.useMemo
import ticketToRide.Locale
import ticketToRide.LocalizedStrings
import ticketToRide.components.GameComponentProps
import ticketToRide.components.myCards

val MyCardsComponent = FC<GameComponentProps> { props ->
    val str = useMemo(props.locale) { strings(props.locale) }

    Typography {
        sx { paddingLeft = 10.px }
        variant = TypographyVariant.h6
        +str.header
    }

    div {
        css {
            display = Display.flex
            flexDirection = FlexDirection.row
            flexWrap = FlexWrap.wrap
        }
        for ((card, count) in props.myCards.groupingBy { it }.eachCount()) {
            div {
                css {
                    display = Display.flex
                    flexDirection = FlexDirection.row
                    flexWrap = FlexWrap.nowrap
                    alignItems = AlignItems.center
                    margin = 12.px
                }
                myCard(card, props.locale)
                div {
                    css {
                        fontSize = FontSize.large
                    }
                    +count.toString()
                }
            }
        }
    }
}

private fun strings(locale: Locale) = object : LocalizedStrings({ locale }) {
    val header by loc(
        Locale.En to "My cards",
        Locale.Ru to "Мои карты"
    )
}
