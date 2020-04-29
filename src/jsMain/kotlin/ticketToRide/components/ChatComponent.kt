package ticketToRide.components

import com.ccfraser.muirwik.components.MColor
import com.ccfraser.muirwik.components.button.mIconButton
import kotlinx.css.*
import kotlinx.html.InputType
import kotlinx.html.js.*
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.KeyboardEvent
import react.*
import styled.*
import ticketToRide.Response

interface ChatProps : RProps {
    var messages: List<Response.ChatMessage>
    var onSendMessage: (String) -> Unit
}

interface ChatState : RState {
    var messageText: String
}

class ChatComponent : RComponent<ChatProps, ChatState>() {

    override fun ChatState.init(props: ChatProps) {
        messageText = ""
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
        styledDiv {
            css {
                margin = 4.px.toString()
                display = Display.flex
                flexDirection = FlexDirection.row
                justifyContent = JustifyContent.spaceBetween
                alignItems = Align.center
                margin = 4.px.toString()
            }
            styledInput(InputType.text) {
                css {
                    width = 100.pct
                    padding = 4.px.toString()
                    fontSize = 1.rem
                }
                attrs {
                    value = state.messageText
                    onKeyPressFunction = { e ->
                        (e.asDynamic().nativeEvent as KeyboardEvent).let {
                            if (it.keyCode == 13 && !it.ctrlKey && !it.altKey && !it.shiftKey) sendMessage()
                        }
                    }
                    onChangeFunction = { e ->
                        val text = (e.target as HTMLInputElement).value
                        setState { messageText = text }
                    }
                }
            }
            mIconButton("send", MColor.secondary) {
                attrs {
                    onClick = { sendMessage() }
                }
            }
        }
    }

    private fun sendMessage() {
        if (state.messageText.isNotBlank()) {
            props.onSendMessage(state.messageText)
            setState { messageText = "" }
        }
    }
}

fun RBuilder.chat(messages: List<Response.ChatMessage>, onSendMessage: (String) -> Unit) {
    child(ChatComponent::class) {
        attrs {
            this.messages = messages
            this.onSendMessage = onSendMessage
        }
    }
}