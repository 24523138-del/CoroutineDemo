package com.example.coroutinedemo.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException

class TaskRepository {

    suspend fun getTasks(shouldFail: Boolean = false): List<Task> =
        withContext(Dispatchers.IO) {
            delay(2000L)
            if (shouldFail) {
                throw IOException("Server is not reachable")
            }
            listOf(
                Task(1, "Write lab report", "Mobile App Development"),
                Task(2, "Read chapter 5", "Data Science"),
                Task(3, "Prepare quiz", "Research Methodology")
            )
        }

    suspend fun getAnnouncement(): String =
        withContext(Dispatchers.IO) {
            delay(1000L)
            "Quiz 2 is next Monday"
        }

    // Latihan 1: permintaan ketiga (1,5 detik)
    suspend fun getCourseCount(): Int =
        withContext(Dispatchers.IO) {
            delay(1500L)
            5
        }

    // Latihan 6 (Tantangan): pengurutan dijalankan di Dispatchers.Default
    suspend fun sortByTitle(tasks: List<Task>, ascending: Boolean): List<Task> =
        withContext(Dispatchers.Default) {
            if (ascending) {
                tasks.sortedBy { it.title.lowercase() }
            } else {
                tasks.sortedByDescending { it.title.lowercase() }
            }
        }
}