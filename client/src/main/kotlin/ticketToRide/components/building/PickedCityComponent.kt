package ticketToRide.components.building

import csstype.*
import emotion.react.css
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import ticketToRide.Locale
import ticketToRide.LocalizedStrings
import ticketToRide.components.*
import ticketToRide.localize
import ticketToRide.playerState.PlayerState

val PickedCityComponent = FC<GameComponentProps> { props ->
    val str = useMemo(props.locale) { strings(props.locale) }
    val playerState = props.playerState as PlayerState.MyTurn.PickedCity

    div {
        css {
            paddingLeft = 10.px
            paddingRight = 10.px
        }
        div {
            css {
                display = Display.flex
                flexDirection = FlexDirection.row
                flexWrap = FlexWrap.nowrap
                justifyContent = JustifyContent.spaceBetween
                alignItems = AlignItems.center
            }
            img {
                src = "/icons/building-segment.png"
                height = 100.0
            }
            div {
                css {
                    display = Display.flex
                    flexDirection = FlexDirection.column
                    justifyContent = JustifyContent.spaceBetween
                    height = 100.px
                }
                with(props.playerState as PlayerState.MyTurn.PickedCity) {
                    Typography {
                        +target.localize(props.locale, props.gameMap)
                        variant = TypographyVariant.h6
                        sx {
                            textAlign = TextAlign.right
                            paddingRight = 16.px
                        }
                    }
                    Button {
                        +str.station

                        color = ButtonColor.primary
                        variant = ButtonVariant.contained
                        onClick = { props.act { playerState.buildStation() } }
                    }
                }
            }
        }
    }
}

private fun strings(locale: Locale) = object : LocalizedStrings({ locale }) {
    val station by loc(
        Locale.En to "Station",
        Locale.Ru to "Станция"
    )
}
