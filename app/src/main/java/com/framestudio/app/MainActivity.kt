package com.framestudio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.framestudio.app.navigation.AppNavGraph
import com.framestudio.app.ui.theme.FrameStudioTheme
import com.framestudio.app.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val factory = AppViewModelFactory(application)
        setContent {
            FrameStudioTheme {
                AppNavGraph(factory = factory)
            }
        }
    }
}
