package org.wrowclif.recipebox;

import java.util.List;

public interface Category {

	public long getId();

	public String getName();

	public void setName(String name);

	public String getDescription();

	public void setDescription(String description);

	public List<Recipe> getRecipes();

	public void addRecipe(Recipe toAdd);

	public void removeRecipe(Recipe toRemove);

	public CategoryFactory getFactory();

	public interface CategoryFactory {

		public Category create(String name);

		public void delete(String name);

	}

}
