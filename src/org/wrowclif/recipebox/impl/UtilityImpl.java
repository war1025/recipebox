package org.wrowclif.recipebox.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.AppData.Transaction;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.Ingredient;
import org.wrowclif.recipebox.Unit;
import org.wrowclif.recipebox.Category;
import org.wrowclif.recipebox.Utility;

import java.util.List;
import java.util.ArrayList;

public class UtilityImpl implements Utility {

	public static final UtilityImpl singleton;

	static {
		singleton = new UtilityImpl();
	}

	protected AppData data;

	private UtilityImpl() {
		data = AppData.getSingleton();
	}

	public Recipe newRecipe(String name) {
		return RecipeImpl.factory.createNew(name);
	}

	public List<Recipe> searchRecipes(final String search, final int maxResults) {
		final String stmt =
			"SELECT r.rid, r.name, r.description, r.preptime, r.cooktime, r.cost, r.vid " +
			"FROM Recipe r " +
			"WHERE r.name LIKE ? " +
			"ORDER BY r.name ASC " +
			"LIMIT ?; ";

		return data.sqlTransaction(new Transaction<List<Recipe>>() {
			public List<Recipe> exec(SQLiteDatabase db) {
				Cursor c = db.rawQuery(stmt, new String[] {"%" + search + "%", maxResults + ""});
				List<Recipe> list = RecipeImpl.factory.createListFromCursor(c);
				c.close();
				return list;
			}
		});
	}

	public Recipe getRecipeById(long id) {
		return RecipeImpl.factory.getRecipeById(id);
	}

	public Ingredient createOrRetrieveIngredient(String name) {
		return IngredientImpl.factory.createOrRetrieveIngredient(name);
	}

	public List<Ingredient> searchIngredients(final String search, final int maxResults) {
		final String stmt =
			"SELECT i.iid, i.name " +
			"FROM Ingredient i " +
			"WHERE i.name LIKE ? " +
			"ORDER BY i.usecount DESC, i.name ASC " +
			"LIMIT ?; ";

		return data.sqlTransaction(new Transaction<List<Ingredient>>() {
			public List<Ingredient> exec(SQLiteDatabase db) {
				Cursor c = db.rawQuery(stmt, new String[] {"%" + search + "%", maxResults + ""});
				List<Ingredient> list = IngredientImpl.factory.createListFromCursor(c);
				c.close();
				return list;
			}
		});
	}

	public List<Unit> searchUnits(final String search, final int maxResults) {
		final String stmt =
			"SELECT u.uid, u.name, u.abbreviation, u.type, u.factor, u.minfraction " +
			"FROM Unit u " +
			"WHERE u.name LIKE ? " +
			"ORDER BY u.name ASC " +
			"LIMIT ?; ";

		return data.sqlTransaction(new Transaction<List<Unit>>() {
			public List<Unit> exec(SQLiteDatabase db) {
				Cursor c = db.rawQuery(stmt, new String[] {"%" + search + "%", maxResults + ""});
				List<Unit> list = UnitImpl.factory.createListFromCursor(c);
				c.close();
				return list;
			}
		});
	}

	public List<Category> searchCategories(final String search, final int maxResults) {
		final String stmt =
			"SELECT c.cid, c.name, c.description " +
			"FROM Category c " +
			"WHERE c.name LIKE ? " +
			"ORDER BY c.name ASC " +
			"LIMIT ?; ";

		return data.sqlTransaction(new Transaction<List<Category>>() {
			public List<Category> exec(SQLiteDatabase db) {
				Cursor c = db.rawQuery(stmt, new String[] {"%" + search + "%", maxResults + ""});
				List<Category> list = CategoryImpl.factory.createListFromCursor(c);
				c.close();
				return list;
			}
		});
	}

	public Category createOrRetrieveCategory(String category) {
		return CategoryImpl.factory.createOrRetrieveCategory(category);
	}
}
