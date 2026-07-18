package com.framestudio.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.framestudio.app.data.ActionEntity
import com.framestudio.app.data.Repository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActionViewModel(private val repository: Repository) : ViewModel() {

    val actions: StateFlow<List<ActionEntity>> = repository.actions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getAction(id: Long): ActionEntity? = repository.getAction(id)

    fun saveAction(action: ActionEntity, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = if (action.id == 0L) {
                repository.addAction(action)
            } else {
                repository.updateAction(action)
                action.id
            }
            onSaved(id)
        }
    }

    fun deleteAction(action: ActionEntity) {
        viewModelScope.launch { repository.deleteAction(action) }
    }
}
