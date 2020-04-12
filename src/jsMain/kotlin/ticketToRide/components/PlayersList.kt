package ticketToRide.components

import com.ccfraser.muirwik.components.*
import kotlinx.css.*
import react.*
import react.dom.*
import styled.*
import ticketToRide.PlayerView

external interface PlayersListProps : RProps {
    var players: List<PlayerView>
}

class PlayersList : RComponent<PlayersListProps, RState>() {
    override fun RBuilder.render() {
        styledDiv {
            css {
                +ComponentStyles.playersList
            }
            for (player in props.players) {
                mPaper {
                    attrs {
                        elevation = 2
                    }
                    css {
                        +ComponentStyles.playerCard
                        backgroundColor = Color(player.color.rgb).withAlpha(0.4)
                    }
                    mTypography(variant = MTypographyVariant.h6) {
                        +player.name.value
                    }
                    styledDiv {
                        css { +ComponentStyles.playerCardIcons }
                        playerCardIcon("/icons/railway-car.png", player.carsLeft)
                        playerCardIcon("/icons/cards-deck.png", player.cardsOnHand)
                        playerCardIcon("/icons/ticket.png", player.ticketsOnHand)
                    }
                }
            }
        }
    }

    private fun RDOMBuilder<*>.playerCardIcon(iconUrl: String, number: Int) {
        p {
            img {
                attrs {
                    src = iconUrl
                    width = "24"
                }
            }
        }
        mTypography(variant = MTypographyVariant.body1) {
            +number.toString()
        }
    }

    private object ComponentStyles : StyleSheet("PlayersList", isStatic = true) {
        val playersList by css {
            display = Display.flex
            flexDirection = FlexDirection.column
            flexWrap = FlexWrap.nowrap
        }
        val playerCard by css {
            minWidth = 300.px
            display = Display.flex
            alignItems = Align.center
            flexDirection = FlexDirection.row
            justifyContent = JustifyContent.spaceBetween
            borderRadius = 4.px
            margin = "5px"
            paddingLeft = 12.px
            paddingRight = 12.px
        }
        val playerCardIcons by css {
            display = Display.flex
            flexDirection = FlexDirection.row
            alignItems = Align.center
            width = 150.px
            justifyContent = JustifyContent.spaceEvenly
        }
    }
}