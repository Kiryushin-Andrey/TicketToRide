package ticketToRide.screens

import csstype.*
import emotion.react.css
import hookstate.Hookstate
import hookstate.useHookstate
import kotlinx.browser.document
import mui.material.Divider
import mui.material.DividerVariant
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import react.*
import react.dom.html.HTMLAttributes
import react.dom.html.ReactHTML.div
import ticketToRide.*
import ticketToRide.components.*
import ticketToRide.components.building.BuildingSegmentComponent
import ticketToRide.components.building.BuildingStationComponent
import ticketToRide.components.building.PickedCityComponent
import ticketToRide.components.cards.CardsDeckComponent
import ticketToRide.components.cards.MyCardsComponent
import ticketToRide.components.chat.chatMessages
import ticketToRide.components.chat.chatSendMessageTextBox
import ticketToRide.components.map.MapComponent
import ticketToRide.components.tickets.MyTicketsComponent
import ticketToRide.playerState.*
import ticketToRide.playerState.PlayerState.MyTurn.*
import web.html.HTMLDivElement

external interface GameScreenProps : GameComponentProps {
    var chatMessages: Array<Response.ChatMessage>
    var onSendMessage: (String) -> Unit
}

val GameScreen = FC<GameScreenProps> { props ->
    val str = useMemo(props.locale) { GameScreenStrings(props.locale) }
    val citiesToHighlight: Hookstate<Set<CityId>> = useHookstate(emptySet<CityId>())
    var searchText by useState("")

    val gameState = props.gameState
    val playerState = props.playerState

    val onKeyDown = useCallback(props.act) { e: Event ->
        if ((e as KeyboardEvent).key == "Escape") {
            (playerState as? PlayerState.MyTurn)?.apply {
                props.act { Blank(props.gameMap, gameState, requests) }
            }
        }
    }
    useEffect(onKeyDown) {
        document.addEventListener("keydown", onKeyDown)
        cleanup {
            document.removeEventListener("keydown", onKeyDown)
        }
    }

    val areas = listOf(
        "left header right",
        "left map right",
        "send map search",
        "cards cards cards"
    )

    div {
        gridLayout(areas)

        when {
            gameState.myTurn -> headerMessage(str.yourTurn, NamedColor.lightgreen)
            gameState.lastRound -> headerMessage(str.lastRound, NamedColor.darksalmon)
            else -> headerMessage(str.playerXmoves(gameState.players[gameState.turn].name.value), NamedColor.white)
        }

        div {
            css {
                gridArea = ident("left")
                verticalPanelCss()
                resize = Resize.horizontal
            }

            playersList(gameState.players, gameState.turn, props.locale)
            horizontalDivider()
            chatMessages(props.chatMessages)
        }

        div {
            css {
                gridArea = ident("send")
            }
            chatSendMessageTextBox(props.locale, props.onSendMessage)
        }

        div {
            css {
                gridArea = ident("map")
                width = 100.pct
                height = 100.pct
            }
            MapComponent {
                copyFrom(props)
                this.citiesToHighlight = citiesToHighlight.get() + getCitiesBySearchText(searchText, props.gameMap, props.locale)
                citiesWithStations = gameState.players.flatMap { p -> p.placedStations.map { it to p } }.associate { it }
                onCityMouseOver = { city -> citiesToHighlight.set { it + city } }
                onCityMouseOut = { city -> citiesToHighlight.set { it - city } }
            }
        }

        div {
            css {
                gridArea = ident("right")
                verticalPanelCss()
            }

            MyCardsComponent {
                copyFrom(props)
            }
            horizontalDivider()

            when (playerState) {
                is PickedCity -> {
                    PickedCityComponent {
                        copyFrom(props)
                    }
                    horizontalDivider()
                }

                is BuildingStation -> {
                    BuildingStationComponent {
                        copyFrom(props)
                    }
                    horizontalDivider()
                }

                is BuildingSegment -> {
                    BuildingSegmentComponent {
                        copyFrom(props)
                    }
                    horizontalDivider()
                }

                else -> {}
            }

            MyTicketsComponent {
                copyFrom(props)
                this.citiesToHighlight = citiesToHighlight.get()
                onTicketMouseOver = { ticket ->
                    citiesToHighlight.set { it + ticket.from + ticket.to }
                }
                onTicketMouseOut = { ticket ->
                    citiesToHighlight.set { it - ticket.from - ticket.to }
                }
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
                onEnter = {
                    getCitiesBySearchText(searchText, props.gameMap, props.locale).takeIf { it.size == 1 }?.let {
                        props.act { props.playerState.onCityClick(it[0]) }
                    }
                }
            }
        }

        div {
            css {
                gridArea = ident("cards")
            }
            CardsDeckComponent {
                copyFrom(props)
            }
        }
    }
}

fun getCitiesBySearchText(input: String, gameMap: GameMap, locale: Locale): List<CityId> {
    return if (input.isNotBlank())
        gameMap.cities
            .filter { it.id.localize(locale, gameMap).startsWith(input) }
            .map { it.id }
    else
        emptyList()
}

fun ChildrenBuilder.headerMessage(text: String, color: BackgroundColor) {
    div {
        css {
            gridArea = ident("header")
            width = 100.pct
            backgroundColor = color
        }
        Typography {
            variant = TypographyVariant.h5
            sx {
                fontStyle = FontStyle.italic
                textAlign = TextAlign.center
            }
            +text
        }
    }
}

fun HTMLAttributes<HTMLDivElement>.gridLayout(areas: List<String>) {
    val rows = listOf(40.px, Auto.auto, 65.px, 120.px)
    css {
        height = 100.pct
        width = 100.pct
        display = Display.grid
        gridTemplateColumns = array<GridTemplateColumns>(0.2.fr, Auto.auto, 360.px)
        gridTemplateRows = array<GridTemplateRows>(*rows.toTypedArray())
        gridTemplateAreas = GridTemplateAreas(*areas.map(::ident).toTypedArray())
    }
}

fun ChildrenBuilder.horizontalDivider() {
    Divider {
        variant = DividerVariant.fullWidth
        sx {
            margin = 5.px
        }
    }
}

fun PropertiesBuilder.verticalPanelCss() {
    display = Display.flex
    flexDirection = FlexDirection.column
    flexWrap = FlexWrap.nowrap
    minWidth = 300.px
    minHeight = Length.minContent
    overflowY = Auto.auto
    marginLeft = 4.px
    marginRight = 4.px
}

class GameScreenStrings(val locale: Locale) : LocalizedStrings({ locale }) {

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
