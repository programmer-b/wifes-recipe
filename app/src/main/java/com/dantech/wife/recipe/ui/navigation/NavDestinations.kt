package com.dantech.wife.recipe.ui.navigation

sealed class NavDestinations(val route: String) {
    data object Home : NavDestinations("home")
    data object Search : NavDestinations("search")
    data object Saved : NavDestinations("saved")
    data object Community : NavDestinations("community")
    
    data object RecipeDetail : NavDestinations("recipe_detail/{recipeId}") {
        fun createRoute(recipeId: String) = "recipe_detail/$recipeId"
    }
    
    data object CommunityRecipeDetail : NavDestinations("community_recipe_detail/{recipeId}") {
        fun createRoute(recipeId: String) = "community_recipe_detail/community_$recipeId"
    }
}