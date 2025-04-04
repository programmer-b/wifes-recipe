package com.dantech.wife.recipe.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dantech.wife.recipe.data.model.Recipe
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap

// Extension property for DataStore
private val Context.recipeDataStore by preferencesDataStore(name = "recipe_preferences")

/**
 * RecipeStorage - A modern Kotlin implementation to replace Room DB while supporting Kotlin 2.x
 * This implementation uses DataStore for persistent storage and in-memory cache
 */
class RecipeStorage(private val context: Context) {
    private val gson = Gson()
    
    // In-memory cache
    private val recipeCache = ConcurrentHashMap<String, SavedRecipeEntity>()
    
    // DataStore keys
    private object PreferencesKeys {
        val SAVED_RECIPES = stringPreferencesKey("saved_recipes")
        val FAVORITE_RECIPES = stringPreferencesKey("favorite_recipes")
    }
    
    // Data accessors similar to Room DAO
    suspend fun insertRecipe(recipe: SavedRecipeEntity) {
        // Update cache
        recipeCache[recipe.recipeId] = recipe
        
        // Persist all recipes
        persistRecipes()
    }
    
    suspend fun updateRecipe(recipe: SavedRecipeEntity) {
        recipeCache[recipe.recipeId] = recipe
        persistRecipes()
    }
    
    suspend fun deleteRecipe(recipe: SavedRecipeEntity) {
        recipeCache.remove(recipe.recipeId)
        persistRecipes()
    }
    
    fun getAllSavedRecipes(): Flow<List<SavedRecipeEntity>> {
        return context.recipeDataStore.data.map { preferences ->
            val json = preferences[PreferencesKeys.SAVED_RECIPES] ?: "[]"
            val type = object : TypeToken<List<SavedRecipeEntity>>() {}.type
            val recipes: List<SavedRecipeEntity> = gson.fromJson(json, type)
            
            // Update cache with loaded data
            recipes.forEach { recipeCache[it.recipeId] = it }
            
            recipes.sortedByDescending { it.createdAt }
        }
    }
    
    fun getFavoriteRecipes(): Flow<List<SavedRecipeEntity>> {
        return getAllSavedRecipes().map { recipes ->
            recipes.filter { it.isFavorite }
        }
    }
    
    suspend fun getRecipeById(recipeId: String): SavedRecipeEntity? {
        // Check cache first
        return recipeCache[recipeId] ?: run {
            // If not in cache, load from preferences and return the matching recipe
            val recipes = loadRecipesFromPreferences()
            val recipe = recipes.find { it.recipeId == recipeId }
            // Update cache if found
            recipe?.let { recipeCache[recipeId] = it }
            recipe
        }
    }
    
    suspend fun isRecipeSaved(recipeId: String): Boolean {
        return getRecipeById(recipeId) != null
    }
    
    suspend fun updateFavoriteStatus(recipeId: String, isFavorite: Boolean) {
        getRecipeById(recipeId)?.let { recipe ->
            val updatedRecipe = recipe.copy(isFavorite = isFavorite)
            recipeCache[recipeId] = updatedRecipe
            persistRecipes()
        }
    }
    
    suspend fun deleteRecipeById(recipeId: String) {
        recipeCache.remove(recipeId)
        persistRecipes()
    }
    
    // Helper methods
    private suspend fun persistRecipes() {
        val recipes = recipeCache.values.toList()
        val json = gson.toJson(recipes)
        
        context.recipeDataStore.edit { preferences ->
            preferences[PreferencesKeys.SAVED_RECIPES] = json
        }
    }
    
    private suspend fun loadRecipesFromPreferences(): List<SavedRecipeEntity> {
        var json = "[]"
        context.recipeDataStore.data.collect { preferences ->
            json = preferences[PreferencesKeys.SAVED_RECIPES] ?: "[]"
            return@collect
        }
        
        val type = object : TypeToken<List<SavedRecipeEntity>>() {}.type
        return gson.fromJson(json, type)
    }
    
    companion object {
        @Volatile
        private var INSTANCE: RecipeStorage? = null
        
        fun getInstance(context: Context): RecipeStorage {
            return INSTANCE ?: synchronized(this) {
                val instance = RecipeStorage(context)
                INSTANCE = instance
                instance
            }
        }
    }
}