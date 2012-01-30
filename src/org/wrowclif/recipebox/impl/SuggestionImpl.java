package org.wrowclif.recipebox.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.Suggestion;
import org.wrowclif.recipebox.db.RecipeBoxOpenHelper;

import java.util.List;
import java.util.ArrayList;

public class SuggestionImpl implements Suggestion {

	protected static final SuggestionFactory factory;

	static {
		factory = new SuggestionFactory();
	}

	private Recipe r1;
	private Recipe r2;
	private long lowId;
	private long hiId;
	private String comments;

	private SuggestionImpl(Recipe r1, Recipe r2) {
		this.r1 = r1;
		this.r2 = r2;
		if(r1.getId() < r2.getId()) {
			this.lowId = r1.getId();
			this.hiId = r2.getId();
		} else {
			this.lowId = r2.getId();
			this.hiId = r2.getId();
		}
		this.comments = "";
	}

	public Recipe getOriginalRecipe() {
		return r1;
	}

	public Recipe getSuggestedRecipe() {
		return r2;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String text) {
		this.comments = text;

		ContentValues values = new ContentValues();
		values.put("comments", text);
		SQLiteDatabase db = factory.helper.getWritableDatabase();
		int ret = db.update("SuggestedWith", values, "rid1 = ? and rid2 = ?",
						new String[] {lowId + "", hiId + ""});
		if(ret != 1) {
			throw new IllegalStateException("SuggestedWith setComments should have affected only one row" +
												" but affected " + ret + " rows");
		}
	}

	protected static class SuggestionFactory {

		protected RecipeBoxOpenHelper helper;

		private SuggestionFactory() {
			helper = AppData.getSingleton().getOpenHelper();
		}

		protected List<Suggestion> createListFromCursor(Recipe r, Cursor c) {
			// r.rid, sw.comments, r.name, r.description, r.preptime, r.cooktime, r.cost, r.vid
			List<Suggestion> list = new ArrayList<Suggestion>(c.getCount());
			ContentValues values = new ContentValues();
			while(c.moveToNext()) {
				values.clear();
				values.put("rid", c.getLong(0));
				values.put("name", c.getString(2));
				values.put("description", c.getString(3));
				values.put("preptime", c.getInt(4));
				values.put("cooktime", c.getInt(5));
				values.put("vid", c.getLong(6));
				Recipe r2 = RecipeImpl.factory.createRecipeFromData(values);
				SuggestionImpl si = new SuggestionImpl(r, r2);
				si.comments = c.getString(1);

				list.add(si);
			}

			return list;
		}

		protected List<Suggestion> getSuggestedWith(Recipe r) {
			String stmt =
				"SELECT r.rid, sw.comments, r.name, r.description, r.preptime, r.cooktime, r.cost, r.vid " +
				"FROM Recipe r, SuggestedWith sw " +
				"WHERE sw.rid1 = ? " +
					"and sw.rid2 = r.rid " +
				"UNION " +
				"SELECT r.rid, sw.comments, r.name, r.description, r.preptime, r.cooktime, r.cost, r.vid " +
				"FROM Recipe r, SuggestedWith sw " +
				"WHERE sw.rid1 = r.rid " +
					"and sw.rid2 = ?; ";

			List<Suggestion> list = null;
			SQLiteDatabase db = factory.helper.getWritableDatabase();
			db.beginTransaction();
				Cursor c1 = db.rawQuery(stmt.replaceAll("\\?",r.getId() + ""), null);
				list = createListFromCursor(r, c1);
				c1.close();
			db.setTransactionSuccessful();
			db.endTransaction();
			return list;
		}

		protected Suggestion addSuggestion(Recipe r1, Recipe r2) {
			if(r1.getId() == r2.getId()) {
				throw new IllegalArgumentException("Cannot suggest a recipe with itself");
			}

			String stmt =
				"INSERT OR REPLACE INTO SuggestedWith(rid1, rid2) " +
					"VALUES (?, ?); ";
			SuggestionImpl si = new SuggestionImpl(r1, r2);
			SQLiteDatabase db = factory.helper.getWritableDatabase();
			db.beginTransaction();
				db.execSQL(stmt, new Object[] {si.lowId, si.hiId});
			db.setTransactionSuccessful();
			db.endTransaction();
			return si;
		}

		protected void removeSuggestion(Recipe r1, Recipe r2) {
			String stmt =
				"DELETE FROM SuggestedWith " +
					"WHERE rid1 = ? " +
						"and rid2 = ?; ";
			SuggestionImpl si = new SuggestionImpl(r1, r2);
			SQLiteDatabase db = factory.helper.getWritableDatabase();
			db.beginTransaction();
				db.execSQL(stmt, new Object[] {si.lowId, si.hiId});
			db.setTransactionSuccessful();
			db.endTransaction();
		}
	}
}


