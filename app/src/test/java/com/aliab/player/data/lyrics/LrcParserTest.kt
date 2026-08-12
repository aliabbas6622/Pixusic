package com.aliab.player.data.lyrics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LrcParserTest {

    @Test
    fun `parses mm ss centiseconds`() {
        val lines = LrcParser.parse("[00:12.50]Hello world")
        assertEquals(listOf(LyricLine(timeMs = 12_500L, text = "Hello world")), lines)
    }

    @Test
    fun `parses bare mm ss without fraction`() {
        val lines = LrcParser.parse("[01:02]No fraction here")
        assertEquals(listOf(LyricLine(timeMs = 62_000L, text = "No fraction here")), lines)
    }

    @Test
    fun `parses single digit minutes and one or three digit fractions`() {
        val lines = LrcParser.parse("[1:02.5]One digit\n[2:03.123]Three digits")
        assertEquals(
            listOf(
                LyricLine(timeMs = 62_500L, text = "One digit"),
                LyricLine(timeMs = 123_123L, text = "Three digits"),
            ),
            lines,
        )
    }

    @Test
    fun `expands multi timestamp lines into one line per timestamp`() {
        val lines = LrcParser.parse("[00:12.00][00:45.00]Chorus")
        assertEquals(
            listOf(
                LyricLine(timeMs = 12_000L, text = "Chorus"),
                LyricLine(timeMs = 45_000L, text = "Chorus"),
            ),
            lines,
        )
    }

    @Test
    fun `sorts out of order lines by timestamp`() {
        val lines = LrcParser.parse("[00:30.00]Late\n[00:10.00]Early")
        assertEquals(listOf("Early", "Late"), lines.map { it.text })
    }

    @Test
    fun `ignores metadata tags and untimestamped lines`() {
        val lines = LrcParser.parse(
            """
            [ti:Some Song]
            [ar:Some Artist]
            [00:01.00]First line
            This line has no timestamp
            """.trimIndent(),
        )
        assertEquals(listOf(LyricLine(timeMs = 1_000L, text = "First line")), lines)
    }

    @Test
    fun `returns empty list for empty or tag only content`() {
        assertTrue(LrcParser.parse("").isEmpty())
        assertTrue(LrcParser.parse("[ar:Artist]\n[al:Album]").isEmpty())
    }

    @Test
    fun `trims whitespace around lyrics text`() {
        val lines = LrcParser.parse("[00:01.00]   padded   ")
        assertEquals("padded", lines.single().text)
    }
}
