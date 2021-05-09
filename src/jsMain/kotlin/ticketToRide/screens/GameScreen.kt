package ticketToRide.screens

import com.ccfraser.muirwik.components.*
import kotlinx.browser.document
import kotlinx.css.*
import kotlinx.html.DIV
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import react.*
import styled.*
import ticketToRide.*
import ticketToRide.components.*
import ticketToRide.components.building.buildingSegment
import ticketToRide.components.building.buildingStation
import ticketToRide.components.building.pickedCity
import ticketToRide.components.cards.cardsDeck
import ticketToRide.components.cards.myCards
import ticketToRide.components.chat.chatMessages
import ticketToRide.components.chat.chatSendMessageTextBox
import ticketToRide.components.map.gameMap
import ticketToRide.components.tickets.myTickets
import ticketToRide.playerState.*
import ticketToRide.playerState.PlayerState.MyTurn.*

external interface GameScreenProps : ComponentBaseProps {
    var gameMap: GameMap
    var calculateScores: Boolean
    var chatMessages: List<Response.ChatMessage>
    var onSendMessage: (String) -> Unit
}

external interface GameScreenState : RState {
    var citiesToHighlight: Set<CityName>
    var searchText: String
}

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
class GameScreen : ComponentBase<GameScreenProps, GameScreenState>() {

    override fun GameScreenState.init() {
        citiesToHighlight = emptySet()
        searchText = ""
    }

    override fun RBuilder.render() {
        val areas = listOf(
            "left header right",
            "left map right",
            "send map search",
            "cards cards cards"
        )

        styledDiv {
            gridLayout(areas)

            when {
                myTurn -> headerMessage(str.yourTurn, Color.lightGreen)
                lastRound -> headerMessage(str.lastRound, Color.darkSalmon)
                else -> headerMessage(str.playerXmoves(players[turn].name.value), Color.white)
            }

            styledDiv {
                css {
                    put("grid-area", "left")
                    +ComponentStyles.verticalPanel
                    put("resize", "horizontal")
                }

                playersList(players, turn, props.calculateScores, props.locale)
                horizontalDivider()
                chatMessages(props.chatMessages)
            }

            styledDiv {
                css {
                    put("grid-area", "send")
                }
                chatSendMessageTextBox(props.locale, props.onSendMessage)
            }

            styledDiv {
                css {
                    put("grid-area", "map")
                    width = 100.pct
                    height = 100.pct
                }
                gameMap(props) {
                    connected = props.connected
                    gameMap = props.gameMap
                    citiesToHighlight = state.citiesToHighlight + getCitiesBySearchText()
                    citiesWithStations = players.flatMap { p -> p.placedStations.map { it to p } }.associate { it }
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

                when (playerState) {
                    is PickedCity -> {
                        pickedCity(props)
                        horizontalDivider()
                    }
                    is BuildingStation -> {
                        buildingStation(props)
                        horizontalDivider()
                    }
                    is BuildingSegment -> {
                        buildingSegment(props)
                        horizontalDivider()
                    }
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
                searchTextBox(props.locale) {
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

    private fun getCitiesBySearchText() = state.searchText.let { input ->
        if (input.isNotBlank())
            props.gameMap.cities.filter { it.name.value.startsWith(input) }.map { it.name }
        else
            emptyList()
    }

    object ComponentStyles : StyleSheet("GameScreen", isStatic = true) {
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

    class Strings(getLocale: () -> Locale) : LocalizedStrings(getLocale) {

        val yourTurn by loc(
            Locale.En to "Your turn",
            Locale.Ru to "Ваш ход"
        )

        val lastRound by loc(
            Locale.En to "Last round",
            Locale.Ru to "Последний круг"
        )

        val playerXmoves by locWithParam<String>(
            Locale.En to { name -> "$name moves" },
            Locale.Ru to { name -> "Ходит $name" }
        )
    }

    private val str = Strings { props.locale }


    override fun componentDidMount() {
        document.addEventListener("keydown", onKeyPress)
    }

    override fun componentWillUnmount() {
        document.removeEventListener("keydown", onKeyPress)
    }

    private val onKeyPress = { e: Event ->
        if ((e as KeyboardEvent).key == "Escape") {
            (playerState as? PlayerState.MyTurn)?.apply {
                act { Blank(gameMap, gameState, requests) }
            }
        }
    }


    companion object {

        fun RBuilder.headerMessage(text: String, color: Color) {
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

        fun StyledDOMBuilder<DIV>.gridLayout(areas: List<String>) {
            val rows = listOf(GridAutoRows(40.px), GridAutoRows.auto, GridAutoRows(65.px), GridAutoRows(120.px))
            css {
                height = 100.pct
                width = 100.pct
                display = Display.grid
                gridTemplateColumns =
                    GridTemplateColumns(GridAutoRows("0.2fr"), GridAutoRows.auto, GridAutoRows(360.px))
                gridTemplateRows = GridTemplateRows(*rows.toTypedArray())
                gridTemplateAreas = GridTemplateAreas(areas.joinToString(" ") { "\"$it\"" })
            }
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
        attrs(builder)
    }
}