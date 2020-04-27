package ticketToRide.components

import com.ccfraser.muirwik.components.*
import kotlinx.css.*
import react.*
import styled.*
import ticketToRide.Ticket

interface TicketProps : RProps {
    var ticket: Ticket
    var highlighted: Boolean
    var checkbox: TicketCheckbox?
    var onMouseOver: () -> Unit
    var onMouseOut: () -> Unit
}

class TicketCheckbox(val checked: Boolean, val onChange: () -> Unit)

class TicketComponent : RComponent<TicketProps, RState>() {
    override fun RBuilder.render() {
        mPaper {
            attrs {
                elevation = if (props.highlighted) 6 else 2
                with(asDynamic()) {
                    onMouseOver = { props.onMouseOver() }
                    onMouseOut = { props.onMouseOut() }
                }
            }
            css {
                borderRadius = 4.px
                margin = 4.px.toString()
                paddingLeft = 12.px
                paddingRight = 12.px
                backgroundColor = Color.chocolate.withAlpha(0.2)
            }
            styledDiv {
                css {
                    minHeight = 40.px
                    display = Display.flex
                    alignItems = Align.center
                    flexDirection = FlexDirection.row
                    justifyContent = JustifyContent.spaceBetween
                }
                mTypography(variant = MTypographyVariant.body2) {
                    props.checkbox?.let {
                        mCheckbox {
                            attrs {
                                color = MOptionColor.primary
                                checked = it.checked
                                onChange = { _, _ -> it.onChange() }
                            }
                        }
                    }
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
}

fun RBuilder.ticket(ticket: Ticket, builder: TicketProps.() -> Unit): ReactElement {
    return child(TicketComponent::class) {
        attrs {
            this.ticket = ticket
            builder()
        }
    }
}