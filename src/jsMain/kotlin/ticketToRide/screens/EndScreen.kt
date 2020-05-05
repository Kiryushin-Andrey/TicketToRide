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
        styledDiv {
            css {
                height = 100.pct
                width = 100.pct
                display = Display.grid
                gridTemplateColumns =
                    GridTemplateColumns(GridAutoRows("0.2fr"), GridAutoRows.auto, GridAutoRows("0.2fr"))
            }

            styledDiv {
                css {
                    +ComponentStyles.verticalPanel
                    put("resize", "horizontal")
                }

                val longestPath = props.players.map { it.longestPath }.max()!!
                for (player in props.players) {
                    playerStats(player, longestPath)
                }
            }

            finalMap {
                gameMap = props.gameMap
                players = props.players.map { it.playerView }
                citiesToHighlight = state.citiesToHighlight
                onCityMouseOver = { setState { citiesToHighlight += it } }
                onCityMouseOut = { setState { citiesToHighlight -= it } }
            }

            styledDiv {
                css {
                    +ComponentStyles.verticalPanel
                }

                chat(props.chatMessages, props.onSendMessage)
            }
        }
    }

    private fun RBuilder.playerStats(player: PlayerFinalStats, longestPath: Int) {
        val fulfilledTicketsPoints = player.fulfilledTickets.sumBy { it.points }
        val unfulfilledTicketPoints = player.unfulfilledTickets.sumBy { it.points }
        val segmentsPoints = player.occupiedSegments.groupingBy { it.length }.eachCount().entries
            .sumBy { (length, count) -> props.gameMap.getPointsForSegments(length) * count }
        val longestPathPoints = (if (player.longestPath == longestPath) props.gameMap.pointsForLongestPath else 0)
        val totalPoints = fulfilledTicketsPoints - unfulfilledTicketPoints + segmentsPoints + longestPathPoints

        mPaper {
            attrs { elevation = 2 }
            css { margin = 4.px.toString() }

            mExpansionPanel {
                css { backgroundColor = Color(player.color.rgb).withAlpha(0.4) }

                mExpansionPanelSummary {
                    attrs {
                        withClasses(
                            "root" to ComponentStyles.getClassName { it::playerSummaryRoot },
                            "content" to ComponentStyles.getClassName { it::playerSummaryContent })
                            "expanded" to ComponentStyles.getClassName { it::summaryExpanded }
                    }
                    playerBlockHeader(player, totalPoints)
                }

                mExpansionPanelDetails {
                    attrs {
                        className = ComponentStyles.getClassName { it::playerDetailsRoot }
                    }
                    if (player.longestPath == longestPath) {
                        longestPathPanel(longestPath)
                    }

                    playerTicketStats(player)

                    playerSegmentStats(player, segmentsPoints)
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

    private fun RBuilder.playerSegmentStats(player: PlayerFinalStats, segmentsPoints: Int) {
        styledDiv {
            css {
                minHeight = 40.px
                paddingTop = 4.px
                paddingLeft = 12.px
                paddingRight = 16.px
                display = Display.flex
                flexDirection = FlexDirection.row
                justifyContent = JustifyContent.spaceBetween
            }

            styledDiv {
                css {
                    display = Display.flex
                    flexDirection = FlexDirection.column
                    flexWrap = FlexWrap.nowrap
                }
                for ((length, count) in player.occupiedSegments.groupingBy { it.length }.eachCount().entries.sortedByDescending { it.key }) {
                    styledDiv {
                        css {
                            display = Display.flex
                            flexDirection = FlexDirection.row
                            justifyContent = JustifyContent.spaceBetween
                            alignItems = Align.center
                            marginBottom = 6.px
                        }
                        styledDiv {
                            css {
                                display = Display.flex
                                flexDirection = FlexDirection.row
                                justifyContent = JustifyContent.left
                                alignItems = Align.center
                            }
                            repeat(length) {
                                styledImg {
                                    css {
                                        marginRight = 4.px
                                    }
                                    attrs {
                                        src = "/icons/railway-car.png"
                                        width = 24.px.toString()
                                    }
                                }
                            }
                        }
                        mTypography(variant = MTypographyVariant.body2) {
                            val pointsPerSegment = props.gameMap.getPointsForSegments(length)
                            +"$count * $pointsPerSegment = ${pointsPerSegment * count}"
                        }
                    }
                }
            }

            pointsLabel(segmentsPoints, Color.lightGreen)
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

        val playerStatsBar by css {
            width = 100.pct
            minHeight = 40.px
            display = Display.flex
            alignItems = Align.center
            flexDirection = FlexDirection.row
            justifyContent = JustifyContent.spaceBetween
            paddingLeft = 12.px
        }

        val playerSummaryRoot by css {
            minHeight = 0.px
            padding = 0.px.toString()
        }

        val playerSummaryContent by css {
            margin = 0.px.toString()
        }

        val playerDetailsRoot by css {
            display = Display.flex
            flexDirection = FlexDirection.column
            padding = 0.px.toString()
            paddingBottom = 10.px
        }

        val summaryExpanded by css {
            margin = 0.px.toString()
            minHeight = 0.px
        }
    }
}

fun RBuilder.endScreen(builder: EndScreenProps.() -> Unit) {
    child(EndScreen::class) {
        attrs(builder)
    }
}