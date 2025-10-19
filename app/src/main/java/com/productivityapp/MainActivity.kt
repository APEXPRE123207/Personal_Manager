package com.productivityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.compose.ui.platform.LocalContext
import com.productivityapp.ui.theme.PersonalManagerTheme
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val icon: ImageVector, val title: String) {
    object Dashboard : Screen("dashboard", Icons.Default.Home, "Dashboard")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Don't use enableEdgeToEdge to avoid empty spaces
        setContent {
            val context = LocalContext.current
            val themePreferences = remember { ThemePreferences(context) }
            val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)
            val scope = rememberCoroutineScope()
            
            PersonalManagerTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                Scaffold { innerPadding ->
                    NavHost(navController, startDestination = "splash", Modifier.padding(innerPadding)) {
                        composable("splash") { SplashScreen(navController) }
                        composable(Screen.Dashboard.route) { 
                            DashboardScreen(
                                navController = navController,
                                isDarkMode = isDarkMode,
                                onToggleTheme = {
                                    scope.launch {
                                        themePreferences.setDarkMode(!isDarkMode)
                                    }
                                }
                            )
                        }
                        composable("tasks") { TaskListScreen(navController) }
                        composable("transactions") { TransactionScreen(navController) }
                        composable("analytics") { AnalyticsScreen(navController) }
                        composable("notes") { NotesScreen(navController) }
                        composable(
                            route = "note_detail/{noteId}",
                            arguments = listOf(navArgument("noteId") { 
                                type = NavType.StringType
                                nullable = true
                            })
                        ) { backStackEntry ->
                            val noteViewModel: NoteViewModel = viewModel(
                                factory = NoteViewModelFactory(
                                    (LocalContext.current.applicationContext as PersonalManagerApplication).noteRepository
                                )
                            )
                            val noteId = backStackEntry.arguments?.getString("noteId")
                            NoteDetailScreen(
                                navController = navController,
                                noteViewModel = noteViewModel,
                                noteId = if (noteId == "null") null else noteId
                            )
                        }
                    }
                }
            }
        }
    }
}
