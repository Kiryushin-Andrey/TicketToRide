package ticketToRide.components

import com.ccfraser.muirwik.components.*
import kotlinx.css.*
import react.*
import styled.*
import ticketToRide.playerState.BuildingStation

class BuildingStationComponent : ComponentBase<ComponentBaseProps, RState>() {
    override fun RBuilder.render() {
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
                    with(props.playerState as BuildingStation) {
                        mTypography(target.value, MTypographyVariant.h6) {
                            css {
                                textAlign = TextAlign.right
                                paddingRight = 16.px
                            }
                        }
                    }
                }
            }

            chooseCardsToDrop(props.playerState as BuildingStation)
        }
    }

    private fun RBuilder.chooseCardsToDrop(playerState: BuildingStation) {
        val occupiedBy = props.gameState.players.find { it.placedStations.contains(playerState.target) }
        when {
            occupiedBy == me ->
                mTypography("Станция уже построена \uD83D\uDE0A", MTypographyVariant.body1) {
                    css { marginTop = 10.px }
                }

            occupiedBy != null ->
                mTypography("Станция уже построена другим игроком \uD83D\uDE1E", MTypographyVariant.body1) {
                    css { marginTop = 10.px }
                }

            else ->
                optionsForCardsToDrop {
                    confirmBtnTitle = "Ставлю станцию"
                    options = playerState.optionsForCardsToDrop
                    chosenCardsToDropIx = playerState.chosenCardsToDropIx
                    onChooseCards = { ix -> act { playerState.chooseCardsToDrop(ix) } }
                    onConfirm = { act { playerState.confirm() } }
                }
        }
    }
}

fun RBuilder.buildingStation(props: ComponentBaseProps) {
    child(BuildingStationComponent::class) {
        attrs {
            this.gameState = props.gameState
            this.playerState = props.playerState
            this.onAction = props.onAction
        }
    }
}