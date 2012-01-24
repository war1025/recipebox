package org.wrowclif.recipebox;

import java.util.Set;

public interface Category {

	public String getName();

	public String getDescription();

	public Set<Recipe> getRecipes();

	public void addRecipe(Recipe toAdd);

	public void removeRecipe(Recipe toRemove);

}
