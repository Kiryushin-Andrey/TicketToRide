package ticketToRide.components.chat

import com.ccfraser.muirwik.components.button.mIconButton
import com.ccfraser.muirwik.components.form.*
import com.ccfraser.muirwik.components.*
import kotlinx.css.*
import org.w3c.dom.HTMLInputElement
import react.*
import styled.*
import ticketToRide.Locale
import ticketToRide.LocalizedStrings

external interface ChatSendMessageTextBoxProps : RProps {
    var locale: Locale
    var onSendMessage: (String) -> Unit
}

external interface ChatSendMessageTextBoxState : RState {
    var messageText: String
}

@JsExport
@Suppress("NON_EXPORTABLE_TYPE")
class ChatSendMessageTextBox : RComponent<ChatSendMessageTextBoxProps, ChatSendMessageTextBoxState>() {

    override fun ChatSendMessageTextBoxState.init(props: ChatSendMessageTextBoxProps) {
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
            mTextField(str.sendMessage) {
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
            mIconButton("send") {
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

    private inner class Strings : LocalizedStrings({ props.locale }) {

        val sendMessage by loc(
            Locale.En to "Send message",
            Locale.Ru to "Отправить сообщение"
        )
    }

    private val str = Strings()
}

fun RBuilder.chatSendMessageTextBox(locale: Locale, onSendMessage: (String) -> Unit) {
    child(ChatSendMessageTextBox::class) {
        attrs {
            this.locale = locale
            this.onSendMessage = onSendMessage
        }
    }
}