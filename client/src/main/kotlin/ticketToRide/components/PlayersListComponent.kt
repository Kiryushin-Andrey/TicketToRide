package ticketToRide.components

import csstype.*
import emotion.react.css
import mui.material.Paper
import mui.material.Tooltip
import mui.material.TooltipPlacement
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import ticketToRide.Locale
import ticketToRide.LocalizedStrings
import ticketToRide.PlayerView
import ticketToRide.components.tickets.pointsLabel

external interface PlayersListComponentProps : Props {
    var players: List<PlayerView>
    var turn: Int
    var locale: Locale
}

private val playersListComponent = FC<PlayersListComponentProps> { props ->
    val str = useMemo(props.locale) { strings(props.locale) }
    for ((ix, player) in props.players.withIndex()) {
        Tooltip {
            title = ReactNode(if (player.away) str.disconnected else "")
            placement = TooltipPlacement.rightStart

            Paper {
                elevation = 2
                sx {
                    color = if (player.away) rgba(red = 0, green = 0, blue = 0, alpha = 0.4) else NamedColor.black
                    backgroundColor = Color(player.color.rgb + "66")
                    minHeight = 60.px
                    margin = 4.px
                    padding = Padding(vertical = 4.px, horizontal = 12.px)
                    if (ix == props.turn) {
                        borderColor = NamedColor.red
                        borderStyle = LineStyle.solid
                        borderWidth = 4.px
                    }
                }


                div {
                    css {
                        width = 100.pct
                        display = Display.flex
                        flexDirection = FlexDirection.row
                        justifyContent = JustifyContent.spaceBetween
                        alignItems = AlignItems.center
                        paddingRight = 16.px
                    }
                    div {
                        css {
                            display = Display.inlineBlock
                            width = 12.px
                            height = 12.px
                            backgroundColor = Color(player.color.rgb)
                            borderRadius = 50.pct
                            marginRight = 8.px
                        }
                    }
                    Typography {
                        variant = TypographyVariant.h6
                        +player.name.value
                    }
                    player.points?.let {
                        pointsLabel(it.toString(), NamedColor.lightyellow)
                    }
                }
                div {
                    css {
                        display = Display.flex
                        flexDirection = FlexDirection.row
                        alignItems = AlignItems.center
                        width = 100.pct
                        justifyContent = JustifyContent.spaceEvenly
                    }

                    playerCardIcon("/icons/railway-car.png", player.carsLeft)
                    playerCardIcon("/icons/station.png", player.stationsLeft)
                    playerCardIcon("/icons/cards-deck.png", player.cardsOnHand)
                    playerCardIcon("/icons/ticket.png", player.ticketsOnHand)
                }
            }
        }
    }
}

private fun strings(locale: Locale) = object : LocalizedStrings({ locale }) {
    val disconnected by loc(
        Locale.En to "Disconnected",
        Locale.Ru to "Отключился"
    )
}

fun ChildrenBuilder.playerCardIcon(iconUrl: String, number: Int) {
    img {
        src = iconUrl
        width = 24.0
    }
    Typography {
        variant = TypographyVariant.body1
        +number.toString()
    }
}

fun ChildrenBuilder.playersList(players: List<PlayerView>, turn: Int, locale: Locale) =
    playersListComponent {
        this.players = players
        this.turn = turn
        this.locale = locale
    }
