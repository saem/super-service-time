package com.github.saem.superservicetime.daemon

import com.github.saem.superservicetime.commandline.Command
import java.io.Writer

interface Daemon {
    fun doTheWork()
    fun uponFinish()
}

abstract class DaemonCommand (val daemon: Daemon) : Command ("daemon") {
    final override fun run(standardWriter: Writer, errorWriter: Writer): Int {
        try {
            daemon.doTheWork()
        } catch (exit: DaemonExitException) {
            try {
                daemon.uponFinish()
            } catch (e: Exception) {
                // @todo do some logging here
                return exit.returnCode
            }
        }

        return 0
    }
}

class DaemonExitException(val returnCode: Int): RuntimeException()