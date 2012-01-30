package org.wrowclif.recipebox.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.database.SQLException;
import android.content.Context;
import android.util.Log;

public class RecipeBoxOpenHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "RECIPEBOX";
	private static final String CREATE_STATEMENT =
		"CREATE TABLE VariantGroup(" +
			"vid INTEGER PRIMARY KEY);" +

		"CREATE TABLE Recipe(" +
			"rid INTEGER PRIMARY KEY AUTOINCREMENT," +
			"name TEXT NOT NULL," +
			"description TEXT NOT NULL DEFAULT ''," +
			"cost INTEGER NOT NULL DEFAULT 0," +
			"preptime INTEGER NOT NULL DEFAULT 0," +
			"cooktime INTEGER NOT NULL DEFAULT 0," +
			"maxinstruction INTEGER DEFAULT 0," +
			"maxingredient INTEGER DEFAULT 0," +
			"vid INTEGER REFERENCES VariantGroup(vid));" +

		"CREATE TABLE Category(" +
			"cid INTEGER PRIMARY KEY AUTOINCREMENT," +
			"name TEXT NOT NULL," +
			"description TEXT NOT NULL);" +

		"CREATE TABLE Ingredient(" +
			"iid INTEGER PRIMARY KEY AUTOINCREMENT," +
			"name TEXT NOT NULL UNIQUE," +
			"usecount INTEGER NOT NULL DEFAULT 0);" +

		"CREATE TABLE Instruction(" +
			"iid INTEGER PRIMARY KEY AUTOINCREMENT," +
			"text TEXT NOT NULL," +
			"num INTEGER NOT NULL," +
			"rid INTEGER REFERENCES Recipe(rid) NOT NULL);" +

		"CREATE TABLE Review(" +
			"revid INTEGER PRIMARY KEY AUTOINCREMENT," +
			"date INTEGER DEFAULT CURRENT_TIMESTAMP NOT NULL," +
			"rating INTEGER(0,10) NOT NULL," +
			"comments TEXT NOT NULL," +
			"rid INTEGER REFERENCES Recipe(rid) NOT NULL);" +

		"CREATE TABLE RecipeCategory(" +
			"rid INTEGER REFERENCES Recipe(rid)," +
			"cid INTEGER REFERENCES Category(cid)," +
			"PRIMARY KEY(rid, cid));" +

		"CREATE TABLE SuggestedWith(" +
			"rid1 INTEGER REFERENCES Recipe(rid)," +
			"rid2 INTEGER REFERENCES Recipe(rid)," +
			"comments TEXT NOT NULL DEFAULT ''," +
			"PRIMARY KEY(rid1, rid2)," +
			"CHECK(rid1 < rid2));" +

		"CREATE TABLE Unit(" +
			"uid INTEGER PRIMARY KEY AUTOINCREMENT," +
			"name TEXT NOT NULL UNIQUE, " +
			"type INTEGER(0,2) NOT NULL DEFAULT 0, " +
			"factor REAL NOT NULL CHECK(factor > 0), " +
			"minfraction INTEGER NOT NULL);" +

		"CREATE TABLE RecipeIngredients(" +
			"rid INTEGER REFERENCES Recipe(rid)," +
			"iid INTEGER REFERENCES Ingredient(iid)," +
			"amount REAL NOT NULL DEFAULT 0," +
			"uid INTEGER REFERENCES Unit(uid) NOT NULL," +
			"num INTEGER NOT NULL," +
			"PRIMARY KEY(rid,iid));" +

		"CREATE TABLE InstructionIngredients(" +
			"instid INTEGER REFERENCES Instruction(iid)," +
			"ingrid INTEGER REFERENCES Ingredient(iid)," +
			"num INTEGER NOT NULL," +
			"PRIMARY KEY(instid, ingrid));" +

		"CREATE TABLE SimilarIngredients(" +
			"iid1 INTEGER REFERENCES Ingredient(iid)," +
			"iid2 INTEGER REFERENCES Ingredient(iid)," +
			"PRIMARY KEY(iid1, iid2)," +
			"CHECK(iid1 < iid2));";

	public RecipeBoxOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		String[] statements = CREATE_STATEMENT.split(";");
		for(String stmt : statements) {
			db.execSQL(stmt);
		}
	}

	public void onUpgrade(SQLiteDatabase db, int oldv, int newv) {

	}

}
