package model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Task model for Week 8.
 * Uses UUID strings (not Int IDs) for production architecture.
 */
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val priority: String? = null,    // Added: Priority field
    val completed: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        const val MIN_TITLE_LENGTH = 1
        const val MAX_TITLE_LENGTH = 100

        // Validation logic
        fun validate(title: String): ValidationResult = when {
            title.isBlank() -> ValidationResult.Error("Title is required.")
            title.length < MIN_TITLE_LENGTH -> ValidationResult.Error("Title too short.")
            title.length > MAX_TITLE_LENGTH -> ValidationResult.Error("Title too long.")
            else -> ValidationResult.Success
        }
    }

    // Convert to map for Pebble templates
    fun toPebbleContext(): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "priority" to priority,
        "completed" to completed,
        "createdAt" to createdAt.format(DateTimeFormatter.ISO_DATE)
    )
}

/**
 * Validation result for form processing.
 */
sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
