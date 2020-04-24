package ticketToRide.components

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.mButton
import kotlinx.css.*
import react.*
import styled.*
import ticketToRide.CityName
import ticketToRide.playerState.*

interface BuildingSegmentProps : ComponentBaseProps {
    var from: CityName
    var to: CityName?
}

interface BuildingSegmentState : RState {
    var cardsToDropIx: Int?
}

class BuildingSegmentComponent : ComponentBase<BuildingSegmentProps, BuildingSegmentState>() {

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
                    mTypography(
                        if (props.to != null) props.from.value else props.from.value + " - ?",
                        MTypographyVariant.h6
                    )
                    props.to?.let { toCityName ->
                        mTypography(
                            toCityName.value,
                            MTypographyVariant.h6
                        )
                    }
                }
            }

            (playerState as? BuildingSegment)?.let { chooseCardsToDrop(it) }
        }
    }

    private fun RBuilder.chooseCardsToDrop(playerState: BuildingSegment) {
        val optionsForCardsToDrop = playerState.optionsForCardsToDrop

        when {
            optionsForCardsToDrop.isEmpty() ->
                mTypography("Не хватает карт на строительство \uD83D\uDE1E", MTypographyVariant.body1) {
                    css { marginTop = 10.px }
                }

            optionsForCardsToDrop.size == 1 -> {
                styledDiv {
                    css {
                        marginTop = 10.px
                        display = Display.flex
                        justifyContent = JustifyContent.spaceBetween
                    }
                    styledDiv {
                        css {
                            display = Display.inlineFlex
                            justifyContent = JustifyContent.left
                        }
                        optionsForCardsToDrop[0].forEach(::myCard)
                    }
                    confirmButton(playerState)
                }
            }

            else -> {
                styledDiv {
                    css {
                        marginTop = 10.px
                        display = Display.flex
                        flexDirection = FlexDirection.column
                        flexWrap = FlexWrap.nowrap
                    }
                    for ((ix, cards) in optionsForCardsToDrop.withIndex()) {
                        styledLabel {
                            mPaper {
                                attrs {
                                    elevation = 4
                                }
                                css {
                                    display = Display.flex
                                    flexDirection = FlexDirection.row
                                    flexWrap = FlexWrap.nowrap
                                    alignItems = Align.center
                                    borderRadius = 4.px
                                    paddingLeft = 10.px
                                    marginBottom = 8.px
                                }
                                mRadio {
                                    attrs {
                                        checked = ix == state.cardsToDropIx
                                        onClick = { setState { cardsToDropIx = ix } }
                                    }
                                }
                                cards.forEach(::myCard)
                            }
                        }
                    }
                    confirmButton(playerState)
                }
            }
        }
    }

    private fun RBuilder.confirmButton(playerState: BuildingSegment) {
        val options = playerState.optionsForCardsToDrop
        val cardsToDropIx = if (options.size > 1) state.cardsToDropIx else 0
        mButton("Строю!", MColor.primary) {
            if (options.size > 1) {
                css { marginTop = 10.px }
            }
            attrs {
                disabled = cardsToDropIx == null
                onClick = {
                    cardsToDropIx?.let {
                        act { playerState.confirm(playerState.optionsForCardsToDrop[it]) }
                    }
                }
            }
        }
    }
}

fun RBuilder.buildingSegment(props: ComponentBaseProps) {
    val playerState = props.playerState
    val (from, to) = when (playerState) {
        is BuildingSegmentFrom -> playerState.from to null
        is BuildingSegment -> with (playerState.segment) { from to to }
        else -> throw Error("BuildingSegment attempted to render in an unexpected state")
    }

    child(BuildingSegmentComponent::class) {
        attrs {
            this.gameState = props.gameState
            this.playerState = props.playerState
            this.onAction = props.onAction
            this.from = from
            this.to = to
        }
    }
}