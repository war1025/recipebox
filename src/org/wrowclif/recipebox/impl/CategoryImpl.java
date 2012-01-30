package org.wrowclif.recipebox.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Category;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.db.RecipeBoxOpenHelper;

import java.util.List;
import java.util.ArrayList;

public class CategoryImpl implements Category {

	protected static final CategoryFactory factory;

	static {
		factory = new CategoryFactory();
	}

	private long id;
	private String name;
	private String description;

	private CategoryImpl(long id) {
		this.name = "";
		this.description = "";
	}

	public long getId() {
		return id;
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

	public List<Recipe> getRecipes() {
		String stmt =
			"SELECT r.rid, r.name, r.description, r.preptime, r.cooktime, r.cost, r.vid " +
			"FROM Recipe r, RecipeCategory rc " +
			"WHERE rc.cid = ? " +
				"and rc.rid = r.rid; ";
		List<Recipe> list = null;
		SQLiteDatabase db = factory.helper.getWritableDatabase();
		db.beginTransaction();
			Cursor c = db.rawQuery(stmt.replaceAll("\\?", id + ""), null);
			list = RecipeImpl.factory.createListFromCursor(c);
			c.close();
		db.setTransactionSuccessful();
		db.endTransaction();
		return list;
	}

	public void addRecipe(Recipe toAdd) {
		factory.addRecipeToCategory(toAdd.getId(), this);
	}

	public void removeRecipe(Recipe toRemove) {
		factory.removeRecipeFromCategory(toRemove.getId(), this);
	}

	public void delete() {
		String stmt1 =
			"DELETE FROM RecipeCategory " +
				"WHERE cid = ?; ";
		String stmt2 =
			"DELETE FROM Category " +
				"WHERE cid = ?; ";

		SQLiteDatabase db = factory.helper.getWritableDatabase();
		db.beginTransaction();
			Object[] params = {id};
			db.execSQL(stmt1, params);
			db.execSQL(stmt2, params);
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	private void itemUpdate(ContentValues values, String operation) {
		SQLiteDatabase db = factory.helper.getWritableDatabase();
		int ret = db.update("Category", values, "cid=?", new String[] {id + ""});
		if(ret != 1) {
			throw new IllegalStateException("Category " + operation + " should have affected only row " + id +
												" but affected " + ret + " rows");
		}
	}

	protected static class CategoryFactory {

		protected RecipeBoxOpenHelper helper;

		private CategoryFactory() {
			helper = AppData.getSingleton().getOpenHelper();
		}

		protected List<Category> createListFromCursor(Cursor c) {
			List<Category> list = new ArrayList<Category>(c.getCount());
			while(c.moveToNext()) {
				CategoryImpl ci = new CategoryImpl(c.getLong(0));
				ci.name = c.getString(1);
				ci.description = c.getString(2);

				list.add(ci);
			}
			return list;
		}

		protected List<Category> getRecipeCategories(long recipeId) {
			String stmt =
				"SELECT c.cid, c.name, c.description " +
				"FROM Category c, RecipeCategory rc " +
				"WHERE c.cid = rc.cid " +
					"and rc.rid = ?; ";

			List<Category> list = null;
			SQLiteDatabase db = factory.helper.getWritableDatabase();
			db.beginTransaction();
				Cursor c1 = db.rawQuery(stmt.replaceAll("\\?", recipeId + ""), null);
				list = createListFromCursor(c1);
				c1.close();
			db.setTransactionSuccessful();
			db.endTransaction();
			return list;
		}

		protected void addRecipeToCategory(long recipeId, Category c) {
			String stmt =
				"INSERT OR REPLACE INTO RecipeCategory(rid, cid) " +
				"VALUES (?, ?); ";
			SQLiteDatabase db = factory.helper.getWritableDatabase();
			db.beginTransaction();
				db.execSQL(stmt, new String[] {recipeId + "", c.getId() + ""});
			db.setTransactionSuccessful();
			db.endTransaction();
		}

		protected void removeRecipeFromCategory(long recipeId, Category c) {
			String stmt =
				"DELETE FROM RecipeCategory " +
				"WHERE rid = ? " +
					"and cid = ?; ";
			SQLiteDatabase db = factory.helper.getWritableDatabase();
			db.beginTransaction();
				db.execSQL(stmt, new String[] {recipeId + "", c.getId() + ""});
			db.setTransactionSuccessful();
			db.endTransaction();
		}

		protected Category createOrRetrieveCategory(String category) {
			String retrieveStmt =
				"SELECT c.cid, c.name, c.description " +
				"FROM Category c " +
				"WHERE name = ?; ";

			CategoryImpl ci = null;
			SQLiteDatabase db = factory.helper.getWritableDatabase();
			db.beginTransaction();
				Cursor c = db.rawQuery(retrieveStmt, new String[] {category});
				if(c.moveToNext()) {
					ci = new CategoryImpl(c.getLong(0));
					ci.name = c.getString(1);
					ci.description = c.getString(2);
				} else {
					ContentValues values = new ContentValues();
					values.put("name", category);
					values.put("description", "");

					long id = db.insert("Category", null, values);

					ci = new CategoryImpl(id);
					ci.name = category;
				}
				c.close();
			db.setTransactionSuccessful();
			db.endTransaction();
			return ci;
		}
	}
}
