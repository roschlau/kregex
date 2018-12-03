package com.github.roschlau.kregex

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PersonTest {

    @Test
    fun `first match`() {
        val input = """
            Thomas: 180cm,75kg
            Jane: 163cm,45kg
            Mark: 175cm,60kg
            """.trimIndent()
        Person.find(input)!!.run {
            assertEquals("Thomas", name)
            body!!.run {
                assertEquals(180.0, height)
                assertEquals(75.0, weight)
            }
        }
    }

    @Test
    fun `all matches`() {
        val input = """
            Thomas: 180cm,75kg
            Jane: 163cm,45kg
            Mark: 175cm,60kg
            """.trimIndent()
        val checks = sequenceOf(
            check("Thomas", 180.0, 75.0),
            check("Jane", 163.0, 45.0),
            check("Mark", 175.0, 60.0)
        )
        Person.findAll(input)
            .zip(checks)
            .forEach { (person, check) -> check(person) }
    }

    private fun check(name: String, height: Double, weight: Double) = { person: Person ->
        assertEquals(name, person.name)
        assertEquals(height, person.body!!.height)
        assertEquals(weight, person.body!!.weight)
    }
}

class Person(match: MatchResult) : TypedRegex(match) {
    companion object : RegexMatcher<Person>("""(\w+): (.+)""", ::Person)

    val name by group()
    val body by group().nested(Body)
}

class Body(match: MatchResult) : TypedRegex(match) {
    companion object : RegexMatcher<Body>("""(\d+)cm,(\d+)kg""", ::Body)

    val height by group().double()
    val weight by group().double()
}
