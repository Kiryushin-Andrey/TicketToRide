package svg

import kotlinx.html.HTMLTag
import kotlinx.html.SVG
import kotlinx.html.TagConsumer
import react.ReactElement
import react.dom.RDOMBuilder
import react.dom.tag

class DEFS(override val consumer: TagConsumer<*>) : HTMLTag("defs", consumer, emptyMap(), null, true, false)

inline fun RDOMBuilder<SVG>.defs(block: RDOMBuilder<DEFS>.() -> Unit): ReactElement = tag(block) { DEFS(it) }
