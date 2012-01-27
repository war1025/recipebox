package org.wrowclif.recipebox;

import java.util.List;

public interface Instruction {

	public long getId();

	public String getText();

	public List<Ingredient> getIngredientsUsed();

	public void setIngredientsUsed(List<Ingredient> ingredients);

}
