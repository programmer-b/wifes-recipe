package com.dantech.wife.recipe.ui.screens.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dantech.wife.recipe.data.local.UserPreferences
import com.dantech.wife.recipe.data.model.CommunityComment
import com.dantech.wife.recipe.data.model.CommunityRecipe
import com.dantech.wife.recipe.data.repository.CommunityRepository
import com.dantech.wife.recipe.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CommunityViewModel(
    private val repository: CommunityRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _communityRecipes = MutableStateFlow<Resource<List<CommunityRecipe>>>(Resource.loading())
    val communityRecipes: StateFlow<Resource<List<CommunityRecipe>>> = _communityRecipes.asStateFlow()
    
    private val _selectedRecipe = MutableStateFlow<CommunityRecipe?>(null)
    val selectedRecipe: StateFlow<CommunityRecipe?> = _selectedRecipe.asStateFlow()
    
    private val _comments = MutableStateFlow<Resource<List<CommunityComment>>>(Resource.success(emptyList()))
    val comments: StateFlow<Resource<List<CommunityComment>>> = _comments.asStateFlow()
    
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()
    
    private val _userAvatar = MutableStateFlow("")
    val userAvatar: StateFlow<String> = _userAvatar.asStateFlow()
    
    init {
        loadCommunityRecipes()
        loadUserProfile()
    }
    
    fun loadCommunityRecipes() {
        viewModelScope.launch {
            _communityRecipes.value = Resource.loading()
            
            try {
                val recipes = repository.getCommunityRecipes()
                _communityRecipes.value = Resource.success(recipes)
            } catch (e: Exception) {
                _communityRecipes.value = Resource.error("Failed to load community recipes: ${e.message}")
            }
        }
    }
    
    fun selectRecipe(recipeId: String) {
        viewModelScope.launch {
            repository.getRecipeById(recipeId)?.let { recipe ->
                _selectedRecipe.value = recipe
                loadComments(recipeId)
            }
        }
    }
    
    fun toggleLike(recipeId: String) {
        viewModelScope.launch {
            repository.toggleLike(recipeId)
            loadCommunityRecipes() // Refresh the list
        }
    }
    
    fun addComment(recipeId: String, text: String) {
        viewModelScope.launch {
            repository.addComment(
                recipeId = recipeId,
                text = text,
                userName = userName.value.ifEmpty { "Anonymous" },
                userAvatarUrl = userAvatar.value.ifEmpty { "https://via.placeholder.com/50" }
            )
            loadComments(recipeId)
        }
    }
    
    private fun loadComments(recipeId: String) {
        viewModelScope.launch {
            _comments.value = Resource.loading()
            
            try {
                val commentsList = repository.getComments(recipeId)
                _comments.value = Resource.success(commentsList)
            } catch (e: Exception) {
                _comments.value = Resource.error("Failed to load comments: ${e.message}")
            }
        }
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            userPreferences.userName
                .catch { /* Handle error */ }
                .collect { name ->
                    _userName.value = name
                }
            
            _userAvatar.value = userPreferences.userAvatar.first()
        }
    }
    
    fun updateUserProfile(name: String, email: String, avatarUrl: String) {
        viewModelScope.launch {
            userPreferences.setUserProfile(name, email, avatarUrl)
            _userName.value = name
            _userAvatar.value = avatarUrl
        }
    }
}