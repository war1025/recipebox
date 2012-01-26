package org.wrowclif.recipebox;

public interface Suggestion {

	public Recipe getOriginalRecipe();

	public Recipe getSuggestedRecipe();

	public String getComments();

	public void setComments(String text);

}
