package org.wrowclif.recipebox.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.util.Log;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.AppData.Transaction;
import org.wrowclif.recipebox.Category;
import org.wrowclif.recipebox.Recipe;

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
		this.id = id;
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
		final String stmt =
			"SELECT r.rid, r.name, r.description, r.preptime, r.cooktime, r.cost, r.imageuri, r.vid " +
			"FROM Recipe r, RecipeCategory rc " +
			"WHERE rc.cid = ? " +
				"and rc.rid = r.rid " +
			"ORDER BY r.name ASC; ";

		return factory.data.sqlTransaction(new Transaction<List<Recipe>>() {
			public List<Recipe> exec(SQLiteDatabase db) {
				Cursor c = db.rawQuery(stmt.replaceAll("\\?", id + ""), null);
				List<Recipe> list = RecipeImpl.factory.createListFromCursor(c);
				c.close();
				return list;
			}
		});
	}

	public void addRecipe(Recipe toAdd) {
		factory.addRecipeToCategory(toAdd.getId(), this);
	}

	public void removeRecipe(Recipe toRemove) {
		factory.removeRecipeFromCategory(toRemove.getId(), this);
	}

	public void delete() {
		final String stmt1 =
			"DELETE FROM RecipeCategory " +
				"WHERE cid = ?; ";
		final String stmt2 =
			"DELETE FROM Category " +
				"WHERE cid = ?; ";

		factory.data.sqlTransaction(new Transaction<Void>() {
			public Void exec(SQLiteDatabase db) {
				Object[] params = {id};
				db.execSQL(stmt1, params);
				db.execSQL(stmt2, params);
				return null;
			}
		});
	}

	private void itemUpdate(ContentValues values, String operation) {
		factory.data.itemUpdate(values, "Category", "cid=?", new String[] {id + ""}, operation);
	}

	protected static class CategoryFactory {

		protected AppData data;

		private CategoryFactory() {
			data = AppData.getSingleton();
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

		protected List<Category> getRecipeCategories(final long recipeId) {
			final String stmt =
				"SELECT c.cid, c.name, c.description " +
				"FROM Category c, RecipeCategory rc " +
				"WHERE c.cid = rc.cid " +
					"and rc.rid = ? " +
				"ORDER BY c.name ASC; ";

			return data.sqlTransaction(new Transaction<List<Category>>() {
				public List<Category> exec(SQLiteDatabase db) {
					Cursor c1 = db.rawQuery(stmt.replaceAll("\\?", recipeId + ""), null);
					List<Category> list = createListFromCursor(c1);
					c1.close();
					return list;
				}
			});
		}

		protected void addRecipeToCategory(final long recipeId, final Category c) {
			final String stmt =
				"INSERT OR IGNORE INTO RecipeCategory(rid, cid) " +
				"VALUES (?, ?); ";

			data.sqlTransaction(new Transaction<Void>() {
				public Void exec(SQLiteDatabase db) {
					db.execSQL(stmt, new String[] {recipeId + "", c.getId() + ""});
					return null;
				}
			});
		}

		protected void removeRecipeFromCategory(final long recipeId, final Category c) {
			final String stmt =
				"DELETE FROM RecipeCategory " +
				"WHERE rid = ? " +
					"and cid = ?; ";

			data.sqlTransaction(new Transaction<Void>() {
				public Void exec(SQLiteDatabase db) {
					db.execSQL(stmt, new String[] {recipeId + "", c.getId() + ""});
					return null;
				}
			});
		}

		protected Category createOrRetrieveCategory(final String category) {
			final String retrieveStmt =
				"SELECT c.cid, c.name, c.description " +
				"FROM Category c " +
				"WHERE name = ?; ";

			return data.sqlTransaction(new Transaction<Category>() {
				public Category exec(SQLiteDatabase db) {
					CategoryImpl ci = null;
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
					return ci;
				}
			});
		}

		protected Category getCategoryByName(final String name) {
			final String stmt =
				"SELECT c.cid, c.name, c.description " +
				"FROM Category c " +
				"WHERE c.name = ?;";

			return data.sqlTransaction(new Transaction<Category>() {
				public Category exec(SQLiteDatabase db) {
					CategoryImpl ci = null;
					Cursor c = db.rawQuery(stmt, new String[] {name});
					if(c.moveToNext()) {
						ci = new CategoryImpl(c.getLong(0));
						ci.name = c.getString(1);
						ci.description = c.getString(2);
					}
					c.close();
					return ci;
				}
			});
		}

		protected Category getCategoryById(final long id) {
			final String stmt =
				"SELECT c.cid, c.name, c.description " +
				"FROM Category c " +
				"WHERE c.cid = ?;";

			return data.sqlTransaction(new Transaction<Category>() {
				public Category exec(SQLiteDatabase db) {
					CategoryImpl ci = null;
					Cursor c = db.rawQuery(stmt, new String[] {id + ""});
					if(c.moveToNext()) {
						ci = new CategoryImpl(id);
						ci.name = c.getString(1);
						ci.description = c.getString(2);
					}
					c.close();
					return ci;
				}
			});
		}
	}
}
