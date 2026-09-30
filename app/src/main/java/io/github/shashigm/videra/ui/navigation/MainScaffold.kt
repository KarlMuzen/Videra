package io.github.shashigm.videra.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.shashigm.videra.data.preferences.PreferencesRepository
import io.github.shashigm.videra.domain.model.LibraryItem
import io.github.shashigm.videra.domain.model.SearchResult
import io.github.shashigm.videra.domain.repository.AddonRepository
import io.github.shashigm.videra.domain.repository.EpisodeProgressRepository
import io.github.shashigm.videra.domain.repository.LibraryRepository
import io.github.shashigm.videra.domain.usecase.InstallAddonUseCase
import io.github.shashigm.videra.media.player.PlayerController
import io.github.shashigm.videra.ui.home.HomeScreen
import io.github.shashigm.videra.ui.home.HomeViewModel
import io.github.shashigm.videra.ui.home.HomeViewModelFactory
import io.github.shashigm.videra.ui.library.LibraryScreen
import io.github.shashigm.videra.ui.library.LibraryViewModel
import io.github.shashigm.videra.ui.library.LibraryViewModelFactory
import io.github.shashigm.videra.ui.onboarding.OnboardingScreen
import io.github.shashigm.videra.ui.onboarding.OnboardingViewModel
import io.github.shashigm.videra.ui.onboarding.OnboardingViewModelFactory
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
    installAddonUseCase: InstallAddonUseCase,
    episodeProgressRepository: EpisodeProgressRepository,
    libraryRepository: LibraryRepository,
    playerController: PlayerController,
    preferencesRepository: PreferencesRepository,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val startDestination = remember(preferencesRepository) {
        if (preferencesRepository.isOnboardingCompleted()) {
            Screen.Feed.route
        } else {
            Screen.Onboarding.route
        }
    }

    val appTheme by preferencesRepository
        .appTheme
        .collectAsStateWithLifecycle()

    DisposableEffect(lifecycleOwner, playerController) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> playerController.pauseAndClearVideoSurface()
                Lifecycle.Event.ON_DESTROY -> playerController.release()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val navController = rememberNavController()

    DisposableEffect(navController, playerController) {
        val listener = NavController.OnDestinationChangedListener {
            _,
            destination,
            _ ->
            val route = destination.route
            val isVerticalRoute =
                route == Screen.Player.route || route == Screen.Feed.route

            if (!isVerticalRoute) {
                playerController.pauseAndClearVideoSurface()
            }
        }

        navController.addOnDestinationChangedListener(listener)

        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val isFeedRoute = currentRoute == Screen.Feed.route
    val isPlayerRoute = currentRoute == Screen.Player.route
    val isOnboardingRoute = currentRoute == Screen.Onboarding.route

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
        factory = PlayerViewModelFactory(
            playerController = playerController,
            addonRepository = addonRepository,
            episodeProgressRepository = episodeProgressRepository
        )
    )
    val onboardingViewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModelFactory(
            preferencesRepository = preferencesRepository,
            installAddon = installAddonUseCase
        )
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    viewModel = onboardingViewModel,
                    onCompleted = {
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(Screen.Onboarding.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Discover.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onItemClick = { addon, mediaItem ->
                        playerViewModel.play(
                            addon = addon,
                            mediaItem = mediaItem
                        )
                        navController.navigate(Screen.Player.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Feed.route) {
                ShortsScreen(
                    viewModel = shortsViewModel,
                    playerViewModel = playerViewModel
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel = searchViewModel,
                    onItemClick = { result: SearchResult ->
                        playerViewModel.play(
                            addon = result.addon,
                            mediaItem = result.mediaItem
                        )
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
                        playerViewModel.playLibraryItem(item)
                        navController.navigate(Screen.Player.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    appTheme = appTheme,
                    onThemeChanged = preferencesRepository::setAppTheme,
                    onManageAddons = {
                        navController.navigate(Screen.AddonManager.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.AddonManager.route) {
                val addonManagerViewModel: AddonManagerViewModel = viewModel(
                    factory = AddonManagerViewModelFactory(addonRepository)
                )
                AddonManagerScreen(
                    viewModel = addonManagerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Player.route) {
                PlayerScreen(
                    viewModel = playerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        if (!isOnboardingRoute && !isPlayerRoute) {
            FloatingNavigationBar(
                currentDestination = currentDestination,
                feedSelected = isFeedRoute,
                onNavigate = { screen ->
                    if (screen.route != currentRoute) {
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Feed.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
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
