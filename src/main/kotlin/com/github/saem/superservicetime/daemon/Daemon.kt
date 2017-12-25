package com.github.saem.superservicetime.daemon

import com.github.saem.superservicetime.commandline.Command
import com.github.saem.superservicetime.commandline.Console
import java.io.Writer

interface Daemon {
    fun doTheWork(): Int
    fun gracefulExit(): Int = Console.EARLY_EXIT
}

abstract class DaemonCommand (name: String, val daemon: Daemon) : Command (name) {
    final override fun run(standardWriter: Writer, errorWriter: Writer): Int {
        return daemon.doTheWork()
    }

    final override fun gracefulExit(): Int {
        return daemon.gracefulExit()
    }
}

