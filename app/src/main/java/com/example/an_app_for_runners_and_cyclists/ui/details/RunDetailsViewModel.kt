package com.example.an_app_for_runners_and_cyclists.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.an_app_for_runners_and_cyclists.data.model.Run
import com.example.an_app_for_runners_and_cyclists.data.repository.RunRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RunDetailsViewModel(
    private val runRepository: RunRepository
) : ViewModel() {

    private val _run = MutableStateFlow<Run?>(null)
    val run: StateFlow<Run?> = _run.asStateFlow()

    fun loadRun(runId: String) {
        viewModelScope.launch {
            runRepository.getRun(runId).collect { run ->
                _run.value = run
            }
        }
    }
}