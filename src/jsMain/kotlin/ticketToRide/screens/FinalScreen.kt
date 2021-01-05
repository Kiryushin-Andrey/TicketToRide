package ticketToRide.screens

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.expansionpanel.*
import kotlinx.css.*
import kotlinx.css.Color
import react.*
import styled.*
import ticketToRide.*
import ticketToRide.components.*
import ticketToRide.components.chat.chatMessages
import ticketToRide.components.chat.chatSendMessageTextBox
import ticketToRide.components.map.finalMap
import ticketToRide.components.tickets.pointsLabel
import ticketToRide.components.tickets.ticket

interface FinalScreenProps : RProps {
    var locale: Locale
    var gameMap: GameMap
    var observing: Boolean
    var players: List<PlayerScore>
    var chatMessages: List<Response.ChatMessage>
    var onSendMessage: (String) -> Unit
}

interface FinalScreenState : RState {
    var playerToHighlight: PlayerName?
    var citiesToHighlight: Set<CityName>
}

class FinalScreen(props: FinalScreenProps) : RComponent<FinalScreenProps, FinalScreenState>(props) {
    override fun FinalScreenState.init(props: FinalScreenProps) {
        citiesToHighlight = emptySet()
    }

    override fun RBuilder.render() {
        val longestPathOfAll = props.players.map { it.longestRoute }.maxOrNull()!!
        val winner = props.players.maxByOrNull { it.getTotalPoints(longestPathOfAll, false) }!!
        val areas =
            if (props.observing) listOf("left header", "left map")
            else listOf("left header right", "left map right")

        styledDiv {
            css {
                height = 100.pct
                width = 100.pct
                display = Display.grid
                gridTemplateColumns =
                    if (props.observing)
                        GridTemplateColumns(GridAutoRows("0.2fr"), GridAutoRows.auto)
                    else
                        GridTemplateColumns(GridAutoRows("0.2fr"), GridAutoRows.auto, GridAutoRows("0.2fr"))
                gridTemplateRows = GridTemplateRows(GridAutoRows(40.px), GridAutoRows.auto)
                gridTemplateAreas = GridTemplateAreas(areas.joinToString(" ") { "\"$it\"" })
            }

            styledDiv {
                css {
                    put("grid-area", "left")
                    +ComponentStyles.verticalPanel
                    put("resize", "horizontal")
                }

                for ((ix, player) in props.players.sortedByDescending { it.getTotalPoints(longestPathOfAll, false) }
                    .withIndex()) {
                    playerStats(player, longestPathOfAll, ix == 0)
                }
            }

            headerMessage(winner)

            styledDiv {
                css {
                    put("grid-area", "map")
                    width = 100.pct
                    height = 100.pct
                }
                finalMap {
                    gameMap = props.gameMap
                    players = props.players
                    playerToHighlight = state.playerToHighlight
                    citiesToHighlight = state.citiesToHighlight
                    citiesWithStations =
                        props.players.flatMap { p -> p.placedStations.map { it to p } }.associate { it }
                    onCityMouseOver = { setState { citiesToHighlight += it } }
                    onCityMouseOut = { setState { citiesToHighlight -= it } }
                }
            }

            if (!props.observing) {
                styledDiv {
                    css {
                        put("grid-area", "right")
                        +ComponentStyles.verticalPanel
                    }

                    chatMessages(props.chatMessages)
                    chatSendMessageTextBox(props.locale, props.onSendMessage)
                }
            }
        }
    }


    private fun RBuilder.headerMessage(winner: PlayerScore) {
        styledDiv {
            css {
                put("grid-area", "header")
                width = 100.pct
                backgroundColor = Color.lightGoldenrodYellow
            }
            mTypography(str.gameOver(winner.name.value), MTypographyVariant.h5) {
                css {
                    fontStyle = FontStyle.italic
                    textAlign = TextAlign.center
                }
            }
        }
    }

    private fun RBuilder.playerStats(player: PlayerScore, longestPathOfAll: Int, expanded: Boolean) {

        mPaper {
            css { margin = 4.px.toString() }
            attrs {
                elevation = if (state.playerToHighlight == player.name) 6 else 2
            }

            mExpansionPanel {
                css { backgroundColor = Color(player.color.rgb).withAlpha(0.4) }
                attrs {
                    defaultExpanded = expanded
                    withClasses(
                        "root" to ComponentStyles.getClassName { it::playerPanelRoot },
                        "expanded" to ComponentStyles.getClassName { it::summaryExpanded })
                    onMouseEnter = { setState { playerToHighlight = player.name } }
                    onMouseLeave = { setState { playerToHighlight = null } }
                }

                mExpansionPanelSummary {
                    attrs {
                        expandIcon = buildElement { mIcon("expand_more") }
                        withClasses(
                            "root" to ComponentStyles.getClassName { it::playerPanelRoot },
                            "content" to ComponentStyles.getClassName { it::playerSummaryContent },
                            "expanded" to ComponentStyles.getClassName { it::summaryExpanded },
                            "expandIcon" to ComponentStyles.getClassName { it::summaryExpandIcon })
                    }
                    playerBlockHeader(player, player.getTotalPoints(longestPathOfAll, false))
                }

                mExpansionPanelDetails {
                    attrs {
                        className = ComponentStyles.getClassName { it::playerDetailsRoot }
                    }

                    if (player.longestRoute == longestPathOfAll)
                        longestPathPanel(longestPathOfAll)

                    playerTicketStats(player)

                    if (player.occupiedSegments.isNotEmpty())
                        playerSegmentStats(player)
                }
            }
        }
    }

