package org.wrowclif.recipebox;

import java.util.Set;

public interface Instruction {

	public String getText();

	public Set<Ingredient> getIngredientsUsed();

}
