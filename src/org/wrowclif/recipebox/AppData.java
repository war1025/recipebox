package org.wrowclif.recipebox;

import android.content.Context;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Scanner;
import java.io.InputStream;
import java.io.IOException;
import org.wrowclif.recipebox.util.JsonUtil;

import org.wrowclif.recipebox.db.RecipeBoxOpenHelper;

public class AppData {

	private RecipeBoxOpenHelper helper;
	private Typeface headingFont;
	private Typeface textFont;

	protected static Context appContext;

	protected static class Inner {
		protected static final AppData singleton;

		static {
			singleton = new AppData();
		}
	}

	private AppData() {
		helper = new RecipeBoxOpenHelper(appContext);
		headingFont = Typeface.createFromAsset(appContext.getAssets(), "gothic.ttf");
		textFont = Typeface.createFromAsset(appContext.getAssets(), "DejaVuSans.ttf");
	}

	public static AppData initialSingleton(Context context) {
		AppData.appContext = context.getApplicationContext();
		Inner.singleton.tryLoadDefaults();

		return Inner.singleton;
	}

	public static AppData getSingleton() {
		return Inner.singleton;
	}

	public Typeface getHeadingFont() {
		return headingFont;
	}

	public Typeface getTextFont() {
		return textFont;
	}

	public void useHeadingFont(TextView tv) {
		tv.setTypeface(headingFont);
	}

	public void useTextFont(TextView tv) {
		tv.setTypeface(textFont);
	}

	public RecipeBoxOpenHelper getOpenHelper() {
		return helper;
	}

	public void close() {
		helper.close();
	}

	public <E> E sqlTransaction(Transaction<E> t) {
		E ret = null;
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
			throw new IllegalStateException(table + " " + op + " should have affected only one row" +
												" but affected " + ret + " rows");
		}
	}

	public interface Transaction<T> {

		public T exec(SQLiteDatabase db);

	}

	private void tryLoadDefaults() {
		helper.getWritableDatabase();
		if(helper.needsDefaultRecipes) {
			new LoadDefaultsTask().execute();
			helper.needsDefaultRecipes = false;
		}
	}

	private class LoadDefaultsTask extends AsyncTask<String, String, String> {
		protected void onPreExecute() {
			String[] msgs = {"Welcome to Recipebox!",
							"We've entered some of our favorite recipes to help you get started.",
							"Tap 'Create' to make a new recipe."};
			for(String msg : msgs) {
				Toast.makeText(appContext, msg, Toast.LENGTH_LONG).show();
			}
		}

		protected String doInBackground(String... nothing) {
			InputStream temp = null;

			try {
				temp = appContext.getAssets().open("defaultRecipes.json");

				Scanner in = new Scanner(temp);

				in.useDelimiter("\\A");

				JsonUtil.fromJson(in.next());
			} catch(IOException e) {
				Log.e("Recipebox AppData", "Error loading recipe: " + e, e);
			} finally {
				if(temp != null) {
					try {
						temp.close();
					} catch(IOException e) {
					}
				}
			}
			return "";
		}

		protected void onPostExecute(String result) {
			String[] msgs = {"Tap 'Browse' to view all recipes sorted by name.",
								"Tap 'Categories' to view recipes by category."};

			for(String msg : msgs) {
				Toast.makeText(appContext, msg, Toast.LENGTH_LONG).show();
			}
		}

	}
}
