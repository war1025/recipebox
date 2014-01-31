package org.wrowclif.recipebox.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.AppData.Transaction;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.Ingredient;
import org.wrowclif.recipebox.Category;
import org.wrowclif.recipebox.Utility;

import android.util.Log;

import java.util.List;
import java.util.ArrayList;

public class UtilityImpl implements Utility {

   private static final String LOG_TAG = "Recipebox Utility";

   public static final UtilityImpl singleton;

   static {
      singleton = new UtilityImpl();
   }

   protected AppData data;

   private UtilityImpl() {
      data = AppData.getSingleton();
   }

   public Recipe newRecipe(String name) {
      return RecipeImpl.factory.createNew(name);
   }

   public List<Recipe> searchRecipes(final String search, final int maxResults) {
      final String stmt =
         "SELECT r.rid, r.name, r.description, r.preptime, r.cooktime, r.cost, r.imageuri, r.vid " +
         "FROM Recipe r " +
         "WHERE r.name LIKE ? " +
         "ORDER BY r.lastviewtime DESC " +
         "LIMIT ?; ";

      return data.sqlTransaction(new Transaction<List<Recipe>>() {
         public List<Recipe> exec(SQLiteDatabase db) {
            Cursor c = db.rawQuery(stmt, new String[] {"%" + search + "%", maxResults + ""});
            List<Recipe> list = RecipeImpl.factory.createListFromCursor(c);
            c.close();
            return list;
         }
      });
   }

   public List<Recipe> getRecentlyViewedRecipes(final int offset, final int maxResults) {
      final String stmt =
         "SELECT r.rid, r.name, r.description, r.preptime, r.cooktime, r.cost, r.imageuri, r.vid " +
         "FROM Recipe r " +
         "ORDER BY r.lastviewtime DESC " +
         "LIMIT ? OFFSET ?;";

      return data.sqlTransaction(new Transaction<List<Recipe>>() {
         public List<Recipe> exec(SQLiteDatabase db) {
            Cursor c = db.rawQuery(stmt, new String[] {maxResults + "", offset + ""});
            List<Recipe> list = RecipeImpl.factory.createListFromCursor(c);
            c.close();
            return list;
         }
      });
   }

   public List<Recipe> getRecipesByName(final int offset, final int maxResults) {
      final String stmt =
         "SELECT r.rid, r.name, r.description, r.preptime, r.cooktime, r.cost, r.imageuri, r.vid " +
         "FROM Recipe r " +
         "ORDER BY r.name ASC " +
         "LIMIT ? OFFSET ?;";

      return data.sqlTransaction(new Transaction<List<Recipe>>() {
         public List<Recipe> exec(SQLiteDatabase db) {
            Cursor c = db.rawQuery(stmt, new String[] {maxResults + "", offset + ""});
            List<Recipe> list = RecipeImpl.factory.createListFromCursor(c);
            c.close();
            return list;
         }
      });
   }

   public List<Recipe> getRecipesForIds(final List<Long> recipeIds) {
      final String stmt =
         "SELECT r.rid, r.name, r.description, r.preptime, r.cooktime, r.cost, r.imageuri, r.vid " +
         "FROM Recipe r " +
         "WHERE r.rid IN (?)" +
         "ORDER BY r.name ASC;";

      if(recipeIds.size() == 0) {
         return new ArrayList<Recipe>();
      }

      return data.sqlTransaction(new Transaction<List<Recipe>>() {
         public List<Recipe> exec(SQLiteDatabase db) {
            List<String> recipe_id_strs = new ArrayList<String>(recipeIds.size());

            StringBuilder query_builder = new StringBuilder();
            for (long id : recipeIds) {
               query_builder.append("?, ");
               recipe_id_strs.add(id + "");
            }
            int length = query_builder.length();
            query_builder.replace(length - 2, length, "");

            String stmt2 = stmt.replace("?", query_builder.toString());

            Cursor c = db.rawQuery(stmt2, recipe_id_strs.toArray(new String[0]));
            List<Recipe> list = RecipeImpl.factory.createListFromCursor(c);
            c.close();
            return list;
         }
      });
   }

   public Recipe getRecipeById(long id) {
      return RecipeImpl.factory.getRecipeById(id);
   }

   public Ingredient getIngredientByName(String name) {
      return IngredientImpl.factory.getIngredientByName(name);
   }

   public Ingredient createOrRetrieveIngredient(String name) {
      return IngredientImpl.factory.createOrRetrieveIngredient(name);
   }

   public List<Ingredient> searchIngredients(final String search, final int maxResults) {
      final String stmt =
         "SELECT i.iid, i.name " +
         "FROM Ingredient i " +
         "WHERE i.name LIKE ? " +
            "or i.name LIKE ? " +
         "ORDER BY i.usecount DESC, i.name ASC " +
         "LIMIT ?; ";

      return data.sqlTransaction(new Transaction<List<Ingredient>>() {
         public List<Ingredient> exec(SQLiteDatabase db) {
            String trimSearch = search.trim();
            Cursor c = db.rawQuery(stmt, new String[] {trimSearch + "%", "% " + trimSearch + "%", maxResults + ""});
            List<Ingredient> list = IngredientImpl.factory.createListFromCursor(c);
            c.close();
            return list;
         }
      });
   }

   public List<Category> searchCategories(final String search, final int maxResults) {
      final String stmt =
         "SELECT c.cid, c.name, c.description " +
         "FROM Category c " +
         "WHERE c.name LIKE ? " +
         "ORDER BY c.name ASC " +
         "LIMIT ?; ";

      return data.sqlTransaction(new Transaction<List<Category>>() {
         public List<Category> exec(SQLiteDatabase db) {
            Cursor c = db.rawQuery(stmt, new String[] {"%" + search + "%", maxResults + ""});
            List<Category> list = CategoryImpl.factory.createListFromCursor(c);
            c.close();
            return list;
         }
      });
   }

   public List<Category> getCategoriesByName(final int offset, final int maxResults) {
      final String stmt =
         "SELECT c.cid, c.name, c.description " +
         "FROM Category c " +
         "ORDER BY c.name ASC " +
         "LIMIT ? " +
         "OFFSET ?;";

      return data.sqlTransaction(new Transaction<List<Category>>() {
         public List<Category> exec(SQLiteDatabase db) {
            Cursor c = db.rawQuery(stmt, new String[] {maxResults + "", offset + ""});
            List<Category> list = CategoryImpl.factory.createListFromCursor(c);
            c.close();
            return list;
         }
      });
   }

   public Category getCategoryByName(String category) {
      return CategoryImpl.factory.getCategoryByName(category);
   }

   public Category createOrRetrieveCategory(String category) {
      return CategoryImpl.factory.createOrRetrieveCategory(category);
   }

   public Category getCategoryById(long id) {
      return CategoryImpl.factory.getCategoryById(id);
   }
}
