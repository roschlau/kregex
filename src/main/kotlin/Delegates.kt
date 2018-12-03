package com.github.roschlau.kregex

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <R, I, O> DelegateProvider<R, I>.transform(transform: (I) -> O) =
    TransformDelegateProvider(this, transform)

class TransformDelegateProvider<R, I, O>(
    private val provider: DelegateProvider<R, I>,
    private val transform: (I) -> O
) : DelegateProvider<R, O> {
    override fun provideDelegate(thisRef: TypedRegex, property: KProperty<*>): ReadOnlyProperty<R, O> {
        val backingDelegate = provider.provideDelegate(thisRef, property)
        return object: ReadOnlyProperty<R, O> {
            override fun getValue(thisRef: R, property: KProperty<*>): O {
                return transform(backingDelegate.getValue(thisRef, property))
            }
        }
    }
}

interface DelegateProvider<R, T> {
    operator fun provideDelegate(thisRef: TypedRegex, property: KProperty<*>): ReadOnlyProperty<R, T>
}

class SimpleValueDelegate<R, T>(val value: T): ReadOnlyProperty<R, T> {
    override fun getValue(thisRef: R, property: KProperty<*>) = value
}
