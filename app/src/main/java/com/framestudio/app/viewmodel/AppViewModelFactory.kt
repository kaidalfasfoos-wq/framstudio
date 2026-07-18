package com.framestudio.app.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.framestudio.app.data.AppDatabase
import com.framestudio.app.data.Repository

/** Factory بسيط لتمرير الـ Repository لكل ViewModel بدون Hilt/Koin لتبسيط المشروع */
class AppViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    private val repository: Repository by lazy {
        Repository(AppDatabase.getInstance(application))
    }

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when {
            modelClass.isAssignableFrom(FrameViewModel::class.java) ->
                FrameViewModel(application, repository) as T
            modelClass.isAssignableFrom(ActionViewModel::class.java) ->
                ActionViewModel(repository) as T
            modelClass.isAssignableFrom(BatchViewModel::class.java) ->
                BatchViewModel(application, repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
