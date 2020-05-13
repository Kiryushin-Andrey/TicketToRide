package ticketToRide.components

import kotlinx.css.*
import react.*
import styled.*
import ticketToRide.Response

class ChatMessagesComponent : RComponent<ChatMessagesComponent.Props, RState>() {

    interface Props : RProps {
        var messages: List<Response.ChatMessage>
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                display = Display.flex
                flexDirection = FlexDirection.columnReverse
                margin = 4.px.toString()
                overflowY = Overflow.auto
            }
            for (message in props.messages.asReversed()) {
                styledDiv {
                    css { marginBottom = 4.px }
                    styledSpan {
                        css {
                            fontWeight = FontWeight.bold
                            fontStyle = FontStyle.italic
                        }
                        +"${message.from.value}: "
                    }
                    +message.message
                }
            }
        }
    }
}

fun RBuilder.chatMessages(messages: List<Response.ChatMessage>) {
    child(ChatMessagesComponent::class) {
        attrs {
            this.messages = messages
        }
    }
}