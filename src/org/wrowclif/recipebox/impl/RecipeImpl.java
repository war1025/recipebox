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

	protected static final RecipeFactory factory;

	static {
		factory = new RecipeFactory();
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

	public List<Suggestion> getSuggestedWith() {
		return SuggestionImpl.factory.getSuggestedWith(this);
	}

	public Suggestion addSuggestion(Recipe r) {
		return SuggestionImpl.factory.addSuggestion(this, r);
	}

	public void removeSuggestion(Recipe r) {
		SuggestionImpl.factory.removeSuggestion(this, r);
	}

	public List<Recipe> getVariants() {
		List<Recipe> l1 = null;
		String stmt =
			"SELECT r.rid, r.name, r.description, r.preptime, r.cooktime, r.cost, r.vid " +
			"FROM Recipe r " +
			"WHERE r.vid = %s " +
				"and r.rid != %s; ";
		SQLiteDatabase db = factory.helper.getWriteableDatabase();
		db.beginTransaction();
			Cursor c1 = db.rawQuery(String.format(stmt, vid + "", id + ""), null);
			l1 = factory.createListFromCursor(c1);
			c1.close();
		db.endTransaction();
		return l1;
	}

	public Recipe branch(String name) {
		String stmt =
			"INSERT INTO SuggestedWith(rid1, rid2, comments) " +
				"SELECT sw.rid1, ? as rid2, sw.comments " +
				"FROM SuggestedWith sw " +
				"WHERE sw.rid2 = $; " +

			"INSERT INTO SuggestedWith(rid1, rid2, comments) " +
				"SELECT ? as rid1, sw.rid2, sw.comments " +
				"FROM SuggestedWith sw " +
				"WHERE sw.rid1 = $; " +

			"INSERT INTO RecipeCategory(cid, rid) " +
				"SELECT ? as rid, rc.cid " +
				"FROM RecipeCategory rc " +
				"WHERE rc.rid = $; " +

			"INSERT INTO RecipeIngredients(rid, iid, uid, amount, num) " +
				"SELECT ? as rid, ri.iid, ri.uid, ri.amount, ri.num " +
				"FROM RecipeIngredients ri " +
				"WHERE ri.rid = $; " +

			"INSERT INTO Instruction(rid, text, num) " +
				"SELECT ? as rid, i.text, i.num " +
				"FROM Instruction i " +
				"WHERE i.rid = $; " +

			"INSERT INTO InstructionIngredients(instrid, ingrid, num) " +
				"SELECT in1.iid, ii.ingrid, ii.num " +
				"FROM Instruction in1, Instruction in2, InstructionIngredients ii " +
				"WHERE in1.rid = ? " +
					"and in2.rid = $ " +
					"and in1.num = in2.num " +
					"and ii.instrid = in2.iid; ";

		ContentValues values = new ContentValues();
		values.put("name", name);
		values.put("description", description);
		values.put("cost", cost);
		values.put("cooktime", cookTime);
		values.put("preptime", prepTime);
		values.put("vid", vid);
		SQLiteDatabase db = factory.helper.getWriteableDatabase();
		db.beginTransaction();
			long newId = db.insert("Recipe", null, values);
			db.execSQL(stmt.replaceAll("?", newId, "$", id));
		db.endTransaction();
		values.put("rid", newId);
		return factory.createRecipeFromData(values);
	}

	public void delete() {
// Delete instructions
// Delete recipe ingredients
// Delete from categories
// Delete recipe
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

	protected static class RecipeFactory {

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

