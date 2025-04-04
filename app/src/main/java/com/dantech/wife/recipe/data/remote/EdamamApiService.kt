package com.dantech.wife.recipe.data.remote

import com.dantech.wife.recipe.BuildConfig
import com.dantech.wife.recipe.data.model.RecipeSearchResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface EdamamApiService {
    
    @GET("api/recipes/v2")
    suspend fun searchRecipes(
        @Query("type") type: String = "public",
        @Query("q") query: String,
        @Query("app_id") appId: String = BuildConfig.EDAMAM_APP_ID,
        @Query("app_key") appKey: String = BuildConfig.EDAMAM_APP_KEY,
        @Query("from") from: Int = 0,
        @Query("to") to: Int = 20,
        @Query("diet") diet: String? = null,
        @Query("health") health: String? = null,
        @Query("cuisineType") cuisineType: String? = null,
        @Query("mealType") mealType: String? = null,
        @Query("dishType") dishType: String? = null,
        @Query("calories") calories: String? = null,
        @Query("time") time: String? = null
    ): Response<RecipeSearchResponse>
    
    @GET("api/recipes/v2")
    suspend fun getRecipeById(
        @Query("type") type: String = "public",
        @Query("app_id") appId: String = BuildConfig.EDAMAM_APP_ID,
        @Query("app_key") appKey: String = BuildConfig.EDAMAM_APP_KEY,
        @Query("uri") uri: String
    ): Response<RecipeSearchResponse>
    
    companion object {
        fun create(): EdamamApiService {
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            
            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
                        .header("Edamam-Account-User", BuildConfig.EDAMAM_USER_ID) // Add the required user ID header
                        .method(original.method, original.body)
                        .build()
                    chain.proceed(request)
                }
                .addInterceptor(logger)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
                
            return Retrofit.Builder()
                .baseUrl(BuildConfig.EDAMAM_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(EdamamApiService::class.java)
        }
    }
}