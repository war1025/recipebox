package org.wrowclif.recipebox.impl;

import org.wrowclif.recipebox.Ingredient;

public class IngredientImpl implements Ingredient {

	private long id;
	private String name;

	private IngredientImpl(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<Ingredient> getSimilarIngredients() {
		return null;
	}

	public void addSimilarIngredient(Ingredient other) {

	}

	public void removeSimilarIngredient(Ingredient other) {

	}
}


