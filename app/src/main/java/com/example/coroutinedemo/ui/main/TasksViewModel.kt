package com.example.coroutinedemo.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutinedemo.data.TaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class TasksViewModel(
    private val repository: TaskRepository = TaskRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun loadData(shouldFail: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, loadDurationMs = 0L)
            }
            val startTime = System.currentTimeMillis()
            try {
                coroutineScope {
                    val tasks = async { repository.getTasks(shouldFail) }
                    val announcement = async { repository.getAnnouncement() }
                    val courseCount = async { repository.getCourseCount() }
                    val loadedTasks = tasks.await()
                    val loadedAnnouncement = announcement.await()
                    val loadedCourseCount = courseCount.await()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            tasks = loadedTasks,
                            announcement = loadedAnnouncement,
                            courseCount = loadedCourseCount,
                            loadDurationMs = System.currentTimeMillis() - startTime,
                            sortAscending = true
                        )
                    }
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load: ${e.message}"
                    )
                }
            }
        }
    }

    fun cancelLoading() {
        loadJob?.cancel()
        _uiState.update {
            it.copy(isLoading = false, errorMessage = "Loading cancelled")
        }
    }

    fun sortTasks() {
        viewModelScope.launch {
            val current = _uiState.value
            val sorted = repository.sortByTitle(current.tasks, current.sortAscending)
            _uiState.update {
                it.copy(tasks = sorted, sortAscending = !current.sortAscending)
            }
        }
    }

    fun onTap() {
        _uiState.update { it.copy(tapCount = it.tapCount + 1) }
    }

    fun blockMainThread() {
        Thread.sleep(3000L)
    }
}