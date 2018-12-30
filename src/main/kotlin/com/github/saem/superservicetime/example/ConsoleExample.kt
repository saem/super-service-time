package com.github.saem.superservicetime.example

import com.github.saem.superservicetime.commandline.Console
import com.github.saem.superservicetime.commandline.SubCommand

fun main(args: Array<String>) {
    Console("cmd", DaemonCommand()).main(args)
}

private class DaemonCommand : SubCommand(name = "server") {
    override fun run() {
        echo("Let's pretend a daemon is running!")
        exit()
    }
}