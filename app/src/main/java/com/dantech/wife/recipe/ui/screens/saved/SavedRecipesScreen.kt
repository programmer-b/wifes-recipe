package com.dantech.wife.recipe.ui.screens.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dantech.wife.recipe.ui.components.ErrorScreen
import com.dantech.wife.recipe.ui.components.LoadingScreen
import com.dantech.wife.recipe.ui.components.RecipeCard
import com.dantech.wife.recipe.utils.Resource

@Composable
fun SavedRecipesScreen(
    viewModel: SavedRecipesViewModel,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Local state for favorites filter
    var showOnlyFavorites by remember { mutableStateOf(false) }
    
    // Get saved recipes state
    val savedRecipesState by viewModel.savedRecipes.collectAsState()
    
    Column(modifier = modifier.fillMaxSize()) {
        // Filter header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Show favorites only",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Checkbox(
                checked = showOnlyFavorites,
                onCheckedChange = { showOnlyFavorites = it }
            )
        }
        
        when (savedRecipesState) {
            is Resource.Loading -> {
                LoadingScreen()
            }
            is Resource.Success -> {
                val recipes = (savedRecipesState as Resource.Success).data
                val displayedRecipes = if (showOnlyFavorites) {
                    recipes.filter { it.isFavorite }
                } else {
                    recipes
                }
                
                if (displayedRecipes.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = if (showOnlyFavorites) {
                                "No favorite recipes yet"
                            } else {
                                "No saved recipes yet"
                            },
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = displayedRecipes,
                            key = { it.recipeId }
                        ) { recipe ->
                            Column {
                                RecipeCard(
                                    recipe = recipe,
                                    onClick = { onRecipeClick(recipe.recipeId) }
                                )
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = { viewModel.deleteRecipe(recipe.recipeId) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Recipe",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is Resource.Error -> {
                ErrorScreen(
                    message = (savedRecipesState as Resource.Error).message ?: "Unknown error",
                    onRetry = { viewModel.loadSavedRecipes() }
                )
            }
        }
    }
}