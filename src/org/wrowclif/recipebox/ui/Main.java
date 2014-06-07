package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.RecipeIngredient;
import org.wrowclif.recipebox.Utility;
import org.wrowclif.recipebox.Instruction;
import org.wrowclif.recipebox2.R;
import org.wrowclif.recipebox.impl.UtilityImpl;

import org.wrowclif.recipebox.ui.components.BaseActivity;
import org.wrowclif.recipebox.ui.components.DialogManager.DialogHandler;
import org.wrowclif.recipebox.ui.components.EnterTextDialog;
import org.wrowclif.recipebox.ui.components.ListAutoCompleteAdapter;
import org.wrowclif.recipebox.ui.components.DynamicLoadAdapter;

import static org.wrowclif.recipebox.util.ConstantInitializer.assignId;
import org.wrowclif.recipebox.util.ShareUtil;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

import java.util.ArrayList;

/**
 * The entry point for Recipebox
 *
 * @param util          Singleton reference to the utility class (Used for making queries, etc.)
 * @param recentAdapter Adapter to dynamically load recently viewed recipes as
 *                      we scroll through the list
 *
 * @param CREATE_RECIPE_DIALOG Id used when we want to open a create recipe dialog
 **/
public class Main extends BaseActivity {

   private Utility util;
   private DynamicLoadAdapter<Recipe> recentAdapter;
   private static final int CREATE_RECIPE_DIALOG = assignId();

   protected int getViewId() {
      return R.layout.main;
   }

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // Initialize the app data. We need to pass in a Context for AppData to work properly.
      AppData.initialSingleton(this);

      util = UtilityImpl.singleton;

      Intent intent = getIntent();

      // Check the intent to see if we need to load any recipes
      if(intent.getData() != null) {
         Log.d("Recipebox", "Intent data not null!");
         if("text/rcpb".equals(intent.getType())) {
            ShareUtil.loadRecipe(this, intent.getData());
         } else if("application/zip".equals(intent.getType())) {
            ShareUtil.loadZipRecipe(this, intent.getData());
         }
      }

      //{ Setup fonts
      this.useHeadingFont(R.id.recipe_label,
                          R.id.categories,
                          R.id.browse,
                          R.id.addrecipe);

      this.useTextFont(R.id.recipesearch);
      //}

      this.setupDialogHandlers();

      //{ Setup the autocomplete list for the search bar
      AutoCompleteTextView tv = (AutoCompleteTextView) findViewById(R.id.recipesearch);
      ListAutoCompleteAdapter.Specifics<Recipe> sp =
            new ListAutoCompleteAdapter.Specifics<Recipe>() {

         /**
          * Returns a row item for the given recipe.
          *
          * @param id The id of the recipe
          * @param r  The recipe to make the row for
          * @param v  The view to reuse or null
          * @param vg The group that the view is a part of
          **/
         public View getView(int id, Recipe r, View v, ViewGroup vg) {
            if(v == null) {
               v = Main.this.inflate(R.layout.autoitem);
            }

            Main.this.useTextFont(v, R.id.child_name);
            TextView tv = (TextView) v.findViewById(R.id.child_name);

            tv.setText(r.getName());

            return v;
         }

         /**
          * Returns the id of the given item
          *
          * @param item The item to get the id for
          **/
         public long getItemId(Recipe item) {
            return item.getId();
         }

         /**
          * Returns the top 5 matching recipes for the given text
          *
          * @param seq The text being searched for
          **/
         public List<Recipe> filter(CharSequence seq) {
            return UtilityImpl.singleton.searchRecipes(seq.toString(), 5);
         }

         /**
          * Returns the name of the given recipe
          *
          * @param result The recipe to get the name of
          **/
         public String convertResultToString(Recipe result) {
            return result.getName();
         }

         /**
          * Called when an item in the search list is clicked.
          * Go to the selected recipe
          *
          * @param av       The adapter
          * @param v        The view that was clicked
          * @param position The position in the list of the clicked item
          * @param id       The id of the recipe
          * @param item     The recipe represented by the clicked item
          **/
         public void onItemClick(AdapterView av, View v, int position, long id, Recipe item) {
            Intent intent = new Intent(Main.this, RecipeTabs.class);
            intent.putExtra("id", id);
            startActivity(intent);
         }
      };

      ListAutoCompleteAdapter<Recipe> adapter = new ListAutoCompleteAdapter<Recipe>(sp);

      adapter.setUpView(tv);

      /**
       * When enter pressed on the keyboard, search for the current text and go to the first matching item.
       * If no items match, open a dialog offering to create that recipe.
       *
       * @param v      The view being edited
       * @param action The action that was taken
       * @param event  The key event that triggered the action
       **/
      tv.setOnEditorActionListener(new OnEditorActionListener() {
         public boolean onEditorAction(TextView v, int action, KeyEvent event) {
            // This combination indicates that enter was pressed
            if((action == EditorInfo.IME_NULL) && (event.getAction() == 0)) {
               String text = v.getText().toString();
               List<Recipe> recipes = util.searchRecipes(text, 1);
               if(recipes.size() > 0) {
                  Intent intent = new Intent(Main.this, RecipeTabs.class);
                  intent.putExtra("id", recipes.get(0).getId());
                  startActivity(intent);
               } else {
                  showDialog(CREATE_RECIPE_DIALOG);
               }
               return true;
            }
            return false;
         }
      });
      //}

