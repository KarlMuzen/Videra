package io.github.shashigm.videra.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
) {
    data object Discover : Screen(
        route = "discover",
        label = "Discover",
        filledIcon = Icons.Filled.Home,
        outlinedIcon = Icons.Outlined.Home
    )

    data object Feed : Screen(
        route = "feed",
        label = "Feed",
        filledIcon = Icons.Filled.SmartDisplay,
        outlinedIcon = Icons.Outlined.SmartDisplay
    )

    data object Search : Screen(
        route = "search",
        label = "Search",
        filledIcon = Icons.Filled.Search,
        outlinedIcon = Icons.Outlined.Search
    )

    data object Library : Screen(
        route = "library",
        label = "Library",
        filledIcon = Icons.Filled.VideoLibrary,
        outlinedIcon = Icons.Outlined.VideoLibrary
    )

    data object Settings : Screen(
        route = "settings",
        label = "Settings",
        filledIcon = Icons.Filled.Settings,
        outlinedIcon = Icons.Outlined.Settings
    )

    data object AddonManager : Screen(
        route = "settings/addons",
        label = "Add-on Manager",
        filledIcon = Icons.Filled.Settings,
        outlinedIcon = Icons.Outlined.Settings
    )

    data object Player : Screen(
        route = "player",
        label = "Player",
        filledIcon = Icons.Filled.SmartDisplay,
        outlinedIcon = Icons.Outlined.SmartDisplay
    )

    data object Onboarding : Screen(
        route = "onboarding",
        label = "Onboarding",
        filledIcon = Icons.Filled.Home,
        outlinedIcon = Icons.Outlined.Home
    )

    companion object {
        val topLevelDestinations: List<Screen> = listOf(
            Discover,
            Feed,
            Search,
            Library,
            Settings
        )
    }
}
