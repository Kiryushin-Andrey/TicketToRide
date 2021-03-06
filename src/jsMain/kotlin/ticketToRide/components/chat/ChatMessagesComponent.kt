package ticketToRide.components.chat

import kotlinx.css.*
import react.*
import styled.*
import ticketToRide.Response

external interface ChatMessagesComponentProps : RProps {
    var messages: List<Response.ChatMessage>
}

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
class ChatMessagesComponent : RComponent<ChatMessagesComponentProps, RState>() {

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