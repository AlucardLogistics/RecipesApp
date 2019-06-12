package com.logistics.alucard.recipesapp.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.logistics.alucard.recipesapp.models.Recipe;
import com.logistics.alucard.recipesapp.repositories.RecipeRepository;

public class RecipeViewModel extends ViewModel {

    private RecipeRepository recipeRepository;
    private String recipeId;
    private boolean isRecipeRetrieved;

    public RecipeViewModel() {
        this.recipeRepository = RecipeRepository.getInstance();
        isRecipeRetrieved = false;
    }

    public LiveData<Recipe> getRecipe() {
        return recipeRepository.getRecipe();
    }
    public LiveData<Boolean> isRecipeRequestTimedOut() {
        return recipeRepository.isRecipeRequestTimedOut();
    }

    public void searchRecipeById(String recipeId) {
        this.recipeId = recipeId;
        recipeRepository.searchRecipeById(recipeId);
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeRetrieved(boolean recipeRetrieved) {
        isRecipeRetrieved = recipeRetrieved;
    }

    public boolean isRecipeRetrieved() {
        return isRecipeRetrieved;
    }
}
