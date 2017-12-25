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
                exitProcessFunction,
                { null })

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
                exitProcessFunction,
                { null })

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
                exitProcessFunction,
                { null })

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
                exitProcessFunction,
                { null })

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
                exitProcessFunction,
                { null })

        val exitCode = runConsole(consoleApp, arrayOf("test"))

        assertEquals(0, exitCode)

        assertEquals(
                "\nProcess exited, with exit code: 0",
                writtenHistoryToString(processHistory))
    }
}

private class TestCommand: Command("test") {
    override fun run(standardWriter: Writer, errorWriter: Writer): Int {
        return 0
    }
}
