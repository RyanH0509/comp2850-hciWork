package utils

import io.ktor.server.application.*
import io.ktor.util.*

val RequestStartTimeKey = AttributeKey<Long>("RequestStartTime")
val RequestIdKey = AttributeKey<String>("RequestId")

class HandledValidationException : Exception()

suspend fun ApplicationCall.timed(
    taskCode: String,
    jsMode: String,
    block: suspend ApplicationCall.() -> Unit
) {
    val start = System.currentTimeMillis()
    attributes.put(RequestStartTimeKey, start)

    val session = request.cookies["sid"] ?: "anon"
    val reqId = attributes.getOrNull(RequestIdKey) ?: newReqId()

    try {
        block()
        val duration = System.currentTimeMillis() - start
        Logger.success(session, reqId, taskCode, duration, jsMode)

    } catch (e: HandledValidationException) {
    } catch (e: Exception) {
        val duration = System.currentTimeMillis() - start
        Logger.write(LogEntry(session, reqId, taskCode, "server_error", e.message ?: "unknown", duration, 500, jsMode))
        throw e
    }
}

fun ApplicationCall.isHtmxRequest(): Boolean =
    request.headers["HX-Request"]?.equals("true", ignoreCase = true) == true

fun ApplicationCall.jsMode(): String =
    if (isHtmxRequest()) "on" else "off"

private var requestCounter = 0

@Synchronized
fun newReqId(): String = "r${String.format("%04d", ++requestCounter)}"
