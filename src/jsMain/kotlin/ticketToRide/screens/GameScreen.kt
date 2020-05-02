package ticketToRide.screens

import com.ccfraser.muirwik.components.*
import kotlinx.css.*
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLSelectElement
import react.*
import react.dom.option
import react.dom.select
import styled.*
import ticketToRide.*
import ticketToRide.CardColor
import ticketToRide.components.*
import ticketToRide.playerState.BuildingSegment
import ticketToRide.playerState.BuildingSegmentFrom

interface GameScreenProps : ComponentBaseProps {
    var gameMap: GameMap
    var chatMessages: List<Response.ChatMessage>
    var onSendMessage: (String) -> Unit
}

interface GameScreenState : RState {
    var citiesToHighlight: Set<CityName>
}

class GameScreen : ComponentBase<GameScreenProps, GameScreenState>() {
    override fun GameScreenState.init() {
        citiesToHighlight = emptySet()
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                height = 100.pct
                width = 100.pct
                display = Display.grid
                gridTemplateColumns = GridTemplateColumns(GridAutoRows("0.2fr"), GridAutoRows.auto, GridAutoRows(360.px))
                gridTemplateRows = GridTemplateRows(GridAutoRows.auto, GridAutoRows(120.px))
            }

            styledDiv {
                css {
                    +ComponentStyles.verticalPanel
                    put("resize", "horizontal")
                }

                if (lastRound) {
                    mTypography("Последний круг", MTypographyVariant.h6, color = MTypographyColor.secondary) {
                        css {
                            marginLeft = 5.px
                        }
                    }
                    horizontalDivider()
                }

                playersList(players, turn)
                horizontalDivider()
                chat(props.chatMessages, props.onSendMessage)
            }

            gameMap(props) {
                gameMap = props.gameMap
                citiesToHighlight = state.citiesToHighlight
                onCityMouseOver = { setState { citiesToHighlight += it } }
                onCityMouseOut = { setState { citiesToHighlight -= it } }
            }

            styledDiv {
                css {
                    +ComponentStyles.verticalPanel
                }

                myCards(props)
                horizontalDivider()

                if (playerState is BuildingSegmentFrom || playerState is BuildingSegment) {
                    buildingSegment(props)
                    horizontalDivider()
                }

                myTickets(props) {
                    citiesToHighlight = state.citiesToHighlight
                    onTicketMouseOver = { setState { citiesToHighlight += listOf(it.from, it.to) } }
                    onTicketMouseOut = { setState { citiesToHighlight -= listOf(it.from, it.to) } }
                }
            }

            cardsDeck(props)
        }
    }

    private object ComponentStyles : StyleSheet("GameScreen", isStatic = true) {
        val verticalPanel by css {
            display = Display.flex
            flexDirection = FlexDirection.column
            flexWrap = FlexWrap.nowrap
            minWidth = 300.px
            minHeight = LinearDimension.minContent
            overflow = Overflow.auto
        }
    }
}

fun RBuilder.horizontalDivider() {
    mDivider(variant = MDividerVariant.fullWidth) {
        css {
            margin = 5.px.toString()
        }
    }
}

fun RBuilder.gameScreen(builder: GameScreenProps.() -> Unit) {
    child(GameScreen::class) {
        attrs {
            builder()
        }
    }
}