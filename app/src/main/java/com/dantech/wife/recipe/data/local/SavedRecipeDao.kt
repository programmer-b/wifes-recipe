package com.dantech.wife.recipe.data.local

import kotlinx.coroutines.flow.Flow

/**
 * SavedRecipeDao - Interface for SavedRecipe data access
 * Kept as a reference for the API contract our RecipeStorage implements
 * Modified to not depend on Room annotations
 */
interface SavedRecipeDao {
    suspend fun insertRecipe(recipe: SavedRecipeEntity)
    
    suspend fun updateRecipe(recipe: SavedRecipeEntity)
    
    suspend fun deleteRecipe(recipe: SavedRecipeEntity)
    
    fun getAllSavedRecipes(): Flow<List<SavedRecipeEntity>>
    
    fun getFavoriteRecipes(): Flow<List<SavedRecipeEntity>>
    
    suspend fun getRecipeById(recipeId: String): SavedRecipeEntity?
    
    suspend fun isRecipeSaved(recipeId: String): Boolean
    
    suspend fun updateFavoriteStatus(recipeId: String, isFavorite: Boolean)
    
    suspend fun deleteRecipeById(recipeId: String)
}