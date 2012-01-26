package org.wrowclif.recipebox.impl;

import org.wrowclif.recipebox.Suggestion;

public class SuggestionImpl implements Suggestion {

	public Recipe getOriginalRecipe();

	public Recipe getSuggestedRecipe();

	public String getComments();

	public void setComments(String text);

}
