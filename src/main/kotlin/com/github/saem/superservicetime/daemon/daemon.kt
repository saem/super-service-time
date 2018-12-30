package com.github.saem.superservicetime.daemon

import com.github.saem.superservicetime.commandline.Console
import com.github.saem.superservicetime.commandline.ExitCommand
import com.github.saem.superservicetime.commandline.SubCommand

interface Daemon {
    fun doTheWork()
    fun uponFinish()
}

abstract class DaemonCommand(private val daemon: Daemon) : SubCommand("daemon") {
    override fun run() {
        try {
            daemon.doTheWork()
        } catch (exit: DaemonExitException) {
            try {
                daemon.uponFinish()
            } catch (e: Exception) {
                // @todo do some logging here
                throw DaemonExitException(Console.GENERAL_ERROR)
            }
        }
    }
}

class DaemonExitException(exitCode: Int) : ExitCommand(exitCode)