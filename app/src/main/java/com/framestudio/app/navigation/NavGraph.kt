package com.framestudio.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.framestudio.app.ui.screens.*
import com.framestudio.app.viewmodel.ActionViewModel
import com.framestudio.app.viewmodel.AppViewModelFactory
import com.framestudio.app.viewmodel.BatchViewModel
import com.framestudio.app.viewmodel.FrameViewModel

object Routes {
    const val HOME = "home"
    const val EDITOR = "editor"
    const val FRAMES = "frames"
    const val FRAME_DESIGNER = "frame_designer"
    const val ACTIONS = "actions"
    const val ACTION_EDITOR = "action_editor?actionId={actionId}"
    const val BATCH = "batch"
    const val RESULT = "result"

    fun actionEditor(actionId: Long? = null) =
        if (actionId == null) "action_editor" else "action_editor?actionId=$actionId"
}

@Composable
fun AppNavGraph(factory: AppViewModelFactory) {
    val navController = rememberNavController()

    val frameViewModel: FrameViewModel = viewModel(factory = factory)
    val actionViewModel: ActionViewModel = viewModel(factory = factory)
    val batchViewModel: BatchViewModel = viewModel(factory = factory)

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onOpenFrames = { navController.navigate(Routes.FRAMES) },
                onOpenActions = { navController.navigate(Routes.ACTIONS) },
                onOpenBatch = { navController.navigate(Routes.BATCH) }
                onOpenEditor = { navController.navigate(Routes.EDITOR) }
            )
        }

        composable(Routes.FRAMES) {
            FrameGalleryScreen(
                viewModel = frameViewModel,
                onBack = { navController.popBackStack() },
                onDesignNew = { navController.navigate(Routes.FRAME_DESIGNER) }
            )
        }

        composable(Routes.FRAME_DESIGNER) {
            FrameDesignerScreen(
                viewModel = frameViewModel,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(Routes.ACTIONS) {
            ActionsListScreen(
                viewModel = actionViewModel,
                onBack = { navController.popBackStack() },
                onCreateNew = { navController.navigate(Routes.actionEditor()) },
                onEdit = { id -> navController.navigate(Routes.actionEditor(id)) }
            )
        }

        composable(
            route = Routes.ACTION_EDITOR,
            arguments = listOf(navArgument("actionId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val actionIdStr = backStackEntry.arguments?.getString("actionId")
            ActionEditorScreen(
                actionId = actionIdStr?.toLongOrNull(),
                actionViewModel = actionViewModel,
                frameViewModel = frameViewModel,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(Routes.BATCH) {
            BatchProcessScreen(
                batchViewModel = batchViewModel,
                actionViewModel = actionViewModel,
                onBack = { navController.popBackStack() },
                onFinished = { navController.navigate(Routes.RESULT) },
                onNeedNewAction = { navController.navigate(Routes.actionEditor()) }
            )
        }

        composable(Routes.RESULT) {
            ResultScreen(
                batchViewModel = batchViewModel,
                onDone = { navController.popBackStack(Routes.HOME, inclusive = false) }
            )
        }
    }
}
