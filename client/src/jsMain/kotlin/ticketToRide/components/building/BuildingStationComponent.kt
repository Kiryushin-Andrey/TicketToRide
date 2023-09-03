package ticketToRide.components.building

import csstype.*
import csstype.px
import emotion.react.css
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import ticketToRide.Locale
import ticketToRide.LocalizedStrings
import ticketToRide.components.*
import ticketToRide.localize
import ticketToRide.PlayerState.MyTurn.BuildingStation

val BuildingStationComponent = FC<GameComponentProps> { props ->
    div {
        css {
            paddingLeft = 10.px
            paddingRight = 10.px
        }
        div {
            css {
                display = Display.flex
                flexDirection = FlexDirection.row
                flexWrap = FlexWrap.nowrap
                justifyContent = JustifyContent.spaceBetween
                alignItems = AlignItems.center
            }
            img {
                src = "/icons/building-segment.png"
                height = 100.0
            }
            div {
                with(props.playerState as BuildingStation) {
                    Typography {
                        sx {
                            textAlign = TextAlign.right
                            paddingRight = 16.px
                        }
                        variant = TypographyVariant.h6
                        +target.localize(props.locale, props.gameMap)
                    }
                }
            }
        }

        chooseCardsToDrop {
            copyFrom(props)
            playerState = props.playerState as BuildingStation
        }
    }
}

private val chooseCardsToDrop = FC<GameComponentProps> { props ->
    val str = useMemo(props.locale) { strings(props.locale) }
    val me = props.gameState.me
    val gameState = props.gameState
    val playerState = props.playerState as BuildingStation

    val occupiedBy = gameState.players.find { it.placedStations.contains(playerState.target) }
    when {
        occupiedBy == me ->
            Typography {
                sx { marginTop = 10.px }
                variant = TypographyVariant.body1
                +str.stationAlreadyPlacedByYou
            }

        occupiedBy != null ->
            Typography {
                sx { marginTop = 10.px }
                variant = TypographyVariant.body1
                +str.stationAlreadyPlacedByAnotherPlayer
            }

        else ->
            OptionsForCardsToDropComponent {
                locale = props.locale
                confirmBtnTitle = str.putStation
                options = playerState.optionsForCardsToDrop
                chosenCardsToDropIx = playerState.chosenCardsToDropIx
                onChooseCards = { ix -> props.act { playerState.chooseCardsToDrop(ix) } }
                onConfirm = { props.act { playerState.confirm() } }
            }
    }
}

private fun strings(locale: Locale) = object : LocalizedStrings({ locale }) {

    val stationAlreadyPlacedByYou by loc(
        Locale.En to "You already have a station here \uD83D\uDE0A",
        Locale.Ru to "У вас уже есть здесь станция \uD83D\uDE0A"
    )

    val stationAlreadyPlacedByAnotherPlayer by loc(
        Locale.En to "Another player has already placed a station here \uD83D\uDE1E",
        Locale.Ru to "Станция уже построена другим игроком \uD83D\uDE1E"
    )

    val putStation by loc(
        Locale.En to "Build station",
        Locale.Ru to "Ставлю станцию"
    )
}
