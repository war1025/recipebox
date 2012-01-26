package org.wrowclif.recipebox.impl;

import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.Ingredient;
import org.wrowclif.recipebox.Instruction;
import org.wrowclif.recipebox.Category;
import org.wrowclif.recipebox.Review;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public class RecipeImpl implements Recipe {

	protected static final RecipeFactoryImpl factory;

	static {
		factory = new RecipeFactoryImpl();
	}

	private long id;
	private String name;
	private String description;
	private int cost;
	private int prepTime;
	private int cookTime;
	private long vid;

	private RecipleImpl(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		ContentValues values = new ContentValues();
		values.put("name", name);
		itemUpdate(values, "setName");
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
		ContentValues values = new ContentValues();
		values.put("description", description);
		itemUpdate(values, "setDescription");
	}

	public int getPrepTime() {
		return prepTime;
	}

	public void setPrepTime(int time) {
		this.prepTime = time;
		ContentValues values = new ContentValues();
		values.put("preptime", time);
		itemUpdate(values, "setPrepTime");
	}

	public int getCookTime() {
		return cookTime;
	}

	public void setCookTime(int time) {
		this.cookTime = time;
		ContentValues values = new ContentValues();
		values.put("cooktime", time);
		itemUpdate(values, "setCookTime");
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
		ContentValues values = new ContentValues();
		values.put("cost", cost);
		itemUpdate(values, "setCost");
	}

	public long getId() {
		return id;
	}

	public List<RecipeIngredient> getIngredients() {
		return RecipeIngredientImpl.factory.getRecipeIngredients(id);
	}

	public RecipeIngredient addIngredient(Ingredient i) {
		return RecipeIngredientImpl.factory.addRecipeIngredient(id);
	}

	public void removeIngredient(RecipeIngredient toRemove) {
		RecipeIngredientImpl.factory.removeRecipeIngredient(toRemove);
	}

	public void reorderIngredients(List<RecipeIngredient> order) {
		RecipeIngredientImpl.factory.reorderRecipeIngredients(order);
	}

	public List<Instruction> getSteps() {
		return InstructionImpl.factory.getRecipeSteps(id);
	}

	public Instruction addStep() {
		return InstructionImpl.factory.addInstruction(id);
	}

	public void removeStep(Instruction i) {
		InstructionImpl.factory.removeInstruction(i);
	}

	public void reorderSteps(List<Instruction> order) {
		InstructionImpl.factory.reorderInstructions(order);
	}

	public List<Review> getReviews() {
		return ReviewImpl.factory.getReviews(id);
	}

	public Review addReview() {
		return ReviewImpl.factory.addReview(id);
	}

	public void removeReview(Review r) {
		ReviewImpl.factory.removeReview(r);
	}

	public List<Category> getCategories() {
		CategoryImpl.factory.getRecipeCategories(id);
	}

	public void addCategory(Category c) {
		CategoryImpl.factory.addRecipeToCategory(id, c);
	}

	public void removeCategory(Category c) {
		CategoryImpl.factory.removeRecipeFromCategory(id, c);
	}

	public List<Recipe> getSuggestedWith() {
		List<Recipe> l1 = null;
		String stmt =
			"SELECT r.id, r.name, r.description, r.preptime, r.cooktime, r.cost, r.vid " +
			"FROM Recipe r, SuggestedWith sw " +
			"WHERE sw.rid1 = %s " +
				"and sw.rid2 = %s ";
		SQLiteDatabase db = factory.helper.getWriteableDatabase();
		db.beginTransaction();
			Cursor c1 = db.rawQuery(String.format(stmt, "r.id", id + ""), null);
			Cursor c2 = db.rawQuery(String.format(stmt, id + "", "r.id"), null);
			l1 = factory.createListFromCursor(c1);
			l1.addAll(factory.createListFromCursor(c2));
			c1.close();
			c2.close();
		db.endTransaction();
		return l1;
	}

	public void addSuggestion(Recipe r) {
// Database update here
	}

	public void removeSuggestion(Recipe r) {
// Database update here
	}

	public List<Recipe> getVariants() {
// Database query here
		return null;
	}

	public void delete() {
// Database update here
	}

	private void itemUpdate(ContentValues values, String operation) {
		SQLiteDatabase db = factory.helper.getWriteableDatabase();
		int ret = db.update("Recipe", values, "rid=?", new String[] {id + ""});
		if(ret != 1) {
			throw new IllegalStateException("Recipe " + operation + " should have affected only row " + id +
												" but affected " + ret + " rows");
		}
	}

	public RecipeFactory getFactory() {
		return RecipeFactoryImpl.factory;
	}

	protected static class RecipeFactoryImpl implements RecipeFactory {

		protected RecipeBoxOpenHelper helper;

		private RecipeFactoryImpl() {
			helper = AppData.singleton().getOpenHelper();
		}

		public Recipe create(String name) {
// Database insert here
			return null;
		}

		public Recipe branch(String name, Recipe toBranch) {
// Database update here
			return null;
		}

	}
}

