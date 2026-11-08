package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AuroraBackground
import com.example.ui.theme.AuroraDarkBackground
import com.example.ui.theme.AuroraDarkCyan
import com.example.ui.theme.AuroraDarkElevated
import com.example.ui.theme.AuroraDarkPrimary
import com.example.ui.theme.AuroraDarkViolet
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.AppViewModel

data class NavItem(
    val destination: AppDestination,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun MainScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val currentDest by viewModel.currentDestination.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val strings = viewModel.strings

    // Natural Android Back Handler
    BackHandler(enabled = currentDest != AppDestination.HOME) {
        viewModel.navigateTo(AppDestination.HOME)
    }

    val navItems = listOf(
        NavItem(AppDestination.HOME, strings.home, Icons.Default.Home, "nav_home"),
        NavItem(AppDestination.MAPS, strings.maps, Icons.Default.Map, "nav_maps"),
        NavItem(AppDestination.WEATHER, strings.weather, Icons.Default.Cloud, "nav_weather"),
        NavItem(AppDestination.ASSISTANT, strings.assistant, Icons.Default.AutoAwesome, "nav_ai"),
        NavItem(AppDestination.PROFILE, strings.profile, Icons.Default.Person, "nav_profile"),
        NavItem(AppDestination.SETTINGS, strings.settings, Icons.Default.Settings, "nav_settings")
    )

    AuroraBackground(
        modifier = modifier.fillMaxSize(),
        isDark = isDark
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isTabletOrDesktop = maxWidth >= 650.dp

            if (isTabletOrDesktop) {
                // Adaptive Tablet / Desktop Layout with Left NavigationRail
                Row(modifier = Modifier.fillMaxSize()) {
                    NavigationRail(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(84.dp)
                            .statusBarsPadding()
                            .navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        navItems.forEach { item ->
                            val isSelected = currentDest == item.destination
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { viewModel.navigateTo(item.destination) },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = if (isSelected) AuroraDarkCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        color = if (isSelected) AuroraDarkCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    indicatorColor = AuroraDarkPrimary.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .statusBarsPadding()
                    ) {
                        ScreenContent(currentDest, viewModel)
                    }
                }
            } else {
                // Mobile Layout with Custom Aurora Obsidian Bottom Bar
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    bottomBar = {
                        AuroraBottomNavigationBar(
                            items = navItems,
                            currentDestination = currentDest,
                            onSelect = { viewModel.navigateTo(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        ScreenContent(currentDest, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun AuroraBottomNavigationBar(
    items: List<NavItem>,
    currentDestination: AppDestination,
    onSelect: (AppDestination) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .border(
                1.dp,
                Brush.linearGradient(listOf(AuroraDarkCyan.copy(alpha = 0.35f), AuroraDarkViolet.copy(alpha = 0.25f))),
                RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentDestination == item.destination

                val pillModifier = if (isSelected) {
                    Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(AuroraDarkPrimary.copy(alpha = 0.3f), AuroraDarkViolet.copy(alpha = 0.25f))
                            )
                        )
                } else {
                    Modifier.clip(RoundedCornerShape(16.dp))
                }

                Column(
                    modifier = Modifier
                        .then(pillModifier)
                        .clickable(onClick = { onSelect(item.destination) })
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag(item.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) AuroraDarkCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) AuroraDarkCyan else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ScreenContent(
    destination: AppDestination,
    viewModel: AppViewModel
) {
    Crossfade(
        targetState = destination,
        label = "screen_transition"
    ) { dest ->
        when (dest) {
            AppDestination.HOME -> HomeScreen(viewModel = viewModel)
            AppDestination.MAPS -> MapsScreen(viewModel = viewModel)
            AppDestination.WEATHER -> WeatherScreen(viewModel = viewModel)
            AppDestination.ASSISTANT -> AssistantScreen(viewModel = viewModel)
            AppDestination.PROFILE -> ProfileScreen(viewModel = viewModel)
            AppDestination.SETTINGS -> SettingsScreen(viewModel = viewModel)
        }
    }
}
