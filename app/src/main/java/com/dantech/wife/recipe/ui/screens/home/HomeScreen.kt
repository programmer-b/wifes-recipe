package com.dantech.wife.recipe.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dantech.wife.recipe.data.model.Recipe
import com.dantech.wife.recipe.ui.components.ErrorScreen
import com.dantech.wife.recipe.ui.components.LoadingScreen
import com.dantech.wife.recipe.ui.components.RecipeCard
import com.dantech.wife.recipe.utils.Resource

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val featuredRecipesState by viewModel.featuredRecipes.collectAsState()
    val popularRecipesState by viewModel.popularRecipes.collectAsState()
    val quickRecipesState by viewModel.quickRecipes.collectAsState()
    val savedRecipeIds by viewModel.savedRecipeIds.collectAsState()
    
    // Determine if any recipes are in loading state
    val isLoading by remember {
        derivedStateOf {
            featuredRecipesState is Resource.Loading ||
            popularRecipesState is Resource.Loading || 
            quickRecipesState is Resource.Loading
        }
    }
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = 16.dp)
    ) {
        // Title and refresh button in a row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recipe & Meal Planner",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            
            // Add refresh button
            IconButton(
                onClick = { viewModel.loadAllRecipes() }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh recipes"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Featured Recipes Section
        SectionTitle(title = "Featured Recipes")
        
        when (val featured = featuredRecipesState) {
            is Resource.Loading -> {
                RecipeRowPlaceholder()
            }
            is Resource.Success -> {
                RecipeRow(
                    recipes = featured.data,
                    savedRecipeIds = savedRecipeIds,
                    onRecipeClick = onRecipeClick,
                    onFavoriteClick = { recipe -> viewModel.toggleFavorite(recipe) }
                )
            }
            is Resource.Error -> {
                ErrorItem(
                    message = featured.message,
                    onRetry = { viewModel.loadFeaturedRecipes() }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Popular Recipes Section
        SectionTitle(title = "Popular Recipes")
        
        when (val popular = popularRecipesState) {
            is Resource.Loading -> {
                RecipeRowPlaceholder()
            }
            is Resource.Success -> {
                RecipeRow(
                    recipes = popular.data,
                    savedRecipeIds = savedRecipeIds,
                    onRecipeClick = onRecipeClick,
                    onFavoriteClick = { recipe -> viewModel.toggleFavorite(recipe) }
                )
            }
            is Resource.Error -> {
                ErrorItem(
                    message = popular.message,
                    onRetry = { viewModel.loadPopularRecipes() }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Quick Recipes Section
        SectionTitle(title = "Quick & Easy (Under 30 min)")
        
        when (val quick = quickRecipesState) {
            is Resource.Loading -> {
                RecipeRowPlaceholder()
            }
            is Resource.Success -> {
                RecipeRow(
                    recipes = quick.data,
                    savedRecipeIds = savedRecipeIds,
                    onRecipeClick = onRecipeClick,
                    onFavoriteClick = { recipe -> viewModel.toggleFavorite(recipe) }
                )
            }
            is Resource.Error -> {
                ErrorItem(
                    message = quick.message,
                    onRetry = { viewModel.loadQuickRecipes() }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun RecipeRow(
    recipes: List<Recipe>,
    savedRecipeIds: Set<String>,
    onRecipeClick: (String) -> Unit,
    onFavoriteClick: (Recipe) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(recipes) { recipe ->
            RecipeCard(
                recipe = recipe,
                isFavorite = savedRecipeIds.contains(recipe.recipeId),
                onFavoriteClick = { onFavoriteClick(recipe) },
                onClick = { onRecipeClick(recipe.recipeId) },
                modifier = Modifier.width(280.dp)
            )
        }
    }
}

@Composable
fun RecipeRowPlaceholder() {
    // Implement a shimmer loading placeholder for recipes
    LoadingScreen()
}

@Composable
fun ErrorItem(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Error: $message",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        androidx.compose.material3.Button(
            onClick = onRetry,
            modifier = Modifier.align(androidx.compose.ui.Alignment.End)
        ) {
            Text(text = "Retry")
        }
    }
}