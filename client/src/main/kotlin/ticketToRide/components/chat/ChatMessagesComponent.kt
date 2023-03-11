package ticketToRide.components.chat

import csstype.*
import emotion.react.css
import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import ticketToRide.Response

external interface ChatMessagesComponentProps : Props {
    var messages: Array<Response.ChatMessage>
}

private val ChatMessagesComponent = FC<ChatMessagesComponentProps> { props ->
    div {
        css {
            display = Display.flex
            flexDirection = FlexDirection.columnReverse
            margin = 4.px
            overflowY = Auto.auto
        }
        props.messages.reversed().forEachIndexed { ix, message ->
            div {
                key = ix.toString()
                css { marginBottom = 4.px }
                span {
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

fun ChildrenBuilder.chatMessages(messages: Array<Response.ChatMessage>) {
    ChatMessagesComponent {
        this.messages = messages
    }
}
