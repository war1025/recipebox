package org.wrowclif.recipebox.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.Ingredient;
import org.wrowclif.recipebox.Unit;
import org.wrowclif.recipebox.Category;
import org.wrowclif.recipebox.Utility;
import org.wrowclif.recipebox.db.RecipeBoxOpenHelper;

import java.util.List;
import java.util.ArrayList;

import java.util.List;

public class UtilityImpl implements Utility {

	public static final UtilityImpl singleton;

	static {
		singleton = new UtilityImpl();
	}

	protected RecipeBoxOpenHelper helper;

	private UtilityImpl() {
		helper = AppData.getSingleton().getOpenHelper();
	}

	public Recipe newRecipe(String name) {
		return RecipeImpl.factory.createNew(name);
	}

	public List<Recipe> searchRecipes(String search, int maxResults) {
		String stmt =
			"SELECT r.rid, r.name, r.description, r.preptime, r.cooktime, r.cost, r.vid " +
			"FROM Recipe r " +
			"WHERE r.name LIKE ? " +
			"ORDER BY r.name ASC " +
			"LIMIT ?; ";

		List<Recipe> list = null;
		SQLiteDatabase db = helper.getWritableDatabase();
		db.beginTransaction();
			Cursor c = db.rawQuery(stmt, new String[] {"%" + search + "%", maxResults + ""});
			list = RecipeImpl.factory.createListFromCursor(c);
			c.close();
		db.setTransactionSuccessful();
		db.endTransaction();
		return list;
	}

	public Ingredient createOrRetrieveIngredient(String name) {
		return IngredientImpl.factory.createOrRetrieveIngredient(name);
	}

	public List<Ingredient> searchIngredients(String search, int maxResults) {
		String stmt =
			"SELECT i.iid, i.name " +
			"FROM Ingredient i " +
			"WHERE i.name LIKE ? " +
			"ORDER BY i.usecount DESC, i.name ASC " +
			"LIMIT ?; ";

		List<Ingredient> list = null;
		SQLiteDatabase db = helper.getWritableDatabase();
		db.beginTransaction();
			Cursor c = db.rawQuery(stmt, new String[] {"%" + search + "%", maxResults + ""});
			list = IngredientImpl.factory.createListFromCursor(c);
			c.close();
		db.setTransactionSuccessful();
		db.endTransaction();
		return list;
	}

	public List<Unit> searchUnits(String search, int maxResults) {
		String stmt =
			"SELECT u.uid, u.name, u.type, u.factor, u.minfraction " +
			"FROM Unit u " +
			"WHERE u.name LIKE ? " +
			"ORDER BY u.name ASC " +
			"LIMIT ?; ";

		List<Unit> list = null;
		SQLiteDatabase db = helper.getWritableDatabase();
		db.beginTransaction();
			Cursor c = db.rawQuery(stmt, new String[] {"%" + search + "%", maxResults + ""});
			list = UnitImpl.factory.createListFromCursor(c);
			c.close();
		db.setTransactionSuccessful();
		db.endTransaction();
		return list;
	}

	public List<Category> searchCategories(String search, int maxResults) {
		String stmt =
			"SELECT c.cid, c.name, c.description " +
			"FROM Category c " +
			"WHERE c.name LIKE ? " +
			"ORDER BY c.name ASC " +
			"LIMIT ?; ";

		List<Category> list = null;
		SQLiteDatabase db = helper.getWritableDatabase();
		db.beginTransaction();
			Cursor c = db.rawQuery(stmt, new String[] {"%" + search + "%", maxResults + ""});
			list = CategoryImpl.factory.createListFromCursor(c);
			c.close();
		db.setTransactionSuccessful();
		db.endTransaction();
		return list;
	}

	public Category createOrRetrieveCategory(String category) {
		return CategoryImpl.factory.createOrRetrieveCategory(category);
	}

}
