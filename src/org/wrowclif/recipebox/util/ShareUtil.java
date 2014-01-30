package org.wrowclif.recipebox.util;

import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.RecipeIngredient;
import org.wrowclif.recipebox.Instruction;

import org.wrowclif.recipebox.ui.RecipeTabs;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.IOException;

import java.util.Scanner;

public class ShareUtil {

   private static final String LOG_TAG = "Recipebox Share Util";

   public static void share(Context ctx, Recipe r) {

      Intent intent = new Intent(Intent.ACTION_SEND);

      Uri zipUri = ZipUtil.zipRecipes(ctx, r);

      String recipeText = toHumanReadable(r);

      intent.putExtra(Intent.EXTRA_SUBJECT, "Recipebox Recipe: " + r.getName());
      intent.putExtra(Intent.EXTRA_TEXT, "A recipe for making " + r.getName() + " is attached.\n\n" + recipeText);
      intent.setType("application/zip");
      intent.putExtra(Intent.EXTRA_STREAM, zipUri);

      if(intent != null) {
         ctx.startActivity(Intent.createChooser(intent, "Share Recipe"));
      }
   }

   public static void export(Context ctx, Recipe... recipes) {
      Intent intent = new Intent(Intent.ACTION_SEND);

      Uri zip_uri = ZipUtil.zipRecipes(ctx, recipes);

      StringBuilder recipe_list_text = new StringBuilder();
      for(Recipe recipe : recipes) {
         recipe_list_text.append(toHumanReadable(recipe));
         recipe_list_text.append("\n====================\n");
      }

      intent.putExtra(Intent.EXTRA_SUBJECT, "Recipebox Recipe Pack");
      intent.putExtra(Intent.EXTRA_TEXT, recipe_list_text.toString());
      intent.setType("application/zip");
      intent.putExtra(Intent.EXTRA_STREAM, zip_uri);

      if(intent != null) {
         ctx.startActivity(Intent.createChooser(intent, "Share Exported Recipes"));
      }
   }

   public static void loadRecipe(Context ctx, Uri uri) {
      Recipe[] recipes = null;
      InputStream temp = null;

      try {
         temp = ctx.getContentResolver().openInputStream(uri);

         Scanner in = new Scanner(temp);

         in.useDelimiter("\\A");

         recipes = JsonUtil.fromJson(in.next());
      } catch(IOException e) {
         Log.e(LOG_TAG, "Error loading recipe: " + e, e);
      } finally {
         if(temp != null) {
            try {
               temp.close();
            } catch(IOException e) {
            }
         }
      }

      if(recipes != null && recipes.length > 0) {
         Intent intent = new Intent(ctx, RecipeTabs.class);
         intent.putExtra("id", recipes[0].getId());
         ctx.startActivity(intent);
      }
   }

   public static void loadZipRecipe(Context ctx, Uri uri) {
      Recipe[] recipes = ZipUtil.unzipRecipes(ctx, uri);

      if(recipes != null && recipes.length > 0) {
         Intent intent = new Intent(ctx, RecipeTabs.class);
         intent.putExtra("id", recipes[0].getId());
         ctx.startActivity(intent);
      }
   }

   public static String toHumanReadable(Recipe r) {
      StringBuilder out = new StringBuilder();

      println(out, r.getName());

      println(out, "\n\n");

      println(out, "Ingredients:");

      for(RecipeIngredient ri : r.getIngredients()) {
         println(out, ri.getAmount() + " " + ri.getName());
      }

      println(out, "\n\n");

      int i = 1;
      for(Instruction in : r.getInstructions()) {
         println(out, i + ". " + in.getText());
         i++;
      }

      return out.toString();

   }

   private static void println(StringBuilder out, String str) {
      out.append(str).append("\n");
   }

}
