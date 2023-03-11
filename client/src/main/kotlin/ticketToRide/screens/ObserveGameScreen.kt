package ticketToRide.screens

import csstype.*
import emotion.react.css
import hookstate.Hookstate
import hookstate.useHookstate
import react.*
import react.dom.html.ReactHTML.div
import ticketToRide.*
import ticketToRide.components.CitySearchTextBox
import ticketToRide.components.cards.observeCardsDeck
import ticketToRide.components.chat.chatMessages
import ticketToRide.components.map.observeMap
import ticketToRide.components.playersList

external interface ObserveGameScreenProps : Props {
    var locale: Locale
    var connected: Boolean
    var gameMap: GameMap
    var gameState: GameStateForObserver
    var chatMessages: Array<Response.ChatMessage>
}

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
val ObserveGameScreen = FC<ObserveGameScreenProps> { props ->
    val str = useMemo(props.locale) { GameScreenStrings(props.locale) }
    val citiesToHighlight: Hookstate<Set<CityId>> = useHookstate(emptySet<CityId>())
    var searchText by useState("")

    val players = props.gameState.players
    val turn = props.gameState.turn
    val openCards = props.gameState.openCards

    val areas = listOf(
        "left header header",
        "left map map",
        "left map map",
        "cards cards search"
    )

    div {
        gridLayout(areas)

        var message = str.playerXmoves(players[turn].name.value)
        if (props.gameState.lastRound)
            message += " (${str.lastRound})"
        headerMessage(message, NamedColor.white)

        div {
            css {
                gridArea = ident("left")
                verticalPanelCss()
                resize = Resize.horizontal
            }
            playersList(players, turn, props.locale)
            horizontalDivider()
            chatMessages(props.chatMessages)
        }

        div {
            css {
                gridArea = ident("map")
                width = 100.pct
                height = 100.pct
            }
            observeMap(props) {
                this.citiesToHighlight = citiesToHighlight.get() + getCitiesBySearchText(searchText, props.gameMap, props.locale)
                this.citiesWithStations = players.flatMap { p -> p.placedStations.map { it to p } }.associate { it }
                this.onCityMouseOver = { city -> citiesToHighlight.set { it + city } }
                this.onCityMouseOut = { city -> citiesToHighlight.set { it - city } }
            }
        }

        div {
            css {
                gridArea = ident("search")
                width = 96.pct
                marginLeft = 4.px
            }
            horizontalDivider()
            CitySearchTextBox {
                locale = props.locale
                text = searchText
                onTextChanged = { searchText = it }
            }
        }

        div {
            css {
                gridArea = ident("cards")
            }
            observeCardsDeck(openCards, props.locale)
        }
    }
}
