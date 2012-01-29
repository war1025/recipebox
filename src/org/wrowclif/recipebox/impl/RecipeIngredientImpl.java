package org.wrowclif.recipebox.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.RecipeIngredient;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.Ingredient;
import org.wrowclif.recipebox.Unit;
import org.wrowclif.recipebox.db.RecipeBoxOpenHelper;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class RecipeIngredientImpl implements RecipeIngredient {

	protected static final RecipeIngredientFactory factory;

	static {
		factory = new RecipeIngredientFactory();
	}

	protected Recipe recipe;
	protected Ingredient ingredient;
	protected Unit unit;
	protected double amount;

	private RecipeIngredientImpl() {

	}

	public String getName() {
		return ingredient.getName();
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;

		ContentValues values = new ContentValues();
		values.put("amount", amount);

		itemUpdate(values, "setAmount");
	}

	public String getUnitName() {
		return unit.getName();
	}

	public Recipe getRecipe() {
		return recipe;
	}

	public Ingredient getIngredient() {
		return ingredient;
	}

	public Unit getUnits() {
		return unit;
	}

	public void setUnits(Unit unit) {
		if(unit == null) {
			throw new IllegalArgumentException("unit cannot be null");
		}
		this.unit = unit;

		ContentValues values = new ContentValues();
		values.put("uid", unit.getId());

		itemUpdate(values, "setUnits");
	}

	protected void itemUpdate(ContentValues values, String operation) {
		SQLiteDatabase db = factory.helper.getWritableDatabase();
		int ret = db.update("RecipeIngredients", values, "rid=? and iid=?",
						new String[] {recipe.getId() + "", ingredient.getId() + ""});
		if(ret != 1) {
			throw new IllegalStateException("RecipeIngredients " + operation + " should have affected only one row" +
												" but affected " + ret + " rows");
		}
	}

	protected static class RecipeIngredientFactory {

		protected RecipeBoxOpenHelper helper;

		private RecipeIngredientFactory() {
			helper = AppData.getSingleton().getOpenHelper();
		}

		protected List<RecipeIngredient> createListFromCursor(Recipe r, Cursor c) {
			// i.iid, i.name, u.uid, u.name, u.type, u.factor, u.minfraction, ri.amount
			List<RecipeIngredient> list = new ArrayList<RecipeIngredient>(c.getCount());
			ContentValues values = new ContentValues();
			while(c.moveToNext()) {
				RecipeIngredientImpl ri = new RecipeIngredientImpl();
				ri.recipe = r;

				values.clear();
				values.put("iid", c.getLong(1));
				values.put("name", c.getString(2));
				ri.ingredient = IngredientImpl.factory.createFromData(values);

				values.clear();
				values.put("uid", c.getLong(3));
				values.put("name", c.getString(4));
				values.put("type", c.getInt(5));
				values.put("factor", c.getDouble(6));
				values.put("minfraction", c.getInt(7));
				ri.unit = UnitImpl.factory.createFromData(values);

				ri.amount = c.getInt(8);

				list.add(ri);
			}

			return list;
		}

		protected List<RecipeIngredient> getRecipeIngredients(Recipe r) {
			String stmt =
				"SELECT i.iid, i.name, u.uid, u.name, u.type, u.factor, u.minfraction, ri.amount " +
				"FROM Ingredient i, Unit u, RecipeIngredients ri " +
				"WHERE ri.rid = ? " +
					"and ri.iid = i.iid " +
					"and ri.uid = u.uid " +
				"ORDER BY ri.num ASC; ";

			List<RecipeIngredient> list = null;
			SQLiteDatabase db = factory.helper.getWritableDatabase();
			db.beginTransaction();
				Cursor c = db.rawQuery(stmt.replaceAll("?", r.getId() + ""), null);
				list = createListFromCursor(r, c);
				c.close();
			db.endTransaction();
			return list;

		}

		protected RecipeIngredient addRecipeIngredient(Recipe r, Ingredient i) {
			String selectStmt =
				"SELECT r.maxingredient " +
				"FROM Recipe r " +
				"WHERE r.rid = ?; ";
			String updateStmt =
				"UPDATE Recipe r " +
				"SET (r.maxingredient = r.maxingredient + 1) " +
				"WHERE r.rid = ?; " +

				"UPDATE Ingredient i " +
				"SET (i.usecount = i.usecount + 1) " +
				"WHERE i.iid = ?; ";

			RecipeIngredientImpl result = null;
			Unit u = UnitImpl.factory.getNullUnit();
			ContentValues values = new ContentValues();
			values.put("rid", r.getId());
			values.put("iid", i.getId());
			values.put("uid", u.getId());
			SQLiteDatabase db = factory.helper.getWritableDatabase();
			db.beginTransaction();
				Cursor c = db.rawQuery(selectStmt.replaceAll("?", r.getId() + ""), null);
				c.moveToNext();
				int max = c.getInt(1);
				c.close();
				max = max + 1;
				db.execSQL(updateStmt, new Object[] {r.getId(), i.getId()});
				values.put("num", max);
				db.insert("RecipeIngredients", null, values);

				result = new RecipeIngredientImpl();
				result.recipe = r;
				result.ingredient = i;
				result.unit = u;
				result.amount = 0;
			db.endTransaction();
			return result;
		}

		protected void removeRecipeIngredient(RecipeIngredient toRemove) {
			String stmt =
				"DELETE FROM RecipeIngredient ri " +
				"WHERE ri.iid = ? " +
					"and ri.rid = ?; " +

				"UPDATE Ingredient i " +
				"SET (i.usecount = i.usecount - 1) " +
				"WHERE i.iid = ?; ";

			SQLiteDatabase db = factory.helper.getWritableDatabase();
			long iid = toRemove.getIngredient().getId();
			long rid = toRemove.getRecipe().getId();
			db.execSQL(stmt, new Object[] {iid, rid, iid});
		}

		protected void reorderRecipeIngredients(List<RecipeIngredient> order) {
			String getIngredientIdsStmt =
				"SELECT ri.iid " +
				"FROM RecipeIngredients ri " +
				"WHERE ri.rid = ?; ";
			String ingredientStmt =
				"UPDATE RecipeIngredients ri " +
					"SET (ri.num = ?) " +
					"WHERE ri.iid = ? " +
						"and ri.rid = ?; ";
			String recipeStmt =
				"UPDATE Recipe r " +
					"SET (r.maxingredient = ?) " +
					"WHERE r.rid = ?; ";

			// Check that all values in 'order' are the same as those in the database currently.
			// Error otherwise.
			if((order == null) || (order.size() == 0)) {
				throw new IllegalArgumentException("Must be at least one ingredient");
			}
			long recipeId = order.get(0).getRecipe().getId();
			long[] orderIds = new long[order.size()];
			for(int i = 0; i < order.size(); i++) {
				RecipeIngredient ri = order.get(i);
				if(ri.getRecipe().getId() != recipeId) {
					throw new IllegalArgumentException("RecipeIngredients must all be from same recipe");
				}
				orderIds[i] = ri.getIngredient().getId();
			}
			Arrays.sort(orderIds);
			boolean valid = false;

			SQLiteDatabase db = factory.helper.getWritableDatabase();
			db.beginTransaction();
				Cursor c1 = db.rawQuery(getIngredientIdsStmt.replaceAll("?", recipeId + ""), null);
				if(orderIds.length == c1.getCount()) {
					long[] actualIds = new long[c1.getCount()];
					for(int i = 0; i < actualIds.length; i++) {
						c1.moveToNext();
						actualIds[i] = c1.getLong(1);
					}
					Arrays.sort(actualIds);
					valid = true;
					for(int i = 0; i < actualIds.length; i++) {
						if(actualIds[i] != orderIds[i]) {
							valid = false;
							break;
						}
					}
				}
				c1.close();
				if(valid) {
					for(int i = 0; i < order.size(); i++) {
						db.execSQL(ingredientStmt, new Object[] {i, order.get(i).getIngredient().getId(), recipeId});
					}
					db.execSQL(recipeStmt, new Object[] {order.size() - 1, recipeId});
				}
			db.endTransaction();
			if(!valid) {
				throw new IllegalArgumentException("Ingredients were not valid");
			}
		}
	}
}

