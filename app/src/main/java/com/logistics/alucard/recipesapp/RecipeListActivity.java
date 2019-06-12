package com.logistics.alucard.recipesapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.logistics.alucard.recipesapp.adapters.OnRecipeListener;
import com.logistics.alucard.recipesapp.adapters.RecipeRecyclerAdapter;
import com.logistics.alucard.recipesapp.models.Recipe;
import com.logistics.alucard.recipesapp.util.Testing;
import com.logistics.alucard.recipesapp.util.VerticalSpacingItemDecorator;
import com.logistics.alucard.recipesapp.viewmodels.RecipeListViewModel;

import java.util.List;

import static com.logistics.alucard.recipesapp.util.Constants.API_KEY;
import static com.logistics.alucard.recipesapp.util.Constants.API_KEY_ONE;
import static com.logistics.alucard.recipesapp.util.Constants.API_KEY_TWO;


public class RecipeListActivity extends BaseActivity implements OnRecipeListener {

    private static final String TAG = "RecipeListActivity";
    private final String RECIPE = "GET_RECIPE";

    private RecipeListViewModel recipeListViewModel;
    private RecyclerView recyclerView;
    private RecipeRecyclerAdapter adapter;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: STARTED");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);
        initNetworkCall();

        recyclerView = findViewById(R.id.recipe_list);
        searchView = findViewById(R.id.search_view);

        recipeListViewModel = ViewModelProviders.of(this)
                .get(RecipeListViewModel.class);

        initRecyclerView();
        subscribeObservers();
        initSearchView();
        if(!recipeListViewModel.isViewingRecipes()) {
            displaySearchCategories();
        }
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        Log.d(TAG, "**************API_KEY: " + API_KEY);
    }

    private void subscribeObservers() {
        recipeListViewModel.getRecipes().observe(this, new Observer<List<Recipe>>() {
            @Override
            public void onChanged(List<Recipe> recipes) {
                if(recipes != null) {
                    if(recipeListViewModel.isViewingRecipes()) {
                        Testing.printRecipes(recipes, TAG);
                        recipeListViewModel.setIsPerformingQuery(false);
                        adapter.setRecipes(recipes);
                    }
                } else {
                    Log.d(TAG, "onChanged: CRASH !!!!!!!!!!");
                }
            }
        });
        recipeListViewModel.isQueryExhausted().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean) {
                    Log.d(TAG, "onChanged: ----- Query is Exhausted ----");
                    adapter.setQueryExhausted();
                }
            }
        });
    }

    private void initNetworkCall() {
        API_KEY = API_KEY_ONE;
    }

    private void initRecyclerView() {
        adapter = new RecipeRecyclerAdapter(this,this);
        recyclerView.setLayoutManager(new LinearLayoutManager( this));
        VerticalSpacingItemDecorator verticalSpacingItemDecorator = new VerticalSpacingItemDecorator(30);
        recyclerView.addItemDecoration(verticalSpacingItemDecorator);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if(recyclerView.canScrollVertically(1)) {
                    recipeListViewModel.searchNextPage();
                }
            }
        });
    }


    private void initSearchView() {
        searchView.onActionViewExpanded();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.displayLoading();
                recipeListViewModel.searchRecipesApi(query, 1);
                searchView.clearFocus();
                recyclerView.requestFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public void onRecipeClick(int position) {
        Intent intent = new Intent(this, RecipeActivity.class);
        intent.putExtra(RECIPE, adapter.getSelectedRecipe(position));
        startActivity(intent);
    }

    @Override
    public void onCategoryClick(String category) {
        adapter.displayLoading();
        recipeListViewModel.searchRecipesApi(category, 1);
        searchView.clearFocus();
        recyclerView.requestFocus();
    }

    private void displaySearchCategories() {
        recipeListViewModel.setIsViewingRecipes(false);
        adapter.displaySearchCategories();
    }

    @Override
    public void onBackPressed() {
        if(recipeListViewModel.onBackPressed()) {
            super.onBackPressed();
        } else {
            displaySearchCategories();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recipe_search_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_categories) {
            displaySearchCategories();
        }
        if(item.getItemId() == R.id.action_api_one) {
            useFirstAPI();
        }
        if(item.getItemId() == R.id.action_api_two) {
            useSecondAPI();
        }
        return super.onOptionsItemSelected(item);
    }

    private void useFirstAPI() {
        API_KEY = API_KEY_ONE;
        Intent intent = new Intent(this, RecipeListActivity.class);
        startActivity(intent);
        finish();
    }

    private void useSecondAPI() {
        API_KEY = API_KEY_TWO;
        Intent intent = new Intent(this, RecipeListActivity.class);
        startActivity(intent);
        finish();
    }
}
