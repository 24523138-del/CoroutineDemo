package com.example.coroutinedemo.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    TasksContent(
        state = state,
        onLoad = { viewModel.loadData() },
        onLoadWithError = { viewModel.loadData(shouldFail = true) },
        onCancel = viewModel::cancelLoading,
        onRetry = { viewModel.loadData() },
        onSort = viewModel::sortTasks,
        onTap = viewModel::onTap,
        onBlock = viewModel::blockMainThread,
        modifier = modifier
    )
}

@Composable
fun TasksContent(
    state: TasksUiState,
    onLoad: () -> Unit,
    onLoadWithError: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onSort: () -> Unit,
    onTap: () -> Unit,
    onBlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Coroutine Demo", style = MaterialTheme.typography.headlineSmall)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onLoad, enabled = !state.isLoading) {
                Text("Load")
            }
            OutlinedButton(
                onClick = onLoadWithError,
                enabled = !state.isLoading
            ) {
                Text("Load (fail)")
            }
            OutlinedButton(onClick = onCancel, enabled = state.isLoading) {
                Text("Cancel")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onTap) {
                Text("Tap me: ${state.tapCount}")
            }
            OutlinedButton(onClick = onBlock) {
                Text("Block UI")
            }
        }

        if (state.isLoading) {
            CircularProgressIndicator()
        }

        // Latihan 2: tombol Retry hanya muncul kalau errorMessage tidak null
        state.errorMessage?.let { message ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(onClick = onRetry, enabled = !state.isLoading) {
                    Text("Retry")
                }
            }
        }

        if (state.announcement.isNotEmpty()) {
            Text("Announcement: ${state.announcement}")
        }

        // Latihan 1: hasil permintaan ketiga + waktu total pemuatan
        if (state.courseCount > 0) {
            Text("Courses this semester: ${state.courseCount}")
        }
        if (state.loadDurationMs > 0L) {
            Text(
                text = "Loaded in ${state.loadDurationMs} ms",
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Latihan 6 (Tantangan): tombol Sort
        if (state.tasks.isNotEmpty()) {
            OutlinedButton(onClick = onSort, enabled = !state.isLoading) {
                Text(if (state.sortAscending) "Sort A-Z" else "Sort Z-A")
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.tasks, key = { it.id }) { task ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            task.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            task.course,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}