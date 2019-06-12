package com.logistics.alucard.recipesapp.requests;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.logistics.alucard.recipesapp.AppExecutors;
import com.logistics.alucard.recipesapp.models.Recipe;
import com.logistics.alucard.recipesapp.requests.responses.RecipeResponse;
import com.logistics.alucard.recipesapp.requests.responses.RecipeSearchResponse;
import com.logistics.alucard.recipesapp.util.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

import static com.logistics.alucard.recipesapp.util.Constants.NETWORK_TIMEOUT;

public class RecipeApiClient {

    private static final String TAG = "RecipeApiClient";

    private static RecipeApiClient instance;
    private MutableLiveData<List<Recipe>> recipes;
    private MutableLiveData<Recipe> liveRecipe;
    private MutableLiveData<Boolean> recipeRequestTimedOut = new MutableLiveData<>();
    private RetrieveRecipesRunnable retrieveRecipesRunnable;
    private RetrieveRecipeRunnable retrieveRecipeRunnable;

    public static RecipeApiClient getInstance() {
        if(instance == null) {
            instance = new RecipeApiClient();
        }
        return instance;
    }

    private RecipeApiClient() {
        recipes = new MutableLiveData<>();
        liveRecipe = new MutableLiveData<>();
    }

    public LiveData<List<Recipe>> getRecipes() {
        return recipes;
    }
    public LiveData<Recipe> getRecipe() {
        return liveRecipe;
    }
    public LiveData<Boolean> isRecipeRequestTimedOut() {
        return recipeRequestTimedOut;
    }


    public void searchRecipesApi(String query, int pageNumber) {
        if(retrieveRecipesRunnable != null) {
            retrieveRecipesRunnable = null;
        }
        retrieveRecipesRunnable = new RetrieveRecipesRunnable(query, pageNumber);
        final Future handler = AppExecutors.getInstance().networkIO().submit(retrieveRecipesRunnable);

        AppExecutors.getInstance().networkIO().schedule(new Runnable() {
            @Override
            public void run() {
                //tell user is timed out
                handler.cancel(true);
            }
        }, NETWORK_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public void searchRecipeById(String recipeId) {
        if(retrieveRecipeRunnable != null) {
            retrieveRecipeRunnable = null;
        }
        retrieveRecipeRunnable = new RetrieveRecipeRunnable(recipeId);
        final Future handler = AppExecutors.getInstance().networkIO().submit(retrieveRecipeRunnable);

        recipeRequestTimedOut.setValue(false);
        AppExecutors.getInstance().networkIO().schedule(new Runnable() {
            @Override
            public void run() {
                //tell user is timed out
                recipeRequestTimedOut.postValue(true);
                handler.cancel(true);
            }
        }, NETWORK_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    private class RetrieveRecipesRunnable implements Runnable {

        private String query;
        private int pageNumber;
        boolean cancelRequest;

        public RetrieveRecipesRunnable(String query, int pageNumber) {
            this.query = query;
            this.pageNumber = pageNumber;
            cancelRequest = false;
        }

        @Override
        public void run() {
            try {
                Response response = getRecipes(query, pageNumber).execute();
                if(cancelRequest) {
                    return;
                }
                if(response.code() == 200) {
                    List<Recipe> list = new ArrayList<>(((RecipeSearchResponse)response.body()).getRecipes());
                    if(pageNumber == 1) {
                        recipes.postValue(list);
                    } else {
                        List<Recipe> currentRecipes = recipes.getValue();
                        if (currentRecipes != null) {
                            currentRecipes.addAll(list);
                        }
                        recipes.postValue(currentRecipes);
                    }
                } else {
                    String error = response.errorBody().string();
                    Log.e(TAG, "run: " + error);
                    recipes.postValue(null);
                }
            } catch (IOException e) {
                e.printStackTrace();
                recipes.postValue(null);
            }

        }

        private Call<RecipeSearchResponse> getRecipes(String query, int pageNumber) {
            return ServiceGenerator.getRecipeApi().searchRecipe(
              Constants.API_KEY,
              query,
              String.valueOf(pageNumber)
            );
        }

        private void cancelRequest() {
            Log.d(TAG, "canceRequest: canceling the request");
            cancelRequest = true;
        }
    }

    private class RetrieveRecipeRunnable implements Runnable {

        private String recipeId;
        boolean cancelRequest;

        public RetrieveRecipeRunnable(String recipeId) {
            this.recipeId = recipeId;
            cancelRequest = false;
        }

        @Override
        public void run() {
            try {
                Response response = getRecipe(recipeId).execute();
                if(cancelRequest) {
                    return;
                }
                if(response.code() == 200) {
                    Recipe recipe = (((RecipeResponse)response.body()).getRecipe());
                    liveRecipe.postValue(recipe);
                } else {
                    String error = response.errorBody().string();
                    Log.e(TAG, "run: " + error);
                    liveRecipe.postValue(null);
                }
            } catch (IOException e) {
                e.printStackTrace();
                liveRecipe.postValue(null);
            }

        }

        private Call<RecipeResponse> getRecipe(String recipeId) {
            return ServiceGenerator.getRecipeApi().getRecipe(
                    Constants.API_KEY,
                    recipeId
            );
        }

        private void cancelRequest() {
            Log.d(TAG, "canceRequest: canceling the request");
            cancelRequest = true;
        }
    }

    public void cancelRequest() {
        if(retrieveRecipesRunnable != null) {
            retrieveRecipesRunnable.cancelRequest();
        }

        if(retrieveRecipeRunnable != null) {
            retrieveRecipeRunnable.cancelRequest();
        }
    }
}
