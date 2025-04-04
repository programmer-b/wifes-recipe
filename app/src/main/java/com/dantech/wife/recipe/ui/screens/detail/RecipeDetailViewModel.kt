package com.dantech.wife.recipe.ui.screens.detail

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
    
    fun loadRecipe(recipeId: String) {
        if (recipeId.isEmpty()) {
            _recipe.value = Resource.error("Invalid recipe ID")
            return
        }
        
        viewModelScope.launch {
            _recipe.value = Resource.loading()
            
            // Reset state
            _isRecipeSaved.value = false
            _isRecipeFavorite.value = false
            
            // First check if the recipe is saved locally
            val savedRecipe = recipeRepository.getSavedRecipeById(recipeId)
            if (savedRecipe != null) {
                _recipe.value = Resource.success(savedRecipe)
                _isRecipeSaved.value = true
                checkIfFavorite(recipeId)
                return@launch
            }
            
            try {
                // Format the URI correctly for the API
                val uri = if (recipeId.startsWith("http")) {
                    recipeId
                } else {
                    "http://www.edamam.com/ontologies/edamam.owl#recipe_$recipeId"
                }
                
                val result = recipeRepository.getRecipeById(uri)
                
                if (result.isSuccessful && result.body() != null && result.body()!!.hits.isNotEmpty()) {
                    val recipe = result.body()!!.hits.first().recipe
                    _recipe.value = Resource.success(recipe)
                    checkIfSaved(recipe.recipeId)
                    checkIfFavorite(recipe.recipeId)
                } else {
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
        viewModelScope.launch {
            val currentRecipe = (recipe.value as? Resource.Success)?.data ?: return@launch
            
            if (isRecipeSaved.value) {
                recipeRepository.deleteRecipe(currentRecipe.recipeId)
                _isRecipeSaved.value = false
                _isRecipeFavorite.value = false
            } else {
                recipeRepository.saveRecipe(currentRecipe)
                _isRecipeSaved.value = true
            }
        }
    }
    
    fun toggleFavorite() {
        viewModelScope.launch {
            val currentRecipe = (recipe.value as? Resource.Success)?.data ?: return@launch
            
            if (!isRecipeSaved.value) {
                // Save the recipe first if it's not saved
                recipeRepository.saveRecipe(currentRecipe)
                _isRecipeSaved.value = true
            }
            
            // Toggle favorite status
            val newFavoriteStatus = !isRecipeFavorite.value
            recipeRepository.updateFavoriteStatus(currentRecipe.recipeId, newFavoriteStatus)
            _isRecipeFavorite.value = newFavoriteStatus
        }
    }
    
    fun shareRecipe() {
        viewModelScope.launch {
            try {
                val currentRecipe = (recipe.value as? Resource.Success)?.data
                
                if (currentRecipe == null) {
                    // Recipe not loaded or error occurred
                    return@launch
                }
                
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
            } catch (e: Exception) {
                // Handle any exceptions that might occur
                // In a production app, you might want to log this or show an error message
            }
        }
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
}