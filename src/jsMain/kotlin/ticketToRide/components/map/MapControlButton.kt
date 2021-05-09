package ticketToRide.components.map

import com.ccfraser.muirwik.components.TooltipPlacement
import com.ccfraser.muirwik.components.button.MIconButtonSize
import com.ccfraser.muirwik.components.button.mIconButton
import com.ccfraser.muirwik.components.mTooltip
import kotlinx.css.*
import pigeonMaps.PigeonProps
import react.*
import styled.css

external interface MapControlButtonProps: PigeonProps {
    var tooltip: String
    var icon: String
    var topPosition: LinearDimension
    var onClick: () -> Unit
}

private val mapControlButton = functionalComponent<MapControlButtonProps> { props ->
    mTooltip(props.tooltip, TooltipPlacement.right) {
        mIconButton(props.icon, size = MIconButtonSize.small) {
            attrs {
                css {
                    position = Position.absolute
                    top = props.topPosition
                    left = 10.px
                    backgroundColor = Color.white
                    borderColor = Color.black
                    borderRadius = 2.px
                    borderStyle = BorderStyle.solid
                    borderWidth = 1.px
                    hover {
                        backgroundColor = Color.lightGray
                    }
                }
                onClick = { props.onClick() }
            }
        }
    }
}

fun RBuilder.mapControlButton(block: MapControlButtonProps.() -> Unit): ReactElement {
    return child(mapControlButton) {
        attrs(block)
    }
}