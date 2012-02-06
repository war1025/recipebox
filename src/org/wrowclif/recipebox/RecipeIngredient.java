package org.wrowclif.recipebox;

public interface RecipeIngredient {

	public String getName();

	public String getAmount();

	public void setAmount(String amount);

	public Recipe getRecipe();

	public Ingredient getIngredient();

	public boolean setIngredient(Ingredient in);
}
