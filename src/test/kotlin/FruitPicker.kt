package com.github.roschlau.kregex

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate


class FruitPickerTest {

    @Test
    fun `first match`() {
        val input = """
            John picked 7 apples on 2/6/2016.
            Peter picked only 2 pears on 13/6/2016.
            Jane collected 5 bananas on 5/7/2016.
            """.trimIndent()
        FruitPicker.find(input)!!.run {
            assertEquals("John", name)
            assertEquals(7, count)
            assertEquals("apples", fruitType)
            assertEquals(LocalDate.of(2016, 6, 2), date)
        }
    }

    @Test
    fun `all matches`() {
        val input = """
            John picked 7 apples on 2/6/2016.
            Peter picked only 2 pears on 13/6/2016.
            Jane collected 5 bananas on 5/7/2016.
            """.trimIndent()
        val checks = sequenceOf(
            check("John", 7, "apples", LocalDate.of(2016, 6, 2)),
            check("Peter", 2, "pears", LocalDate.of(2016, 6, 13)),
            check("Jane", 5, "bananas", LocalDate.of(2016, 7, 5))
        )
        FruitPicker.findAll(input)
            .zip(checks)
            .forEach { (person, check) -> check(person) }
    }

    private fun check(name: String, count: Int, fruitType: String, date: LocalDate) = { picker: FruitPicker ->
        assertEquals(name, picker.name)
        assertEquals(count, picker.count)
        assertEquals(fruitType, picker.fruitType)
        assertEquals(date, picker.date)
    }
}

class FruitPicker(match: MatchResult) : TypedRegex(match) {
    companion object : RegexMatcher<FruitPicker>("""([A-Z]\w+).+(\d) (\w+) on (\d+/\d+/\d+)""", ::FruitPicker)

    val name by group()
    val count by group().int()
    val fruitType by group()
    val date by group().localDate("d/M/yyyy")
}
