package com.example.fish_tasks_android.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String, // "top", "right", "bottom", "left"
    val priority: String, // "low", "medium", "high"
    val deadline: Long,   // timestamp in ms
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "active", // "active", "completed", "deleted"
    val workStatus: String = "pending", // "pending", "in_progress"
    val description: String = "",
    val expiredTriggered: Boolean = false,
    val completedAt: Long? = null,
    val deletedAt: Long? = null,
    val expiredAt: Long? = null,
    val scoreAwarded: Int? = null,
    val completedStateKey: String? = null,
    val completedLate: Boolean = false
)
