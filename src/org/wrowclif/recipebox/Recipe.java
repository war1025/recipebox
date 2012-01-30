package org.wrowclif.recipebox;

import java.util.List;

public interface Recipe {

	public String getName();

	public void setName(String name);

	public String getDescription();

	public void setDescription(String description);

	public int getPrepTime();

	public void setPrepTime(int time);

	public int getCookTime();

	public void setCookTime(int time);

	public int getCost();

	public void setCost(int cost);

	public long getId();

	public List<RecipeIngredient> getIngredients();

	public RecipeIngredient addIngredient(Ingredient toAdd);

	public void removeIngredient(RecipeIngredient toRemove);

	public void reorderIngredients(List<RecipeIngredient> order);

	public List<Instruction> getInstructions();

	public Instruction addInstruction();

	public void removeInstruction(Instruction i);

	public void reorderInstructions(List<Instruction> order);

	public List<Review> getReviews();

	public Review addReview();

	public void removeReview(Review r);

	public List<Category> getCategories();

	public void addCategory(Category c);

	public void removeCategory(Category c);

	public List<Suggestion> getSuggestedWith();

	public Suggestion addSuggestion(Recipe r);

	public void removeSuggestion(Recipe r);

	public List<Recipe> getVariants();

	public Recipe branch(String name);

	public void delete();

}
