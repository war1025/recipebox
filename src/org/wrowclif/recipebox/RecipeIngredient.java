package org.wrowclif.recipebox;

public interface RecipeIngredient {

	public String getName();

	public String getAmount();

	public void setAmount(String amound);

	public Recipe getRecipe();

	public Ingredient getIngredient();
}
