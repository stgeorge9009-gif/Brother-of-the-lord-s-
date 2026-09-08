package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Dashboard : Screen("dashboard")
    object People : Screen("people")
    object AddEditPerson : Screen("add_edit_person/{personId}") {
        fun createRoute(personId: Long) = "add_edit_person/$personId"
    }
    object PersonDetail : Screen("person_detail/{personId}") {
        fun createRoute(personId: Long) = "person_detail/$personId"
    }
    object EditPersonPackage : Screen("edit_person_package/{personId}") {
        fun createRoute(personId: Long) = "edit_person_package/$personId"
    }
    object Products : Screen("products")
    object AddEditProduct : Screen("add_edit_product/{productId}") {
        fun createRoute(productId: Long) = "add_edit_product/$productId"
    }
    object EditAssistance : Screen("edit_assistance/{assistanceId}") {
        fun createRoute(assistanceId: Long) = "edit_assistance/$assistanceId"
    }
    object MonthlyAssistance : Screen("monthly_assistance")
    object Calendar : Screen("calendar")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
}
