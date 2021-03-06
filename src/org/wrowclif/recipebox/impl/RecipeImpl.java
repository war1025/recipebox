package org.wrowclif.recipebox.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.util.Log;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.AppData.Transaction;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.RecipeIngredient;
import org.wrowclif.recipebox.Ingredient;
import org.wrowclif.recipebox.Instruction;
import org.wrowclif.recipebox.Category;
import org.wrowclif.recipebox.Review;
import org.wrowclif.recipebox.Suggestion;

import org.wrowclif.recipebox.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;

public class RecipeImpl implements Recipe {
	private static final String LOG_TAG = "Recipebox RecipeImpl";

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
	protected String imageUri;
	protected long vid;

	private RecipeImpl(long id) {
		this.id = id;
		this.name = "";
		this.description = "";
		this.cost = 0;
		this.prepTime = 0;
		this.cookTime = 0;
		this.imageUri = "";
		this.vid = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name.trim();
		ContentValues values = new ContentValues();
		values.put("name", this.name);
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

	public String getImageUri() {
		return imageUri;
	}

	public void setImageUri(String uri) {
		this.imageUri = uri;
		ContentValues values = new ContentValues();
		values.put("imageuri", uri);
		itemUpdate(values, "setImageUri");
	}

	public long getId() {
		return id;
	}

	public List<RecipeIngredient> getIngredients() {
		return RecipeIngredientImpl.factory.getRecipeIngredients(this);
	}

	public RecipeIngredient addIngredient(Ingredient i) {
		return RecipeIngredientImpl.factory.addRecipeIngredient(this, i);
	}

	public void removeIngredient(RecipeIngredient toRemove) {
		RecipeIngredientImpl.factory.removeRecipeIngredient(toRemove);
	}

	public void swapIngredientPositions(RecipeIngredient a, RecipeIngredient b) {
		RecipeIngredientImpl.factory.swapIngredientPositions(a, b);
	}

	public void reorderIngredients(List<RecipeIngredient> order) {
		RecipeIngredientImpl.factory.reorderRecipeIngredients(order);
	}

	public List<Instruction> getInstructions() {
		return InstructionImpl.factory.getRecipeSteps(id);
	}

	public Instruction addInstruction() {
		return InstructionImpl.factory.addInstruction(id);
	}

	public void removeInstruction(Instruction i) {
		InstructionImpl.factory.removeInstruction(i);
	}

	public void swapInstructionPositions(Instruction a, Instruction b) {
		InstructionImpl.factory.swapInstructionPositions(a, b);
	}

	public void reorderInstructions(List<Instruction> order) {
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
		return CategoryImpl.factory.getRecipeCategories(id);
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
		final String stmt =
			"SELECT r.rid, r.name, r.description, r.preptime, r.cooktime, r.cost, r.imageuri, r.vid " +
			"FROM Recipe r " +
			"WHERE r.vid = %s " +
				"and r.rid != %s; ";

		return factory.data.sqlTransaction(new Transaction<List<Recipe>>() {
			public List<Recipe> exec(SQLiteDatabase db) {
				Cursor c1 = db.rawQuery(String.format(stmt, vid + "", id + ""), null);
				List<Recipe> l1 = factory.createListFromCursor(c1);
				c1.close();
				return l1;
			}
		});
	}

	public long getLastViewTime() {
		final String stmt =
			"SELECT r.lastviewtime " +
			"FROM Recipe r " +
			"WHERE r.rid = ?;";

		return factory.data.sqlTransaction(new Transaction<Long>() {
			public Long exec(SQLiteDatabase db) {
				Cursor c = db.rawQuery(stmt, new String[] {id + ""});
				c.moveToNext();
				long time = c.getLong(0);
				c.close();
				return time;
			}
		});
	}

	public void updateLastViewTime() {
		final String stmt =
			"UPDATE Recipe " +
			"SET lastviewtime = CURRENT_TIMESTAMP " +
			"WHERE rid = ?;";

		factory.data.sqlTransaction(new Transaction<Void>() {
			public Void exec(SQLiteDatabase db) {
				db.execSQL(stmt, new Object[] {id});
				return null;
			}
		});
	}

	public long getCreateTime() {
		final String stmt =
			"SELECT r.createtime " +
			"FROM Recipe r " +
			"WHERE r.rid = ?;";

		return factory.data.sqlTransaction(new Transaction<Long>() {
			public Long exec(SQLiteDatabase db) {
				Cursor c = db.rawQuery(stmt, new String[] {id + ""});
				c.moveToNext();
				long time = c.getLong(0);
				c.close();
				return time;
			}
		});
	}

	public Recipe branch(final String name) {
		final String stmt =
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

			"INSERT INTO RecipeIngredients(rid, iid, amount, num) " +
				"SELECT ? as rid, ri.iid, ri.amount, ri.num " +
				"FROM RecipeIngredients ri " +
				"WHERE ri.rid = $; " +

			"INSERT INTO Instruction(rid, text, num) " +
				"SELECT ? as rid, i.text, i.num " +
				"FROM Instruction i " +
				"WHERE i.rid = $; " +

			"INSERT INTO InstructionIngredients(instid, ingrid, num) " +
				"SELECT in1.iid, ii.ingrid, ii.num " +
				"FROM Instruction in1, Instruction in2, InstructionIngredients ii " +
				"WHERE in1.rid = ? " +
					"and in2.rid = $ " +
					"and in1.num = in2.num " +
					"and ii.instid = in2.iid; " +

			"INSERT INTO RecipeCategory(rid, cid) " +
				"SELECT ? as rid, rc.cid " +
				"FROM RecipeCategory rc " +
				"WHERE rc.rid = $; " +

			"UPDATE Ingredient " +
				"SET usecount = usecount + 1 " +
				"WHERE iid IN ( " +
					"SELECT ri.iid " +
					"FROM RecipeIngredients ri " +
					"WHERE ri.rid = ?); " +

			"UPDATE Recipe " +
				"SET maxingredient = ( " +
					"SELECT r.maxingredient " +
					"FROM Recipe r " +
					"WHERE r.rid = $)" +
				"WHERE rid = ?; " +

			"UPDATE Recipe " +
				"SET maxinstruction = ( " +
					"SELECT r.maxinstruction " +
					"FROM Recipe r " +
					"WHERE r.rid = $)" +
				"WHERE rid = ?;" +

			"UPDATE Recipe " +
				"SET createtime = CURRENT_TIMESTAMP, lastviewtime = CURRENT_TIMESTAMP " +
				"WHERE rid = ?;";

		final ContentValues values = new ContentValues();
		values.put("name", name);
		values.put("description", description);
		values.put("cost", cost);
		values.put("cooktime", cookTime);
		values.put("preptime", prepTime);
		values.put("imageuri", imageUri);
		values.put("vid", vid);

		return factory.data.sqlTransaction(new Transaction<Recipe>() {
			public Recipe exec(SQLiteDatabase db) {
				long newId = db.insert("Recipe", null, values);
				String[] stmts = stmt.replaceAll("\\?", newId + "").replaceAll("\\$", id + "").split(";");
				for(String s : stmts) {
					db.execSQL(s);
				}
				values.put("rid", newId);
				Recipe branched = factory.createRecipeFromData(values);
				String imageUri = ImageUtil.copyImage(RecipeImpl.this, branched);
				if(imageUri != null) {
					branched.setImageUri(imageUri);
				}
				return branched;
			}
		});
	}

	public void delete() {
		final String stmt =
			"UPDATE Ingredient " +
				"SET usecount = usecount - 1 " +
				"WHERE iid IN ( " +
					"SELECT ri.iid " +
					"FROM RecipeIngredients ri " +
					"WHERE ri.rid = ?); " +

			"DELETE FROM SuggestedWith " +
				"WHERE rid1 = ?; " +

			"DELETE FROM SuggestedWith " +
				"WHERE rid2 = ?; " +

			"DELETE FROM RecipeCategory " +
				"WHERE rid = ?; " +

			"DELETE FROM RecipeIngredients " +
				"WHERE rid = ?; " +

			"DELETE FROM InstructionIngredients " +
				"WHERE instid IN (" +
					"SELECT i.iid " +
					"FROM Instruction i " +
					"WHERE i.rid = ?); " +

			"DELETE FROM Instruction " +
				"WHERE rid = ?; " +

			"DELETE FROM VariantGroup " +
				"WHERE NOT EXISTS (" +
					"SELECT r2.vid " +
					"FROM Recipe r1, Recipe r2 " +
					"WHERE r1.rid = ? " +
						"and r1.vid = r2.vid " +
						"and r1.rid != r2.rid);" +

			"DELETE FROM Recipe " +
				"WHERE rid = ?;";

		final String useCountStmt =
			"SELECT i.iid " +
			"FROM Ingredient i, RecipeIngredients ri " +
			"WHERE i.iid = ri.iid " +
				"and ri.rid = ? " +
				"and i.usecount = 1;";

		ImageUtil.deleteImage(this);
		factory.data.sqlTransaction(new Transaction<Void>() {
			public Void exec(SQLiteDatabase db) {
				// Get list of ingredients to remove
				Cursor c = db.rawQuery(useCountStmt, new String[] {id + ""});
				long[] iids = new long[c.getCount()];
				int cur = 0;
				while(c.moveToNext()) {
					iids[cur] = c.getLong(0);
					cur++;
				}
				c.close();

				// Delete recipe
				String[] stmts = stmt.replaceAll("\\?", id + "").split(";");
				for(String s : stmts) {
					db.execSQL(s);
				}

				// Delete any ingredients that aren't used anymore
				ContentValues cv = new ContentValues();
				for(long iid : iids) {
					cv.put("name", "");
					cv.put("iid", iid);
					Ingredient toRemove = IngredientImpl.factory.createFromData(cv);
					toRemove.delete();
				}
				return null;
			}
		});
	}

	private void itemUpdate(ContentValues values, String operation) {
		factory.data.itemUpdate(values, "Recipe", "rid=?", new String[] {id + ""}, operation);
	}

	protected static class RecipeFactory {

		protected AppData data;

		private RecipeFactory() {
			data = AppData.getSingleton();
		}

		protected Recipe createNew(final String name) {
			final ContentValues values = new ContentValues();
			values.put("name", name);
			long now = System.currentTimeMillis();

			values.put("createtime", now);
			values.put("lastviewtime", now);

			return data.sqlTransaction(new Transaction<Recipe>() {
				public Recipe exec(SQLiteDatabase db) {
					long id = db.insert("Recipe", null, values);
					values.clear();
					values.put("vid", id);
					db.insert("VariantGroup", null, values);
					db.update("Recipe", values, "rid=?", new String[] {id + ""});
					RecipeImpl created = new RecipeImpl(id);
					created.name = name;
					return created;
				}
			});
		}

		protected List<Recipe> createListFromCursor(Cursor c) {
			//  r.rid, r.name, r.description, r.preptime, r.cooktime, r.cost, r.imageuri, r.vid
			List<Recipe> list = new ArrayList<Recipe>(c.getCount());
			while(c.moveToNext()) {
				RecipeImpl r = new RecipeImpl(c.getLong(0));
				r.name = c.getString(1);
				r.description = c.getString(2);
				r.prepTime = c.getInt(3);
				r.cookTime = c.getInt(4);
				r.cost = c.getInt(5);
				r.imageUri = c.getString(6);
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
			r.imageUri = values.getAsString("imageuri");

			return r;
		}

		protected Recipe getRecipeById(final long id) {
			final String stmt =
				"SELECT r.rid, r.name, r.description, r.preptime, r.cooktime, r.cost, r.imageuri, r.vid " +
				"FROM Recipe r " +
				"WHERE r.rid = ?;";

			return data.sqlTransaction(new Transaction<Recipe>() {
				public Recipe exec(SQLiteDatabase db) {
					Cursor c = db.rawQuery(stmt, new String[] {id + ""});
					List<Recipe> list = createListFromCursor(c);
					c.close();
					if(list.size() > 0) {
						return list.get(0);
					} else {
						return null;
					}
				}
			});
		}
	}
}

