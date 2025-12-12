package storage

import model.Task
import java.io.File

/**
 * Simple in-memory task storage with CSV persistence.
 * Week 8 uses String UUID IDs.
 */
class TaskStore(private val csvFile: File = File("data/tasks.csv")) {
    private val tasks = mutableListOf<Task>()

    init {
        if (csvFile.exists()) {
            csvFile.readLines().forEachIndexed { index, line ->
                // Skip header or empty lines
                if (index == 0 || line.isBlank()) return@forEachIndexed

                val parts = line.split(",")
                // We need at least id and title (size >= 2)
                if (parts.size >= 2) {
                    try {
                        val id = parts[0].trim()
                        val title = parts[1].trim()

                        // Handle optional priority (index 2)
                        val priority = if (parts.size > 2) {
                            parts[2].trim().takeIf { it.isNotBlank() && it != "null" }
                        } else null

                        // Handle optional completed (index 3)
                        val completed = if (parts.size > 3) {
                            parts[3].trim().toBoolean()
                        } else false

                        // FIX: Use named arguments to avoid type mismatch errors
                        tasks.add(
                            Task(
                                id = id,
                                title = title,
                                priority = priority,
                                completed = completed
                            )
                        )
                    } catch (e: Exception) {
                        println("Error parsing line $index: $line")
                    }
                }
            }
        } else {
            // Create file if not exists
            csvFile.parentFile?.mkdirs()
            if (!csvFile.exists()) {
                csvFile.createNewFile()
                save() // Write header
            }
        }
    }

    private fun save() {
        val header = "id,title,priority,completed\n"
        val content = tasks.joinToString("\n") { task ->
            // Handle null priority safely
            val p = task.priority ?: ""
            "${task.id},${task.title},$p,${task.completed}"
        }
        csvFile.writeText(header + content)
    }

    fun getAll(): List<Task> = tasks.toList()

    fun getById(id: String): Task? = tasks.find { it.id == id }

    fun add(task: Task) {
        tasks.add(task)
        save()
    }

    fun update(task: Task): Boolean {
        val index = tasks.indexOfFirst { it.id == task.id }
        return if (index != -1) {
            tasks[index] = task
            save()
            true
        } else false
    }

    fun delete(id: String): Boolean {
        val removed = tasks.removeIf { it.id == id }
        if (removed) {
            save()
        }
        return removed
    }

    fun search(query: String): List<Task> {
        if (query.isBlank()) return getAll()

        val normalizedQuery = query.trim().lowercase()
        return getAll().filter { task ->
            task.title.lowercase().contains(normalizedQuery)
        }
    }
}
