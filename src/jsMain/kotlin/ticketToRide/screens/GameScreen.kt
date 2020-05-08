package ticketToRide.screens

import com.ccfraser.muirwik.components.*
import kotlinx.css.*
import react.*
import styled.*
import ticketToRide.*
import ticketToRide.components.*
import ticketToRide.playerState.*

interface GameScreenProps : ComponentBaseProps {
    var gameMap: GameMap
    var chatMessages: List<Response.ChatMessage>
    var onSendMessage: (String) -> Unit
}

interface GameScreenState : RState {
    var citiesToHighlight: Set<CityName>
    var searchText: String
}

class GameScreen : ComponentBase<GameScreenProps, GameScreenState>() {
    override fun GameScreenState.init() {
        citiesToHighlight = emptySet()
        searchText = ""
    }

    override fun RBuilder.render() {
        val rows = listOf(GridAutoRows(40.px), GridAutoRows.auto, GridAutoRows(65.px), GridAutoRows(120.px))
        val areas = listOf(
            "left header right",
            "left map right",
            "send map search",
            "cards cards cards")
        styledDiv {
            css {
                height = 100.pct
                width = 100.pct
                display = Display.grid
                gridTemplateColumns = GridTemplateColumns(GridAutoRows("0.2fr"), GridAutoRows.auto, GridAutoRows(360.px))
                gridTemplateRows = GridTemplateRows(*rows.toTypedArray())
                gridTemplateAreas = GridTemplateAreas(areas.joinToString(" ") { "\"$it\"" })
            }

            when {
                myTurn -> headerMessage("Ваш ход", Color.lightGreen)
                lastRound -> headerMessage("Последний круг", Color.darkSalmon)
                else -> headerMessage("Ходит ${players[turn].name.value}", Color.white)
            }

            styledDiv {
                css {
                    put("grid-area", "left")
                    +ComponentStyles.verticalPanel
                    put("resize", "horizontal")
                }

                playersList(players, turn)
                horizontalDivider()
                chatMessages(props.chatMessages)
            }

            styledDiv {
                css {
                    put("grid-area", "send")
                }
                chatSendMessageTextBox(props.onSendMessage)
            }

            styledDiv {
                css {
                    put("grid-area", "map")
                    width = 100.pct
                    height = 100.pct
                }
                gameMap(props) {
                    gameMap = props.gameMap
                    citiesToHighlight = state.citiesToHighlight + getCitiesBySearchText()
                    onCityMouseOver = { setState { citiesToHighlight += it } }
                    onCityMouseOut = { setState { citiesToHighlight -= it } }
                }
            }

            styledDiv {
                css {
                    put("grid-area", "right")
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

            styledDiv {
                css {
                    put("grid-area", "search")
                    width = 96.pct
                    marginLeft = 4.px
                }
                horizontalDivider()
                searchTextBox {
                    text = state.searchText
                    onTextChanged = { setState { searchText = it } }
                    onEnter = {
                        getCitiesBySearchText().takeIf { it.size == 1 }?.let {
                            act { onCityClick(it[0]) }
                        }
                    }
                }
            }

            styledDiv {
                css {
                    put("grid-area", "cards")
                }
                cardsDeck(props)
            }
        }
    }

    private fun RBuilder.headerMessage(text: String, color: Color) {
        styledDiv {
            css {
                put("grid-area", "header")
                width = 100.pct
                backgroundColor = color
            }
            mTypography(text, MTypographyVariant.h5) {
                css {
                    fontStyle = FontStyle.italic
                    textAlign = TextAlign.center
                }
            }
        }
    }

    private fun getCitiesBySearchText() = state.searchText.let { input ->
        if (input.isNotBlank()) props.gameMap.cities.filter { it.name.startsWith(input) }.map { CityName(it.name) }
        else emptyList()
    }

    private object ComponentStyles : StyleSheet("GameScreen", isStatic = true) {
        val verticalPanel by css {
            display = Display.flex
            flexDirection = FlexDirection.column
            flexWrap = FlexWrap.nowrap
            minWidth = 300.px
            minHeight = LinearDimension.minContent
            overflowY = Overflow.auto
            marginLeft = 4.px
            marginRight = 4.px
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