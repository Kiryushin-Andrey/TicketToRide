package ticketToRide.components

import com.ccfraser.muirwik.components.*
import kotlinx.css.*
import react.*
import styled.*
import ticketToRide.Locale
import ticketToRide.LocalizedStrings
import ticketToRide.PlayerView

class PlayersListComponent : RComponent<PlayersListComponent.Props, RState>() {

    interface Props : RProps {
        var players: List<PlayerView>
        var turn: Int
        var locale: Locale
    }

    override fun RBuilder.render() {
        for ((ix, player) in props.players.withIndex()) {
            mTooltip(if (player.away) str.disconnected else "", TooltipPlacement.rightStart) {
                mPaper {
                    attrs {
                        elevation = 2
                    }
                    css {
                        +ComponentStyles.playerCard
                        color = if (player.away) Color.black.withAlpha(0.4) else Color.black
                        backgroundColor = Color(player.color.rgb).withAlpha(0.4)
                        if (ix == props.turn) {
                            borderColor = Color.red
                            borderStyle = BorderStyle.solid
                            borderWidth = 4.px
                        }
                    }
                    mTypography(variant = MTypographyVariant.h6) {
                        +player.name.value
                    }
                    styledDiv {
                        css { +ComponentStyles.playerCardIcons }
                        playerCardIcon("/icons/railway-car.png", player.carsLeft)
                        playerCardIcon("/icons/station.png", player.stationsLeft)
                        playerCardIcon("/icons/cards-deck.png", player.cardsOnHand)
                        playerCardIcon("/icons/ticket.png", player.ticketsOnHand)
                    }
                }
            }
        }
    }

    object ComponentStyles : StyleSheet("PlayersList", isStatic = true) {
        val playerCard by css {
            minHeight = 60.px
            display = Display.flex
            alignItems = Align.flexStart
            flexDirection = FlexDirection.column
            justifyContent = JustifyContent.spaceBetween
            borderRadius = 4.px
            margin = 4.px.toString()
            paddingTop = 4.px
            paddingBottom = 4.px
            paddingLeft = 12.px
            paddingRight = 12.px
        }
        val playerCardIcons by css {
            display = Display.flex
            flexDirection = FlexDirection.row
            alignItems = Align.center
            width = 100.pct
            justifyContent = JustifyContent.spaceEvenly
        }
    }

    private inner class Strings : LocalizedStrings({ props.locale }) {
        val disconnected by loc(
            Locale.En to "Disconnected",
            Locale.Ru to "Отключился"
        )
    }
    private val str = Strings()
}

fun RBuilder.playerCardIcon(iconUrl: String, number: Int, style: RuleSet = {}) {
    styledImg {
        css {
            style()
        }
        attrs {
            src = iconUrl
            width = 24.px.toString()
        }
    }
    mTypography(variant = MTypographyVariant.body1) {
        +number.toString()
    }
}

fun RBuilder.playersList(players: List<PlayerView>, turn: Int, locale: Locale) = child(PlayersListComponent::class) {
    attrs {
        this.players = players
        this.turn = turn
        this.locale = locale
    }
}