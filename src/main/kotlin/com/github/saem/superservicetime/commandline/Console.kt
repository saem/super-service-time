package com.github.saem.superservicetime.commandline

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.DefaultHelpFormatter
import com.xenomachina.argparser.ShowHelpException
import com.xenomachina.argparser.SystemExitException
import java.io.OutputStreamWriter
import java.io.Writer

class Console (
        args: Array<String>,
        private val appName: String,
        private val standardWriter: Writer,
        private val errorWriter: Writer,
        private val commands: Map<String, Command>
    ) {

    companion object {
        const val INVALID_COMMAND = 1

        fun create(args: Array<String>,
                   appName: String,
                   commands: Map<String, Command>): Console =
                Console(
                        args,
                        appName,
                        OutputStreamWriter(System.out),
                        OutputStreamWriter(System.err),
                        commands)
    }

    private val parser = ArgParser(
            args,
            ArgParser.Mode.POSIX,
            DefaultHelpFormatter("Command line interface."))

    private val command by parser.positional(
            "COMMAND",
            "The command to run (eg. daemon)") {
        toCommand(this)
    }

    // @todo improve the experience by outputting more helpful error messages
    // @todo improve the experience by outputting did you mean?
    private fun toCommand(commandString: String) =
            commands[commandString.toLowerCase()] ?:
                    throw InvalidCommandException(commandString)

    fun run(): Int {
        try {
            parser.run {
                return command.run(standardWriter, errorWriter)
            }
        } catch (h: ShowHelpException) {
            return printAndGetExitCode(h)
        } catch (e: SystemExitException) {
            return printAndGetExitCode(e)
        }
    }

    private fun printAndGetExitCode(e: SystemExitException): Int {
        val writer = if (e.returnCode == 0) standardWriter else errorWriter
        e.printUserMessage(writer, appName, 0)
        writer.flush()
        return e.returnCode
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
