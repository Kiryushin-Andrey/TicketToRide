package ticketToRide.components

import com.ccfraser.muirwik.components.*
import kotlinx.css.*
import org.w3c.dom.Image
import react.*
import styled.*
import ticketToRide.playerState.*
import kotlin.browser.window

interface BuildingSegmentState : RState {
    var showArrivalGif: Boolean
}

class BuildingSegmentComponent : ComponentBase<ComponentBaseProps, BuildingSegmentState>() {
    override fun BuildingSegmentState.init() {
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
                        mTypography(segment.from.value, MTypographyVariant.h6) {
                            css {
                                textAlign = TextAlign.right
                                paddingRight = 16.px
                            }
                        }
                        mTypography(segment.to.value, MTypographyVariant.h6) {
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
        val segment = playerState.segment
        val occupiedBy = props.gameState.players.find { it.occupiedSegments.contains(segment) }
        when {
            occupiedBy == me ->
                mTypography("Сегмент уже построен \uD83D\uDE0A", MTypographyVariant.body1) {
                    css { marginTop = 10.px }
                }

            occupiedBy != null ->
                mTypography("Сегмент уже занят другим игроком \uD83D\uDE1E", MTypographyVariant.body1) {
                    css { marginTop = 10.px }
                }

            me.carsLeft < segment.length ->
                mTypography("Не хватает вагонов \uD83D\uDE1E", MTypographyVariant.body1) {
                    css { marginTop = 10.px }
                }

            else ->
                optionsForCardsToDrop {
                    confirmBtnTitle = "Строю сегмент"
                    options = playerState.optionsForCardsToDrop
                    chosenCardsToDropIx = playerState.chosenCardsToDropIx
                    onChooseCards = { ix -> act { playerState.chooseCardsToDrop(ix) } }
                    onConfirm = {
                        setState { showArrivalGif = true }
                        window.setTimeout({ act { playerState.confirm() } }, 5000)
                    }
                }
        }
    }
}

fun RBuilder.buildingSegment(props: ComponentBaseProps) {
    child(BuildingSegmentComponent::class) {
        attrs {
            this.gameState = props.gameState
            this.playerState = props.playerState
            this.onAction = props.onAction
        }
    }
}