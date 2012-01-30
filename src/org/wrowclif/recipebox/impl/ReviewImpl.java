package org.wrowclif.recipebox.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Review;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.db.RecipeBoxOpenHelper;

import java.util.List;
import java.util.ArrayList;

public class ReviewImpl implements Review {

	protected static final ReviewFactory factory;

	static {
		factory = new ReviewFactory();
	}

	private long id;
	private int rating;
	private long date;
	private String comments;

	public ReviewImpl() {

	}

	public long getId() {
		return id;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		if(rating < 0 || rating > 10) {
			throw new IllegalArgumentException("Rating must be between 0 and 10 inclusive");
		}
		this.rating = rating;

		ContentValues values = new ContentValues();
		values.put("rating", rating);
		itemUpdate(values, "setRating");
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;

		ContentValues values = new ContentValues();
		values.put("date", date);
		itemUpdate(values, "setDate");
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;

		ContentValues values = new ContentValues();
		values.put("comments", comments);
		itemUpdate(values, "setComments");
	}

	private void itemUpdate(ContentValues values, String operation) {
		SQLiteDatabase db = factory.helper.getWritableDatabase();
		int ret = db.update("Review", values, "revid=?", new String[] {id + ""});
		if(ret != 1) {
			throw new IllegalStateException("Review " + operation + " should have affected only row " + id +
												" but affected " + ret + " rows");
		}
	}

	protected static class ReviewFactory {

		protected RecipeBoxOpenHelper helper;

		private ReviewFactory() {
			helper = AppData.getSingleton().getOpenHelper();
		}

		protected List<Review> createListFromCursor(Cursor c) {
			// r.revid, r.date, r.rating, r.comments
			List<Review> list = new ArrayList<Review>(c.getCount());
			while(c.moveToNext()) {
				ReviewImpl ri = new ReviewImpl();
				ri.id = c.getLong(0);
				ri.date = c.getLong(1);
				ri.rating = c.getInt(2);
				ri.comments = c.getString(3);

				list.add(ri);
			}
			return list;
		}

		protected List<Review> getReviews(long recipeId) {
			String stmt =
				"SELECT r.revid, r.date, r.rating, r.comments " +
				"FROM Review r " +
				"WHERE r.rid = ? " +
				"ORDER BY r.date ASC; ";

			List<Review> list = null;
			SQLiteDatabase db = factory.helper.getWritableDatabase();
			db.beginTransaction();
				Cursor c = db.rawQuery(stmt, new String[] {recipeId + ""});
				list = createListFromCursor(c);
				c.close();
			db.setTransactionSuccessful();
			db.endTransaction();
			return list;
		}

		protected Review addReview(long recipeId) {
			SQLiteDatabase db = factory.helper.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("comments", "");
			values.put("rating", 10);
			long id = db.insert("Review", null, values);

			ReviewImpl ri = new ReviewImpl();
			ri.id = id;
			ri.comments = "";
			ri.rating = 10;

			return ri;
		}

		protected void removeReview(Review r) {
			String stmt =
				"DELETE FROM Review " +
					"WHERE revid = ?; ";

			SQLiteDatabase db = factory.helper.getWritableDatabase();
			db.beginTransaction();
				db.execSQL(stmt, new Object[] {r.getId()});
			db.setTransactionSuccessful();
			db.endTransaction();
		}
	}

}
