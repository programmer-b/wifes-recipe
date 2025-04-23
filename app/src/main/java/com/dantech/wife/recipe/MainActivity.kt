package com.dantech.wife.recipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dantech.wife.recipe.data.local.RecipeStorage
import com.dantech.wife.recipe.data.local.UserPreferences
import com.dantech.wife.recipe.data.remote.EdamamApiService
import com.dantech.wife.recipe.data.repository.CommunityRepository
import com.dantech.wife.recipe.data.repository.RecipeRepository
import com.dantech.wife.recipe.ui.components.RecipeBottomNavigation
import com.dantech.wife.recipe.ui.navigation.NavDestinations
import com.dantech.wife.recipe.ui.navigation.RecipeNavHost
import com.dantech.wife.recipe.ui.screens.community.CommunityViewModel
import com.dantech.wife.recipe.ui.screens.detail.RecipeDetailViewModel
import com.dantech.wife.recipe.ui.screens.home.HomeViewModel
import com.dantech.wife.recipe.ui.screens.saved.SavedRecipesViewModel
import com.dantech.wife.recipe.ui.screens.search.SearchViewModel
import com.dantech.wife.recipe.ui.theme.RecipeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val application = application as RecipeApplication
        
        setContent {
            RecipeTheme {
                RecipeApp(
                    recipeRepository = application.recipeRepository,
                    communityRepository = application.communityRepository,
                    userPreferences = application.userPreferences,
                    backPressedDispatcher = onBackPressedDispatcher
                )
            }
        }
    }
}

@Composable
fun RecipeApp(
    recipeRepository: RecipeRepository,
    communityRepository: CommunityRepository,
    userPreferences: UserPreferences,
    backPressedDispatcher: OnBackPressedDispatcher,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: NavDestinations.Home.route
    
    // Create ViewModels
    val homeViewModel = viewModel { HomeViewModel(recipeRepository) }
    val searchViewModel = viewModel { SearchViewModel(recipeRepository, userPreferences) }
    val savedRecipesViewModel = viewModel { SavedRecipesViewModel(recipeRepository) }
    val recipeDetailViewModel = viewModel { RecipeDetailViewModel(recipeRepository, communityRepository, userPreferences) }
    val communityViewModel = viewModel { CommunityViewModel(communityRepository, userPreferences) }
    
    // Handle system back button press - modify to always exit if on a top-level destination
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, navController) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Check if we're on a top-level destination
                val isOnMainDestination = currentRoute == NavDestinations.Home.route ||
                        currentRoute == NavDestinations.Search.route ||
                        currentRoute == NavDestinations.Saved.route ||
                        currentRoute == NavDestinations.Community.route
                
                if (isOnMainDestination) {
                    // If on a main tab, either navigate to Home or exit the app
                    if (currentRoute != NavDestinations.Home.route) {
                        // Navigate to home
                        navController.navigate(NavDestinations.Home.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        // On Home tab, exit the app
                        this.isEnabled = false
                        backPressedDispatcher.onBackPressed()
                    }
                } else {
                    // Otherwise, regular back behavior
                    navController.navigateUp()
                }
            }
        }
        backPressedDispatcher.addCallback(lifecycleOwner, callback)
        onDispose {
            callback.remove()
        }
    }
    
    // Check if we're on a detail screen
    val showBottomNav = currentRoute == NavDestinations.Home.route ||
            currentRoute == NavDestinations.Search.route ||
            currentRoute == NavDestinations.Saved.route ||
            currentRoute == NavDestinations.Community.route
    
    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                RecipeBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        // Always clear the back stack when navigating to a root destination
                        // Use a different approach that doesn't use the current route or saveState
                        // which could be causing the navigation loop
                        navController.navigate(route) {
                            // Clear the entire back stack
                            popUpTo(0) { inclusive = true }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                        }
                    }
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            RecipeNavHost(
                navController = navController,
                homeViewModel = homeViewModel,
                searchViewModel = searchViewModel,
                savedRecipesViewModel = savedRecipesViewModel,
                recipeDetailViewModel = recipeDetailViewModel,
                communityViewModel = communityViewModel
            )
        }
    }
}