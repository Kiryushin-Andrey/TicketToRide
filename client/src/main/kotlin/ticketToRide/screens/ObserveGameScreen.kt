package ticketToRide.screens

import kotlinx.css.*
import react.RBuilder
import react.RComponent
import react.RProps
import react.setState
import styled.css
import styled.styledDiv
import ticketToRide.*
import ticketToRide.components.cards.observeCardsDeck
import ticketToRide.components.chat.chatMessages
import ticketToRide.components.map.observeMap
import ticketToRide.components.playersList
import ticketToRide.components.searchTextBox
import ticketToRide.screens.GameScreen.Companion.gridLayout
import ticketToRide.screens.GameScreen.Companion.headerMessage

external interface ObserveGameScreenProps : RProps {
    var locale: Locale
    var connected: Boolean
    var gameMap: GameMap
    var gameState: GameStateForObserver
    var calculateScores: Boolean
    var chatMessages: List<Response.ChatMessage>
}

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
class ObserveGameScreen : RComponent<ObserveGameScreenProps, GameScreenState>() {

    override fun GameScreenState.init() {
        citiesToHighlight = emptySet()
        searchText = ""
    }

    private val players get() = props.gameState.players
    private val turn get() = props.gameState.turn
    private val openCards get() = props.gameState.openCards

    override fun RBuilder.render() {
        val areas = listOf(
            "left header header",
            "left map map",
            "left map map",
            "cards cards search"
        )

        styledDiv {
            gridLayout(areas)

            var message = str.playerXmoves(players[turn].name.value)
            if (props.gameState.lastRound)
                message += " (${str.lastRound})"
            headerMessage(message, Color.white)

            styledDiv {
                css {
                    put("grid-area", "left")
                    +GameScreen.ComponentStyles.verticalPanel
                    put("resize", "horizontal")
                }
                playersList(players, turn, props.calculateScores, props.locale)
                horizontalDivider()
                chatMessages(props.chatMessages)
            }

            styledDiv {
                css {
                    put("grid-area", "map")
                    width = 100.pct
                    height = 100.pct
                }
                observeMap(props) {
                    citiesToHighlight = state.citiesToHighlight + getCitiesBySearchText()
                    citiesWithStations = players.flatMap { p -> p.placedStations.map { it to p } }.associate { it }
                    onCityMouseOver = { setState { citiesToHighlight += it } }
                    onCityMouseOut = { setState { citiesToHighlight -= it } }
                }
            }

            styledDiv {
                css {
                    put("grid-area", "search")
                    width = 96.pct
                    marginLeft = 4.px
                }
                horizontalDivider()
                searchTextBox(props.locale) {
                    text = state.searchText
                    onTextChanged = { setState { searchText = it } }
                }
            }

            styledDiv {
                css {
                    put("grid-area", "cards")
                }
                observeCardsDeck(openCards, props.locale)
            }
        }
    }

    private fun getCitiesBySearchText() = state.searchText.let { input ->
        if (input.isNotBlank())
            props.gameMap.cities
                .filter { it.id.localize(props.locale, props.gameMap).startsWith(input) }
                .map { it.id }
        else
            emptyList()
    }

    private val str = GameScreen.Strings { props.locale }
}

fun RBuilder.observeGameScreen(builder: ObserveGameScreenProps.() -> Unit) {
    child(ObserveGameScreen::class) {
        attrs(builder)
    }
}