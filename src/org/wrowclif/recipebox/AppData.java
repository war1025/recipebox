package org.wrowclif.recipebox;

import android.content.Context;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.wrowclif.recipebox.db.RecipeBoxOpenHelper;

public class AppData {

	private RecipeBoxOpenHelper helper;

	protected static Context appContext;

	protected static class Inner {
		protected static final AppData singleton;

		static {
			singleton = new AppData();
		}
	}

	private AppData() {
		helper = new RecipeBoxOpenHelper(appContext);
	}

	public static AppData initialSingleton(Context context) {
		AppData.appContext = context.getApplicationContext();
		return Inner.singleton;
	}

	public static AppData getSingleton() {
		return Inner.singleton;
	}

	public RecipeBoxOpenHelper getOpenHelper() {
		return helper;
	}

	public void close() {
		helper.close();
	}

	public <E> E sqlTransaction(Transaction<E> t) {
		E ret = null;
		Log.d("Recipebox", "Context: " + appContext + " Helper: " + helper);
		SQLiteDatabase db = helper.getWritableDatabase();
		db.beginTransaction();
		try {
			ret = t.exec(db);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		return ret;
	}

	public void itemUpdate(ContentValues cv, String table, String where, String[] values, String op) {
		SQLiteDatabase db = helper.getWritableDatabase();
		int ret = db.update(table, cv, where, values);
		if(ret != 1) {
			throw new IllegalStateException(table + " " + op + " setComments should have affected only one row" +
												" but affected " + ret + " rows");
		}
	}

	public interface Transaction<T> {

		public T exec(SQLiteDatabase db);

	}
}
