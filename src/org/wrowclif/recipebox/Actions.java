package org.wrowclif.recipebox;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.AppData.Transaction;

public enum Actions {
   INITIAL_LOAD_START("Welcome to Recipebox!",
                  "We've entered some of our favorite recipes to help you get started."),

   INITIAL_LOAD_COMPLETE(),

   BROWSE("All of your recipes are listed here, sorted alphabetically."),

   CATEGORIES("A recipe can be in as many or as few categories as you wish",
            "We've provided some common categories, but feel free to customize them",
            "Simply tap the 'Menu' button on your phone, then tap 'Edit'"),

   CATEGORIES_EDIT(),

   CATEGORY("To add or remove recipes, tap the 'Menu' button, then tap 'Edit'"),

   CATEGORY_EDIT(),

   RECIPE_SHARE(),

   RECIPE_EDIT_DIALOG("There are two ways to edit recipes",
               "Tap 'Edit This Recipe' to update the current recipe",
               "Tap 'Create Variant' to create a new recipe using this recipe as the base"),

   RECIPE_EDIT(),

   RECIPE_INFO("Use the tabs along the bottom to navigate the recipe.",
            "Tap the 'Menu' button to view additional actions." ),

   RECIPE_INFO_EDIT(),

   RECIPE_ADD_CATEGORY("Recipes can be in as many categories as you like"),

   RECIPE_ADD_RELATED("Relating two recipes allows you to quickly reference one from the other",
                  "Make relations between recipes that go well together"),

   RECIPE_INGREDIENTS(),

   RECIPE_INGREDIENTS_PRE_REORDER("Press down on an ingredient's name to show reorder arrows"),

   RECIPE_INGREDIENTS_REORDER("Press the ingredient's name again to hide the arrows"),

   ADD_RECIPE_INGREDIENT("Ingredients consist of an amount and an ingredient",
                     "Recipebox remembers which ingredients you use and will suggest them as you type"),

   RECIPE_INSTRUCTIONS(),

   RECIPE_INSTRUCTIONS_PRE_REORDER("Press down on an instruction's text to show reorder arrows"),

   RECIPE_INSTRUCTIONS_REORDER("Press the instruction's text again to hide the arrows"),

   IMAGE_SHOWN("Tap the image to view it full size"),

   EXPORT("Add recipes to export in a single bundle");

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
