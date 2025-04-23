package com.dantech.wife.recipe.ui.screens.search

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dantech.wife.recipe.ui.components.ErrorScreen
import com.dantech.wife.recipe.ui.components.LoadingScreen
import com.dantech.wife.recipe.ui.components.RecipeCard
import com.dantech.wife.recipe.ui.components.SearchField
import com.dantech.wife.recipe.utils.Resource

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    
    // Local state for search query
    var searchQuery by remember { mutableStateOf("") }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { 
                    viewModel.search(searchQuery)
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Search Results or History
        when (searchResults) {
            is Resource.Loading -> {
                LoadingScreen()
            }
            is Resource.Success -> {
                val recipes = (searchResults as Resource.Success).data
                
                // Check if this is an actual search result (after user action) rather than initial state
                val hasPerformedSearch = viewModel.hasPerformedSearch.collectAsState().value
                
                if (recipes.isEmpty() && searchQuery.isNotBlank() && hasPerformedSearch) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "No recipes found for '$searchQuery'",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (recipes.isEmpty() && (!hasPerformedSearch || searchQuery.isBlank())) {
                    // Show search history
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        if (searchHistory.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Searches",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                IconButton(onClick = { viewModel.clearSearchHistory() }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear History"
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Display search history items
                            LazyColumn {
                                items(searchHistory) { query ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = query,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.clickable {
                                                searchQuery = query
                                                viewModel.search(query)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Show search results
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(recipes) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                onClick = { onRecipeClick(recipe.recipeId) }
                            )
                        }
                    }
                }
            }
            is Resource.Error -> {
                ErrorScreen(
                    message = (searchResults as Resource.Error).message ?: "Unknown error",
                    onRetry = { viewModel.search(searchQuery) }
                )
            }
        }
    }
}