package com.wakeup.alarm.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wakeup.alarm.WakeUpApplication
import com.wakeup.alarm.ui.home.HomeScreen
import com.wakeup.alarm.ui.home.HomeViewModel
import com.wakeup.alarm.ui.setalarm.SetAlarmScreen
import com.wakeup.alarm.ui.setalarm.SetAlarmViewModel
import com.wakeup.alarm.ui.theme.WakeUpTheme
import com.wakeup.alarm.util.PermissionHelper

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels {
        val app = application as WakeUpApplication
        HomeViewModel.Factory(app.repository, app.alarmScheduler)
    }

    private val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate()

        checkPermissions()

        setContent {
            WakeUpTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            viewModel = homeViewModel,
                            onAddAlarmClick = {
                                navController.navigate("setAlarm/-1")
                            },
                            onEditAlarmClick = { alarmId ->
                                navController.navigate("setAlarm/$alarmId")
                            }
                        )
                    }

                    composable(
                        route = "setAlarm/{alarmId}",
                        arguments = listOf(navArgument("alarmId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val alarmIdArg = backStackEntry.arguments?.getLong("alarmId") ?: -1L
                        val app = application as WakeUpApplication
                        val setAlarmViewModel: SetAlarmViewModel = viewModels<SetAlarmViewModel> {
                            SetAlarmViewModel.Factory(app.repository, app.alarmScheduler, alarmIdArg)
                        }.value

                        SetAlarmScreen(
                            viewModel = setAlarmViewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionHelper.hasNotificationPermission(this)) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (!PermissionHelper.canScheduleExactAlarms(this)) {
            PermissionHelper.requestExactAlarmPermissionIntent(this)?.let { intent ->
                startActivity(intent)
            }
        }
    }
}
