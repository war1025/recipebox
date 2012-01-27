package org.wrowclif.recipebox.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.RecipeIngredient;
import org.wrowclif.recipebox.Ingredient;
import org.wrowclif.recipebox.Instruction;
import org.wrowclif.recipebox.Category;
import org.wrowclif.recipebox.Review;
import org.wrowclif.recipebox.Unit;
import org.wrowclif.recipebox.Suggestion;
import org.wrowclif.recipebox.db.RecipeBoxOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class RecipeImpl implements Recipe {

	protected static final RecipeFactory factory;

	static {
		factory = new RecipeFactory();
	}

	protected long id;
	protected String name;
	protected String description;
	protected int cost;
	protected int prepTime;
	protected int cookTime;
	protected long vid;

	private RecipeImpl(long id) {
		this.id = id;
		this.name = "";
		this.description = "";
		this.cost = 0;
		this.prepTime = 0;
		this.cookTime = 0;
		this.vid = id;
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
		return RecipeIngredientImpl.factory.getRecipeIngredients(this);
	}

	public RecipeIngredient addIngredient(Ingredient i, Unit u) {
		return RecipeIngredientImpl.factory.addRecipeIngredient(this, i, u);
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
		InstructionImpl.factory.reorderInstructions(id, order);
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
		SQLiteDatabase db = factory.helper.getWritableDatabase();
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
		SQLiteDatabase db = factory.helper.getWritableDatabase();
		db.beginTransaction();
			long newId = db.insert("Recipe", null, values);
			db.execSQL(stmt.replaceAll("?", newId + "").replaceAll("$", id + ""));
		db.endTransaction();
		values.put("rid", newId);
		return factory.createRecipeFromData(values);
	}

	public void delete() {
		String stmt =
			"DELETE FROM SuggestedWith sw" +
				"WHERE sw.rid1 = ?; " +

			"DELETE FROM SuggestedWith sw" +
				"WHERE sw.rid2 = ?; " +

			"DELETE FROM RecipeCategory rc" +
				"WHERE rc.rid = ?; " +

			"DELETE FROM RecipeIngredients ri " +
				"WHERE ri.rid = ?; " +

			"DELETE FROM InstructionIngredients ii " +
				"WHERE ii.instrid IN (" +
					"SELECT i.iid " +
					"FROM Instruction i " +
					"WHERE i.rid = ?); " +

			"DELETE FROM Instruction i " +
				"WHERE i.rid = ?; " +

			"DELETE FROM VariantGroup vg " +
				"WHERE NOT EXISTS (" +
					"SELECT r2.vid " +
					"FROM Recipe r1, Recipe r2 " +
					"WHERE r1.rid = ? " +
						"and r1.vid = r2.vid " +
						"and r1.rid != r2.rid);" +

			"DELETE FROM Recipe r " +
				"WHERE r.rid = ?;";

		SQLiteDatabase db = factory.helper.getWritableDatabase();
		db.beginTransaction();
			db.execSQL(stmt.replaceAll("?", id + ""));
		db.endTransaction();
	}

	private void itemUpdate(ContentValues values, String operation) {
		SQLiteDatabase db = factory.helper.getWritableDatabase();
		int ret = db.update("Recipe", values, "rid=?", new String[] {id + ""});
		if(ret != 1) {
			throw new IllegalStateException("Recipe " + operation + " should have affected only row " + id +
												" but affected " + ret + " rows");
		}
	}

	protected static class RecipeFactory {

		protected RecipeBoxOpenHelper helper;

		private RecipeFactory() {
			helper = AppData.getSingleton().getOpenHelper();
		}

		protected Recipe createNew(String name) {
			ContentValues values = new ContentValues();
			values.put("name", name);
			SQLiteDatabase db = helper.getWritableDatabase();
			db.beginTransaction();
				long id = db.insert("Recipe", null, values);
				values.remove("name");
				values.put("vid", id);
				db.insert("VariantGroup", null, values);
				db.update("Recipe", values, "rid=?", new String[] {id + ""});
			db.endTransaction();

			RecipeImpl created = new RecipeImpl(id);
			created.name = name;

			return created;
		}

		protected List<Recipe> createListFromCursor(Cursor c) {
			//  r.rid, r.name, r.description, r.preptime, r.cooktime, r.cost, r.vid
			List<Recipe> list = new ArrayList<Recipe>(c.getCount());
			while(c.moveToNext()) {
				RecipeImpl r = new RecipeImpl(c.getLong(1));
				r.name = c.getString(2);
				r.description = c.getString(3);
				r.prepTime = c.getInt(4);
				r.cookTime = c.getInt(5);
				r.cost = c.getInt(6);
				r.vid = c.getLong(7);
				list.add(r);
			}
			return list;
		}

		protected Recipe createRecipeFromData(ContentValues values) {
			RecipeImpl r = new RecipeImpl(values.getAsLong("rid"));
			r.name = values.getAsString("name");
			r.vid = values.getAsLong("vid");
			r.description = values.getAsString("description");
			r.prepTime = values.getAsInteger("preptime");
			r.cookTime = values.getAsInteger("cooktime");
			r.cost = values.getAsInteger("cost");

			return r;
		}

	}
}

