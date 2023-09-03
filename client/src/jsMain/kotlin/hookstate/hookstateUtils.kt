package hookstate

import js.core.jso

fun <S> HookstateRoot<S>.getNoProxy(): S =
    asDynamic().get(jso { noproxy = true }) as S
