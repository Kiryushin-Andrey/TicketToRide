package svg

import kotlinx.html.HTMLTag
import kotlinx.html.TagConsumer
import react.ReactElement
import react.dom.RDOMBuilder
import react.dom.tag

enum class PatternUnits { userSpaceOnUse, objectBoundingBox }

class PATTERN(override val consumer: TagConsumer<*>) : HTMLTag("pattern", consumer, emptyMap(), null, true, false) {
    var id by stringAttribute("id")
    var x by intAttribute("x")
    var y by intAttribute("y")
    var width by intAttribute("width")
    var height by intAttribute("height")
    var patternTransform by stringAttribute("patternTransform")
    var patternUnits by enumAttribute<PatternUnits>("patternUnits")
}

inline fun RDOMBuilder<DEFS>.pattern(block: RDOMBuilder<PATTERN>.() -> Unit): ReactElement = tag(block) { PATTERN(it) }
