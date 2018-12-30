package com.github.saem.superservicetime.commandline

import com.github.ajalt.clikt.output.CliktConsole
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.util.*

class ConsoleTest {
    @Test
    fun noArgs() {
        val writerHistory = ArrayList<ConsoleHistory>()
        val console = TestConsole(writerHistory)

        val consoleApp = Console("app", console)

        val exitCode = consoleApp.run(listOf())

        assertEquals(Console.GENERAL_ERROR, exitCode)
        assertTrue(historyHasErrors(writerHistory))
        assertEquals("""
                    |Usage: app [OPTIONS]
                    |
                    |Options:
                    |  -h, --help  Show this message and exit
                    |
                    |Error: Must call a sub-command.
                    |
                """.trimMargin(),
                historyToString(writerHistory))
    }

    @Test
    fun invalidCommand() {
        val writerHistory = ArrayList<ConsoleHistory>()
        val console = TestConsole(writerHistory)

        val consoleApp = Console("app", console)

        val exitCode = consoleApp.run(listOf("NOTACOMMAND"))

        assertEquals(Console.GENERAL_ERROR, exitCode)
        assertTrue(historyHasErrors(writerHistory))
        assertEquals("""
                    |Usage: app [OPTIONS]
                    |
                    |Error: Got unexpected extra argument (NOTACOMMAND)
                    |
                    """.trimMargin(),
                historyToString(writerHistory)
        )
    }

    @Test
    fun askForHelp() {
        val writerHistory = ArrayList<ConsoleHistory>()
        val console = TestConsole(writerHistory)

        val consoleApp = Console("app", console)

        val exitCode = consoleApp.run(listOf("--help"))

        assertEquals(Console.SUCCESS, exitCode)
        assertTrue(historyHasNoErrors(writerHistory))
        assertEquals("""
                    |Usage: app [OPTIONS]
                    |
                    |Options:
                    |  -h, --help  Show this message and exit
                    |
                    """.trimMargin(),
                historyToString(writerHistory))
    }

    @Test
    fun askForHelpUsingTheShortForm() {
        val writerHistory = ArrayList<ConsoleHistory>()
        val console = TestConsole(writerHistory)

        val consoleApp = Console("app", console)

        val exitCode = consoleApp.run(listOf("-h"))

        assertEquals(Console.SUCCESS, exitCode)
        assertTrue(historyHasNoErrors(writerHistory))
        assertEquals("""
                    |Usage: app [OPTIONS]
                    |
                    |Options:
                    |  -h, --help  Show this message and exit
                    |
                    """.trimMargin(),
                historyToString(writerHistory))
    }
}

fun historyHasErrors(history: List<ConsoleHistory>): Boolean =
        history.any { it is PrintError }

fun historyHasNoErrors(history: List<ConsoleHistory>): Boolean =
        !historyHasErrors(history)

fun historyToString(history: List<ConsoleHistory>): String {
    return history.fold("") { s, m ->
        s + when (m) {
            is PrintMessage -> m.text
            is PrintError -> m.text
        }
    }
}

sealed class ConsoleHistory
data class PrintMessage(val text: String) : ConsoleHistory()
data class PrintError(val text: String) : ConsoleHistory()

class TestConsole(
        private val writerHistory: MutableList<ConsoleHistory>)
    : CliktConsole {
    override fun print(text: String, error: Boolean) {
        writerHistory.add(if (error) PrintError(text) else PrintMessage(text))
    }

    override fun promptForLine(prompt: String, hideInput: Boolean): String? {
        TODO("not implemented")
    }

    override val lineSeparator: String get() = System.lineSeparator()
}