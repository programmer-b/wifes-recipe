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
        return apiService.getRecipeById(uri = uri)
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