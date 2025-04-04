package com.dantech.wife.recipe.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RecipeSearchResponse(
    val hits: List<RecipeHit> = emptyList(),
    val count: Int = 0,
    val from: Int = 0,
    val to: Int = 0,
    val more: Boolean = false,
    val nextPage: String? = null
)

@Serializable
data class RecipeHit(
    val recipe: Recipe
)

@Serializable
data class Recipe(
    val uri: String = "",
    val label: String = "",
    val image: String = "",
    val source: String = "",
    val url: String = "",
    val yield: Double = 0.0,
    val dietLabels: List<String> = emptyList(),
    val healthLabels: List<String> = emptyList(),
    val cautions: List<String> = emptyList(),
    val ingredientLines: List<String> = emptyList(),
    val ingredients: List<Ingredient> = emptyList(),
    val calories: Double = 0.0,
    val totalTime: Double = 0.0,
    val cuisineType: List<String> = emptyList(),
    val mealType: List<String> = emptyList(),
    val dishType: List<String> = emptyList(),
    val totalNutrients: Map<String, Nutrient> = emptyMap(),
    val instructionLines: List<String> = emptyList(),
    val isFavorite: Boolean = false
) {
    val recipeId: String
        get() = uri.split("#").last()
}

@Serializable
data class Ingredient(
    val text: String = "",
    val quantity: Double = 0.0,
    val measure: String? = null,
    val food: String = "",
    val weight: Double = 0.0,
    val foodCategory: String? = null,
    val image: String? = null
)

@Serializable
data class Nutrient(
    val label: String = "",
    val quantity: Double = 0.0,
    val unit: String = ""
)