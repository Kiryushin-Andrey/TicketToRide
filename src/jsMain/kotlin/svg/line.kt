package svg

import kotlinx.html.HTMLTag
import kotlinx.html.TagConsumer
import react.RBuilder
import react.ReactElement
import react.dom.tag

class LINE(override val consumer: TagConsumer<*>) : HTMLTag("line", consumer, emptyMap(), null, true, false) {
    var x1 by intAttribute("x1")
    var y1 by intAttribute("y1")
    var x2 by intAttribute("x2")
    var y2 by intAttribute("y2")
    var stroke by stringAttribute("stroke")
    var strokeWidth by intAttribute("strokeWidth")
    var transform by stringAttribute("transform")
}

inline fun RBuilder.line(block: LINE.() -> Unit): ReactElement = tag({ attrs(block) }) { LINE(it) }
