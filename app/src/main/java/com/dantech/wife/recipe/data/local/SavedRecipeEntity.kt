package com.dantech.wife.recipe.data.local

import com.dantech.wife.recipe.data.model.Recipe
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * SavedRecipeEntity - Entity representing a saved recipe
 * Modified to not depend on Room annotations while maintaining compatibility
 */
data class SavedRecipeEntity(
    val recipeId: String,
    val recipeData: String, // JSON representation of Recipe
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * RecipeConverters - Utility class for converting between Recipe and JSON
 */
class RecipeConverters {
    private val gson = Gson()
    
    fun fromRecipe(recipe: Recipe): String {
        return gson.toJson(recipe)
    }
    
    fun toRecipe(json: String): Recipe {
        val type = object : TypeToken<Recipe>() {}.type
        return gson.fromJson(json, type)
    }
    
    fun fromStringList(list: List<String>): String {
        return gson.toJson(list)
    }
    
    fun toStringList(json: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }
}

/**
 * Extension functions for converting between SavedRecipeEntity and Recipe
 */
fun SavedRecipeEntity.toRecipe(): Recipe {
    return RecipeConverters().toRecipe(recipeData)
}

fun Recipe.toSavedRecipeEntity(isFavorite: Boolean = false): SavedRecipeEntity {
    return SavedRecipeEntity(
        recipeId = recipeId,
        recipeData = RecipeConverters().fromRecipe(this),
        isFavorite = isFavorite
    )
}