      //{ Add recipe button
      View addRecipe = findViewById(R.id.addrecipe);

      addRecipe.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            Recipe r = util.newRecipe("New Recipe");
            Intent intent = new Intent(Main.this, RecipeTabs.class);
            intent.putExtra("id", r.getId());
            intent.putExtra("edit", true);
            startActivity(intent);
         }
      });
      //}

      //{ Browse recipes button
      View browseRecipe = findViewById(R.id.browse);

      browseRecipe.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            Intent intent = new Intent(Main.this, Browse.class);
            startActivity(intent);
         }
      });
      //}

      //{ Categories button
      View browseCategories = findViewById(R.id.categories);

      browseCategories.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            Intent intent = new Intent(Main.this, BrowseCategories.class);
            startActivity(intent);
         }
      });
      //}

      //{ Set up recently viewed recipes list
      ListView lv = (ListView) findViewById(R.id.recent_recipes);

      DynamicLoadAdapter.Specifics<Recipe> sp2 = new DynamicLoadAdapter.Specifics<Recipe>() {

         /**
          * Gets the view for the given recipe
          *
          * @param id The id of the recipe
          * @param r  The recipe
          * @param v  The view to reuse or null
          * @param vg The group the given view belongs to
          **/
         public View getView(int id, Recipe r, View v, ViewGroup vg) {
            if(v == null) {
               v = Main.this.inflate(R.layout.category_item);
            }

            // Don't show edit buttons
            v.findViewById(R.id.edit_group).setVisibility(View.GONE);

            Main.this.useTextFont(v, R.id.name_box);
            TextView tv = (TextView) v.findViewById(R.id.name_box);

            // If the recipe is null, then we are at the end of the current list and need to load more recipes.
            if(r == null) {
               tv.setText("Loading...");
            // Otherwise we need to set the item to show the recipe's name
            } else {
               tv.setText(r.getName());
            }

            return v;
         }

         /**
          * Gets the id for the given recipe
          *
          * @param item The item to get the id for
          **/
         public long getItemId(Recipe item) {
            return item.getId();
         }

         /**
          * Loads the next set of items to be shown in the list.
          * This is called when we reach the end of the list and need to load more items
          *
          * @param offset The offset to start getting items from
          * @param max    The maximum number of items to retrieve
          **/
         public List<Recipe> filter(int offset, int max) {
            List<Recipe> list = UtilityImpl.singleton.getRecentlyViewedRecipes(offset, max);
            if(list.size() == max) {
               list.add(null);
            }
            return list;
         }

         /**
          * Returns the representation of the given recipe as a string (its name)
          *
          * @param result The recipe to get the name of
          **/
         public String convertResultToString(Recipe result) {
            return (result == null) ? "Null" : result.getName();
         }

         /**
          * Called when a recipe is clicked in the list.
          * Opens the selected recipe
          *
          * @param av       The parent view
          * @param v        The view that was clicked
          * @param position The position of the item in the list
          * @param id       The id of the recipe in the view
          * @param item     The recipe held by the view
          **/
         public void onItemClick(AdapterView av, View v, int position, long id, Recipe item) {
            Intent intent = new Intent(Main.this, RecipeTabs.class);
            intent.putExtra("id", id);
            startActivity(intent);
         }
      };

      recentAdapter = new DynamicLoadAdapter<Recipe>(sp2);

      recentAdapter.setUpList(lv);
      lv.requestFocus();
      //}

   }

   /**
    * Called when this activity is about to resume execution
    **/
   public void onResume() {
      super.onResume();
      // Reload the recently viewed recipes
      recentAdapter.clear();
   }

   private void setupDialogHandlers() {

      DialogHandler create_handler = new DialogHandler() {

         public Dialog createDialog(Bundle bundle) {
            EnterTextDialog etd = new EnterTextDialog(Main.this, R.layout.show_text_dialog);

            etd.setTitle("Create New Recipe");

            // If the user accepts, we take the text in the search bar
            // and create a recipe with that name.
            etd.setOkButtonText("Create");
            etd.setOkListener(new OnClickListener() {
               public void onClick(View v) {
                  TextView tv = (TextView) findViewById(R.id.recipesearch);

                  String name = tv.getText().toString();
                  Recipe r = util.newRecipe(name);
                  Intent intent = new Intent(Main.this, RecipeTabs.class);

                  intent.putExtra("id", r.getId());
                  intent.putExtra("edit", true);

                  startActivity(intent);
               }
            });

            return etd;
         }

         public void prepareDialog(Dialog d, Bundle bundle) {
            EnterTextDialog etd = (EnterTextDialog) d;
            TextView tv = (TextView) findViewById(R.id.recipesearch);

            // Customize the text shown in the dialog based on the search text.
            String name = tv.getText().toString();
            etd.setEditHtml("There is no recipe for <b>" + name + "</b>.<br><br>" +
                            "Would you like to create a new recipe?");

         }
      };
      this.dialogManager.registerHandler(CREATE_RECIPE_DIALOG, create_handler);
   }

}
