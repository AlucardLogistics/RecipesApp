package com.logistics.alucard.recipesapp.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.logistics.alucard.recipesapp.models.Recipe;
import com.logistics.alucard.recipesapp.repositories.RecipeRepository;

import java.util.List;

public class RecipeListViewModel extends ViewModel {

    private RecipeRepository recipeRepository;
    private boolean isViewingRecipes;
    private boolean isPerformingQuery;

    public RecipeListViewModel() {
        recipeRepository = RecipeRepository.getInstance();
        isPerformingQuery = false;
    }

    public LiveData<List<Recipe>> getRecipes() {
        return recipeRepository.getRecipes();
    }
    public LiveData<Boolean> isQueryExhausted() {
        return recipeRepository.getIsQueryExhausted();
    }

    public void searchRecipesApi(String query, int pageNumber) {
        isViewingRecipes = true;
        isPerformingQuery = true;
        recipeRepository.searchRecipesApi(query, pageNumber);
    }

    public boolean isViewingRecipes() {
        return isViewingRecipes;
    }

    public void setIsViewingRecipes(boolean isViewingRecipes) {
        this.isViewingRecipes = isViewingRecipes;
    }

    public void setIsPerformingQuery(Boolean isPerformingQuery) {
        this.isPerformingQuery = isPerformingQuery;
    }

    public boolean isPerfromingQuery() {
        return isPerformingQuery;
    }

    public void searchNextPage() {
        if(!isPerformingQuery
                && isViewingRecipes
                && !isQueryExhausted().getValue()) {
            recipeRepository.searchNextPage();
        }
    }

    public boolean onBackPressed() {
        if(isPerformingQuery) {
            recipeRepository.cancelRequest();
            isPerformingQuery = false;
        }

        if(isViewingRecipes) {
            isViewingRecipes = false;
            return false;
        }
        return true;
    }
}
