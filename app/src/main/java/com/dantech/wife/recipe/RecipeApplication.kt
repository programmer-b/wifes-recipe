package com.dantech.wife.recipe

import android.app.Application
import com.dantech.wife.recipe.data.local.RecipeStorage
import com.dantech.wife.recipe.data.local.UserPreferences
import com.dantech.wife.recipe.data.remote.EdamamApiService
import com.dantech.wife.recipe.data.repository.CommunityRepository
import com.dantech.wife.recipe.data.repository.RecipeRepository

class RecipeApplication : Application() {
    
    // Lazy initialization of dependencies
    val recipeStorage by lazy { RecipeStorage.getInstance(this) }
    val apiService by lazy { EdamamApiService.create() }
    val userPreferences by lazy { UserPreferences(this) }
    
    // Repositories
    val recipeRepository by lazy { 
        RecipeRepository(apiService, recipeStorage)
    }
    val communityRepository by lazy { CommunityRepository() }
    
    override fun onCreate() {
        super.onCreate()
        // Initialize global components if needed
    }
}