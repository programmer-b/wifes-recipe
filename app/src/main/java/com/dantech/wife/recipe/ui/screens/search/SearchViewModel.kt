package com.dantech.wife.recipe.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dantech.wife.recipe.data.local.UserPreferences
import com.dantech.wife.recipe.data.model.Recipe
import com.dantech.wife.recipe.data.repository.RecipeRepository
import com.dantech.wife.recipe.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class SearchViewModel(
    private val repository: RecipeRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _searchResults = MutableStateFlow<Resource<List<Recipe>>>(Resource.success(emptyList()))
    val searchResults: StateFlow<Resource<List<Recipe>>> = _searchResults.asStateFlow()
    
    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()
    
    private val _savedRecipeIds = MutableStateFlow<Set<String>>(emptySet())
    val savedRecipeIds: StateFlow<Set<String>> = _savedRecipeIds.asStateFlow()
    
    private val _selectedFilters = MutableStateFlow<Map<FilterType, String>>(emptyMap())
    val selectedFilters: StateFlow<Map<FilterType, String>> = _selectedFilters.asStateFlow()
    
    init {
        collectSearchHistory()
        collectSavedRecipes()
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun search(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) return
        
        viewModelScope.launch {
            _searchResults.value = Resource.loading()
            
            try {
                val result = repository.searchRecipes(
                    query = trimmedQuery,
                    from = 0,
                    to = 20,
                    diet = selectedFilters.value[FilterType.DIET],
                    health = selectedFilters.value[FilterType.HEALTH],
                    cuisineType = selectedFilters.value[FilterType.CUISINE],
                    mealType = selectedFilters.value[FilterType.MEAL_TYPE],
                    dishType = selectedFilters.value[FilterType.DISH_TYPE]
                )
                
                if (result.isSuccessful && result.body() != null) {
                    val recipes = result.body()!!.hits.map { it.recipe }
                    _searchResults.value = Resource.success(recipes)
                    userPreferences.addSearchQuery(trimmedQuery)
                } else {
                    _searchResults.value = Resource.error("Failed to search recipes: ${result.message()}")
                }
            } catch (e: IOException) {
                _searchResults.value = Resource.error("Network error: ${e.message}")
            } catch (e: HttpException) {
                _searchResults.value = Resource.error("API error: ${e.message}")
            } catch (e: Exception) {
                _searchResults.value = Resource.error("Unknown error: ${e.message}")
            }
        }
    }
    
    fun clearSearchHistory() {
        viewModelScope.launch {
            userPreferences.clearSearchHistory()
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
    
    fun updateFilter(type: FilterType, value: String?) {
        _selectedFilters.value = if (value == null) {
            selectedFilters.value - type
        } else {
            selectedFilters.value + (type to value)
        }
    }
    
    fun clearFilters() {
        _selectedFilters.value = emptyMap()
    }
    
    private fun collectSearchHistory() {
        viewModelScope.launch {
            userPreferences.searchHistory
                .catch { /* Handle error */ }
                .collect { history ->
                    _searchHistory.value = history
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

enum class FilterType {
    DIET,
    HEALTH,
    CUISINE,
    MEAL_TYPE,
    DISH_TYPE
}