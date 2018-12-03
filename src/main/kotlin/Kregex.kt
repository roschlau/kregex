package com.github.roschlau.kregex

import org.intellij.lang.annotations.Language
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class RegexMatcher<T : TypedRegex>(
    @Language("RegExp") pattern: String,
    private val constructor: (MatchResult) -> T
) {
    private val regex = Regex(pattern)

    fun find(str: String): T? =
        regex.find(str)
            ?.let(constructor)

    fun findAll(str: String): Sequence<T> =
        regex.findAll(str)
            .map(constructor)
}

abstract class TypedRegex(
    val match: MatchResult
) {
    private val captures = mutableMapOf<String, String>()
    private var nextImplicitGroup: Int? = 1

    protected fun entireMatch(): Group = group(0)

    protected fun group(group: Int? = null): Group =
        group
            ?.let(::Group)?.also { nextImplicitGroup = null }
            ?: nextImplicitGroup?.let { nextGroup -> Group(nextGroup).also { nextImplicitGroup = nextGroup + 1 } }
            ?: throw ImplicitAfterExplicitGroupsException()

    protected fun Group.int(): DelegateProvider<TypedRegex, Int> = transform(String::toInt)
    protected fun Group.double(): DelegateProvider<TypedRegex, Double> = transform(String::toDouble)

    protected fun <T : TypedRegex> Group.nested(matcher: RegexMatcher<T>): DelegateProvider<TypedRegex, T?> =
        transform(matcher::find)

    protected fun Group.localDate(pattern: String): DelegateProvider<TypedRegex, LocalDate> =
        transform { LocalDate.parse(it, DateTimeFormatter.ofPattern(pattern)) }

    override fun toString() = captures.toString()

    class Group(val group: Int) : DelegateProvider<TypedRegex, String> {
        override fun provideDelegate(thisRef: TypedRegex, property: KProperty<*>): ReadOnlyProperty<TypedRegex, String> {
            val groupValue = thisRef.match.groupValues[group]
            thisRef.captures += property.name to groupValue
            return SimpleValueDelegate(groupValue)
        }
    }
}

class ImplicitAfterExplicitGroupsException
    : Exception("You can not use implicit groups after you have already declared explicit groups")
