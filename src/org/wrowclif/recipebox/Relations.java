package org.wrowclif.recipebox;

import java.util.Set;

public interface Relations {

	public Set<Recipe> getVariants(Recipe r);

	public void addVariant(Recipe r1, Recipe r2);

	public void removeVariant(Recipe r1, Recipe r2);

	public Set<Recipe> getSuggestions(Recipe r);

	public void addSuggestion(Recipe r1, Recipe r2);

	public void removeSuggestion(Recipe r1, Recipe r2);

}




