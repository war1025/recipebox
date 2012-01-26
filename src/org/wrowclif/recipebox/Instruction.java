package org.wrowclif.recipebox;

import java.util.List;

public interface Instruction {

	public long getId();

	public String getText();

	public List<Ingredient> getIngredientsUsed();

	public void addIngredient(Ingredient toAdd);

	public void removeIngredient(Ingredient toRemove);

	public void reorderIngredients(List<Ingredient> order);

}
