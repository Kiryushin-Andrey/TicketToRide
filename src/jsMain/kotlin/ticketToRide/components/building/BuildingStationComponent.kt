package ticketToRide.components.building

import com.ccfraser.muirwik.components.*
import kotlinx.css.*
import react.*
import styled.*
import ticketToRide.Locale
import ticketToRide.LocalizedStrings
import ticketToRide.components.ComponentBase
import ticketToRide.components.ComponentBaseProps
import ticketToRide.components.componentBase
import ticketToRide.localize
import ticketToRide.playerState.PlayerState.MyTurn.BuildingStation

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
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
                        mTypography(target.localize(props.locale, props.gameMap), MTypographyVariant.h6) {
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
                mTypography(str.stationAlreadyPlacedByYou, MTypographyVariant.body1) {
                    css { marginTop = 10.px }
                }

            occupiedBy != null ->
                mTypography(str.stationAlreadyPlacedByAnotherPlayer, MTypographyVariant.body1) {
                    css { marginTop = 10.px }
                }

            else ->
                optionsForCardsToDrop(props.locale) {
                    confirmBtnTitle = str.putStation
                    options = playerState.optionsForCardsToDrop
                    chosenCardsToDropIx = playerState.chosenCardsToDropIx
                    onChooseCards = { ix -> act { playerState.chooseCardsToDrop(ix) } }
                    onConfirm = { act { playerState.confirm() } }
                }
        }
    }

    private inner class Strings : LocalizedStrings({ props.locale }) {

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

    private val str = Strings()
}

fun RBuilder.buildingStation(props: ComponentBaseProps) =
    componentBase<BuildingStationComponent, ComponentBaseProps>(props)