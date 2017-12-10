package com.github.saem.superservicetime.daemon

import com.github.saem.superservicetime.commandline.Command
import java.io.Writer

class Daemon () {
    abstract fun doTheWork() {}
    abstract fun uponFinish() {}
}

abstract class DaemonCommand (val daemon: Daemon) : Command () {
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
    }
}

class DaemonExitException(val returnCode: Int): RuntimeException() {

}