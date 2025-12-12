package routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.loader.ClasspathLoader
import model.Task
import storage.TaskStore
import utils.Page
import utils.Logger
import utils.RequestIdKey
import utils.HandledValidationException
import utils.timed
import utils.jsMode
import utils.newReqId
import java.io.StringWriter

fun Routing.configureTaskRoutes(store: TaskStore) {
    // 1. Setup Pebble Engine locally
    val pebble =
        PebbleEngine
            .Builder()
            .loader(ClasspathLoader().apply { prefix = "templates/" })
            .build()

    // 2. Local helper: Render template to String
    fun renderTemplate(
        templateName: String,
        model: Map<String, Any?>,
    ): String {
        val writer = StringWriter()
        pebble.getTemplate(templateName).evaluate(writer, model)
        return writer.toString()
    }

    // 3. Helper: Check if request is from HTMX
    fun ApplicationCall.isHtmx(): Boolean = request.headers["HX-Request"]?.equals("true", ignoreCase = true) == true

    // GET /tasks/fragment - HTMX search & pagination
    get("/tasks/fragment") {
        val reqId = newReqId()
        call.attributes.put(RequestIdKey, reqId)
        val jsMode = call.jsMode()

        // Determine if this is a Filter task or just viewing
        val qParam = call.request.queryParameters["q"]?.trim().orEmpty()
        val taskCode = if (qParam.isNotEmpty()) "T1_filter" else "T0_view"

        call.timed(taskCode, jsMode) {
            val q =
                call.request.queryParameters["q"]
                    ?.trim()
                    .orEmpty()
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1

            val tasks = store.search(q).map { it.toPebbleContext() }
            val pageData = Page.paginate(tasks, currentPage = page, pageSize = 10)

            val list = renderTemplate("tasks/_list.peb", mapOf("page" to pageData, "q" to q))
            val pager = renderTemplate("tasks/_pager.peb", mapOf("page" to pageData, "q" to q))
            val status =
                """<div id="status" hx-swap-oob="true">Updated: showing ${pageData.items.size} of ${pageData.totalItems} tasks</div>"""

            call.respondText(text = list + pager + status, contentType = ContentType.Text.Html)
        }
    }

    // GET /tasks - Full Page Load
    get("/tasks") {
        val reqId = newReqId()
        call.attributes.put(RequestIdKey, reqId)
        val jsMode = call.jsMode()

        // Determine if this is a Filter task or just viewing
        val qParam = call.request.queryParameters["q"]?.trim().orEmpty()
        val taskCode = if (qParam.isNotEmpty()) "T1_filter" else "T0_view"

        call.timed(taskCode, jsMode) {
            val q =
                call.request.queryParameters["q"]
                    ?.trim()
                    .orEmpty()
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1

            // Capture error parameter from query string (for No-JS validation)
            val error = call.request.queryParameters["error"]

            val tasks = store.search(q).map { it.toPebbleContext() }
            val pageData = Page.paginate(tasks, currentPage = page, pageSize = 10)

            val html =
                renderTemplate(
                    "tasks/index.peb",
                    mapOf(
                        "page" to pageData,
                        "q" to q,
                        "title" to "Tasks",
                        "error" to error, // Pass error to template
                    ),
                )
            call.respondText(text = html, contentType = ContentType.Text.Html)
        }
    }

    // POST /tasks - Add new task
    // with Server-Side Validation
    post("/tasks") {
        val reqId = newReqId()
        call.attributes.put(RequestIdKey, reqId)
        val session = call.request.cookies["sid"] ?: "anon"
        val jsMode = call.jsMode()

        call.timed(taskCode = "T3_add", jsMode = jsMode) {
            val params = call.receiveParameters()
            val title = params["title"]?.trim().orEmpty()
            val priority = params["priority"]?.trim().takeIf { !it.isNullOrBlank() }

            // --- Validation Logic ---
            if (title.isBlank()) {
                Logger.validationError(session, reqId, "T3_add", "blank_title", jsMode)

                if (call.isHtmx()) {
                    val errorHtml = """
                        <small id="title-error" hx-swap-oob="true" style="color: #c62828; font-weight: bold; margin-top: 0.25rem; display: block;">
                            Title is required. Please enter a task name.
                        </small>
                    """.trimIndent()
                    call.response.header("HX-Reswap", "none")
                    call.respondText(errorHtml, ContentType.Text.Html, HttpStatusCode.OK)
                } else {
                    call.response.headers.append("Location", "/tasks?error=title")
                    call.respond(HttpStatusCode.SeeOther)
                }

                throw HandledValidationException()
            }

            // --- Success Logic ---
            val newTask = Task(title = title, priority = priority)
            store.add(newTask)

            if (call.isHtmx()) {
                val tasks = store.search("").map { it.toPebbleContext() }
                val pageData = Page.paginate(tasks, currentPage = 1, pageSize = 10)
                val list = renderTemplate("tasks/_list.peb", mapOf("page" to pageData, "q" to ""))
                val pager = renderTemplate("tasks/_pager.peb", mapOf("page" to pageData, "q" to ""))
                val status =
                    """<div id="status" hx-swap-oob="true" class="success">Task "${newTask.title}" added.</div>"""

                val clearErrorHtml = """<small id="title-error" hx-swap-oob="true" style="display: none;"></small>"""

                call.respondText(
                    text = list + pager + status + clearErrorHtml,
                    contentType = ContentType.Text.Html,
                    status = HttpStatusCode.Created
                )
            } else {
                call.response.headers.append("Location", "/tasks")
                call.respond(HttpStatusCode.SeeOther)
            }
        }
    }

    // POST /tasks/{id}/delete
    delete("/tasks/{id}") {
        val reqId = newReqId()
        call.attributes.put(RequestIdKey, reqId)
        val jsMode = call.jsMode()

        call.timed("T3_delete", jsMode) {
            val id = call.parameters["id"]
            val removed = id?.let { store.delete(it) } ?: false

            if (removed) {
                val status = """<div id="status" hx-swap-oob="true">Task deleted.</div>"""
                call.respondText(text = status, contentType = ContentType.Text.Html)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }

    // 2. No-JS Fallback (Standard HTTP POST)
    post("/tasks/{id}/delete") {
        val reqId = newReqId()
        call.attributes.put(RequestIdKey, reqId)
        val jsMode = call.jsMode()

        call.timed("T3_delete", jsMode) {
            val id = call.parameters["id"]
            val removed = id?.let { store.delete(it) } ?: false

            call.response.headers.append("Location", "/tasks")
            call.respond(HttpStatusCode.SeeOther)
        }
    }

    // GET /tasks/{id}/edit - Show edit form
    get("/tasks/{id}/edit") {
        val id = call.parameters["id"]
        val task = id?.let { store.getById(it) }

        if (task == null) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }

        val errorParam = call.request.queryParameters["error"]
        val errorMessage = if (errorParam == "blank") "Title is required." else null

        if (call.isHtmx()) {
            val html =
                renderTemplate(
                    "tasks/_edit.peb",
                    mapOf(
                        "task" to task.toPebbleContext(),
                        "error" to errorMessage,
                    ),
                )
            call.respondText(text = html, contentType = ContentType.Text.Html)
        } else {
            val q =
                call.request.queryParameters["q"]
                    ?.trim()
                    .orEmpty()
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1

            val tasks = store.search(q).map { it.toPebbleContext() }
            val pageData = Page.paginate(tasks, currentPage = page, pageSize = 5)

            val html =
                renderTemplate(
                    "tasks/index.peb",
                    mapOf(
                        "page" to pageData,
                        "q" to q,
                        "title" to "Tasks",
                        "editingId" to id,
                        "error" to errorMessage,
                    ),
                )
            call.respondText(text = html, contentType = ContentType.Text.Html)
        }
    }

    // POST /tasks/{id}/edit
    post("/tasks/{id}/edit") {
        val reqId = newReqId()
        call.attributes.put(RequestIdKey, reqId)
        val session = call.request.cookies["sid"] ?: "anon"
        val jsMode = call.jsMode()

        call.timed("T4_edit", jsMode) {
            val id = call.parameters["id"]
            val newTitle = call.receiveParameters()["title"]?.trim()

            if (id == null) {
                call.respond(HttpStatusCode.NotFound)
                return@timed
            }

            if (newTitle.isNullOrBlank()) {
                Logger.validationError(session, reqId, "T4_edit", "blank_title", jsMode)

                if (call.isHtmx()) {
                    val task = store.getById(id)
                    if (task != null) {
                        val html =
                            renderTemplate(
                                "tasks/_edit.peb",
                                mapOf(
                                    "task" to task.toPebbleContext(),
                                    "error" to "Title is required.",
                                ),
                            )
                        call.respondText(text = html, contentType = ContentType.Text.Html)
                    }
                } else {
                    call.respondRedirect("/tasks")
                }
                throw HandledValidationException()
            }

            val task = store.getById(id)
            if (task != null) {
                val updatedTask = task.copy(title = newTitle)
                store.update(updatedTask)

                if (call.isHtmx()) {
                    val html = renderTemplate("tasks/_item.peb", mapOf("task" to updatedTask.toPebbleContext()))
                    val status = """<div id="status" hx-swap-oob="true">Task updated.</div>"""
                    call.respondText(text = html + status, contentType = ContentType.Text.Html)
                } else {
                    call.respondRedirect("/tasks")
                }
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }

    // GET /tasks/{id}/view
    get("/tasks/{id}/view") {
        val id = call.parameters["id"]
        val task = id?.let { store.getById(it) }

        if (task != null) {
            val html = renderTemplate("tasks/_item.peb", mapOf("task" to task.toPebbleContext()))
            call.respondText(text = html, contentType = ContentType.Text.Html)
        } else {
            call.respondRedirect("/tasks")
        }
    }

    // GET /tasks/{id}/delete/confirm - Show confirmation page (No-JS)
    get("/tasks/{id}/delete/confirm") {
        val id = call.parameters["id"]
        val task = id?.let { store.getById(it) }

        if (task == null) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }

        val html = renderTemplate("tasks/delete_confirm.peb", mapOf("task" to task))
        call.respondText(text = html, contentType = ContentType.Text.Html)
    }
}
