package org.wrowclif.recipebox.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.AppData.Transaction;
import org.wrowclif.recipebox.Instruction;
import org.wrowclif.recipebox.Ingredient;
import org.wrowclif.recipebox.Recipe;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class InstructionImpl implements Instruction {

	protected static final InstructionFactory factory;

	static {
		factory = new InstructionFactory();
	}

	protected long id;
	protected String text;
	protected int num;

	private InstructionImpl(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;

		ContentValues values = new ContentValues();
		values.put("text", text);
		factory.data.itemUpdate(values, "Instruction", "iid=?", new String[] {id + ""}, "setText");
	}

	public List<Ingredient> getIngredientsUsed() {
		final String stmt =
			"SELECT i.iid, i.name " +
			"FROM InstructionIngredients ii, Ingredient i " +
			"WHERE ii.ingrid = i.iid " +
				"and ii.instid = ? " +
			"ORDER BY ii.num ASC; ";

		return factory.data.sqlTransaction(new Transaction<List<Ingredient>>() {
			public List<Ingredient> exec(SQLiteDatabase db) {
				Cursor c = db.rawQuery(stmt.replaceAll("\\?", id + ""), null);
				List<Ingredient> list = IngredientImpl.factory.createListFromCursor(c);
				c.close();
				return list;
			}
		});
	}

	public void setIngredientsUsed(final List<Ingredient> order) {
		final String removeAllStmt =
			"DELETE FROM InstructionIngredients " +
			"WHERE instid = ?;";
		final String insertStmt =
			"INSERT OR REPLACE INTO InstructionIngredients(instid, ingrid, num) " +
			"VALUES(?, ?, ?);";

		factory.data.sqlTransaction(new Transaction<Void>() {
			public Void exec(SQLiteDatabase db) {
				db.execSQL(removeAllStmt, new String[] {id + ""});
				for(int i = 0; i < order.size(); i++) {
					db.execSQL(insertStmt, new String[] {id + "", order.get(i).getId() + "", i + ""});
				}
				return null;
			}
		});
	}

	protected static class InstructionFactory {

		protected AppData data;

		private InstructionFactory() {
			data = AppData.getSingleton();
		}

		protected List<Instruction> createListFromCursor(Cursor c) {
			List<Instruction> list = new ArrayList<Instruction>(c.getCount());
			while(c.moveToNext()) {
				InstructionImpl i = new InstructionImpl(c.getLong(0));
				i.text = c.getString(1);
				list.add(i);
			}
			return list;
		}

		protected List<Instruction> getRecipeSteps(final long recipeId) {
			final String stmt =
				"SELECT i.iid, i.text " +
				"FROM Instruction i " +
				"WHERE i.rid = ? " +
				"ORDER BY i.num ASC;";

			return data.sqlTransaction(new Transaction<List<Instruction>>() {
				public List<Instruction> exec(SQLiteDatabase db) {
					Cursor c1 = db.rawQuery(stmt.replaceAll("\\?", recipeId + ""), null);
					List<Instruction> list = createListFromCursor(c1);
					c1.close();
					return list;
				}
			});
		}

		protected Instruction addInstruction(final long recipeId) {
			final String selectStmt =
				"SELECT r.maxinstruction " +
				"FROM Recipe r " +
				"WHERE r.rid = ?; ";

			final String updateStmt =
				"UPDATE Recipe " +
				"SET maxinstruction = maxinstruction + 1 " +
				"WHERE rid = ?; ";

			final ContentValues values = new ContentValues();
			values.put("text", "");
			values.put("rid", recipeId);

			return data.sqlTransaction(new Transaction<Instruction>() {
				public Instruction exec(SQLiteDatabase db) {
					Cursor c = db.rawQuery(selectStmt.replaceAll("\\?", recipeId + ""), null);
					c.moveToNext();
					int max = c.getInt(0);
					c.close();
					max = max + 1;
					db.execSQL(updateStmt, new Object[] {recipeId});
					values.put("num", max);
					long iid = db.insert("Instruction", null, values);
					return new InstructionImpl(iid);
				}
			});
		}

		protected void removeInstruction(final Instruction i) {
			final String stmt =
				"DELETE FROM Instruction " +
				"WHERE iid = ?; ";

			data.sqlTransaction(new Transaction<Void>() {
				public Void exec(SQLiteDatabase db) {
					db.execSQL(stmt, new Object[] {i.getId()});
					return null;
				}
			});
		}

		protected void swapInstructionPositions(final Instruction a, final Instruction b) {
			final String sameRecipeStmt =
				"SELECT i1.rid " +
				"FROM Instruction i1, Instruction i2 " +
				"WHERE i1.iid = ? " +
					"and i2.iid = ? " +
					"and i1.rid = i2.rid;";

			final String getNumStmt =
				"SELECT i.num " +
				"FROM Instruction i " +
				"WHERE i.iid = ?;";

			final String setNumStmt =
				"UPDATE Instruction " +
				"SET num = ? " +
				"WHERE iid = ?;";

			if(a.getId() == b.getId()) {
				return;
			}

			data.sqlTransaction(new Transaction<Void>() {
				public Void exec(SQLiteDatabase db) {
					Cursor c = db.rawQuery(sameRecipeStmt, new String[] {a.getId() + "", b.getId() + ""});
					if(c.getCount() == 0) {
						c.close();
						throw new IllegalArgumentException("Instructions belong to different recipes");
					}
					c.close();
					c = db.rawQuery(getNumStmt, new String[] {a.getId() + ""});
					c.moveToNext();
					int numA = c.getInt(0);
					c.close();
					c = db.rawQuery(getNumStmt, new String[] {b.getId() + ""});
					c.moveToNext();
					int numB = c.getInt(0);
					c.close();
					db.execSQL(setNumStmt, new Object[] {numB, a.getId()});
					db.execSQL(setNumStmt, new Object[] {numA, b.getId()});
					return null;
				}
			});
		}

		protected void reorderInstructions(final long recipeId, final List<Instruction> order) {
			final String getInstructionIdsStmt =
				"SELECT i.iid " +
				"FROM Instruction i " +
				"WHERE i.rid = ?; ";
			final String instructionStmt =
				"UPDATE Instruction " +
					"SET num = ? " +
					"WHERE iid = ? " +
						"and rid = ?; ";
			final String recipeStmt =
				"UPDATE Recipe " +
					"SET maxinstruction = ? " +
					"WHERE rid = ?; ";

			// Check that all values in 'order' are the same as those in the database currently.
			// Error otherwise.
			final long[] orderIds = new long[order.size()];
			for(int i = 0; i < order.size(); i++) {
				orderIds[i] = order.get(i).getId();
			}
			Arrays.sort(orderIds);

			boolean valid = data.sqlTransaction(new Transaction<Boolean>() {
				public Boolean exec(SQLiteDatabase db) {
					boolean valid = false;
					Cursor c1 = db.rawQuery(getInstructionIdsStmt.replaceAll("\\?", recipeId + ""), null);
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
							db.execSQL(instructionStmt, new Object[] {i, order.get(i).getId(), recipeId});
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
