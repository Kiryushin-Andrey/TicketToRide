package ticketToRide.components

import react.RBuilder
import react.RComponent
import react.RProps
import react.RState

interface ChatProps : RProps {
}

class ChatComponent : RComponent<ChatProps, RState>() {
    override fun RBuilder.render() {
    }
}

fun RBuilder.chat(builder: ChatProps.() -> Unit) {
    child(ChatComponent::class) {
        attrs(builder)
    }
}