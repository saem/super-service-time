package com.github.saem.superservicetime.example

import com.github.saem.superservicetime.commandline.Command
import com.github.saem.superservicetime.commandline.Console
import java.io.Writer
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    exitProcess(Console.create(
            args,
            "example",
            listOf(DaemonCommand()).associateBy { c -> c.name }).run())
}



private class DaemonCommand : Command("daemon") {
    override fun run(standardWriter: Writer, errorWriter: Writer): Int {
        standardWriter.write("Let's pretend a daemon is running!")
        return 0;
    }
}