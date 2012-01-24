package org.wrowclif.recipebox.impl;

import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.Ingredient;
import org.wrowclif.recipebox.Instruction;
import org.wrowclif.recipebox.Category;
import org.wrowclif.recipebox.Review;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class RecipeImpl implements Recipe {

	private String name;

	private Set<Ingredient> ingredients;
	private List<Instruction> instructions;
	private Set<Review> reviews;
	private Set<Category> categories;

	public RecipeImpl(String name) {
		this.name = name;
		this.ingredients = new HashSet<Ingredient>();
		this.instructions = new ArrayList<Instruction>();
		this.reviews = new HashSet<Review>();
		this.categories = new HashSet<Category>();
	}

	public Set<Ingredient> getIngredients() {
		return Collections.unmodifiableSet(ingredients);
	}

	public List<Instruction> getSteps() {
		return Collections.unmodifiableList(instructions);
	}

	public Set<Review> getReviews() {
		return Collections.unmodifiableSet(reviews);
	}

	public void addReview(Review r) {
		reviews.add(r);
	}

	public Set<Category> getCategories() {
		return Collections.unmodifiableSet(categories);
	}

	public void addCategory(Category c) {
		categories.add(c);
	}

	public void removeCategory(Category c) {
		categories.remove(c);
	}
}
