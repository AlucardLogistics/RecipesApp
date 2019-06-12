package com.logistics.alucard.recipesapp.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.logistics.alucard.recipesapp.models.Recipe;
import com.logistics.alucard.recipesapp.requests.RecipeApiClient;

import java.util.List;

public class RecipeRepository {

    private static RecipeRepository instance;
    private RecipeApiClient recipeApiClient;
    private String query;
    private int pageNumber;
    private MutableLiveData<Boolean> isQueryExhausted = new MutableLiveData<>();
    private MediatorLiveData<List<Recipe>> mediatorRecipes = new MediatorLiveData<>();

    public static RecipeRepository getInstance() {
        if(instance == null) {
            instance = new RecipeRepository();
        }
        return instance;
    }

    private RecipeRepository() {
        recipeApiClient = RecipeApiClient.getInstance();
        initMediators();
    }

    private void initMediators() {
        LiveData<List<Recipe>> recipeListApiSource = recipeApiClient.getRecipes();
        mediatorRecipes.addSource(recipeListApiSource, new Observer<List<Recipe>>() {
            @Override
            public void onChanged(List<Recipe> recipes) {
                if(recipes != null) {
                    mediatorRecipes.setValue(recipes);
                    doneQuery(recipes);
                } else {
                    //search db cache
                    doneQuery(null);
                }
            }
        });
    }

    public MutableLiveData<Boolean> getIsQueryExhausted() {
        return isQueryExhausted;
    }

    public void doneQuery(List<Recipe> list) {
        if(list != null) {
            if(list.size() % 30 != 0) {
                isQueryExhausted.setValue(true);
            }
        } else {
            isQueryExhausted.setValue(true);
        }
    }

    public LiveData<List<Recipe>> getRecipes() {
        return mediatorRecipes;
    }
    public LiveData<Recipe> getRecipe() {
        return recipeApiClient.getRecipe();
    }
    public LiveData<Boolean> isRecipeRequestTimedOut() {
        return recipeApiClient.isRecipeRequestTimedOut();
    }

    public void searchRecipesApi(String query, int pageNumber) {
        if(pageNumber == 0) {
            pageNumber = 1;
        }
        this.query = query;
        this.pageNumber = pageNumber;
        isQueryExhausted.setValue(false);
        recipeApiClient.searchRecipesApi(query, pageNumber);
    }

    public void searchRecipeById(String recipeId) {
        recipeApiClient.searchRecipeById(recipeId);
    }

    public void searchNextPage() {
        recipeApiClient.searchRecipesApi(query, pageNumber + 1);
    }

    public void cancelRequest() {
        recipeApiClient.cancelRequest();
    }
}
