package io.github.shashigm.videra.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.shashigm.videra.domain.model.LibraryItem
import io.github.shashigm.videra.domain.model.SearchResult
import io.github.shashigm.videra.domain.repository.AddonRepository
import io.github.shashigm.videra.domain.repository.LibraryRepository
import io.github.shashigm.videra.media.player.PlayerController
import io.github.shashigm.videra.ui.home.HomeScreen
import io.github.shashigm.videra.ui.home.HomeViewModel
import io.github.shashigm.videra.ui.home.HomeViewModelFactory
import io.github.shashigm.videra.ui.library.LibraryScreen
import io.github.shashigm.videra.ui.library.LibraryViewModel
import io.github.shashigm.videra.ui.library.LibraryViewModelFactory
import io.github.shashigm.videra.ui.player.MiniPlayer
import io.github.shashigm.videra.ui.player.PlayerScreen
import io.github.shashigm.videra.ui.player.PlayerViewModel
import io.github.shashigm.videra.ui.player.PlayerViewModelFactory
import io.github.shashigm.videra.ui.search.SearchScreen
import io.github.shashigm.videra.ui.search.SearchViewModel
import io.github.shashigm.videra.ui.search.SearchViewModelFactory
import io.github.shashigm.videra.ui.settings.AddonManagerScreen
import io.github.shashigm.videra.ui.settings.AddonManagerViewModel
import io.github.shashigm.videra.ui.settings.AddonManagerViewModelFactory
import io.github.shashigm.videra.ui.settings.SettingsScreen
import io.github.shashigm.videra.ui.shorts.ShortsScreen
import io.github.shashigm.videra.ui.shorts.ShortsViewModel
import io.github.shashigm.videra.ui.shorts.ShortsViewModelFactory

@Composable
fun MainScaffold(
    addonRepository: AddonRepository,
    libraryRepository: LibraryRepository,
    playerController: PlayerController,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, playerController) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    playerController.pause()
                }

                Lifecycle.Event.ON_DESTROY -> {
                    playerController.release()
                }

                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val addonManagerViewModel: AddonManagerViewModel = viewModel(
        factory = AddonManagerViewModelFactory(addonRepository)
    )
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(addonRepository)
    )
    val shortsViewModel: ShortsViewModel = viewModel(
        factory = ShortsViewModelFactory(addonRepository)
    )
    val searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModelFactory(addonRepository)
    )
    val libraryViewModel: LibraryViewModel = viewModel(
        factory = LibraryViewModelFactory(libraryRepository)
    )
    val playerViewModel: PlayerViewModel = viewModel(
        factory = PlayerViewModelFactory(playerController)
    )

    val isPlayerRoute = currentDestination?.route == Screen.Player.route
    val isImmersiveRoute = isPlayerRoute ||
        currentDestination?.route == Screen.Shorts.route

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (!isPlayerRoute) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MiniPlayer(
                        viewModel = playerViewModel
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
                                            popUpTo(
                                                navController.graph.startDestinationId
                                            ) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
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
        }
    ) { innerPadding ->
        val navContentModifier = if (isImmersiveRoute) {
            Modifier.fillMaxSize()
        } else {
            Modifier.padding(innerPadding)
        }

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = navContentModifier
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onItemClick = { item ->
                        playerViewModel.play(item)
                        navController.navigate(Screen.Player.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Shorts.route) {
                ShortsScreen(
                    viewModel = shortsViewModel,
                    playerViewModel = playerViewModel
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel = searchViewModel,
                    onItemClick = { result: SearchResult ->
                        playerViewModel.play(result.mediaItem)
                        navController.navigate(Screen.Player.route) {
                            launchSingleTop = true
                        }
                    },
                    onSaveItem = { result ->
                        libraryViewModel.save(
                            addon = result.addon,
                            mediaItem = result.mediaItem
                        )
                    }
                )
            }

            composable(Screen.Library.route) {
                LibraryScreen(
                    viewModel = libraryViewModel,
                    onItemClick = { item: LibraryItem ->
                        playerViewModel.play(item.mediaItem)
                        navController.navigate(Screen.Player.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onManageAddons = {
                        navController.navigate(Screen.AddonManager.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.AddonManager.route) {
                AddonManagerScreen(
                    viewModel = addonManagerViewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Player.route) {
                PlayerScreen(
                    viewModel = playerViewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
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