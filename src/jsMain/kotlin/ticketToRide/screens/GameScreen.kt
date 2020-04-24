package ticketToRide.screens

import com.ccfraser.muirwik.components.MDividerVariant
import com.ccfraser.muirwik.components.mDivider
import kotlinx.css.*
import react.*
import styled.StyleSheet
import styled.css
import styled.styledDiv
import ticketToRide.*
import ticketToRide.components.*
import ticketToRide.playerState.BuildingSegment
import ticketToRide.playerState.BuildingSegmentFrom

interface GameScreenProps : ComponentBaseProps {
    var gameMap: GameMap
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
                +ComponentStyles.screen
            }

            styledDiv {
                css {
                    +ComponentStyles.verticalPanel
                    put("resize", "horizontal")
                }

                playersList(players, turn)
                divider()
                chat { }
            }

            mainMap(props) {
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
                divider()

                if (playerState is BuildingSegmentFrom || playerState is BuildingSegment) {
                    buildingSegment(props)
                    divider()
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

    private fun RBuilder.divider() {
        mDivider(variant = MDividerVariant.fullWidth) {
            css {
                margin = 5.px.toString()
            }
        }
    }

    private object ComponentStyles : StyleSheet("GameScreen", isStatic = true) {
        val screen by css {
            height = 100.pct
            width = 100.pct
            display = Display.grid
            gridTemplateColumns = GridTemplateColumns(GridAutoRows("0.2fr"), GridAutoRows.auto, GridAutoRows(360.px))
            gridTemplateRows = GridTemplateRows(GridAutoRows.auto, GridAutoRows(120.px))
        }
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

fun RBuilder.gameScreen(builder: GameScreenProps.() -> Unit) {
    child(GameScreen::class) {
        attrs {
            builder()
        }
    }
}