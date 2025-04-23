package com.dantech.wife.recipe.ui.screens.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dantech.wife.recipe.data.local.UserPreferences
import com.dantech.wife.recipe.data.model.Recipe
import com.dantech.wife.recipe.data.repository.CommunityRepository
import com.dantech.wife.recipe.data.repository.RecipeRepository
import com.dantech.wife.recipe.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class RecipeDetailViewModel(
    private val recipeRepository: RecipeRepository,
    private val communityRepository: CommunityRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _recipe = MutableStateFlow<Resource<Recipe>>(Resource.loading())
    val recipe: StateFlow<Resource<Recipe>> = _recipe.asStateFlow()
    
    private val _isRecipeSaved = MutableStateFlow(false)
    val isRecipeSaved: StateFlow<Boolean> = _isRecipeSaved.asStateFlow()
    
    private val _isRecipeFavorite = MutableStateFlow(false)
    val isRecipeFavorite: StateFlow<Boolean> = _isRecipeFavorite.asStateFlow()
    
    private val _cookingInstructions = MutableStateFlow<List<String>>(emptyList())
    val cookingInstructions: StateFlow<List<String>> = _cookingInstructions.asStateFlow()
    
    fun loadRecipe(recipeId: String) {
        if (recipeId.isEmpty()) {
            Log.d("RecipeDetailsViewModel", "Invalid recipe ID")
            _recipe.value = Resource.error("Invalid recipe ID")
            return
        }
        
        viewModelScope.launch {
            Log.d("RecipeDetailsViewModel", "Loading recipe with ID: $recipeId")
            _recipe.value = Resource.loading()
            
            // Reset state
            _isRecipeSaved.value = false
            _isRecipeFavorite.value = false
            
            // Check if this is a community recipe
            if (recipeId.startsWith("community_")) {
                val communityRecipeId = recipeId.removePrefix("community_")
                Log.d("RecipeDetailsViewModel", "Loading community recipe with ID: $communityRecipeId")
                
                try {
                    // Fetch the community recipe
                    val communityRecipe = communityRepository.getRecipeById(communityRecipeId)
                    if (communityRecipe != null) {
                        // Create a Recipe object from CommunityRecipe
                        val recipe = Recipe(
                            uri = "community_${communityRecipe.id}",
                            label = communityRecipe.title,
                            image = communityRecipe.imageUrl,
                            source = communityRecipe.authorName,
                            url = "", // Community recipes might not have a URL
                            yield = communityRecipe.servings.toDouble(),
                            calories = 0.0, // Community recipes might not have calories
                            totalTime = (communityRecipe.preparationTime + communityRecipe.cookingTime).toDouble(),
                            ingredientLines = communityRecipe.ingredients,
                            ingredients = emptyList(), // We don't parse the ingredients
                            dietLabels = emptyList(),
                            healthLabels = emptyList(),
                            cautions = emptyList(),
                            digest = emptyList(),
                            totalDaily = emptyMap(),
                            totalNutrients = emptyMap(),
                            cuisineType = communityRecipe.tags.filter { it.contains("cuisine", ignoreCase = true) },
                            mealType = communityRecipe.tags.filter { it.contains("meal", ignoreCase = true) },
                            dishType = communityRecipe.tags.filter { it.contains("dish", ignoreCase = true) },
                            instructionLines = communityRecipe.instructions,
                            summary = communityRecipe.description,
                            tags = communityRecipe.tags,
                            isFavorite = false
                        )
                        
                        _recipe.value = Resource.success(recipe)
                        _cookingInstructions.value = communityRecipe.instructions
                        return@launch
                    } else {
                        _recipe.value = Resource.error("Community recipe not found")
                        return@launch
                    }
                } catch (e: Exception) {
                    _recipe.value = Resource.error("Error loading community recipe: ${e.message}")
                    return@launch
                }
            }
            
            // First check if the recipe is saved locally
            Log.d("RecipeDetailsViewModel", "Checking saved recipes for ID: $recipeId")
            val savedRecipe = recipeRepository.getSavedRecipeById(recipeId)
            Log.d("RecipeDetailsViewModel", "Saved recipe: $savedRecipe")
            if (savedRecipe != null) {
                Log.d("RecipeDetailsViewModel", "Recipe found in local storage: $savedRecipe")
                _recipe.value = Resource.success(savedRecipe)
                _isRecipeSaved.value = true
                checkIfFavorite(recipeId)
                fetchCookingInstructions(savedRecipe)
                return@launch
            } else {
                Log.d("RecipeDetailsViewModel", "Recipe not found in local storage, fetching from API")
            }
            
            try {
                // Format the URI correctly for the API
                val uri = if (recipeId.startsWith("http")) {
                    recipeId
                } else {
                    // Extract the ID portion without any prefixes
                    val plainId = if (recipeId.startsWith("recipe_")) {
                        recipeId.substring(7) // Remove "recipe_" prefix
                    } else {
                        recipeId
                    }
                    "http://www.edamam.com/ontologies/edamam.owl#recipe_$plainId"
                }
                Log.d("RecipeDetailsViewModel", "Original recipeId: $recipeId, Formatted URI: $uri")
                Log.d("RecipeDetailsViewModel", "Fetching recipe from API: $uri")
                
                val result = recipeRepository.getRecipeById(uri)
                Log.d("RecipeDetailsViewModel", "API result: $result")
                
                if (result.isSuccessful) {
                    Log.d("RecipeDetailsViewModel", "API response code: ${result.code()}")
                    Log.d("RecipeDetailsViewModel", "API response body: ${result.body()}")
                    
                    if (result.body() != null) {
                        Log.d("RecipeDetailsViewModel", "Hits count: ${result.body()!!.hits.size}")
                        Log.d("RecipeDetailsViewModel", "Response count: ${result.body()!!.count}")
                        
                        if (result.body()!!.hits.isNotEmpty()) {
                            Log.d("RecipeDetailsViewModel", "Recipe fetched successfully")
                            val recipe = result.body()!!.hits.first().recipe
                            _recipe.value = Resource.success(recipe)
                            checkIfSaved(recipe.recipeId)
                            checkIfFavorite(recipe.recipeId)
                            fetchCookingInstructions(recipe)
                        } else {
                            Log.d("RecipeDetailsViewModel", "No recipe found in hits array")
                            _recipe.value = Resource.error("Recipe not found")
                        }
                    } else {
                        Log.d("RecipeDetailsViewModel", "Response body is null")
                        _recipe.value = Resource.error("Failed to load recipe: Empty response")
                    }
                } else {
                    Log.d("RecipeDetailsViewModel", "Failed API call: ${result.code()} - ${result.message()}")
                    _recipe.value = Resource.error("Failed to load recipe: ${result.message()}")
                }
            } catch (e: IOException) {
                _recipe.value = Resource.error("Network error: ${e.message}")
            } catch (e: HttpException) {
                _recipe.value = Resource.error("API error: ${e.message}")
            } catch (e: Exception) {
                _recipe.value = Resource.error("Unknown error: ${e.message}")
            }
        }
    }
    
    fun toggleSaveRecipe() {
        val currentRecipe = (recipe.value as? Resource.Success)?.data ?: return
        
        // Optimistically update UI state immediately
        val newSavedStatus = !isRecipeSaved.value
        _isRecipeSaved.value = newSavedStatus
        
        if (!newSavedStatus) {
            _isRecipeFavorite.value = false
        }
        
        // Then perform the actual operation
        viewModelScope.launch {
            try {
                if (newSavedStatus) {
                    recipeRepository.saveRecipe(currentRecipe)
                } else {
                    recipeRepository.deleteRecipe(currentRecipe.recipeId)
                }
            } catch (e: Exception) {
                // If operation fails, revert the UI state
                _isRecipeSaved.value = !newSavedStatus
                if (!newSavedStatus) {
                    checkIfFavorite(currentRecipe.recipeId)
                }
                Log.e("RecipeDetailsViewModel", "Error toggling saved status: ${e.message}")
            }
        }
    }
    
    fun toggleFavorite() {
        val currentRecipe = (recipe.value as? Resource.Success)?.data ?: return
        
        // Optimistically update UI state immediately
        val newFavoriteStatus = !isRecipeFavorite.value
        _isRecipeFavorite.value = newFavoriteStatus
        
        // If we're favoriting a recipe, it must be saved
        if (newFavoriteStatus && !isRecipeSaved.value) {
            _isRecipeSaved.value = true
        }
        
        // Then perform the actual operation
        viewModelScope.launch {
            try {
                if (!isRecipeSaved.value) {
                    // Save the recipe first if it's not saved
                    recipeRepository.saveRecipe(currentRecipe)
                }
                
                // Update favorite status
                recipeRepository.updateFavoriteStatus(currentRecipe.recipeId, newFavoriteStatus)
            } catch (e: Exception) {
                // If operation fails, revert the UI state
                _isRecipeFavorite.value = !newFavoriteStatus
                Log.e("RecipeDetailsViewModel", "Error toggling favorite status: ${e.message}")
            }
        }
    }
    
    private val _shareComplete = MutableStateFlow(false)
    val shareComplete: StateFlow<Boolean> = _shareComplete.asStateFlow()
    
    fun shareRecipe() {
        val currentRecipe = (recipe.value as? Resource.Success)?.data ?: return
        
        // Reset share state
        _shareComplete.value = false
        
        viewModelScope.launch {
            try {
                // Get user info from preferences
                val userName = userPreferences.userName.first()
                val userAvatar = userPreferences.userAvatar.first()
                
                // Convert to community recipe
                val communityRecipe = communityRepository.convertRecipeToCommunity(
                    recipe = currentRecipe,
                    userName = userName.ifEmpty { "Anonymous" },
                    userAvatarUrl = userAvatar.ifEmpty { "https://via.placeholder.com/50" }
                )
                
                // Add to community repository
                communityRepository.addRecipe(communityRecipe)
                
                // If the recipe isn't saved, save it
                if (!isRecipeSaved.value) {
                    recipeRepository.saveRecipe(currentRecipe)
                    _isRecipeSaved.value = true
                }
                
                // Signal completion
                _shareComplete.value = true
                Log.d("RecipeDetailsViewModel", "Recipe shared successfully")
            } catch (e: Exception) {
                Log.e("RecipeDetailsViewModel", "Error sharing recipe: ${e.message}")
            }
        }
    }
    
    // Reset share completion state - call this after navigating
    fun resetShareState() {
        _shareComplete.value = false
    }
    
    private fun checkIfSaved(recipeId: String) {
        viewModelScope.launch {
            _isRecipeSaved.value = recipeRepository.isRecipeSaved(recipeId)
            if (_isRecipeSaved.value) {
                checkIfFavorite(recipeId)
            }
        }
    }
    
    private fun checkIfFavorite(recipeId: String) {
        viewModelScope.launch {
            // In a real app, this would get the favorite status from the saved recipe
            recipeRepository.getFavoriteRecipes().collectLatest { favoriteRecipes ->
                _isRecipeFavorite.value = favoriteRecipes.any { it.recipeId == recipeId }
            }
        }
    }
    
    /**
     * Fetches cooking instructions for a recipe
     * This will try to use existing instructions, or fetch them from the source URL
     */
    private fun fetchCookingInstructions(recipe: Recipe) {
        viewModelScope.launch {
            try {
                Log.d("RecipeDetailsViewModel", "Fetching cooking instructions for ${recipe.label}")
                
                // First check if recipe already has instructions
                if (recipe.instructionLines.isNotEmpty()) {
                    Log.d("RecipeDetailsViewModel", "Using existing instructions from recipe")
                    _cookingInstructions.value = recipe.instructionLines
                    return@launch
                }
                
                // Otherwise try to fetch from source URL
                Log.d("RecipeDetailsViewModel", "Trying to fetch instructions from source URL")
                val instructions = recipeRepository.getRecipeInstructions(recipe)
                _cookingInstructions.value = instructions
                
                Log.d("RecipeDetailsViewModel", "Fetched ${instructions.size} instructions")
            } catch (e: Exception) {
                Log.e("RecipeDetailsViewModel", "Error fetching instructions: ${e.message}")
                // Provide a fallback message
                _cookingInstructions.value = listOf("Visit ${recipe.source} for full instructions at: ${recipe.url}")
            }
        }
    }
}