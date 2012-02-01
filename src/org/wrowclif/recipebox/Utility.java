package org.wrowclif.recipebox;

import java.util.List;

public interface Utility {

	public Recipe newRecipe(String name);

	public List<Recipe> searchRecipes(String search, int maxResults);

	public Recipe getRecipeById(long id);

	public Ingredient createOrRetrieveIngredient(String name);

	public List<Ingredient> searchIngredients(String search, int maxResults);

	public List<Unit> searchUnits(String search, int maxResults);

	public List<Category> searchCategories(String search, int maxResults);

	public Category createOrRetrieveCategory(String category);

}
