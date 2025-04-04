package com.dantech.wife.recipe.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dantech.wife.recipe.ui.screens.community.CommunityScreen
import com.dantech.wife.recipe.ui.screens.community.CommunityViewModel
import com.dantech.wife.recipe.ui.screens.detail.RecipeDetailScreen
import com.dantech.wife.recipe.ui.screens.detail.RecipeDetailViewModel
import com.dantech.wife.recipe.ui.screens.home.HomeScreen
import com.dantech.wife.recipe.ui.screens.home.HomeViewModel
import com.dantech.wife.recipe.ui.screens.saved.SavedRecipesScreen
import com.dantech.wife.recipe.ui.screens.saved.SavedRecipesViewModel
import com.dantech.wife.recipe.ui.screens.search.SearchScreen
import com.dantech.wife.recipe.ui.screens.search.SearchViewModel

@Composable
fun RecipeNavHost(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    searchViewModel: SearchViewModel,
    savedRecipesViewModel: SavedRecipesViewModel,
    recipeDetailViewModel: RecipeDetailViewModel,
    communityViewModel: CommunityViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavDestinations.Home.route,
        modifier = modifier
    ) {
        composable(NavDestinations.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onRecipeClick = { recipeId ->
                    navController.navigate(NavDestinations.RecipeDetail.createRoute(recipeId))
                }
            )
        }
        
        composable(NavDestinations.Search.route) {
            SearchScreen(
                viewModel = searchViewModel,
                onRecipeClick = { recipeId ->
                    navController.navigate(NavDestinations.RecipeDetail.createRoute(recipeId))
                }
            )
        }
        
        composable(NavDestinations.Saved.route) {
            SavedRecipesScreen(
                viewModel = savedRecipesViewModel,
                onRecipeClick = { recipeId ->
                    navController.navigate(NavDestinations.RecipeDetail.createRoute(recipeId))
                }
            )
        }
        
        composable(NavDestinations.Community.route) {
            CommunityScreen(
                viewModel = communityViewModel,
                onRecipeClick = { recipeId ->
                    navController.navigate(NavDestinations.CommunityRecipeDetail.createRoute(recipeId))
                },
                onShareRecipe = { /* Handle recipe sharing */ }
            )
        }
        
        composable(
            route = NavDestinations.RecipeDetail.route,
            arguments = listOf(
                navArgument("recipeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val recipeId = remember {
                backStackEntry.arguments?.getString("recipeId") ?: ""
            }
            
            RecipeDetailScreen(
                recipeId = recipeId,
                viewModel = recipeDetailViewModel,
                onNavigateUp = { navController.navigateUp() },
                onShareRecipe = { 
                    // When the user shares a recipe, navigate back to the community tab
                    navController.navigate(NavDestinations.Community.route) {
                        // Pop up to the community tab to avoid having multiple community screens stacked
                        popUpTo(NavDestinations.Community.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = NavDestinations.CommunityRecipeDetail.route,
            arguments = listOf(
                navArgument("recipeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val recipeId = remember {
                backStackEntry.arguments?.getString("recipeId") ?: ""
            }
            
            // Use the same RecipeDetailScreen but customize for community recipes
            RecipeDetailScreen(
                recipeId = recipeId,
                viewModel = recipeDetailViewModel,
                onNavigateUp = { navController.navigateUp() },
                onShareRecipe = {
                    // Navigate back to community screen after sharing
                    navController.navigate(NavDestinations.Community.route) {
                        popUpTo(NavDestinations.Community.route) { inclusive = true }
                    }
                }
            )
        }
    }
}