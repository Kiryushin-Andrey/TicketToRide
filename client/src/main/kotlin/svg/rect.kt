package svg

import kotlinx.html.CommonAttributeGroupFacade
import kotlinx.html.HTMLTag
import kotlinx.html.TagConsumer
import react.RBuilder
import react.ReactElement
import react.dom.tag
import styled.StyledDOMBuilder
import styled.styledTag

class RECT(override val consumer: TagConsumer<*>) : HTMLTag("rect", consumer, emptyMap(), null, true, false),
    CommonAttributeGroupFacade {
    var x by intAttribute("x")
    var y by intAttribute("y")
    var width by intAttribute("width")
    var height by intAttribute("height")
    var fill by stringAttribute("fill")
    var stroke by stringAttribute("stroke")
    var strokeWidth by intAttribute("strokeWidth")
    var transform by stringAttribute("transform")
}

inline fun RBuilder.styledRect(block: StyledDOMBuilder<RECT>.() -> Unit): ReactElement = styledTag(block) { RECT(it) }