    private fun RBuilder.playerBlockHeader(player: PlayerScore, totalPoints: Int) {
        styledDiv {
            css {
                +ComponentStyles.playerStatsBar
                paddingRight = 16.px
            }
            mTypography(variant = MTypographyVariant.h6) {
                +player.name.value
            }
            pointsLabel(totalPoints.toString(), Color.lightGreen)
        }
    }

    private fun RBuilder.longestPathPanel(longestPath: Int) {
        mPaper {
            attrs { elevation = 2 }
            css {
                borderRadius = 4.px
                margin = 4.px.toString()
                paddingLeft = 12.px
                paddingRight = 12.px
                backgroundColor = Color.lightGoldenrodYellow
            }
            styledDiv {
                css {
                    minHeight = 40.px
                    display = Display.flex
                    alignItems = Align.center
                    flexDirection = FlexDirection.row
                    justifyContent = JustifyContent.spaceBetween
                }
                mTypography(variant = MTypographyVariant.body2) {
                    +str.longestRoute(longestPath)
                }
                pointsLabel(props.gameMap.pointsForLongestRoute, Color.lightGreen)
            }
        }
    }

    private fun RBuilder.playerTicketStats(player: PlayerScore) {
        for (ticket in player.fulfilledTickets) {
            ticket(ticket) {
                finalScreen = true
                fulfilled = true
                onMouseOver = { setState { citiesToHighlight += listOf(ticket.from, ticket.to) } }
                onMouseOut = { setState { citiesToHighlight -= listOf(ticket.from, ticket.to) } }
            }
        }
        for (ticket in player.unfulfilledTickets) {
            ticket(ticket) {
                finalScreen = true
                fulfilled = false
                onMouseOver = { setState { citiesToHighlight += listOf(ticket.from, ticket.to) } }
                onMouseOut = { setState { citiesToHighlight -= listOf(ticket.from, ticket.to) } }
            }
        }
    }

    private fun RBuilder.playerSegmentStats(player: PlayerScore) {
        styledDiv {
            css { +ComponentStyles.playerSegmentStats }
            styledDiv {
                css {
                    display = Display.flex
                    flexDirection = FlexDirection.column
                    flexWrap = FlexWrap.nowrap
                    width = 80.pct
                }
                for ((length, count) in player.occupiedSegments.groupingBy { it.length }.eachCount().entries.sortedByDescending {  it.key }) {
                    val pointsPerSegment = props.gameMap.getPointsForSegments(length)
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
            pointsLabel(player.segmentsPoints + player.stationPoints, Color.lightGreen)
        }
    }

    private fun RBuilder.repeatedIconsWithPoints(count: Int, imgUrl: String, pointsText: String) {
        styledDiv {
            css { +ComponentStyles.repeatedIconsWithPoints }
            styledDiv {
                css { +ComponentStyles.repeatedIcons }
                repeat(count) {
                    styledImg {
                        css {
                            marginRight = 4.px
                        }
                        attrs {
                            src = imgUrl
                            width = 24.px.toString()
                        }
                    }
                }
            }
            mTypography(variant = MTypographyVariant.body2) {
                +pointsText
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

        val playerSegmentStats by css {
            minHeight = 40.px
            paddingTop = 4.px
            paddingLeft = 12.px
            paddingRight = 16.px
            display = Display.flex
            flexDirection = FlexDirection.row
            justifyContent = JustifyContent.spaceBetween
        }

        val repeatedIconsWithPoints by css {
            display = Display.flex
            flexDirection = FlexDirection.row
            justifyContent = JustifyContent.spaceBetween
            alignItems = Align.center
            marginBottom = 6.px
        }

        val repeatedIcons by css {
            display = Display.flex
            flexDirection = FlexDirection.row
            justifyContent = JustifyContent.left
            alignItems = Align.center
        }

        val playerStatsBar by css {
            width = 100.pct
            minHeight = 40.px
            display = Display.flex
            alignItems = Align.center
            flexDirection = FlexDirection.row
            justifyContent = JustifyContent.spaceBetween
            paddingLeft = 12.px
        }

        val playerPanelRoot by css {
            minHeight = 0.px
            padding = 0.px.toString()
            "&.Mui-expanded" {
                margin = 0.px.toString()
                minHeight = 0.px
            }
        }

        val playerSummaryContent by css {
            margin = 0.px.toString()
            "&.Mui-expanded" {
                margin = 0.px.toString()
                minHeight = 0.px
            }
        }

        val playerDetailsRoot by css {
            display = Display.flex
            flexDirection = FlexDirection.column
            padding = 0.px.toString()
            paddingBottom = 10.px
        }

        val summaryExpandIcon by css {
            marginRight = 0.px
        }

        val summaryExpanded by css {}
    }

    private inner class Strings : LocalizedStrings({ props.locale }) {

        val gameOver by locWithParam<String>(
            Locale.En to { name -> "Game is over, $name won!" },
            Locale.Ru to { name -> "Игра закончена. Победил $name!" }
        )

        val longestRoute by locWithParam<Int>(
            Locale.En to { n -> "Longest route - $n wagons!" },
            Locale.Ru to { n -> "Самый длинный маршрут - $n вагонов!" }
        )
    }

    private val str = Strings()
}

fun RBuilder.finalScreen(builder: FinalScreenProps.() -> Unit) {
    child(FinalScreen::class) {
        attrs(builder)
    }
}