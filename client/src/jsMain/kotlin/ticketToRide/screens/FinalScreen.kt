package ticketToRide.screens

import csstype.*
import emotion.react.css
import js.core.jso
import mui.icons.material.ExpandMore
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import ticketToRide.*
import ticketToRide.components.chat.chatMessages
import ticketToRide.components.chat.chatSendMessageTextBox
import ticketToRide.components.map.FinalMapComponent
import ticketToRide.components.tickets.pointsLabel
import ticketToRide.components.tickets.ticket

external interface FinalScreenProps : Props {
    var locale: Locale
    var gameMap: GameMap
    var observing: Boolean
    var players: List<PlayerScore>
    var chatMessages: Collection<Response.ChatMessage>
    var onSendMessage: (String) -> Unit
}

val FinalScreen = FC<FinalScreenProps> { props ->
    val str = useMemo(props.locale) { FinalScreenStrings(props.locale) }
    val highlightedCitiesState = useState(emptySet<CityId>())
    var highlightedCities by highlightedCitiesState
    val highlightedPlayersState = useState<PlayerName?>(null)
    var highlightedPlayers by highlightedPlayersState

    val longestPathOfAll = props.players.map { it.longestRoute }.maxOrNull()!!
    val winner = props.players.maxByOrNull { it.getTotalPoints(longestPathOfAll, false) }!!
    val areas =
        if (props.observing) listOf("left header", "left map")
        else listOf("left header right", "left map right")

    div {
        css {
            height = 100.pct
            width = 100.pct
            display = Display.grid
            gridTemplateColumns =
                if (props.observing)
                    array<GridTemplateColumns>(0.2.fr, Auto.auto)
                else
                    array<GridTemplateColumns>(0.2.fr, Auto.auto, 0.2.fr)
            gridTemplateRows = array<GridTemplateRows>(40.px, Auto.auto)
            gridTemplateAreas = GridTemplateAreas(*areas.map(::ident).toTypedArray())
        }

        div {
            className = ComponentStyles.verticalPanel
            css {
                gridArea = ident("left")
                resize = Resize.horizontal
            }

            for ((ix, player) in props.players
                .sortedByDescending { it.getTotalPoints(longestPathOfAll, false) }
                .withIndex()
            ) {
                playerStats(player, highlightedPlayersState, highlightedCitiesState, longestPathOfAll, ix == 0, props, str)
            }
        }

        headerMessage(str.gameOver(winner.name.value))

        div {
            css {
                gridArea = ident("map")
                width = 100.pct
                height = 100.pct
            }
            FinalMapComponent {
                locale = props.locale
                gameMap = props.gameMap
                players = props.players
                this.playerToHighlight = highlightedPlayers
                this.citiesToHighlight = highlightedCities
                citiesWithStations = props.players.flatMap { p -> p.placedStations.map { it to p } }.associate { it }
                onCityMouseOver = { highlightedCities += it }
                onCityMouseOut = { highlightedCities -= it }
            }
        }

        if (!props.observing) {
            div {
                className = ComponentStyles.verticalPanel
                css {
                    gridArea = ident("right")
                }

                chatMessages(props.chatMessages)
                chatSendMessageTextBox(props.locale, props.onSendMessage)
            }
        }
    }
}


