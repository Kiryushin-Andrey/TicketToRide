package ticketToRide.components.building

import csstype.*
import emotion.react.css
import kotlinx.browser.window
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
import ticketToRide.PlayerState.MyTurn.*

val BuildingSegmentComponent = FC<GameComponentProps> { props ->
    var showArrivalGif by useState(false)
    val onShowTrainArrival = useCallback(showArrivalGif) { showArrivalGif = true }

    if (showArrivalGif) {
        img {
            src = "/images/lumiere.gif"
        }
        return@FC
    }

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
                with(props.playerState as BuildingSegment) {
                    Typography {
                        sx {
                            textAlign = TextAlign.right
                            paddingRight = 16.px
                        }
                        variant = TypographyVariant.h6
                        +from.localize(props.locale, props.gameMap)
                    }
                    Typography {
                        sx {
                            textAlign = TextAlign.right
                            paddingRight = 16.px
                        }
                        variant = TypographyVariant.h6
                        +to.localize(props.locale, props.gameMap)
                    }
                }
            }
        }

        chooseCardsToDrop {
            copyFrom(props)
            playerState = props.playerState as BuildingSegment
            showTrainArrival = onShowTrainArrival
        }
    }
}

external interface CardsToDropForSegmentProps : GameComponentProps {
    var showTrainArrival: () -> Unit
}

private val chooseCardsToDrop = FC<CardsToDropForSegmentProps> { props ->
    val str = useMemo(props.locale) { strings(props.locale) }
    val me = props.gameState.me
    val playerState = props.playerState as BuildingSegment

    when {
        playerState.availableSegments.isEmpty() -> {
            val occupiedByMe = me.occupiedSegments.any { it.connects(playerState.from, playerState.to) }
            val message = if (occupiedByMe) str.segmentAlreadyTakenByYou else str.segmentAlreadyTakenByAnotherPlayer
            Typography {
                sx { marginTop = 10.px }
                variant = TypographyVariant.body1
                +message
            }
        }

        me.carsLeft < playerState.length ->
            Typography {
                sx { marginTop = 10.px }
                variant = TypographyVariant.body1
                +str.notEnoughCars
            }

        else ->
            OptionsForCardsToDropComponent {
                locale = props.locale
                confirmBtnTitle = str.buildSegment
                options = playerState.optionsForCardsToDrop.map { it.second }
                chosenCardsToDropIx = playerState.chosenCardsToDropIx
                onChooseCards = { ix -> props.act { playerState.chooseCardsToDrop(ix) } }
                onConfirm = {
                    props.showTrainArrival()
                    window.setTimeout({ props.act { playerState.confirm() } }, 3000)
                }
            }
    }
}

private fun strings(locale: Locale) = object : LocalizedStrings({ locale }) {

    val buildSegment by loc(
        Locale.En to "Build segment",
        Locale.Ru to "Строю сегмент"
    )

    val segmentAlreadyTakenByYou by loc(
        Locale.En to "Segment already taken by you \uD83D\uDE0A",
        Locale.Ru to "Сегмент уже построен \uD83D\uDE0A"
    )

    val segmentAlreadyTakenByAnotherPlayer by loc(
        Locale.En to "Segment already taken by another player \uD83D\uDE1E",
        Locale.Ru to "Сегмент уже занят другим игроком \uD83D\uDE1E"
    )

    val notEnoughCars by loc(
        Locale.En to "Not enough wagons \uD83D\uDE1E",
        Locale.Ru to "Не хватает вагонов на строительство \uD83D\uDE1E"
    )
}
