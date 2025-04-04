package com.dantech.wife.recipe.ui.screens.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dantech.wife.recipe.data.model.Recipe
import com.dantech.wife.recipe.data.repository.RecipeRepository
import com.dantech.wife.recipe.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SavedRecipesViewModel(
    private val repository: RecipeRepository
) : ViewModel() {
    
    // State for saved recipes
    private val _savedRecipes = MutableStateFlow<Resource<List<Recipe>>>(Resource.loading())
    val savedRecipes: StateFlow<Resource<List<Recipe>>> = _savedRecipes.asStateFlow()
    
    init {
        loadSavedRecipes()
    }
    
    fun loadSavedRecipes() {
        viewModelScope.launch {
            try {
                repository.getAllSavedRecipes().collect { recipes ->
                    _savedRecipes.value = Resource.success(recipes)
                }
            } catch (e: Exception) {
                _savedRecipes.value = Resource.error("Failed to load saved recipes: ${e.message}")
            }
        }
    }
    
    fun toggleFavorite(recipe: Recipe) {
        viewModelScope.launch {
            repository.updateFavoriteStatus(recipe.recipeId, !recipe.isFavorite)
        }
    }
    
    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            repository.deleteRecipe(recipeId)
        }
    }
}