package org.wrowclif.recipebox.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.util.Log;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.AppData.Transaction;
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
		factory.data.itemUpdate(values, "RecipeIngredients", "rid=? and iid=?",
						new String[] {recipe.getId() + "", ingredient.getId() + ""}, operation);
	}

	protected static class RecipeIngredientFactory {

		protected AppData data;

		private RecipeIngredientFactory() {
			data = AppData.getSingleton();
		}

		protected List<RecipeIngredient> createListFromCursor(Recipe r, Cursor c) {
			// i.iid, i.name, u.uid, u.name, u.abbreviation, u.type, u.factor, u.minfraction, ri.amount
			List<RecipeIngredient> list = new ArrayList<RecipeIngredient>(c.getCount());
			ContentValues values = new ContentValues();
			while(c.moveToNext()) {
				RecipeIngredientImpl ri = new RecipeIngredientImpl();
				ri.recipe = r;

				values.clear();
				values.put("iid", c.getLong(0));
				values.put("name", c.getString(1));
				ri.ingredient = IngredientImpl.factory.createFromData(values);

				values.clear();
				values.put("uid", c.getLong(2));
				values.put("name", c.getString(3));
				values.put("abbreviation", c.getString(4));
				values.put("type", c.getInt(5));
				values.put("factor", c.getDouble(6));
				values.put("minfraction", c.getInt(7));
				ri.unit = UnitImpl.factory.createFromData(values);

				ri.amount = c.getInt(7);

				list.add(ri);
			}

			return list;
		}

		protected List<RecipeIngredient> getRecipeIngredients(final Recipe r) {
			final String stmt =
				"SELECT i.iid, i.name, u.uid, u.name, u.abbreviation, u.type, u.factor, u.minfraction, ri.amount " +
				"FROM Ingredient i, Unit u, RecipeIngredients ri " +
				"WHERE ri.rid = ? " +
					"and ri.iid = i.iid " +
					"and ri.uid = u.uid " +
				"ORDER BY ri.num ASC; ";


			return data.sqlTransaction(new Transaction<List<RecipeIngredient>>() {
				public List<RecipeIngredient> exec(SQLiteDatabase db) {
					Cursor c = db.rawQuery(stmt.replaceAll("\\?", r.getId() + ""), null);
					List<RecipeIngredient> list = createListFromCursor(r, c);
					c.close();
					return list;
				}
			});
		}

		protected RecipeIngredient addRecipeIngredient(final Recipe r, final Ingredient i) {
			final String selectStmt =
				"SELECT r.maxingredient " +
				"FROM Recipe r " +
				"WHERE r.rid = ?; ";
			final String updateStmt =
				"UPDATE Recipe " +
				"SET maxingredient = maxingredient + 1 " +
				"WHERE rid = ?; ";
			final String useCountStmt =
				"UPDATE Ingredient " +
				"SET usecount = usecount + 1 " +
				"WHERE iid = ?; ";

			final Unit u = UnitImpl.factory.getNullUnit();
			final ContentValues values = new ContentValues();
			values.put("rid", r.getId());
			values.put("iid", i.getId());
			values.put("uid", u.getId());
			return data.sqlTransaction(new Transaction<RecipeIngredient>() {
				public RecipeIngredient exec(SQLiteDatabase db) {
					Cursor c = db.rawQuery(selectStmt.replaceAll("\\?", r.getId() + ""), null);
					c.moveToNext();
					int max = c.getInt(0);
					c.close();
					max = max + 1;
					db.execSQL(updateStmt, new Object[] {r.getId()});
					db.execSQL(useCountStmt, new Object[] {i.getId()});
					values.put("num", max);
					db.insert("RecipeIngredients", null, values);

					RecipeIngredientImpl result = new RecipeIngredientImpl();
					result.recipe = r;
					result.ingredient = i;
					result.unit = u;
					result.amount = 0;
					return result;
				}
			});
		}

		protected void removeRecipeIngredient(final RecipeIngredient toRemove) {
			final String stmt =
				"DELETE FROM RecipeIngredient " +
				"WHERE iid = ? " +
					"and rid = ?; ";
			final String useCountStmt =
				"UPDATE Ingredient " +
				"SET usecount = usecount - 1 " +
				"WHERE iid = ?; ";

			final long iid = toRemove.getIngredient().getId();
			final long rid = toRemove.getRecipe().getId();

			data.sqlTransaction(new Transaction<Void>() {
				public Void exec(SQLiteDatabase db) {
					db.execSQL(stmt, new Object[] {iid, rid});
					db.execSQL(useCountStmt, new Object[] {iid});
					return null;
				}
			});
		}

		protected void reorderRecipeIngredients(final List<RecipeIngredient> order) {
			final String getIngredientIdsStmt =
				"SELECT ri.iid " +
				"FROM RecipeIngredients ri " +
				"WHERE ri.rid = ?; ";
			final String ingredientStmt =
				"UPDATE RecipeIngredients " +
					"SET num = ? " +
					"WHERE iid = ? " +
						"and rid = ?; ";
			final String recipeStmt =
				"UPDATE Recipe " +
					"SET maxingredient = ? " +
					"WHERE rid = ?; ";

			// Check that all values in 'order' are the same as those in the database currently.
			// Error otherwise.
			if((order == null) || (order.size() == 0)) {
				throw new IllegalArgumentException("Must be at least one ingredient");
			}
			final long recipeId = order.get(0).getRecipe().getId();
			final long[] orderIds = new long[order.size()];
			for(int i = 0; i < order.size(); i++) {
				RecipeIngredient ri = order.get(i);
				if(ri.getRecipe().getId() != recipeId) {
					throw new IllegalArgumentException("RecipeIngredients must all be from same recipe");
				}
				orderIds[i] = ri.getIngredient().getId();
			}
			Arrays.sort(orderIds);
			boolean valid = data.sqlTransaction(new Transaction<Boolean>() {
				public Boolean exec(SQLiteDatabase db) {
					boolean valid = false;
					Cursor c1 = db.rawQuery(getIngredientIdsStmt.replaceAll("\\?", recipeId + ""), null);
					if(orderIds.length == c1.getCount()) {
						long[] actualIds = new long[c1.getCount()];
						for(int i = 0; i < actualIds.length; i++) {
							c1.moveToNext();
							actualIds[i] = c1.getLong(0);
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
					return valid;
				}
			});
			if(!valid) {
				throw new IllegalArgumentException("Ingredients were not valid");
			}
		}
	}
}

