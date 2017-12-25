package com.github.saem.superservicetime.example

import com.github.saem.superservicetime.commandline.Command
import com.github.saem.superservicetime.commandline.Console
import java.io.Writer

fun main(args: Array<String>) {
    Console.create(
            "example",
            listOf(HelloCommand()).associateBy { c -> c.name })
            .run(args)
}

private class HelloCommand : Command("hello") {
    override fun run(standardWriter: Writer, errorWriter: Writer): Int {
        standardWriter.write("Oh Hai!")
        return 0
    }
}