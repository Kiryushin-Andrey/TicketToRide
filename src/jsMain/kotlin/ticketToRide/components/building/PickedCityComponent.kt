package ticketToRide.components.building

import com.ccfraser.muirwik.components.MColor
import com.ccfraser.muirwik.components.MTypographyVariant
import com.ccfraser.muirwik.components.button.MButtonVariant
import com.ccfraser.muirwik.components.button.mButton
import com.ccfraser.muirwik.components.mTypography
import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv
import styled.styledImg
import ticketToRide.Locale
import ticketToRide.LocalizedStrings
import ticketToRide.components.ComponentBase
import ticketToRide.components.ComponentBaseProps
import ticketToRide.components.componentBase
import ticketToRide.playerState.PlayerState

class PickedCityComponent : ComponentBase<ComponentBaseProps, RState>() {
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
                    css {
                        display = Display.flex
                        flexDirection = FlexDirection.column
                        justifyContent = JustifyContent.spaceBetween
                        height = 100.px
                    }
                    with(props.playerState as PlayerState.MyTurn.PickedCity) {
                        mTypography(target.value, MTypographyVariant.h6) {
                            css {
                                textAlign = TextAlign.right
                                paddingRight = 16.px
                            }
                        }
                        mButton(str.station, MColor.primary, MButtonVariant.contained) {
                            attrs {
                                onClick = { act { buildStation() } }
                            }
                        }
                    }
                }
            }
        }
    }

    private inner class Strings : LocalizedStrings({ props.locale }) {
        val station by loc(
            Locale.En to "Station",
            Locale.Ru to "Станция"
        )
    }

    private val str = Strings()
}

fun RBuilder.pickedCity(props: ComponentBaseProps) = componentBase<PickedCityComponent, ComponentBaseProps>(props)