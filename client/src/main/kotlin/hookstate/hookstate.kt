@file:JsModule("@hookstate/core")
@file:JsNonModule
package hookstate

import js.core.jso

external fun <Value, State> useHookstate(initialValue: Value): State

external interface Hookstate<S> {
    fun get(): S
    fun set(value: S)
    fun set(setter: (S) -> S)
    fun merge(value: S)
    fun merge(merger: (S) -> S)
}

external interface HookstateRoot<S> {
    fun get()
    fun merge(value: S)
    fun merge(merger: (S) -> S)
}
