package com.logistics.alucard.recipesapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.logistics.alucard.recipesapp.models.Recipe;
import com.logistics.alucard.recipesapp.viewmodels.RecipeViewModel;

import static com.logistics.alucard.recipesapp.util.Constants.API_KEY;

public class RecipeActivity extends BaseActivity {

    private static final String TAG = "RecipeActivity";
    private final String RECIPE = "GET_RECIPE";

    private AppCompatImageView recipeImage;
    private TextView recipeTitle, recipeRank;
    private LinearLayout recipeIgredientsContainer;
    private ScrollView scrollView;

    private RecipeViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        recipeImage = findViewById(R.id.recipe_image);
        recipeTitle = findViewById(R.id.recipe_title);
        recipeRank = findViewById(R.id.recipe_social_score);
        recipeIgredientsContainer = findViewById(R.id.ingredients_container);
        scrollView = findViewById(R.id.parent);

        viewModel = ViewModelProviders.of(this).get(RecipeViewModel.class);

        Log.d(TAG, "**************API_KEY: " + API_KEY);

        showProgressBar(true);
        getIncomingIntent();
        subscribeObservers();

    }

    private void subscribeObservers() {
        Log.d(TAG, "subscribeObservers: STARTED");
        viewModel.getRecipe().observe(this, new Observer<Recipe>() {
            @Override
            public void onChanged(Recipe recipe) {
                if(recipe != null) {
                    if (recipe.getRecipe_id().equals(viewModel.getRecipeId())) {
                        setRecipeProperties(recipe);
                        viewModel.setRecipeRetrieved(true);
                    }
                }
            }
        });
        viewModel.isRecipeRequestTimedOut().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean && !viewModel.isRecipeRetrieved()) {
                    displayErrorScreen("Error retrieving data! Check network connection");
                }
            }
        });
    }

    private void getIncomingIntent() {
        Log.d(TAG, "getIncomingIntent: STARTED");
        if(getIntent().hasExtra(RECIPE)) {
            Recipe recipe = getIntent().getParcelableExtra(RECIPE);
            Log.d(TAG, "getIncomingIntent: " + recipe.getTitle());
            viewModel.searchRecipeById(recipe.getRecipe_id());
        }
    }

    private void setRecipeProperties(Recipe recipe) {
        if(recipe != null) {
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_launcher_background);

            Glide.with(this)
                    .setDefaultRequestOptions(requestOptions)
                    .load(recipe.getImage_url())
                    .into(recipeImage);

            recipeTitle.setText(recipe.getTitle());
            recipeRank.setText(String.valueOf(Math.round(recipe.getSocial_rank())));

            recipeIgredientsContainer.removeAllViews();
            for(String ingredient: recipe.getIngredients()) {
                TextView textView = new TextView(this);
                textView.setText(ingredient);
                textView.setTextSize(15);
                textView.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                recipeIgredientsContainer.addView(textView);
            }
        }

        showParent();
        showProgressBar(false);
    }

    private void showParent() {
        scrollView.setVisibility(View.VISIBLE);
    }

    private void displayErrorScreen(String errorMessage) {
        recipeTitle.setText("Error retrieving the recipe ... ");
        recipeRank.setText("q.q");
        TextView textView = new TextView(this);
        if(!errorMessage.equals("")) {
            textView.setText(errorMessage);
        } else {
            textView.setText("Error");
        }
        textView.setTextSize(15);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        recipeIgredientsContainer.addView(textView);

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_launcher_background);

        Glide.with(this)
                .setDefaultRequestOptions(requestOptions)
                .load(R.drawable.ic_launcher_background)
                .into(recipeImage);
        showParent();
        showProgressBar(false);
    }
}
