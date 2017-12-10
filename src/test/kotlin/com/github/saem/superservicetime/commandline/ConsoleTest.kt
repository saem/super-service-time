package com.github.saem.superservicetime.commandline

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.Writer
import java.util.*

class ConsoleTest {
    @Test
    fun noArgs() {
        val writerHistory = ArrayList<WriterMessage>()
        val outWriter = OutWriter(writerHistory)

        val consoleApp = Console(
                arrayOf(""),
                "test",
                outWriter,
                outWriter,
                listOf(TestCommand()).associateBy { c -> c.name })

        val exitCode = consoleApp.run()

        assertEquals(Console.INVALID_COMMAND, exitCode)
        assertEquals(
                "test: No command provided, try using --help\n",
                writtenHistoryToString(writerHistory))
    }

    @Test
    fun invalidCommand() {
        val writerHistory = ArrayList<WriterMessage>()
        val outWriter = OutWriter(writerHistory)

        val consoleApp = Console(
                arrayOf("NOTACOMMAND"),
                "test",
                outWriter,
                outWriter,
                listOf(TestCommand()).associateBy { c -> c.name })

        val exitCode = consoleApp.run()

        assertEquals(Console.INVALID_COMMAND, exitCode)
        assertEquals(
                "test: Invalid command NOTACOMMAND, try using --help\n",
                writtenHistoryToString(writerHistory)
        )
    }

    @Test
    fun askForHelp() {
        val writerHistory = ArrayList<WriterMessage>()
        val outWriter = OutWriter(writerHistory)

        val consoleApp = Console(
                arrayOf("--help"),
                "test",
                outWriter,
                outWriter,
                listOf(TestCommand()).associateBy { c -> c.name })

        val exitCode = consoleApp.run()

        assertEquals(0, exitCode)

        val historyString = writtenHistoryToString(writerHistory)

        assertTrue(
                historyString.startsWith("usage: test"),
                "Help message didn't start with 'usage: test', instead was: " + historyString)
    }

    @Test
    fun askForHelpUsingTheShortForm() {
        val writerHistory = ArrayList<WriterMessage>()
        val outWriter = OutWriter(writerHistory)

        val consoleApp = Console(
                arrayOf("-h"),
                "test",
                outWriter,
                outWriter,
                listOf(TestCommand()).associateBy { c -> c.name })

        val exitCode = consoleApp.run()

        assertEquals(0, exitCode)

        val historyString = writtenHistoryToString(writerHistory)

        assertTrue(
                historyString.startsWith("usage: test"),
                "Help message didn't start with 'usage: test', instead was: " + historyString)
    }
}

class OutWriter(val writerHistory: MutableList<WriterMessage>) : Writer() {
    override fun flush() {
        writerHistory.add(Flush())
    }

    override fun close() {
        writerHistory.add(Close())
    }

    override fun write(p0: CharArray?, p1: Int, p2: Int) {
        writerHistory.add(Write(p0, p1, p2))
    }
}

fun historyIsValid(history: List<WriterMessage>): Boolean {
    if (!history.contains(Close())) {
        return true
    }

    // No writes or flushes after a close is called
    return !history.subList(history.indexOf(Close()), history.size).any {
        when (it) {
            is Write -> true
            is Flush -> true
            else -> false
        }
    }
}

fun writtenHistoryToString(history: List<WriterMessage>): String {
    assertTrue(
            historyIsValid(history),
            "Invalid history, cannot convert history to String")

    return history.fold("", { s, m ->
        s + when (m) {
            is Write -> m.chars?.joinToString(separator = "", limit = m.finish, truncated = "") ?: ""
            else -> ""
        }
    })
}

sealed class WriterMessage
data class Write(val chars: CharArray?, val start: Int, val finish: Int) : WriterMessage() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Write

        if (!Arrays.equals(chars, other.chars)) return false
        if (start != other.start) return false
        if (finish != other.finish) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chars?.let { Arrays.hashCode(it) } ?: 0
        result = 31 * result + start
        result = 31 * result + finish
        return result
    }
}

data class Flush(val dummy: Int = 0) : WriterMessage()
data class Close(val dummy: Int = 1) : WriterMessage()

private class TestCommand: Command("test") {
    override fun run(standardWriter: Writer, errorWriter: Writer): Int {
        return 0;
    }
}