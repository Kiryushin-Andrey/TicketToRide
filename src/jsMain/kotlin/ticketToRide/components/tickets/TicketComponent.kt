package ticketToRide.components.tickets

import com.ccfraser.muirwik.components.*
import kotlinx.css.*
import react.*
import react.dom.label
import styled.*
import ticketToRide.Ticket

class TicketCheckbox(val checked: Boolean, val onChange: () -> Unit)

class TicketComponent : RComponent<TicketComponent.Props, RState>() {

    interface Props : RProps {
        var ticket: Ticket
        var highlighted: Boolean
        var fulfilled: Boolean
        var finalScreen: Boolean
        var checkbox: TicketCheckbox?
        var onMouseOver: () -> Unit
        var onMouseOut: () -> Unit
    }

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
                backgroundColor = if (props.fulfilled && !props.finalScreen) Color.lightGreen else Color.linen
            }
            label {
                styledDiv {
                    css {
                        minHeight = 40.px
                        display = Display.flex
                        alignItems = Align.center
                        flexDirection = FlexDirection.row
                        justifyContent = JustifyContent.spaceBetween
                        if (props.checkbox != null) {
                            cursor = Cursor.pointer
                        }
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
                    when {
                        props.finalScreen && props.fulfilled ->
                            pointsLabel("+${props.ticket.points}", Color.lightGreen)
                        props.finalScreen && !props.fulfilled ->
                            pointsLabel("-${props.ticket.points}", Color.lightCoral)
                        else ->
                            pointsLabel(props.ticket.points, Color.orange)
                    }
                }
            }
        }
    }
}

fun RBuilder.ticket(ticket: Ticket, builder: TicketComponent.Props.() -> Unit): ReactElement {
    return child(TicketComponent::class) {
        attrs {
            this.ticket = ticket
            builder()
        }
    }
}