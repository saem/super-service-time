package com.github.saem.superservicetime.daemon

import com.github.saem.superservicetime.commandline.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*


class DaemonTest {
    @Test
    fun runDaemonThatFailsToStart() {
        val processHistory = ArrayList<ProcessAction>()
        val outWriter = OutWriter(processHistory)
        val exitProcessFunction = PretendExitProcessWithHistory(processHistory)

        val consoleApp = Console(
                "test",
                listOf(FailOnStartDaemonTestCommand()).associateBy { c -> c.name },
                outWriter,
                outWriter,
                exitProcessFunction,
                { null })

        val exitCode = runConsole(consoleApp, arrayOf("test"))

        Assertions.assertEquals(-1, exitCode)

        Assertions.assertEquals(
                "\nProcess exited, with exit code: -1",
                writtenHistoryToString(processHistory))
    }

    @Test
    fun runDaemonAndThenSignalItToExit() {
        val processHistory = ArrayList<ProcessAction>()
        val outWriter = OutWriter(processHistory)
        val exitProcessFunction = PretendExitProcessWithHistory(processHistory)

        val consoleApp = Console(
                "test",
                listOf(FailOnStartDaemonTestCommand()).associateBy { c -> c.name },
                outWriter,
                outWriter,
                exitProcessFunction,
                { null })

        val exitCode = runConsole(consoleApp, arrayOf("test"))

        Assertions.assertEquals(-1, exitCode)

        Assertions.assertEquals(
                "\nProcess exited, with exit code: -1",
                writtenHistoryToString(processHistory))
    }
}

private class FailOnStartDaemonTestCommand : DaemonCommand("test", FailOnStartTestDaemon())

private class FailOnStartTestDaemon : Daemon {
    override fun doTheWork(): Int {
        // something bad happened, and it failed to start

        return -1
    }
}

private class DaemonTestCommand: DaemonCommand("test", WorkingDaemon())

private class WorkingDaemon: Daemon {
    override fun doTheWork(): Int {

    }
}