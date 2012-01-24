package org.wrowclif.recipebox.impl;

import org.wrowclif.recipebox.Category;
import org.wrowclif.recipebox.Recipe;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CategoryImpl implements Category {

	private String name;
	private String description;
	private Set<Recipe> recipes;

	public CategoryImpl(String name) {
		this.name = name;
		this.description = null;
		this.recipes = new HashSet<Recipe>();
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<Recipe> getRecipes() {
		return Collections.unmodifiableSet(recipes);
	}

	public void addRecipe(Recipe toAdd) {
		throw new IllegalArgumentException("Recipe type not recognized");
	}

	public void addRecipe(RecipeImpl toAdd) {
		recipes.add(toAdd);
		toAdd.addCategory(this);
	}

	public void removeRecipe(Recipe toRemove) {
		throw new IllegalArgumentException("Recipe type not recognized");
	}

	public void removeRecipe(RecipeImpl toRemove) {
		recipes.remove(toRemove);
		toRemove.removeCategory(this);
	}
}
