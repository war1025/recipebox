package org.wrowclif.recipebox;

import android.content.Context;

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
		AppData.appContext = context;
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
}
