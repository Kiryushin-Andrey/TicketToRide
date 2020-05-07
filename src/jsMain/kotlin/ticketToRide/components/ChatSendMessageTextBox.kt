package ticketToRide.components

import com.ccfraser.muirwik.components.button.mIconButton
import com.ccfraser.muirwik.components.form.*
import com.ccfraser.muirwik.components.*
import kotlinx.css.*
import org.w3c.dom.HTMLInputElement
import react.*
import styled.*

interface ChatSendMessageProps : RProps {
    var onSendMessage: (String) -> Unit
}

interface ChatSendMessageState : RState {
    var messageText: String
}

class ChatSendMessageTextBox : RComponent<ChatSendMessageProps, ChatSendMessageState>() {
    override fun ChatSendMessageState.init(props: ChatSendMessageProps) {
        messageText = ""
    }

    override fun RBuilder.render() {
        styledDiv {
            css {
                margin = 4.px.toString()
                display = Display.flex
                flexDirection = FlexDirection.row
                justifyContent = JustifyContent.spaceBetween
                alignItems = Align.center
                margin = 4.px.toString()
            }
            mTextField("Отправить сообщение") {
                attrs {
                    value = state.messageText
                    onChange = { e ->
                        val text = (e.target as HTMLInputElement).value
                        setState { messageText = text }
                    }
                    onKeyDown = { e -> if (e.keyCode == 13) sendMessage() }
                    asDynamic().size = "small"
                    fullWidth = true
                    variant = MFormControlVariant.outlined
                    margin = MFormControlMargin.dense
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

fun RBuilder.chatSendMessageTextBox(onSendMessage: (String) -> Unit) {
    child(ChatSendMessageTextBox::class) {
        attrs {
            this.onSendMessage = onSendMessage
        }
    }
}