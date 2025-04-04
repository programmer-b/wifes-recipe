package com.dantech.wife.recipe.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dantech.wife.recipe.ui.components.ErrorScreen
import com.dantech.wife.recipe.ui.components.LoadingScreen
import com.dantech.wife.recipe.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    viewModel: RecipeDetailViewModel,
    recipeId: String,
    onNavigateUp: () -> Unit,
    onShareRecipe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recipeState by viewModel.recipe.collectAsState()
    val isRecipeSaved by viewModel.isRecipeSaved.collectAsState()
    val isFavorite by viewModel.isRecipeFavorite.collectAsState()
    
    // Load the recipe when the screen is displayed
    androidx.compose.runtime.LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Recipe Details",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Single favorite button that handles both saving and favoriting
                    IconButton(onClick = { 
                        if (isRecipeSaved && isFavorite) {
                            // If already favorited, remove from favorites but keep saved
                            viewModel.toggleFavorite()
                        } else if (isRecipeSaved) {
                            // If saved but not favorited, mark as favorite
                            viewModel.toggleFavorite()
                        } else {
                            // If not saved, save it
                            viewModel.toggleSaveRecipe()
                        }
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else if (isRecipeSaved) Icons.Default.FavoriteBorder else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else if (isRecipeSaved) "Add to favorites" else "Save recipe"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (recipeState is Resource.Success) {
                        viewModel.shareRecipe()
                        onShareRecipe()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Share, 
                    contentDescription = "Share Recipe"
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
            when (recipeState) {
                is Resource.Loading -> {
                    LoadingScreen()
                }
                is Resource.Success -> {
                    val recipe = (recipeState as Resource.Success).data
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Recipe Image
                        AsyncImage(
                            model = recipe.image,
                            contentDescription = recipe.label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        )
                        
                        // Recipe Details
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = recipe.label,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                InfoCard(title = "Calories", value = "${recipe.calories.toInt()}")
                                InfoCard(title = "Time", value = "${recipe.totalTime.toInt()} min")
                                InfoCard(title = "Servings", value = recipe.yield.toInt().toString())
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Health Labels
                            Text(
                                text = "Health Labels",
                                style = MaterialTheme.typography.titleLarge
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = recipe.healthLabels.joinToString(", "),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Ingredients
                            Text(
                                text = "Ingredients",
                                style = MaterialTheme.typography.titleLarge
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            recipe.ingredientLines.forEach { ingredient ->
                                Text(
                                    text = "• $ingredient",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // View Full Recipe Button
                            if (recipe.url.isNotEmpty()) {
                                Divider()
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "View full recipe and instructions on the source website",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    ErrorScreen(
                        message = (recipeState as Resource.Error).message ?: "Unknown error",
                        onRetry = { viewModel.loadRecipe(recipeId) }
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}