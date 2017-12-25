package com.github.saem.superservicetime.commandline

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.Writer
import java.util.*

class ConsoleTest {
    @Test
    fun noArgs() {
        val processHistory = ArrayList<ProcessAction>()
        val outWriter = OutWriter(processHistory)
        val exitProcessFunction = PretendExitProcessWithHistory(processHistory)

        val consoleApp = Console(
                "test",
                listOf(TestCommand()).associateBy { c -> c.name },
                outWriter,
                outWriter,
                exitProcessFunction) { null }

        val exitCode = runConsole(consoleApp, arrayOf(""))

        assertEquals(Console.INVALID_COMMAND, exitCode)
        assertEquals(
                "test: No command provided, try using --help\n\nProcess exited, with exit code: -1",
                writtenHistoryToString(processHistory))
    }

    @Test
    fun invalidCommand() {
        val processHistory = ArrayList<ProcessAction>()
        val outWriter = OutWriter(processHistory)
        val exitProcessFunction = PretendExitProcessWithHistory(processHistory)

        val consoleApp = Console(
                "test",
                listOf(TestCommand()).associateBy { c -> c.name },
                outWriter,
                outWriter,
                exitProcessFunction) { null }

        val exitCode = runConsole(consoleApp, arrayOf("NOTACOMMAND"))

        assertEquals(Console.INVALID_COMMAND, exitCode)
        assertEquals(
                "test: Invalid command NOTACOMMAND, try using --help\n\nProcess exited, with exit code: -1",
                writtenHistoryToString(processHistory)
        )
    }

    @Test
    fun askForHelp() {
        val processHistory = ArrayList<ProcessAction>()
        val outWriter = OutWriter(processHistory)
        val exitProcessFunction = PretendExitProcessWithHistory(processHistory)

        val consoleApp = Console(
                "test",
                listOf(TestCommand()).associateBy { c -> c.name },
                outWriter,
                outWriter,
                exitProcessFunction) { null }

        val exitCode = runConsole(consoleApp, arrayOf("--help"))

        assertEquals(0, exitCode)

        val historyString = writtenHistoryToString(processHistory)

        assertTrue(
                historyString.startsWith("usage: test"),
                "Help message didn't start with 'usage: test', instead was: " + historyString)
    }

    @Test
    fun askForHelpUsingTheShortForm() {
        val processHistory = ArrayList<ProcessAction>()
        val outWriter = OutWriter(processHistory)
        val exitProcessFunction = PretendExitProcessWithHistory(processHistory)

        val consoleApp = Console(
                "test",
                listOf(TestCommand()).associateBy { c -> c.name },
                outWriter,
                outWriter,
                exitProcessFunction) { null }

        val exitCode = runConsole(consoleApp, arrayOf("-h"))

        assertEquals(0, exitCode)

        val historyString = writtenHistoryToString(processHistory)

        assertTrue(
                historyString.startsWith("usage: test"),
                "Help message didn't start with 'usage: test', instead was: " + historyString)
    }

    @Test
    fun runTheTestCommand() {
        val processHistory = ArrayList<ProcessAction>()
        val outWriter = OutWriter(processHistory)
        val exitProcessFunction = PretendExitProcessWithHistory(processHistory)

        val consoleApp = Console(
                "test",
                listOf(TestCommand()).associateBy { c -> c.name },
                outWriter,
                outWriter,
                exitProcessFunction) { null }

        val exitCode = runConsole(consoleApp, arrayOf("test"))

        assertEquals(0, exitCode)

        assertEquals(
                "\nProcess exited, with exit code: 0",
                writtenHistoryToString(processHistory))
    }
}

/**
 * All this code below is to help the testing, things like:
 * - get around the real exitProcess being called, and killing the test
 * - capture a history (effects) of a command, and make sure it's valid
 * - fixture data like test commands
 */

private fun runConsole(consoleApp: Console, args: Array<String>): Int {
    try {
        consoleApp.run(args)
    } catch (e: ExitException) {
        return e.exitCode
    }

    // this shouldn't run because consoleApp should always throw an exception due to the exitProcess fn we provide
    return -99
}

private class TestCommand: Command("test") {
    override fun run(standardWriter: Writer, errorWriter: Writer): Int {
        return 0
    }
}

private class OutWriter(val writerHistory: MutableList<ProcessAction>) : Writer() {
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

private fun PretendExitProcessWithHistory(processHistory: MutableList<ProcessAction>): (Int) -> Nothing =
        fun (exitCode: Int): Nothing {
            processHistory.add(ExitProcess(exitCode))
            throw ExitException(exitCode)
        }

private data class ExitException(val exitCode: Int): RuntimeException("Pretend exit from the JVM")

private fun historyIsValid(history: List<ProcessAction>): Boolean {

    // @todo update to deal with separate output streams

    // No writes or flushes after a close is called
    val writerCloseValidity = !history.contains(Close()) || history.contains(Close()) &&
            history.subList(history.indexOf(Close()), history.size).any {
                when (it) {
                    is Write -> false
                    is Flush -> false
                    else -> true
                }
    }

    val exitProcessCall: ExitProcess? = history.filterIsInstance(ExitProcess::class.java).firstOrNull()

    return writerCloseValidity &&
            exitProcessCall != null &&
            history.indexOf(exitProcessCall) == history.size - 1
}

private fun writtenHistoryToString(history: List<ProcessAction>): String {
    assertTrue(
            historyIsValid(history),
            "Invalid history, cannot convert history to String")

    return history.fold("", { s, m ->
        s + when (m) {
            is Write -> m.chars?.joinToString(separator = "", limit = m.finish, truncated = "") ?: ""
            is ExitProcess -> "\nProcess exited, with exit code: " + m.exitCode
            else -> ""
        }
    })
}

sealed class ProcessAction

data class ExitProcess(val exitCode: Int): ProcessAction()

sealed class WriterMessage: ProcessAction()
data class Flush(val dummy: Int = 0) : WriterMessage()
data class Close(val dummy: Int = 1) : WriterMessage()
data class Write(val chars: CharArray?, private val start: Int, val finish: Int) : WriterMessage() {

    // equals, and hashCode were generated by IntelliJ, because trouble with arrays in data classes -- type erasure :(

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
