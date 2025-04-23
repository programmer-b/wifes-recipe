package com.dantech.wife.recipe.data.repository

import com.dantech.wife.recipe.data.local.RecipeStorage
import com.dantech.wife.recipe.data.local.toRecipe
import com.dantech.wife.recipe.data.local.toSavedRecipeEntity
import com.dantech.wife.recipe.data.model.Recipe
import com.dantech.wife.recipe.data.model.RecipeSearchResponse
import com.dantech.wife.recipe.data.remote.EdamamApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Response

/**
 * RecipeRepository - Repository for managing recipe data
 * Updated to use RecipeStorage instead of Room DAO
 */
class RecipeRepository(
    private val apiService: EdamamApiService,
    private val recipeStorage: RecipeStorage
) {
    // Remote data sources
    suspend fun searchRecipes(
        query: String,
        from: Int = 0,
        to: Int = 20,
        diet: String? = null,
        health: String? = null,
        cuisineType: String? = null,
        mealType: String? = null,
        dishType: String? = null
    ): Response<RecipeSearchResponse> {
        return apiService.searchRecipes(
            query = query,
            from = from,
            to = to,
            diet = diet,
            health = health,
            cuisineType = cuisineType,
            mealType = mealType,
            dishType = dishType
        )
    }
    
    suspend fun getRecipeById(uri: String): Response<RecipeSearchResponse> {
        android.util.Log.d("RecipeRepository", "Calling API with URI: $uri")
        val response = apiService.getRecipeById(uri = uri)
        android.util.Log.d("RecipeRepository", "Response code: ${response.code()}, Body: ${response.body()}")
        return response
    }
    
    /**
     * Attempts to fetch cooking instructions from the recipe URL
     * This method returns instructions for recipes that don't have them
     */
    suspend fun getRecipeInstructions(recipe: Recipe): List<String> {
        // If the recipe already has instructions, return them
        if (recipe.instructionLines.isNotEmpty()) {
            return recipe.instructionLines
        }
        
        try {
            // Try to fetch from the recipe URL
            val sourceUrl = recipe.url
            if (sourceUrl.isNotEmpty()) {
                android.util.Log.d("RecipeRepository", "Fetching instructions from source: $sourceUrl")
                val instructions = apiService.getRecipeInstructions(sourceUrl)
                android.util.Log.d("RecipeRepository", "Fetched instructions: $instructions")
                return instructions.instructions ?: listOf("Visit ${recipe.source} for full instructions.")
            }
        } catch (e: Exception) {
            android.util.Log.e("RecipeRepository", "Error fetching instructions: ${e.message}")
        }
        
        // Default message if no instructions are available
        return listOf("Visit ${recipe.source} for full instructions at: ${recipe.url}")
    }
    
    // Local data sources using RecipeStorage
    suspend fun saveRecipe(recipe: Recipe) {
        val entity = recipe.toSavedRecipeEntity()
        recipeStorage.insertRecipe(entity)
    }
    
    suspend fun updateFavoriteStatus(recipeId: String, isFavorite: Boolean) {
        recipeStorage.updateFavoriteStatus(recipeId, isFavorite)
    }
    
    suspend fun deleteRecipe(recipeId: String) {
        recipeStorage.deleteRecipeById(recipeId)
    }
    
    suspend fun isRecipeSaved(recipeId: String): Boolean {
        return recipeStorage.isRecipeSaved(recipeId)
    }
    
    fun getAllSavedRecipes(): Flow<List<Recipe>> {
        return recipeStorage.getAllSavedRecipes().map { entities ->
            entities.map { it.toRecipe() }
        }
    }
    
    fun getFavoriteRecipes(): Flow<List<Recipe>> {
        return recipeStorage.getFavoriteRecipes().map { entities ->
            entities.map { it.toRecipe() }
        }
    }
    
    suspend fun getSavedRecipeById(recipeId: String): Recipe? {
        return recipeStorage.getRecipeById(recipeId)?.toRecipe()
    }
}