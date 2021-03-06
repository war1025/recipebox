package org.wrowclif.recipebox.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.util.Log;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.AppData.Transaction;
import org.wrowclif.recipebox.Ingredient;

import java.util.List;
import java.util.ArrayList;

public class IngredientImpl implements Ingredient {
	private static final String LOG_TAG = "Recipebox IngredientImpl";

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
		final String stmt =
			"SELECT i.iid, i.name " +
			"FROM Ingredient i, SimilarIngredients si " +
			"WHERE si.iid1 = i.iid " +
				"and si.iid2 = ? " +
			"UNION " +
			"SELECT i.iid, i.name " +
			"FROM Ingredient i, SimilarIngredients si " +
			"WHERE si.iid1 = ? " +
				"and si.iid2 = i.iid; ";

		return factory.data.sqlTransaction(new Transaction<List<Ingredient>>() {
			public List<Ingredient> exec(SQLiteDatabase db) {
				Cursor c = db.rawQuery(stmt.replaceAll("\\?", id + ""), null);
				List<Ingredient> list = factory.createListFromCursor(c);
				c.close();
				return list;
			}
		});
	}

	public void addSimilarIngredient(final Ingredient other) {
		final String stmt =
			"INSERT OR IGNORE INTO SimilarIngredients(iid1, iid2) " +
			"VALUES(?, ?); ";

		final long lowId;
		final long hiId;

		if(id < other.getId()) {
			lowId = id;
			hiId = other.getId();
		} else if(other.getId() < id) {
			lowId = other.getId();
			hiId = id;
		} else {
			throw new IllegalArgumentException("Ingredient cannot be similar to itself");
		}

		factory.data.sqlTransaction(new Transaction<Void>() {
			public Void exec(SQLiteDatabase db) {
				db.execSQL(stmt, new Object[] {lowId, hiId});
				return null;
			}
		});
	}

	public void removeSimilarIngredient(final Ingredient other) {
		final String stmt =
			"DELETE FROM SimilarIngredients " +
			"WHERE iid1 = ? " +
				"and iid2 = ?; ";

		final long lowId;
		final long hiId;

		if(id < other.getId()) {
			lowId = id;
			hiId = other.getId();
		} else if(other.getId() < id) {
			lowId = other.getId();
			hiId = id;
		} else {
			throw new IllegalArgumentException("Ingredient cannot be similar to itself");
		}

		factory.data.sqlTransaction(new Transaction<Void>() {
			public Void exec(SQLiteDatabase db) {
				db.execSQL(stmt, new Object[] {lowId, hiId});
				return null;
			}
		});
	}

	public boolean delete() {
		final String useCountStmt =
			"SELECT Count(*) " +
			"FROM RecipeIngredients ri " +
			"WHERE iid = ?; ";

		final String useCountCorrectionStmt =
			"UPDATE Ingredient " +
			"SET usecount = ? " +
			"WHERE iid = ?;";

		final String deleteInstructionIngredients =
			"DELETE FROM InstructionIngredients " +
			"WHERE ingrid = ?; ";

		final String deleteStmt =
			"DELETE FROM Ingredient " +
			"WHERE iid = ?; ";

		return factory.data.sqlTransaction(new Transaction<Boolean>() {
			public Boolean exec(SQLiteDatabase db) {
				Cursor c = db.rawQuery(useCountStmt, new String[] {id + ""});
				try {
					if(c.moveToNext() && (c.getInt(0) != 0)) {
						db.execSQL(useCountCorrectionStmt, new Object[] {c.getInt(0), id});
						Log.e(LOG_TAG, "Attempted to remove an ingredient that was still in use: " + name);
						return false;
					}
				} finally {
					c.close();
				}

				for(Ingredient i : IngredientImpl.this.getSimilarIngredients()) {
					IngredientImpl.this.removeSimilarIngredient(i);
				}

				db.execSQL(deleteInstructionIngredients, new Object[] {id});

				db.execSQL(deleteStmt, new Object[] {id});

				return true;
			}
		});
	}

	protected static class IngredientFactory {

		protected AppData data;

		private IngredientFactory() {
			data = AppData.getSingleton();
		}

		protected String sanitizeIngredientName(String name) {
			return name.trim().replaceAll("\\s", " ").toLowerCase();
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

		protected Ingredient getIngredientByName(String name) {
			final String retrieveStmt =
				"SELECT i.iid " +
				"FROM Ingredient i " +
				"WHERE i.name = ?; ";

			final String sanitizedName = sanitizeIngredientName(name);

			return data.sqlTransaction(new Transaction<Ingredient>() {
				public Ingredient exec(SQLiteDatabase db) {
					IngredientImpl ii = null;
					Cursor c = db.rawQuery(retrieveStmt, new String[] {sanitizedName});
					if(c.moveToNext()) {
						ii = new IngredientImpl(c.getLong(0));
						ii.name = sanitizedName;
					}
					c.close();
					return ii;
				}
			});
		}

		protected Ingredient createOrRetrieveIngredient(String name) {
			final String retrieveStmt =
				"SELECT i.iid " +
				"FROM Ingredient i " +
				"WHERE i.name = ?; ";

			final String sanitizedName = sanitizeIngredientName(name);

			return data.sqlTransaction(new Transaction<Ingredient>() {
				public Ingredient exec(SQLiteDatabase db) {
					IngredientImpl ii = null;
					Cursor c = db.rawQuery(retrieveStmt, new String[] {sanitizedName});
					if(c.moveToNext()) {
						ii = new IngredientImpl(c.getLong(0));
					} else {
						ContentValues values = new ContentValues();
						values.put("name", sanitizedName);
						long id = db.insert("Ingredient", null, values);
						ii = new IngredientImpl(id);
					}
					ii.name = sanitizedName;
					c.close();
					return ii;
				}
			});
		}

	}
}


