package com.dantech.wife.recipe.data.repository

import com.dantech.wife.recipe.data.model.CommunityComment
import com.dantech.wife.recipe.data.model.CommunityRecipe
import com.dantech.wife.recipe.data.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * This is a mock implementation of CommunityRepository.
 * In a real app, this would interact with a backend service.
 */
class CommunityRepository {
    private val _communityRecipes = MutableStateFlow<List<CommunityRecipe>>(generateMockRecipes())
    val communityRecipes: Flow<List<CommunityRecipe>> = _communityRecipes.asStateFlow()
    
    private val _comments = MutableStateFlow<Map<String, List<CommunityComment>>>(emptyMap())
    
    suspend fun getCommunityRecipes(): List<CommunityRecipe> {
        // In a real app, this would make an API call
        return _communityRecipes.value
    }
    
    suspend fun getRecipeById(recipeId: String): CommunityRecipe? {
        return _communityRecipes.value.find { it.id == recipeId }
    }
    
    suspend fun addRecipe(recipe: CommunityRecipe) {
        _communityRecipes.update { currentList ->
            currentList + recipe
        }
    }
    
    suspend fun toggleLike(recipeId: String) {
        _communityRecipes.update { currentList ->
            currentList.map { 
                if (it.id == recipeId) {
                    it.copy(
                        isLiked = !it.isLiked,
                        likesCount = if (it.isLiked) it.likesCount - 1 else it.likesCount + 1
                    )
                } else {
                    it
                }
            }
        }
    }
    
    suspend fun getComments(recipeId: String): List<CommunityComment> {
        return _comments.value[recipeId] ?: emptyList()
    }
    
    suspend fun addComment(recipeId: String, text: String, userName: String, userAvatarUrl: String) {
        val newComment = CommunityComment(
            id = UUID.randomUUID().toString(),
            recipeId = recipeId,
            userId = "current-user", // This would come from auth in a real app
            userName = userName,
            userAvatarUrl = userAvatarUrl,
            text = text,
            createdAt = System.currentTimeMillis()
        )
        
        _comments.update { currentMap ->
            val currentComments = currentMap[recipeId] ?: emptyList()
            currentMap + (recipeId to (currentComments + newComment))
        }
        
        // Update comment count on the recipe
        _communityRecipes.update { currentList ->
            currentList.map {
                if (it.id == recipeId) {
                    it.copy(commentsCount = it.commentsCount + 1)
                } else {
                    it
                }
            }
        }
    }
    
    suspend fun convertRecipeToCommunity(recipe: Recipe, userName: String, userAvatarUrl: String): CommunityRecipe {
        return CommunityRecipe(
            id = UUID.randomUUID().toString(),
            title = recipe.label,
            description = recipe.healthLabels.joinToString(", "),
            imageUrl = recipe.image,
            authorId = "current-user", // This would come from auth in a real app
            authorName = userName,
            authorAvatarUrl = userAvatarUrl,
            ingredients = recipe.ingredientLines,
            instructions = recipe.instructionLines.ifEmpty { 
                listOf("Visit original source for instructions: ${recipe.url}") 
            },
            preparationTime = recipe.totalTime.toInt() / 2,
            cookingTime = recipe.totalTime.toInt() / 2,
            servings = recipe.yield.toInt(),
            difficulty = if (recipe.totalTime < 30) "Easy" else if (recipe.totalTime < 60) "Medium" else "Hard",
            tags = recipe.dietLabels + recipe.healthLabels,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
    
    private fun generateMockRecipes(): List<CommunityRecipe> {
        return listOf(
            CommunityRecipe(
                id = "1",
                title = "Homemade Pizza",
                description = "Classic homemade pizza with fresh ingredients",
                imageUrl = "https://via.placeholder.com/300",
                authorId = "user1",
                authorName = "Chef John",
                authorAvatarUrl = "https://via.placeholder.com/50",
                ingredients = listOf(
                    "2 1/2 cups flour",
                    "1 tsp salt",
                    "1 tsp sugar",
                    "1 tbsp olive oil",
                    "1 cup warm water",
                    "2 1/4 tsp yeast",
                    "1/2 cup tomato sauce",
                    "2 cups mozzarella cheese",
                    "Toppings of choice"
                ),
                instructions = listOf(
                    "In a bowl, mix flour, salt, and sugar.",
                    "In another bowl, mix warm water, yeast, and olive oil.",
                    "Combine wet and dry ingredients and knead until smooth.",
                    "Let dough rise for 1 hour.",
                    "Roll out dough, add sauce, cheese, and toppings.",
                    "Bake at 475°F for 10-12 minutes."
                ),
                preparationTime = 60,
                cookingTime = 12,
                servings = 4,
                difficulty = "Medium",
                tags = listOf("Italian", "Dinner", "Comfort Food"),
                likesCount = 42,
                commentsCount = 7,
                createdAt = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000,
                updatedAt = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
            ),
            CommunityRecipe(
                id = "2",
                title = "Chocolate Chip Cookies",
                description = "Soft and chewy chocolate chip cookies",
                imageUrl = "https://via.placeholder.com/300",
                authorId = "user2",
                authorName = "Baker Betty",
                authorAvatarUrl = "https://via.placeholder.com/50",
                ingredients = listOf(
                    "2 1/4 cups flour",
                    "1 tsp baking soda",
                    "1 tsp salt",
                    "1 cup butter, softened",
                    "3/4 cup sugar",
                    "3/4 cup brown sugar",
                    "2 eggs",
                    "2 tsp vanilla extract",
                    "2 cups chocolate chips"
                ),
                instructions = listOf(
                    "Preheat oven to 375°F.",
                    "In a small bowl, mix flour, baking soda, and salt.",
                    "In a large bowl, cream butter and sugars until light and fluffy.",
                    "Beat in eggs and vanilla.",
                    "Gradually add flour mixture.",
                    "Stir in chocolate chips.",
                    "Drop by rounded tablespoons onto ungreased baking sheets.",
                    "Bake for 9-11 minutes until golden brown."
                ),
                preparationTime = 15,
                cookingTime = 10,
                servings = 24,
                difficulty = "Easy",
                tags = listOf("Dessert", "Baking", "Sweet"),
                likesCount = 78,
                commentsCount = 15,
                createdAt = System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000,
                updatedAt = System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000
            )
        )
    }
}