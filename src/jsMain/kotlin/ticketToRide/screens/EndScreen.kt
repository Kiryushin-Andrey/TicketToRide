package ticketToRide.screens

import com.ccfraser.muirwik.components.*
import kotlinx.css.*
import kotlinx.css.Color
import react.*
import styled.StyleSheet
import styled.css
import styled.styledDiv
import ticketToRide.*
import ticketToRide.components.*

interface EndScreenProps : RProps {
    var gameMap: GameMap
    var players: List<PlayerFinalStats>
}

interface EndScreenState : RState {
    var citiesToHighlight: Set<CityName>
}

class EndScreen(props: EndScreenProps) : RComponent<EndScreenProps, EndScreenState>(props) {
    override fun EndScreenState.init(props: EndScreenProps) {
        citiesToHighlight = emptySet()
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                height = 100.pct
                width = 100.pct
                display = Display.grid
                gridTemplateColumns = GridTemplateColumns(GridAutoRows("0.2fr"), GridAutoRows.auto, GridAutoRows(360.px))
            }

            styledDiv {
                css {
                    +ComponentStyles.verticalPanel
                    put("resize", "horizontal")
                }

                for (player in props.players) {
                    render(player)
                    horizontalDivider()
                }
            }

            finalMap {
                gameMap = props.gameMap
                players = props.players.map { it.playerView }
                citiesToHighlight = state.citiesToHighlight
                onCityMouseOver = { setState { citiesToHighlight += it } }
                onCityMouseOut = { setState { citiesToHighlight -= it } }
            }

            styledDiv {
                css {
                    +ComponentStyles.verticalPanel
                }

                chat { }
            }
        }
    }

    private fun RBuilder.render(player: PlayerFinalStats) {
        mPaper {
            attrs {
                elevation = 2
            }
            css {
                margin = 4.px.toString()
            }
            styledDiv {
                css {
                    +PlayersList.ComponentStyles.playerCard
                    backgroundColor = Color(player.color.rgb).withAlpha(0.4)
                }
                mTypography(variant = MTypographyVariant.h6) {
                    +player.name.value
                }
                styledDiv {
                    css { +PlayersList.ComponentStyles.playerCardIcons }
                    playerCardIcon("/icons/railway-car.png", player.carsLeft)
                    playerCardIcon("/icons/ticket.png", player.ticketsCount)
                }
            }
            for (ticket in player.fulfilledTickets) {
                ticket(ticket) {
                    onMouseOver = { setState { citiesToHighlight += listOf(ticket.from, ticket.to) } }
                    onMouseOut = { setState { citiesToHighlight -= listOf(ticket.from, ticket.to) } }
                }
            }
            for (ticket in player.unfulfilledTickets) {
                ticket(ticket) {
                    onMouseOver = { setState { citiesToHighlight += listOf(ticket.from, ticket.to) } }
                    onMouseOut = { setState { citiesToHighlight -= listOf(ticket.from, ticket.to) } }
                }
            }
        }
    }

    private object ComponentStyles : StyleSheet("GameScreen", isStatic = true) {
        val verticalPanel by css {
            display = Display.flex
            flexDirection = FlexDirection.column
            flexWrap = FlexWrap.nowrap
            minWidth = 350.px
            minHeight = LinearDimension.minContent
            overflow = Overflow.auto
        }
    }
}

fun RBuilder.endScreen(builder: EndScreenProps.() -> Unit) {
    child(EndScreen::class) {
        attrs(builder)
    }
}