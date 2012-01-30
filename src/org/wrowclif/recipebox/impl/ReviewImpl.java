package org.wrowclif.recipebox.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.AppData.Transaction;
import org.wrowclif.recipebox.Review;
import org.wrowclif.recipebox.Recipe;

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
		factory.data.itemUpdate(values, "Review", "revid=?", new String[] {id + ""}, operation);
	}

	protected static class ReviewFactory {

		protected AppData data;

		private ReviewFactory() {
			data = AppData.getSingleton();
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

		protected List<Review> getReviews(final long recipeId) {
			final String stmt =
				"SELECT r.revid, r.date, r.rating, r.comments " +
				"FROM Review r " +
				"WHERE r.rid = ? " +
				"ORDER BY r.date ASC; ";

			return data.sqlTransaction(new Transaction<List<Review>>() {
				public List<Review> exec(SQLiteDatabase db) {
					Cursor c = db.rawQuery(stmt, new String[] {recipeId + ""});
					List<Review> list = createListFromCursor(c);
					c.close();
					return list;
				}
			});
		}

		protected Review addReview(final long recipeId) {
			return data.sqlTransaction(new Transaction<Review>() {
				public Review exec(SQLiteDatabase db) {
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
			});
		}

		protected void removeReview(final Review r) {
			final String stmt =
				"DELETE FROM Review " +
					"WHERE revid = ?; ";

			data.sqlTransaction(new Transaction<Void>() {
				public Void exec(SQLiteDatabase db) {
					db.execSQL(stmt, new Object[] {r.getId()});
					return null;
				}
			});
		}
	}
}
