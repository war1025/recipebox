package org.wrowclif.recipebox.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Ingredient;
import org.wrowclif.recipebox.db.RecipeBoxOpenHelper;

import java.util.List;
import java.util.ArrayList;

public class IngredientImpl implements Ingredient {

	protected static final IngredientFactory factory;

	static {
		factory = new IngredientFactory();
	}

	private long id;
	private String name;

	private IngredientImpl(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<Ingredient> getSimilarIngredients() {
		String stmt =
			"SELECT i.iid, i.name " +
			"FROM Ingredient i, SimilarIngredients si " +
			"WHERE si.iid1 = i.iid " +
				"and si.iid2 = ? " +
			"UNION " +
			"SELECT i.iid, i.name " +
			"FROM Ingredient i, SimilarIngredients si " +
			"WHERE si.iid1 = ? " +
				"and si.iid2 = i.iid; ";

		List<Ingredient> list = null;
		SQLiteDatabase db = factory.helper.getWritableDatabase();
		db.beginTransaction();
			Cursor c = db.rawQuery(stmt.replaceAll("\\?", id + ""), null);
			list = factory.createListFromCursor(c);
			c.close();
		db.setTransactionSuccessful();
		db.endTransaction();
		return list;
	}

	public void addSimilarIngredient(Ingredient other) {
		String stmt =
			"INSERT OR REPLACE INTO SimilarIngredients(iid1, iid2) " +
			"VALUES(?, ?); ";

		long lowId;
		long hiId;

		if(id < other.getId()) {
			lowId = id;
			hiId = other.getId();
		} else if(other.getId() < id) {
			lowId = other.getId();
			hiId = id;
		} else {
			throw new IllegalArgumentException("Ingredient cannot be similar to itself");
		}

		SQLiteDatabase db = factory.helper.getWritableDatabase();
		db.beginTransaction();
			db.execSQL(stmt, new Object[] {lowId, hiId});
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public void removeSimilarIngredient(Ingredient other) {
		String stmt =
			"DELETE FROM SimilarIngredients " +
			"WHERE iid1 = ? " +
				"and iid2 = ?; ";

		long lowId;
		long hiId;

		if(id < other.getId()) {
			lowId = id;
			hiId = other.getId();
		} else if(other.getId() < id) {
			lowId = other.getId();
			hiId = id;
		} else {
			throw new IllegalArgumentException("Ingredient cannot be similar to itself");
		}

		SQLiteDatabase db = factory.helper.getWritableDatabase();
		db.beginTransaction();
			db.execSQL(stmt, new Object[] {lowId, hiId});
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	protected static class IngredientFactory {

		protected RecipeBoxOpenHelper helper;

		private IngredientFactory() {
			helper = AppData.getSingleton().getOpenHelper();
		}

		protected List<Ingredient> createListFromCursor(Cursor c) {
			List<Ingredient> list = new ArrayList<Ingredient>(c.getCount());

			while(c.moveToNext()) {
				IngredientImpl ii = new IngredientImpl(c.getLong(0));
				ii.name = c.getString(1);

				list.add(ii);
			}

			return list;
		}

		protected Ingredient createFromData(ContentValues v) {
			IngredientImpl ii = new IngredientImpl(v.getAsLong("iid"));
			ii.name = v.getAsString("name");

			return ii;
		}

		protected Ingredient createOrRetrieveIngredient(String name) {
			String retrieveStmt =
				"SELECT i.iid " +
				"FROM Ingredient i " +
				"WHERE i.name = ?; ";

			IngredientImpl ii = null;
			SQLiteDatabase db = factory.helper.getWritableDatabase();
			db.beginTransaction();
				Cursor c = db.rawQuery(retrieveStmt, new String[] {name});
				if(c.moveToNext()) {
					ii = new IngredientImpl(c.getLong(0));
				} else {
					ContentValues values = new ContentValues();
					values.put("name", name);
					long id = db.insert("Ingredient", null, values);
					ii = new IngredientImpl(id);
				}
				ii.name = name;
				c.close();
			db.setTransactionSuccessful();
			db.endTransaction();
			return ii;
		}

	}
}


