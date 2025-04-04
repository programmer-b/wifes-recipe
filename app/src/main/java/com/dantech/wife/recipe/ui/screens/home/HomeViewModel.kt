package com.dantech.wife.recipe.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dantech.wife.recipe.data.model.Recipe
import com.dantech.wife.recipe.data.repository.RecipeRepository
import com.dantech.wife.recipe.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class HomeViewModel(
    private val repository: RecipeRepository
) : ViewModel() {
    
    private val _featuredRecipes = MutableStateFlow<Resource<List<Recipe>>>(Resource.loading())
    val featuredRecipes: StateFlow<Resource<List<Recipe>>> = _featuredRecipes.asStateFlow()
    
    private val _popularRecipes = MutableStateFlow<Resource<List<Recipe>>>(Resource.loading())
    val popularRecipes: StateFlow<Resource<List<Recipe>>> = _popularRecipes.asStateFlow()
    
    private val _quickRecipes = MutableStateFlow<Resource<List<Recipe>>>(Resource.loading())
    val quickRecipes: StateFlow<Resource<List<Recipe>>> = _quickRecipes.asStateFlow()
    
    private val _savedRecipeIds = MutableStateFlow<Set<String>>(emptySet())
    val savedRecipeIds: StateFlow<Set<String>> = _savedRecipeIds.asStateFlow()
    
    init {
        loadFeaturedRecipes()
        loadPopularRecipes()
        loadQuickRecipes()
        collectSavedRecipes()
    }
    
    fun loadFeaturedRecipes() {
        viewModelScope.launch {
            _featuredRecipes.value = Resource.loading()
            
            try {
                val result = repository.searchRecipes(
                    query = "trending",
                    from = 0,
                    to = 10
                )
                
                if (result.isSuccessful && result.body() != null) {
                    val recipes = result.body()!!.hits.map { it.recipe }
                    _featuredRecipes.value = Resource.success(recipes)
                } else {
                    _featuredRecipes.value = Resource.error("Failed to load featured recipes: ${result.message()}")
                }
            } catch (e: IOException) {
                _featuredRecipes.value = Resource.error("Network error: ${e.message}")
            } catch (e: HttpException) {
                _featuredRecipes.value = Resource.error("API error: ${e.message}")
            } catch (e: Exception) {
                _featuredRecipes.value = Resource.error("Unknown error: ${e.message}")
            }
        }
    }
    
    fun loadPopularRecipes() {
        viewModelScope.launch {
            _popularRecipes.value = Resource.loading()
            
            try {
                val result = repository.searchRecipes(
                    query = "popular",
                    from = 0,
                    to = 10
                )
                
                if (result.isSuccessful && result.body() != null) {
                    val recipes = result.body()!!.hits.map { it.recipe }
                    _popularRecipes.value = Resource.success(recipes)
                } else {
                    _popularRecipes.value = Resource.error("Failed to load popular recipes: ${result.message()}")
                }
            } catch (e: IOException) {
                _popularRecipes.value = Resource.error("Network error: ${e.message}")
            } catch (e: HttpException) {
                _popularRecipes.value = Resource.error("API error: ${e.message}")
            } catch (e: Exception) {
                _popularRecipes.value = Resource.error("Unknown error: ${e.message}")
            }
        }
    }
    
    fun loadQuickRecipes() {
        viewModelScope.launch {
            _quickRecipes.value = Resource.loading()
            
            try {
                val result = repository.searchRecipes(
                    query = "quick",
                    from = 0,
                    to = 10,
                    mealType = "lunch" // Quick lunch recipes
                )
                
                if (result.isSuccessful && result.body() != null) {
                    val recipes = result.body()!!.hits.map { it.recipe }
                    _quickRecipes.value = Resource.success(recipes)
                } else {
                    _quickRecipes.value = Resource.error("Failed to load quick recipes: ${result.message()}")
                }
            } catch (e: IOException) {
                _quickRecipes.value = Resource.error("Network error: ${e.message}")
            } catch (e: HttpException) {
                _quickRecipes.value = Resource.error("API error: ${e.message}")
            } catch (e: Exception) {
                _quickRecipes.value = Resource.error("Unknown error: ${e.message}")
            }
        }
    }
    
    fun toggleFavorite(recipe: Recipe) {
        viewModelScope.launch {
            val isSaved = repository.isRecipeSaved(recipe.recipeId)
            
            if (isSaved) {
                repository.deleteRecipe(recipe.recipeId)
            } else {
                repository.saveRecipe(recipe)
            }
        }
    }
    
    private fun collectSavedRecipes() {
        viewModelScope.launch {
            repository.getAllSavedRecipes().collect { recipes ->
                _savedRecipeIds.value = recipes.map { it.recipeId }.toSet()
            }
        }
    }
}