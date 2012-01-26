package org.wrowclif.recipebox;

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
		this.appContext = context;
		return Inner.singleton;
	}

	public static AppData getSingleton() {
		return Inner.singleton;
	}

	public RecipeBoxOpenHelper getOpenHelper() {
		return helper;
	}
}
