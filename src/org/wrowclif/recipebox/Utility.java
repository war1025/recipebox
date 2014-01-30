package org.wrowclif.recipebox;

import java.util.List;

public interface Utility {

   public Recipe newRecipe(String name);

   public List<Recipe> searchRecipes(String search, int maxResults);

   public List<Recipe> getRecentlyViewedRecipes(int offset, int maxResults);

   public List<Recipe> getRecipesByName(int offset, int maxResults);

   public List<Recipe> getRecipesForIds(List<Long> recipeIds);

   public Recipe getRecipeById(long id);

   public Ingredient getIngredientByName(String name);

   public Ingredient createOrRetrieveIngredient(String name);

   public List<Ingredient> searchIngredients(String search, int maxResults);

   public List<Category> searchCategories(String search, int maxResults);

   public List<Category> getCategoriesByName(int offset, int maxResults);

   public Category getCategoryByName(String category);

   public Category createOrRetrieveCategory(String category);

   public Category getCategoryById(long id);

}
