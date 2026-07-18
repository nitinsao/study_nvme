package com.nvmeacademy.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.nvmeacademy.app.ui.chapter.ChapterScreen
import com.nvmeacademy.app.ui.commanddetail.CommandDetailScreen
import com.nvmeacademy.app.ui.home.HomeScreen
import com.nvmeacademy.app.ui.search.SearchScreen

sealed class Destination(val route: String, val label: String) {
    data object Home : Destination("home", "Learn")
    data object Search : Destination("search", "Reference")
    data object Chapter : Destination("chapter/{chapterId}", "Chapter")
    data object CommandDetail : Destination("command/{commandId}", "Command")

    companion object {
        fun chapterRoute(chapterId: Int) = "chapter/$chapterId"
        fun commandRoute(commandId: Int) = "command/$commandId"
    }
}

private val bottomBarDestinations = listOf(Destination.Home, Destination.Search)

@Composable
fun NvmeAcademyNavHost() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination
            val showBottomBar = bottomBarDestinations.any { dest ->
                currentRoute?.hierarchy?.any { it.route == dest.route } == true
            }
            if (showBottomBar) {
                NavigationBar {
                    bottomBarDestinations.forEach { dest ->
                        val selected = currentRoute?.hierarchy?.any { it.route == dest.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (dest == Destination.Home) Icons.Filled.MenuBook else Icons.Filled.Search,
                                    contentDescription = dest.label
                                )
                            },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable(Destination.Home.route) {
                HomeScreen(onChapterClick = { chapterId ->
                    navController.navigate(Destination.chapterRoute(chapterId))
                })
            }
            composable(Destination.Search.route) {
                SearchScreen(onCommandClick = { commandId ->
                    navController.navigate(Destination.commandRoute(commandId))
                })
            }
            composable(
                route = Destination.Chapter.route,
                arguments = listOf(navArgument("chapterId") { type = NavType.IntType })
            ) { backStackEntry ->
                val chapterId = backStackEntry.arguments?.getInt("chapterId") ?: return@composable
                ChapterScreen(chapterId = chapterId, onBack = { navController.popBackStack() })
            }
            composable(
                route = Destination.CommandDetail.route,
                arguments = listOf(navArgument("commandId") { type = NavType.IntType })
            ) { backStackEntry ->
                val commandId = backStackEntry.arguments?.getInt("commandId") ?: return@composable
                CommandDetailScreen(commandId = commandId, onBack = { navController.popBackStack() })
            }
        }
    }
}

