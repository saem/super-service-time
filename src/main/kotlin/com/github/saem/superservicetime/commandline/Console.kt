package com.github.saem.superservicetime.commandline

import com.github.ajalt.clikt.core.*
import java.lang.RuntimeException
import kotlin.system.exitProcess

open class Console(
        appName: String,
        vararg subCommands: SubCommand
) {

    companion object {
        const val SUCCESS = 0
        const val GENERAL_ERROR = 1 // used by Clikt as well
    }

    protected val command = BaseCommand(appName)
            .subcommands(*subCommands)

    fun run(argv: List<String>): Int =
            try {
                command.parse(argv)
                SUCCESS
            } catch (e: Exception) {
                exceptionHandler(e)
            }

    fun main(argv: Array<String>) {
        exitProcess(run(argv.toList()))
    }

    open fun exceptionHandler(e: Exception) = clickExceptionHandling(e)

    private fun clickExceptionHandling(
            e: Exception,
            errorCode: Int = GENERAL_ERROR
    ): Int {
        fun success(msg: String? = null) = msg?.let{command.writeOutput(msg)}.let { SUCCESS }
        fun error(msg: String?, code: Int = errorCode) = command.writeError(msg).let { code }

        return when (e) {
            is PrintHelpMessage -> success(e.command.getFormattedHelp())
            is PrintMessage -> success(e.message)
            is UsageError -> error(e.helpMessage(command.context))
            is ExitCommand -> if (e.error) error(e.message, e.code) else success(e.message)
            is CliktError -> error(e.message)
            is Abort -> if (e.error) error(e.message) else success(e.message)
            else -> SUCCESS
        }
    }
}

abstract class SubCommand(
        name: String,
        help: String = "",
        epilog: String = "",
        invokeWithoutSubCommand: Boolean = true
) : CliktCommand(
        help = help,
        epilog = epilog,
        name = name,
        invokeWithoutSubcommand = invokeWithoutSubCommand
) {
    protected fun exit(code: Int = Console.SUCCESS, message: String? = null) {
        throw ExitCommand(code, message)
    }
}

open class ExitCommand(
        val code: Int = Console.SUCCESS,
        message: String? = null) : RuntimeException(message) {
    val error: Boolean
        get() = code != 0
}

class BaseCommand(appName: String) : NoRunCliktCommand(name = appName) {
    fun writeOutput(message: Any?, trailingNewline: Boolean = true) {
        echo(message, trailingNewline)
    }

    fun writeError(message: Any?, trailingNewline: Boolean = true) {
        echo(message, trailingNewline, err = true)
    }
}