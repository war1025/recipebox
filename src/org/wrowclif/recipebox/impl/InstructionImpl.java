package org.wrowclif.recipebox.impl;

import org.wrowclif.recipebox.Instruction;
import org.wrowclif.recipebox.Ingredient;

import java.util.List;
import java.util.ArrayList;

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
		SQLiteDatabase db = factory.helper.getWriteableDatabase();
		int ret = db.update("Instruction", values, "iid=?", new String[] {id + ""});
		if(ret != 1) {
			throw new IllegalStateException("Instruction setText should have affected only row " + id +
												" but affected " + ret + " rows");
		}
	}

	public List<Ingredient> getIngredientsUsed() {
		List<Recipe> list = null;
		String stmt =
			"SELECT i.iid, i.name " +
			"FROM InstructionIngredients ii, Ingredient i " +
			"WHERE ii.ingrid = i.iid " +
				"and ii.instid = ? " +
			"ORDER BY ii.num ASC; ";
		SQLiteDatabase db = factory.helper.getWriteableDatabase();
		db.beginTransaction();
			Cursor c = db.rawQuery(stmt.replaceAll("?", id), null);
			list = IngredientImpl.factory.createListFromCursor(c);
		db.endTransaction();
		return list;
	}

	public void setIngredientsUsed(List<Ingredient> order) {
		String removeAllStmt =
			"DELETE FROM InstructionIngredients ii " +
			"WHERE ii.instid = ?;";
		String insertStmt =
			"INSERT OR REPLACE INTO InstructionIngredients(instid, ingrid, num) " +
			"VALUES(?, ?, ?);";
		SQLiteDatabase db = factory.helper.getWriteableDatabase();
		db.beginTransaction();
			db.execSQL(removeAllStmt, new String[] {id + ""});
			for(int i = 0; i < order.size(); i++) {
				db.execSQL(insertStmt, new String[] {id + "", order.get(i).getId() + "", i + ""});
			}
		db.endTransaction();
	}

	protected static class InstructionFactory {

		protected RecipeBoxOpenHelper helper;

		private InstructionFactory() {
			helper = AppData.singleton().getOpenHelper();
		}

		protected List<Instruction> createListFromCursor(Cursor c) {
			List<Instruction> list = new ArrayList<Instruction>(c.getCount());
			while(c.moveToNext()) {
				Instruction i = new Instruction(c.getAsLong(1));
				i.text = c.getAsString(2);
				list.add(i);
			}
			return list;
		}

		protected List<Instruction> getRecipeSteps(long recipeId) {
			List<Instruction> list = null;
			String stmt =
				"SELECT i.iid, i.text " +
				"FROM Instruction i " +
				"WHERE i.rid = ? " +
				"ORDER BY i.num ASC;";
			SQLiteDatabase db = factory.helper.getWriteableDatabase();
			db.beginTransaction();
				Cursor c1 = db.rawQuery(stmt.replaceAll("?", recipeId), null);
				list = createListFromCursor(c1);
			db.endTransaction();
			return list;
		}

		protected Instruction addInstruction(long recipeId) {
			String insertStmt =
				"INSERT INTO Instruction(text, num, rid) " +
				"VALUES ('', ?, ?); ";
			String selectStmt =
				"SELECT r.maxinstruction " +
				"FROM Recipe r " +
				"WHERE r.rid = ?; ";
			String updateStmt =
				"UPDATE Recipe r " +
				"SET (r.maxinstruction = ?) " +
				"WHERE r.rid = ?; ";

			Instruction result = null;
			ContentValues values = new ContentValues();
			values.put("text", "");
			values.put("rid", recipeId);
			SQLiteDatabase db = factory.helper.getWriteableDatabase();
			db.beginTransaction();
				int max = db.rawQuery(selectStmt.replaceAll("?", recipeId), null);
				max = max + 1;
				db.execSQL(updateStmt, new Object[] {max, recipeId});
				values.put("num", max);
				long iid = db.insert("Instruction", null, values);
				result = new InstructionImpl(iid);
			db.endTransaction();
			return result;
		}

		protected void removeInstruction(Instruction i) {
			String stmt =
				"DELETE FROM Instruction i " +
				"WHERE i.iid = ?; ";
			db.execSQL(stmt, new Object[] {i.getId()});
		}

		protected void reorderInstructions(long recipeId, List<Instruction> order) {
			String instructionStmt =
				"UPDATE Instruction i " +
					"SET (i.num = ?) " +
					"WHERE i.iid = ? " +
						"and i.rid = ?; ";
			String recipeStmt =
				"UPDATE Recipe r " +
					"SET (r.maxinstruction = ?) " +
					"WHERE r.rid = ?; ";

			SQLiteDatabase db = factory.helper.getWriteableDatabase();
			db.beginTransaction();
				for(int i = 0; i < order.size(); i++) {
					db.execSQL(instructionStmt, new Object[] {i, order.get(i).getId(), recipeId});
				}
				db.execSQL(recipeStmt, new Object[] {order.size() - 1, recipeId});
			db.endTransaction();
		}
	}

}
