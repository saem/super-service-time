package com.github.saem.superservicetime.commandline

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.DefaultHelpFormatter
import com.xenomachina.argparser.ShowHelpException
import com.xenomachina.argparser.SystemExitException
import sun.misc.Signal
import java.io.OutputStreamWriter
import java.io.Writer
import kotlin.system.exitProcess

class Console (
        private val appName: String,
        private val commands: Map<String, Command>,
        private val standardWriter: Writer,
        private val errorWriter: Writer,
        private val exitProcess: (Int) -> Nothing,
        private val sigIntHandler: (Signal) -> Int?
) {

    companion object {
        const val INVALID_COMMAND = -1
        const val EARLY_EXIT = 1

        fun create(appName: String,
                   commands: Map<String, Command>,
                   sigIntHandler: (Signal) -> Int? = { null }): Console =
                Console(
                        appName,
                        commands,
                        OutputStreamWriter(System.out),
                        OutputStreamWriter(System.err),
                        ::exitProcess,
                        sigIntHandler)
    }

    fun run(args: Array<String>) {
        val parser = ArgParser(
                args,
                ArgParser.Mode.POSIX,
                DefaultHelpFormatter("Command line interface."))

        val command by parser.positional(
                "COMMAND",
                "The command to run (eg. daemon)") {
            toCommand(this)
        }

        setupSignalHandling()

        try {
            parser.run {
                exitProcess(command.run(standardWriter, errorWriter))
            }
        } catch (h: ShowHelpException) {
            printAndExit(h)
        } catch (e: SystemExitException) {
            printAndExit(e)
        }
    }

    // @todo improve the experience by outputting more helpful error messages
    // @todo improve the experience by outputting 'did you mean?'
    private fun toCommand(commandString: String) =
            commands[commandString.toLowerCase()] ?:
                    throw InvalidCommandException(commandString)

    private fun setupSignalHandling() {
        Signal.handle(Signal("INT"), {
            exitProcess(sigIntHandler(it) ?: EARLY_EXIT)
        })
    }

    private fun printAndExit(e: SystemExitException): Nothing {
        val writer = if (e.returnCode == 0) standardWriter else errorWriter
        e.printUserMessage(writer, appName, 0)
        writer.flush()
        exitProcess(e.returnCode)
    }
}

private class InvalidCommandException(invalidCommandString: String):
        SystemExitException(
                when(invalidCommandString) {
                    "" -> "No command provided, try using --help"
                    else -> "Invalid command $invalidCommandString, try using --help"
                },
                Console.INVALID_COMMAND)

abstract class Command(val name: String) {
    override fun toString(): String = name

    abstract fun run(standardWriter: Writer, errorWriter: Writer): Int
}