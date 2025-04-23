package com.dantech.wife.recipe.ui.screens.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dantech.wife.recipe.data.local.UserPreferences
import com.dantech.wife.recipe.data.model.CommunityComment
import com.dantech.wife.recipe.data.model.CommunityRecipe
import com.dantech.wife.recipe.data.repository.CommunityRepository
import com.dantech.wife.recipe.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
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
    
    // Control for auto-refresh
    private val _autoRefreshEnabled = MutableStateFlow(true)
    val autoRefreshEnabled: StateFlow<Boolean> = _autoRefreshEnabled.asStateFlow()
    
    // Refresh state for UI
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    // Flag for new comments availability
    private val _hasNewComments = MutableStateFlow(false)
    val hasNewComments: StateFlow<Boolean> = _hasNewComments.asStateFlow()
    
    private var autoRefreshJob: Job? = null
    
    init {
        loadCommunityRecipes()
        loadUserProfile()
        startAutoRefresh()
    }
    
    fun loadCommunityRecipes(silent: Boolean = false) {
        viewModelScope.launch {
            // Only show loading state when not in silent mode
            if (!silent) {
                _communityRecipes.value = Resource.loading()
            }
            
            try {
                val recipes = repository.getCommunityRecipes()
                
                // If we already have data, compare to see if there are changes
                val current = _communityRecipes.value
                if (current is Resource.Success) {
                    val currentRecipes = current.data
                    
                    // Check if there are new comments by comparing comment counts
                    val hasNewComments = recipes.any { newRecipe ->
                        val oldRecipe = currentRecipes.find { it.id == newRecipe.id }
                        oldRecipe != null && newRecipe.commentsCount > oldRecipe.commentsCount
                    }
                    
                    if (hasNewComments) {
                        _hasNewComments.value = true
                    }
                }
                
                _communityRecipes.value = Resource.success(recipes)
            } catch (e: Exception) {
                if (!silent) {
                    _communityRecipes.value = Resource.error("Failed to load community recipes: ${e.message}")
                }
            } finally {
                _isRefreshing.value = false
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
    
    private fun loadComments(recipeId: String, silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _comments.value = Resource.loading()
            }
            
            try {
                val commentsList = repository.getComments(recipeId)
                
                // If we already have comments, check for new ones
                val current = _comments.value
                if (current is Resource.Success && current.data.isNotEmpty()) {
                    if (commentsList.size > current.data.size) {
                        // New comments available
                        _hasNewComments.value = true
                    }
                }
                
                _comments.value = Resource.success(commentsList)
                
                // Reset the new comments flag when comments are explicitly loaded (not by auto-refresh)
                if (!silent) {
                    _hasNewComments.value = false
                }
            } catch (e: Exception) {
                if (!silent) {
                    _comments.value = Resource.error("Failed to load comments: ${e.message}")
                }
            }
        }
    }
    
    fun startAutoRefresh() {
        if (autoRefreshJob != null) return
        
        autoRefreshJob = viewModelScope.launch {
            while (isActive && autoRefreshEnabled.value) {
                delay(15000) // Refresh every 15 seconds
                
                // Silent refresh (doesn't show loading indicators)
                _isRefreshing.value = true
                loadCommunityRecipes(silent = true)
                
                // If a recipe is selected, also refresh its comments
                val currentRecipe = _selectedRecipe.value
                if (currentRecipe != null) {
                    loadComments(currentRecipe.id, silent = true)
                }
                
                _isRefreshing.value = false
            }
        }
    }
    
    fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
        _autoRefreshEnabled.value = false
    }
    
    fun toggleAutoRefresh() {
        if (_autoRefreshEnabled.value) {
            stopAutoRefresh()
        } else {
            _autoRefreshEnabled.value = true
            startAutoRefresh()
        }
    }
    
    // Manual refresh (with loading indicators)
    fun refreshContent() {
        _hasNewComments.value = false
        _isRefreshing.value = true
        loadCommunityRecipes()
        val currentRecipe = _selectedRecipe.value
        if (currentRecipe != null) {
            loadComments(currentRecipe.id)
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
    
    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
    }
}