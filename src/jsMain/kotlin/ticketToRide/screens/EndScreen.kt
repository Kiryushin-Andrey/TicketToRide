package ticketToRide.screens

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.expansionpanel.*
import kotlinx.css.*
import kotlinx.css.Color
import react.*
import styled.*
import ticketToRide.*
import ticketToRide.components.*

interface EndScreenProps : RProps {
    var gameMap: GameMap
    var players: List<PlayerFinalStats>
    var chatMessages: List<Response.ChatMessage>
    var onSendMessage: (String) -> Unit
}

interface EndScreenState : RState {
    var citiesToHighlight: Set<CityName>
}

class EndScreen(props: EndScreenProps) : RComponent<EndScreenProps, EndScreenState>(props) {
    override fun EndScreenState.init(props: EndScreenProps) {
        citiesToHighlight = emptySet()
    }

    override fun RBuilder.render() {
        val longestPathOfAll = props.players.map { it.longestPath }.max()!!
        val winner = props.players.maxBy { it.getTotalPoints(longestPathOfAll) }!!

        styledDiv {
            css {
                height = 100.pct
                width = 100.pct
                display = Display.grid
                gridTemplateColumns = GridTemplateColumns(GridAutoRows("0.2fr"), GridAutoRows.auto, GridAutoRows("0.2fr"))
                gridTemplateRows = GridTemplateRows(GridAutoRows(40.px), GridAutoRows.auto)
                gridTemplateAreas = GridTemplateAreas("\"left header right\" \"left map right\"")
            }

            styledDiv {
                css {
                    put("grid-area", "left")
                    +ComponentStyles.verticalPanel
                    put("resize", "horizontal")
                }

                for ((ix, player) in props.players.sortedByDescending { it.getTotalPoints(longestPathOfAll) }.withIndex()) {
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
                    players = props.players.map { it.playerView }
                    citiesToHighlight = state.citiesToHighlight
                    citiesWithStations = props.players.map { it.playerView }.getStations()
                    onCityMouseOver = { setState { citiesToHighlight += it } }
                    onCityMouseOut = { setState { citiesToHighlight -= it } }
                }
            }

            styledDiv {
                css {
                    put("grid-area", "right")
                    +ComponentStyles.verticalPanel
                }

                chatMessages(props.chatMessages)
                chatSendMessageTextBox(props.onSendMessage)
            }
        }
    }


    private fun RBuilder.headerMessage(winner: PlayerFinalStats) {
        styledDiv {
            css {
                put("grid-area", "header")
                width = 100.pct
                backgroundColor = Color.lightGoldenrodYellow
            }
            mTypography("Игра закончена. Победил ${winner.name.value}!", MTypographyVariant.h5) {
                css {
                    fontStyle = FontStyle.italic
                    textAlign = TextAlign.center
                }
            }
        }
    }

    private fun RBuilder.playerStats(player: PlayerFinalStats, longestPathOfAll: Int, expanded: Boolean) {

        mPaper {
            css { margin = 4.px.toString() }
            attrs { elevation = 2 }

            mExpansionPanel {
                css { backgroundColor = Color(player.color.rgb).withAlpha(0.4) }
                attrs {
                    defaultExpanded = expanded
                    withClasses(
                        "root" to ComponentStyles.getClassName { it::playerPanelRoot },
                        "expanded" to ComponentStyles.getClassName { it::summaryExpanded })
                }

                mExpansionPanelSummary {
                    attrs {
                        expandIcon = buildElement { mIcon("expand_more") }!!
                        withClasses(
                            "root" to ComponentStyles.getClassName { it::playerPanelRoot },
                            "content" to ComponentStyles.getClassName { it::playerSummaryContent },
                            "expanded" to ComponentStyles.getClassName { it::summaryExpanded },
                            "expandIcon" to ComponentStyles.getClassName { it::summaryExpandIcon })
                    }
                    playerBlockHeader(player, player.getTotalPoints(longestPathOfAll))
                }

                mExpansionPanelDetails {
                    attrs {
                        className = ComponentStyles.getClassName { it::playerDetailsRoot }
                    }

                    if (player.longestPath == longestPathOfAll)
                        longestPathPanel(longestPathOfAll)

                    playerTicketStats(player)

                    if (player.occupiedSegments.isNotEmpty())
                        playerSegmentStats(player)
                }
            }
        }
    }

    private fun RBuilder.playerBlockHeader(player: PlayerFinalStats, totalPoints: Int) {
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
                    +"Самый длинный маршрут! $longestPath вагонов"
                }
                pointsLabel(props.gameMap.pointsForLongestPath, Color.lightGreen)
            }
        }
    }

    private fun RBuilder.playerTicketStats(player: PlayerFinalStats) {
        for (ticket in player.fulfilledTickets) {
            ticket(ticket) {
                fulfilled = true
                onMouseOver = { setState { citiesToHighlight += listOf(ticket.from, ticket.to) } }
                onMouseOut = { setState { citiesToHighlight -= listOf(ticket.from, ticket.to) } }
            }
        }
        for (ticket in player.unfulfilledTickets) {
            ticket(ticket) {
                fulfilled = false
                onMouseOver = { setState { citiesToHighlight += listOf(ticket.from, ticket.to) } }
                onMouseOut = { setState { citiesToHighlight -= listOf(ticket.from, ticket.to) } }
            }
        }
    }

    private fun RBuilder.playerSegmentStats(player: PlayerFinalStats) {
        styledDiv {
            css { +ComponentStyles.playerSegmentStats }
            styledDiv {
                css {
                    display = Display.flex
                    flexDirection = FlexDirection.column
                    flexWrap = FlexWrap.nowrap
                    width = 80.pct
                }
                for ((length, count) in player.occupiedSegments.groupingBy { it.length }.eachCount().entries.sortedByDescending { it.key }) {
                    val pointsPerSegment = props.gameMap.getPointsForSegments(length)
                    repeatedIconsWithPoints(
                        length,
                        "/icons/railway-car.png",
                        "$count * $pointsPerSegment = ${pointsPerSegment * count}"
                    )
                }
                player.playerView.stationsLeft.takeIf { it > 0 }?.let {
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
}

fun RBuilder.endScreen(builder: EndScreenProps.() -> Unit) {
    child(EndScreen::class) {
        attrs(builder)
    }
}