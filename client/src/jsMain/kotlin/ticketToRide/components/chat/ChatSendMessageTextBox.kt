package ticketToRide.components.chat

import csstype.*
import emotion.react.css
import mui.icons.material.Send
import mui.material.*
import mui.material.Size
import react.*
import react.dom.html.ReactHTML.div
import react.dom.onChange
import ticketToRide.Locale
import ticketToRide.LocalizedStrings

external interface ChatSendMessageTextBoxProps : Props {
    var locale: Locale
    var onSendMessage: (String) -> Unit
}

private val ChatSendMessageTextBox = FC<ChatSendMessageTextBoxProps> { props ->
    val str = useMemo(props.locale) { strings(props.locale) }
    var messageText by useState("")
    val sendMessage = useCallback(messageText) {
        if (messageText.isNotBlank()) {
            props.onSendMessage(messageText)
            messageText = ""
        }
    }

    div {
        css {
            margin = 4.px
            display = Display.flex
            flexDirection = FlexDirection.row
            justifyContent = JustifyContent.spaceBetween
            alignItems = AlignItems.center
            margin = 4.px
        }

        TextField {
            label = ReactNode(str.sendMessage)
            value = messageText
            onChange = { e ->
                messageText = e.target.asDynamic().value as String
            }
            onKeyDown = { e -> if (e.key == "Enter") sendMessage() }
            size = Size.small
            fullWidth = true
            variant = FormControlVariant.outlined
            margin = FormControlMargin.dense
        }
        IconButton {
            Send()
            onClick = { sendMessage() }
        }
    }
}

private fun strings(locale: Locale) = object : LocalizedStrings({ locale }) {
    val sendMessage by loc(
        Locale.En to "Send message",
        Locale.Ru to "Отправить сообщение"
    )
}

fun ChildrenBuilder.chatSendMessageTextBox(locale: Locale, onSendMessage: (String) -> Unit) {
    ChatSendMessageTextBox {
        this.locale = locale
        this.onSendMessage = onSendMessage
    }
}
