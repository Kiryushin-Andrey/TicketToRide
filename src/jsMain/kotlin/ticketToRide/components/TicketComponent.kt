package ticketToRide.components

import com.ccfraser.muirwik.components.MTypographyVariant
import com.ccfraser.muirwik.components.mPaper
import com.ccfraser.muirwik.components.mTypography
import kotlinx.css.*
import react.*
import styled.css
import styled.styledDiv
import ticketToRide.Ticket

interface TicketProps : RProps {
    var ticket: Ticket
}

class TicketComponent : RComponent<TicketProps, RState>() {
    override fun RBuilder.render() {
        styledDiv {
            css {
                minHeight = 40.px
                display = Display.flex
                alignItems = Align.center
                flexDirection = FlexDirection.row
                justifyContent = JustifyContent.spaceBetween
                cursor = Cursor.default
            }
            mTypography(variant = MTypographyVariant.body2) {
                +"${props.ticket.from.value} - ${props.ticket.to.value}"
            }
            mPaper {
                attrs { elevation = 4 }
                css {
                    width = 20.px
                    height = 20.px
                    textAlign = TextAlign.center
                    verticalAlign = VerticalAlign.middle
                    padding = 3.px.toString()
                    borderRadius = 50.pct
                    backgroundColor = Color.orange.withAlpha(0.7)
                }
                +props.ticket.points.toString()
            }
        }
    }
}

fun RBuilder.ticket(ticket: Ticket): ReactElement {
    return child(TicketComponent::class) {
        attrs {
            this.ticket = ticket
        }
    }
}