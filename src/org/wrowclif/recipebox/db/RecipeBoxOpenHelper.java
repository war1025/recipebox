package org.wrowclif.recipebox.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.database.SQLException;
import android.content.Context;

public class RecipeBoxOpenHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "RECIPEBOX";
	private static final String CREATE_STATEMENT =
		"CREATE TABLE VariantGroup(" +
			"vid INTEGER PRIMARY KEY);" +

		"CREATE TABLE Recipe(" +
			"rid INTEGER PRIMARY KEY AUTOINCREMENT," +
			"name TEXT NOT NULL," +
			"description TEXT," +
			"cost INTEGER," +
			"preptime INTEGER," +
			"cooktime INTEGER," +
			"maxinstruction INTEGER DEFAULT 0," +
			"maxingredient INTEGER DEFAULT 0," +
			"vid INTEGER REFERENCES VariantGroup(vid));" +

		"CREATE TABLE Category(" +
			"cid INTEGER PRIMARY KEY AUTOINCREMENT," +
			"name TEXT NOT NULL," +
			"description TEXT);" +

		"CREATE TABLE Ingredient(" +
			"iid INTEGER PRIMARY KEY AUTOINCREMENT," +
			"name TEXT NOT NULL UNIQUE);" +

		"CREATE TABLE Instruction(" +
			"iid INTEGER PRIMARY KEY AUTOINCREMENT," +
			"text TEXT NOT NULL," +
			"num INTEGER NOT NULL," +
			"rid INTEGER REFERENCES Recipe(rid) NOT NULL);" +

		"CREATE TABLE Review(" +
			"revid INTGER PRIMARY KEY AUTOINCREMENT," +
			"date INTEGER DEFAULT CURRENT_TIMESTAMP NOT NULL," +
			"rating INTEGER(0,10) NOT NULL," +
			"comments TEXT," +
			"rid INTEGER REFERENCES Recipe(rid) NOT NULL);" +

		"CREATE TABLE RecipeCategory(" +
			"rid INTEGER REFERENCES Recipe(rid)," +
			"cid INTEGER REFERENCES Category(cid)," +
			"PRIMARY KEY(rid, cid));" +

		"CREATE TABLE SuggestedWith(" +
			"rid1 INTEGER REFERENCES Recipe(rid)," +
			"rid2 INTEGER REFERENCES Recipe(rid)," +
			"comments TEXT," +
			"PRIMARY KEY(rid1, rid2)," +
			"CHECK(rid1 < rid2));" +

		"CREATE TABLE Unit(" +
			"uid INTEGER PRIMARY KEY AUTOINCREMENT," +
			"name TEXT NOT NULL UNIQUE);" +

		"CREATE TABLE RecipeIngredients(" +
			"rid INTEGER REFERENCES Recipe(rid)," +
			"iid INTEGER REFERENCES Ingredient(iid)," +
			"amount REAL," +
			"uid INTEGER REFERENCES Unit(uid)," +
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
			"CHECK(iid1 < iid2));" +

		"CREATE TABLE UnitConversion(" +
			"uid1 INTEGER REFERENCES Unit(uid)," +
			"uid2 INTEGER REFERENCES Unit(uid)," +
			"factor REAL NOT NULL CHECK(factor > 0)," +
			"PRIMARY KEY(uid1, uid2));";

	public RecipeBoxOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_STATEMENT);
	}

	public void onUpgrade(SQLiteDatabase db, int oldv, int newv) {

	}

}
