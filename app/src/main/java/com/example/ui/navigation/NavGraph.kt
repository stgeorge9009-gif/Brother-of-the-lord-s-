package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateNext = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToPeople = { navController.navigate(Screen.People.route) },
                onNavigateToProducts = { navController.navigate(Screen.Products.route) },
                onNavigateToAssistance = { navController.navigate(Screen.MonthlyAssistance.route) },
                onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToPersonDetail = { personId ->
                    navController.navigate(Screen.PersonDetail.createRoute(personId))
                }
            )
        }

        composable(Screen.People.route) {
            PeopleScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddPerson = {
                    navController.navigate(Screen.AddEditPerson.createRoute(-1L))
                },
                onNavigateToPersonDetail = { personId ->
                    navController.navigate(Screen.PersonDetail.createRoute(personId))
                }
            )
        }

        composable(
            route = Screen.AddEditPerson.route,
            arguments = listOf(navArgument("personId") { type = NavType.LongType })
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getLong("personId") ?: -1L
            AddEditPersonScreen(
                viewModel = viewModel,
                personId = personId,
                onNavigateBack = { navController.popBackStack() },
                onPersonSaved = { savedId ->
                    navController.navigate(Screen.PersonDetail.createRoute(savedId)) {
                        popUpTo(Screen.People.route)
                    }
                }
            )
        }

        composable(
            route = Screen.PersonDetail.route,
            arguments = listOf(navArgument("personId") { type = NavType.LongType })
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getLong("personId") ?: -1L
            PersonDetailScreen(
                viewModel = viewModel,
                personId = personId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditPerson = { id ->
                    navController.navigate(Screen.AddEditPerson.createRoute(id))
                },
                onNavigateToEditPackage = { id ->
                    navController.navigate(Screen.EditPersonPackage.createRoute(id))
                },
                onNavigateToEditAssistance = { assistanceId ->
                    navController.navigate(Screen.EditAssistance.createRoute(assistanceId))
                }
            )
        }

        composable(
            route = Screen.EditPersonPackage.route,
            arguments = listOf(navArgument("personId") { type = NavType.LongType })
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getLong("personId") ?: -1L
            EditPersonPackageScreen(
                viewModel = viewModel,
                personId = personId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Products.route) {
            ProductsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddProduct = {
                    navController.navigate(Screen.AddEditProduct.createRoute(-1L))
                },
                onNavigateToEditProduct = { productId ->
                    navController.navigate(Screen.AddEditProduct.createRoute(productId))
                }
            )
        }

        composable(
            route = Screen.AddEditProduct.route,
            arguments = listOf(navArgument("productId") { type = NavType.LongType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getLong("productId") ?: -1L
            AddEditProductScreen(
                viewModel = viewModel,
                productId = productId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditAssistance.route,
            arguments = listOf(navArgument("assistanceId") { type = NavType.LongType })
        ) { backStackEntry ->
            val assistanceId = backStackEntry.arguments?.getLong("assistanceId") ?: -1L
            EditAssistanceScreen(
                viewModel = viewModel,
                assistanceId = assistanceId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MonthlyAssistance.route) {
            MonthlyAssistanceScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPersonDetail = { personId ->
                    navController.navigate(Screen.PersonDetail.createRoute(personId))
                }
            )
        }

        composable(Screen.Calendar.route) {
            CalendarScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPersonDetail = { personId ->
                    navController.navigate(Screen.PersonDetail.createRoute(personId))
                }
            )
        }

        composable(Screen.Reports.route) {
            ReportsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProducts = { navController.navigate(Screen.Products.route) }
            )
        }
    }
}
