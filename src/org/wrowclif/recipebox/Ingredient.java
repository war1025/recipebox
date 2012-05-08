package org.wrowclif.recipebox;

import java.util.List;

public interface Ingredient {

	public long getId();

	public String getName();

	public List<Ingredient> getSimilarIngredients();

	public void addSimilarIngredient(Ingredient other);

	public void removeSimilarIngredient(Ingredient other);

	public boolean delete();

}
