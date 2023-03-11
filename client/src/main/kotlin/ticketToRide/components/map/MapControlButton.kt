package ticketToRide.components.map

import csstype.*
import emotion.react.css
import mui.icons.material.SvgIconComponent
import mui.material.*
import mui.material.Size
import mui.system.sx
import pigeonMaps.PigeonProps
import react.*

external interface MapControlButtonProps: PigeonProps {
    var tooltip: String
    var icon: SvgIconComponent
    var topPosition: Top
    var onClick: () -> Unit
}

val MapControlButton = FC<MapControlButtonProps> { props ->
    Tooltip {
        placement = TooltipPlacement.right
        title = ReactNode(props.tooltip)

        IconButton {
            size = Size.small
            sx {
                position = Position.absolute
                top = props.topPosition
                left = 10.px
                backgroundColor = NamedColor.white
                borderColor = NamedColor.black
                borderRadius = 2.px
                borderStyle = LineStyle.solid
                borderWidth = 1.px
                hover {
                    backgroundColor = NamedColor.lightgray
                }
            }
            onClick = { props.onClick() }
            +props.icon.create()
        }
    }
}
