package io.github.shashigm.videra.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun MainScaffold(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Reserved global slot for the Now Playing / Mini-Player bar.
                // This lives outside NavHost so it survives top-level tab changes.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                )

                NavigationBar {
                    Screen.topLevelDestinations.forEach { screen ->
                        val selected = currentDestination
                            ?.hierarchy
                            ?.any { destination ->
                                destination.route == screen.route
                            } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                androidx.compose.material3.Icon(
                                    imageVector = if (selected) {
                                        screen.filledIcon
                                    } else {
                                        screen.outlinedIcon
                                    },
                                    contentDescription = screen.label
                                )
                            },
                            label = {
                                Text(text = screen.label)
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(0.dp)
        ) {
            composable(Screen.Home.route) {
                PlaceholderScreen(title = "Home")
            }

            composable(Screen.Shorts.route) {
                PlaceholderScreen(title = "Shorts")
            }

            composable(Screen.Search.route) {
                PlaceholderScreen(title = "Search")
            }

            composable(Screen.Library.route) {
                PlaceholderScreen(title = "Library")
            }

            composable(Screen.Settings.route) {
                PlaceholderScreen(title = "Settings")
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Videra",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
