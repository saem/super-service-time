package com.github.saem.superservicetime.example

import com.github.saem.superservicetime.commandline.Command
import com.github.saem.superservicetime.commandline.Console
import java.io.Writer

fun main(args: Array<String>) {
    Console.create(
            "example",
            listOf(DaemonCommand()).associateBy { c -> c.name })
            .run(args)
}

private class DaemonCommand : Command("daemon") {
    override fun run(standardWriter: Writer, errorWriter: Writer): Int {
        standardWriter.write("Let's pretend a daemon is running!")
        return 0
    }
}