private fun ChildrenBuilder.headerMessage(text: String) {
    div {
        css {
            gridArea = ident("header")
            width = 100.pct
            backgroundColor = NamedColor.lightgoldenrodyellow
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

private fun ChildrenBuilder.playerStats(
    player: PlayerScore,
    highlightedPlayerState: StateInstance<PlayerName?>,
    highlightedCitiesState: StateInstance<Set<CityId>>,
    longestPathOfAll: Int,
    expanded: Boolean,
    props: FinalScreenProps,
    str: FinalScreenStrings
) {
    val (highlightedPlayer, setHighlightedPlayer) = highlightedPlayerState

    Paper {
        sx { margin = 4.px }
        elevation = if (player.name == highlightedPlayer) 6 else 2

        Accordion {
            sx { backgroundColor = Color(player.color.rgb + "66") }
            defaultExpanded = expanded
            classes = jso {
                this.root = ComponentStyles.playerPanelRoot
                this.expanded = ComponentStyles.summaryExpanded
            }
            onMouseEnter = { setHighlightedPlayer(player.name) }
            onMouseLeave = { setHighlightedPlayer(null) }

            AccordionSummary {
                expandIcon = ExpandMore.create()
                classes = jso {
                    this.root to ComponentStyles.playerPanelRoot
                    this.content to ComponentStyles.playerSummaryContent
                    this.expanded to ComponentStyles.summaryExpanded
                    this.expandIconWrapper to ComponentStyles.summaryExpandIcon
                }
                playerBlockHeader(player, player.getTotalPoints(longestPathOfAll, false))
            }

            AccordionDetails {
                className = ComponentStyles.playerDetailsRoot

                if (player.longestRoute == longestPathOfAll)
                    longestPathPanel(str.longestRoute(longestPathOfAll), props.gameMap)

                playerTicketStats(player, props, highlightedCitiesState)

                if (player.occupiedSegments.isNotEmpty())
                    playerSegmentStats(player, props.gameMap)
            }
        }
    }
}

private fun ChildrenBuilder.playerBlockHeader(player: PlayerScore, totalPoints: Int) {
    div {
        className = ComponentStyles.playerStatsBar
        Typography {
            variant = TypographyVariant.h6
            +player.name.value
        }
        pointsLabel(totalPoints.toString(), NamedColor.lightgreen)
    }
}

private fun ChildrenBuilder.longestPathPanel(message: String, gameMap: GameMap) {
    Paper {
        elevation = 2
        sx {
            borderRadius = 4.px
            margin = 4.px
            paddingLeft = 12.px
            paddingRight = 12.px
            backgroundColor = NamedColor.lightgoldenrodyellow
        }
        div {
            css {
                minHeight = 40.px
                display = Display.flex
                alignItems = AlignItems.center
                flexDirection = FlexDirection.row
                justifyContent = JustifyContent.spaceBetween
            }
            Typography {
                variant = TypographyVariant.body2
                +message
            }
            pointsLabel(gameMap.pointsForLongestRoute, NamedColor.lightgreen)
        }
    }
}

private fun ChildrenBuilder.playerTicketStats(
    player: PlayerScore,
    props: FinalScreenProps,
    highlightedCitiesState: StateInstance<Set<CityId>>
) {
    val (highlightedCities, setHighligtedCities) = highlightedCitiesState
    for (ticket in player.fulfilledTickets) {
        ticket(ticket, props.gameMap, props.locale) {
            finalScreen = true
            fulfilled = true
            onMouseOver = { setHighligtedCities(highlightedCities + ticket.from + ticket.to) }
            onMouseOut = { setHighligtedCities(highlightedCities - ticket.from - ticket.to) }
        }
    }
    for (ticket in player.unfulfilledTickets) {
        ticket(ticket, props.gameMap, props.locale) {
            finalScreen = true
            fulfilled = false
            onMouseOver = { setHighligtedCities(highlightedCities + ticket.from + ticket.to) }
            onMouseOut = { setHighligtedCities(highlightedCities - ticket.from - ticket.to) }
        }
    }
}

private fun ChildrenBuilder.playerSegmentStats(player: PlayerScore, gameMap: GameMap) {
    div {
        className = ComponentStyles.playerSegmentStats
        div {
            css {
                display = Display.flex
                flexDirection = FlexDirection.column
                flexWrap = FlexWrap.nowrap
                width = 80.pct
            }
            for ((length, count) in player.occupiedSegments.groupingBy { it.length }.eachCount().entries.sortedByDescending {  it.key }) {
                val pointsPerSegment = gameMap.getPointsForSegments(length)
                repeatedIconsWithPoints(
                    length,
                    "/icons/railway-car.png",
                    "$count * $pointsPerSegment = ${pointsPerSegment * count}"
                )
            }
            player.stationsLeft.takeIf { it > 0 }?.let {
                repeatedIconsWithPoints(it, "/icons/station.png", "$it * $PointsPerStation = ${it * PointsPerStation}")
            }
        }
        pointsLabel(player.segmentsPoints + player.stationPoints, NamedColor.lightgreen)
    }
}

private fun ChildrenBuilder.repeatedIconsWithPoints(count: Int, imgUrl: String, pointsText: String) {
    div {
        className = ComponentStyles.repeatedIconsWithPoints
        div {
            className = ComponentStyles.repeatedIcons
            repeat(count) {
                img {
                    css {
                        marginRight = 4.px
                    }
                    src = imgUrl
                    width = 24.0
                }
            }
        }
        Typography {
            variant = TypographyVariant.body2
            +pointsText
        }
    }
}

private object ComponentStyles {
    val verticalPanel = emotion.css.ClassName {
        display = Display.flex
        flexDirection = FlexDirection.column
        flexWrap = FlexWrap.nowrap
        minWidth = 350.px
        minHeight = Length.minContent
        overflow = Auto.auto
    }

    val playerSegmentStats = emotion.css.ClassName {
        minHeight = 40.px
        paddingTop = 4.px
        paddingLeft = 12.px
        paddingRight = 16.px
        display = Display.flex
        flexDirection = FlexDirection.row
        justifyContent = JustifyContent.spaceBetween
    }

    val repeatedIconsWithPoints = emotion.css.ClassName {
        display = Display.flex
        flexDirection = FlexDirection.row
        justifyContent = JustifyContent.spaceBetween
        alignItems = AlignItems.center
        marginBottom = 6.px
    }

    val repeatedIcons = emotion.css.ClassName {
        display = Display.flex
        flexDirection = FlexDirection.row
        justifyContent = JustifyContent.left
        alignItems = AlignItems.center
    }

    val playerStatsBar = emotion.css.ClassName {
        width = 100.pct
        minHeight = 40.px
        display = Display.flex
        alignItems = AlignItems.center
        flexDirection = FlexDirection.row
        justifyContent = JustifyContent.spaceBetween
        paddingLeft = 12.px
        paddingRight = 16.px
    }

    val playerPanelRoot = emotion.css.ClassName {
        minHeight = 0.px
        padding = 0.px
        "&.Mui-expanded" {
            margin = 0.px
            minHeight = 0.px
        }
    }

    val playerSummaryContent = emotion.css.ClassName {
        margin = 0.px
        "&.Mui-expanded" {
            margin = 0.px
            minHeight = 0.px
        }
    }

    val playerDetailsRoot = emotion.css.ClassName {
        display = Display.flex
        flexDirection = FlexDirection.column
        padding = 0.px
        paddingBottom = 10.px
    }

    val summaryExpandIcon = emotion.css.ClassName {
        marginRight = 0.px
    }

    val summaryExpanded = emotion.css.ClassName {}
}

private class FinalScreenStrings(locale: Locale) : LocalizedStrings({ locale }) {

    val gameOver by locWithParam<String>(
        Locale.En to { name -> "Game is over, $name won!" },
        Locale.Ru to { name -> "Игра закончена. Победил $name!" }
    )

    val longestRoute by locWithParam<Int>(
        Locale.En to { n -> "Longest route - $n wagons!" },
        Locale.Ru to { n -> "Самый длинный маршрут - $n вагонов!" }
    )
}
