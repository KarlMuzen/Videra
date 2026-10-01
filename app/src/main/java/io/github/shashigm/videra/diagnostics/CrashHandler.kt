package io.github.shashigm.videra.diagnostics

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import java.io.PrintWriter
import java.io.StringWriter

class CrashHandler private constructor(
    context: Context,
    private val delegate: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    private val applicationContext = context.applicationContext

    private val sharedPreferences: SharedPreferences =
        applicationContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    override fun uncaughtException(
        thread: Thread,
        throwable: Throwable
    ) {
        val writer = StringWriter()
        throwable.printStackTrace(PrintWriter(writer))
        val stackTrace = writer.toString()

        try {
            applicationContext
                .getSystemService(Context.CLIPBOARD_SERVICE)
                ?.let { service ->
                    val clipboard = service as? ClipboardManager
                        ?: return@let
                    clipboard.setPrimaryClip(
                        ClipData.newPlainText(
                            "Videra crash trace",
                            stackTrace
                        )
                    )
                }
        } catch (_: Throwable) {
            // Clipboard capture must never suppress process termination.
        }

        try {
            val log = buildString {
                append("Timestamp: ")
                append(System.currentTimeMillis())
                append('\n')
                append("Thread: ")
                append(thread.name)
                append('\n')
                append("Exception: ")
                append(throwable::class.java.name)
                append(": ")
                append(throwable.message.orEmpty())
                append('\n')
                append('\n')
                append(stackTrace)
            }

            // commit() is intentional because the process may terminate
            // immediately after this uncaught-exception handler returns.
            sharedPreferences.edit()
                .putString(KEY_LAST_CRASH_LOG, log)
                .commit()
        } catch (_: Throwable) {
            // Crash logging must never prevent process termination.
        }

        try {
            delegate?.uncaughtException(thread, throwable)
        } finally {
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    companion object {
        private const val PREFS_NAME = "videra_preferences"
        private const val KEY_LAST_CRASH_LOG = "last_crash_log"

        fun install(context: Context) {
            val current = Thread.getDefaultUncaughtExceptionHandler()
            if (current is CrashHandler) {
                return
            }

            Thread.setDefaultUncaughtExceptionHandler(
                CrashHandler(
                    context = context.applicationContext,
                    delegate = current
                )
            )
        }

        fun getLastCrashLog(context: Context): String? {
            return context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LAST_CRASH_LOG, null)
        }

        fun clearLastCrashLog(context: Context) {
            context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_LAST_CRASH_LOG)
                .apply()
        }
    }
}
