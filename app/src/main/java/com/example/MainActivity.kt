package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.AppNavGraph
import com.example.ui.navigation.Screen
import com.example.ui.theme.ChurchGold
import com.example.ui.theme.ChurchNavy
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

                        val showBottomNav = currentRoute != Screen.Splash.route

                        val bottomNavItems = listOf(
                            BottomNavItem("الرئيسية", Screen.Dashboard.route, Icons.Default.Home, "nav_item_dashboard"),
                            BottomNavItem("المخزن", Screen.Warehouse.route, Icons.Default.Inventory2, "nav_item_warehouse"),
                            BottomNavItem("تجميعة الأسر", Screen.FamilyPackagesSummary.route, Icons.Default.FamilyRestroom, "nav_item_family_packages"),
                            BottomNavItem("الأسر", Screen.People.route, Icons.Default.People, "nav_item_people"),
                            BottomNavItem("التقارير", Screen.Reports.route, Icons.Default.Assessment, "nav_item_reports")
                        )

                        Scaffold(
                            bottomBar = {
                                if (showBottomNav) {
                                    NavigationBar(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        tonalElevation = 8.dp,
                                        modifier = Modifier.testTag("app_bottom_navigation_bar")
                                    ) {
                                        bottomNavItems.forEach { item ->
                                            val isSelected = currentRoute == item.route
                                            NavigationBarItem(
                                                selected = isSelected,
                                                onClick = {
                                                    if (currentRoute != item.route) {
                                                        navController.navigate(item.route) {
                                                            popUpTo(Screen.Dashboard.route) {
                                                                saveState = true
                                                            }
                                                            launchSingleTop = true
                                                            restoreState = true
                                                        }
                                                    }
                                                },
                                                icon = {
                                                    Icon(
                                                        imageVector = item.icon,
                                                        contentDescription = item.title,
                                                        tint = if (isSelected) ChurchNavy else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                },
                                                label = {
                                                    Text(
                                                        text = item.title,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        fontSize = 11.sp,
                                                        color = if (isSelected) ChurchNavy else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                },
                                                colors = NavigationBarItemDefaults.colors(
                                                    indicatorColor = ChurchGold.copy(alpha = 0.25f)
                                                ),
                                                modifier = Modifier.testTag(item.testTag)
                                            )
                                        }
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                AppNavGraph(
                                    navController = navController,
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
