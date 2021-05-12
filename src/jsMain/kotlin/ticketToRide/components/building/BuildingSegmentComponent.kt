package ticketToRide.components.building

import com.ccfraser.muirwik.components.*
import kotlinx.css.*
import org.w3c.dom.Image
import react.*
import styled.*
import ticketToRide.Locale
import ticketToRide.LocalizedStrings
import ticketToRide.components.ComponentBase
import ticketToRide.components.ComponentBaseProps
import ticketToRide.components.componentBase
import ticketToRide.playerState.PlayerState.MyTurn.*
import kotlinx.browser.window
import ticketToRide.localize

external interface BuildingSegmentComponentState : RState {
    var showArrivalGif: Boolean
}

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
class BuildingSegmentComponent : ComponentBase<ComponentBaseProps, BuildingSegmentComponentState>() {

    override fun BuildingSegmentComponentState.init() {
        Image().src = "/images/lumiere.gif"
    }

    override fun RBuilder.render() {
        if (state.showArrivalGif) {
            styledImg(src = "/images/lumiere.gif") {}
            return
        }

        styledDiv {
            css {
                paddingLeft = 10.px
                paddingRight = 10.px
            }
            styledDiv {
                css {
                    display = Display.flex
                    flexDirection = FlexDirection.row
                    flexWrap = FlexWrap.nowrap
                    justifyContent = JustifyContent.spaceBetween
                    alignItems = Align.center
                }
                styledImg {
                    attrs {
                        src = "/icons/building-segment.png"
                        height = 100.px.toString()
                    }
                }
                styledDiv {
                    with(props.playerState as BuildingSegment) {
                        mTypography(from.localize(props.locale, props.gameMap), MTypographyVariant.h6) {
                            css {
                                textAlign = TextAlign.right
                                paddingRight = 16.px
                            }
                        }
                        mTypography(to.localize(props.locale, props.gameMap), MTypographyVariant.h6) {
                            css {
                                textAlign = TextAlign.right
                                paddingRight = 16.px
                            }
                        }
                    }
                }
            }

            chooseCardsToDrop(props.playerState as BuildingSegment)
        }
    }

    private fun RBuilder.chooseCardsToDrop(playerState: BuildingSegment) {
        when {
            playerState.availableSegments.isEmpty() -> {
                val occupiedByMe = me.occupiedSegments.any { it.connects(playerState.from, playerState.to) }
                val message = if (occupiedByMe) str.segmentAlreadyTakenByYou else str.segmentAlreadyTakenByAnotherPlayer
                mTypography(message, MTypographyVariant.body1) {
                    css { marginTop = 10.px }
                }
            }

            me.carsLeft < playerState.length ->
                mTypography(str.notEnoughCars, MTypographyVariant.body1) {
                    css { marginTop = 10.px }
                }

            else ->
                optionsForCardsToDrop(props.locale) {
                    confirmBtnTitle = str.buildSegment
                    options = playerState.optionsForCardsToDrop.map { it.second }
                    chosenCardsToDropIx = playerState.chosenCardsToDropIx
                    onChooseCards = { ix -> act { playerState.chooseCardsToDrop(ix) } }
                    onConfirm = {
                        setState { showArrivalGif = true }
                        window.setTimeout({ act { playerState.confirm() } }, 3000)
                    }
                }
        }
    }

    private inner class Strings : LocalizedStrings({ props.locale }) {

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

    private val str = Strings()
}

fun RBuilder.buildingSegment(props: ComponentBaseProps) =
    componentBase<BuildingSegmentComponent, ComponentBaseProps>(props)