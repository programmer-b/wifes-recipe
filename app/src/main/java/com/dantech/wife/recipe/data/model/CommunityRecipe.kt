package com.dantech.wife.recipe.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CommunityRecipe(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val preparationTime: Int = 0, // in minutes
    val cookingTime: Int = 0, // in minutes
    val servings: Int = 0,
    val difficulty: String = "",
    val tags: List<String> = emptyList(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val isLiked: Boolean = false
)

@Serializable
data class CommunityComment(
    val id: String = "",
    val recipeId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String = "",
    val text: String = "",
    val createdAt: Long = 0
)