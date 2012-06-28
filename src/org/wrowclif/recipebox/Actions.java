package org.wrowclif.recipebox;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.AppData.Transaction;

public enum Actions {
	INITIAL_LOAD_START("Welcome to Recipebox!",
						"We've entered some of our favorite recipes to help you get started.",
						"Tap 'Create' to make a new recipe."),

	INITIAL_LOAD_COMPLETE("Tap 'Browse' to view all recipes sorted by name.",
							"Tap 'Categories' to view recipes by category."),

	BROWSE("All of your recipes are listed here, sorted alphabetically."),

	CATEGORIES("A recipe can be in as many or as few categories as you wish",
				"We've provided some common categories, but feel free to customize them",
				"Simply tap the 'Menu' button on your phone, then tap 'Edit'"),

	CATEGORIES_EDIT("Create new categories by tapping the 'Plus' icon",
					"Rename categories by tapping the 'Pencil' icon next to their name",
					"Remove categories by tapping the 'X' icon next to their name",
					"Exit edit mode by tapping the 'Check' icon"),

	CATEGORY("Tap the name of a recipe to view that recipe.",
				"To add or remove recipes, tap the 'Menu' button, then tap 'Edit'"),

	CATEGORY_EDIT("Tap the 'Plus' icon to add a recipe to this category",
					"Tap the 'X' icon next to a recipe's name to remove it from this category",
					"Tap the 'Check' icon to leave edit mode"),

	RECIPE_SHARE("Easily email your favorite recipes to friends and family!",
					"The email contains a recipe attachment that can be loaded into Recipebox",
					"The recipe is also written into the body of the email for easy viewing"),

	RECIPE_EDIT_DIALOG("There are two ways to edit recipes",
				"Tap 'Edit This Recipe' to update the current recipe",
				"Tap 'Create Variant' to create a new recipe using this recipe as the base"),

	RECIPE_EDIT("'Pencil' icons let you edit items",
				"'Plus' icons let you add items",
				"'X' icons let you remove items",
				"'Check' icons let you exit edit mode"),

	RECIPE_INFO("Recipes are divided into Info, Ingredients, and Instructions.",
				"Use the tabs along the bottom to navigate the recipe.",
				"Tap the 'Menu' button to view additional actions." ),

	RECIPE_INFO_EDIT("Edit Prep and Cook times by tapping the times under the label"),

	RECIPE_INGREDIENTS("All of the ingredients you need for the recipe are listed here"),

	RECIPE_INGREDIENTS_PRE_REORDER("Press down on an ingredient's name to show reorder arrows"),

	RECIPE_INGREDIENTS_REORDER("Press the ingredient's name again to hide the arrows"),

	ADD_RECIPE_INGREDIENT("Ingredients consist of an amount and an ingredient",
							"Recipebox remembers which ingredients you use and will suggest them as you type"),

	RECIPE_INSTRUCTIONS("Steps to make a recipe are listed under Instructions"),

	RECIPE_INSTRUCTIONS_EDIT("Press down on an instruction's text to show reorder arrows"),

	RECIPE_INSTRUCTIONS_REORDER("Press the instruction's text again to hide the arrows");

	private boolean alreadyShown;
	private String[] msgs;

	Actions(String... msgs) {
		this.alreadyShown = false;
		this.msgs = msgs;
	}

	public void showNotifications() {
		final String select =
			"SELECT n.action " +
			"FROM Notifications n " +
			"WHERE n.action = ?;";

		final String insert =
			"INSERT INTO Notifications(action) " +
				"VALUES(?);";

		// Short circuit so we don't have to call the database needlessly
		if(alreadyShown) {
			return;
		}

		AppData data = AppData.getSingleton();

		boolean show = data.sqlTransaction(new Transaction<Boolean>() {
			public Boolean exec(SQLiteDatabase db) {
				boolean ret = false;
				Cursor c = db.rawQuery(select, new String[] {Actions.this.toString()});
				if(!c.moveToNext()) {
					ret = true;
					db.execSQL(insert, new Object[] {Actions.this.toString()});
				}
				c.close();
				return ret;
			}
		});

		if(show) {
			for(String msg : msgs) {
				Toast.makeText(data.getContext(), msg, Toast.LENGTH_LONG).show();
			}
		}

		this.alreadyShown = true;
	}
}
