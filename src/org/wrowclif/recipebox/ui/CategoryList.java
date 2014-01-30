package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.Actions;
import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Category;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.RecipeIngredient;
import org.wrowclif.recipebox.Utility;
import org.wrowclif.recipebox.Instruction;
import org.wrowclif.recipebox2.R;

import org.wrowclif.recipebox.impl.UtilityImpl;

import org.wrowclif.recipebox.ui.components.EnterTextDialog;
import org.wrowclif.recipebox.ui.components.RecipePickDialog;
import org.wrowclif.recipebox.ui.components.ListAutoCompleteAdapter;
import org.wrowclif.recipebox.ui.components.DynamicLoadAdapter;

import static org.wrowclif.recipebox.util.ConstantInitializer.assignId;

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
import android.widget.EditText;
import android.widget.Filter;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.text.TextWatcher;
import android.text.Editable;
import android.text.InputType;

import java.util.ArrayList;

/**
 * Shows all recipes in a given category and allows the user to add / remove recipes to the category.
 *
 * @param util     Reference to the Utility singleton for making queries
 * @param category The category that we are listing
 * @param adapter  The load adapter that we use to get recipes to put in the list
 * @param edit     Whether or not we are in edit mode
 *
 * @param ADD_RECIPE_DIALOG    Dialog ID to create a dialog for adding a recipe to this category
 * @param DELETE_RECIPE_DIALOG Dialog ID to create a dialog for removing a recipe from this category
 **/
public class CategoryList extends Activity {

   private Utility util;
   private Category category;
   private DynamicLoadAdapter<Recipe> adapter;
   private boolean edit;

   private static final int ADD_RECIPE_DIALOG = assignId();
   private static final int DELETE_RECIPE_DIALOG = assignId();

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.categories);

      Actions.CATEGORY.showNotifications();

      util = UtilityImpl.singleton;
   }

   /**
    * Called when this activity is resuming with a new intent
    *
    * @param intent The new intent to use for setting up the activity
    **/
   public void onNewIntent(Intent intent) {
      category = util.getCategoryById(intent.getLongExtra("id", -1));

      //{ Setup the recipe list
      ListView lv = (ListView) findViewById(R.id.category_list);

      createDynamicLoadAdapter();

      adapter.setUpList(lv);
      //}

      // Setup the header
      TextView label = (TextView) findViewById(R.id.category_label);
      label.setText(category.getName());
      AppData.getSingleton().useHeadingFont(label);

      //{ Add recipe button
      TextView addButton = (TextView) findViewById(R.id.add_button);

      addButton.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            showDialog(ADD_RECIPE_DIALOG);
         }
      });
      //}

      //{ Done editing button
      View doneButton = findViewById(R.id.done_button);

      doneButton.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            setEditing(false);
         }
      });
      //}

      setEditing(false);
   }

   /**
    * Called when this activity is resuming execution.
    **/
    public void onResume() {
      super.onResume();

      // Ensure that we setup the view with the proper intent
      onNewIntent(getIntent());
   }

   /**
    * Set the view to be in the proper edit mode
    *
    * @param editing Whether or not the view should be in edit mode
    **/
   protected void setEditing(boolean editing) {
      // The views that change when editing
      View[] views = {findViewById(R.id.add_button), findViewById(R.id.done_button)};

      if(editing) {
         Actions.CATEGORY_EDIT.showNotifications();
         for(View v : views) {
            v.setVisibility(View.VISIBLE);
         }
      } else {
         for(View v : views) {
            v.setVisibility(View.GONE);
         }
      }

      this.edit = editing;
      // Notify so we refresh the UI
      adapter.notifyDataSetChanged();
   }

   /**
    * Called when an existing dialog is about to be re-shown with new data
    *
    * @param id     The dialog id
    * @param dialog The dialog that is created for the given id
    * @param bundle Any data that is needed to set up the dialog before showing it
    **/
   protected void onPrepareDialog(int id, Dialog d, Bundle bundle) {

      if(id == ADD_RECIPE_DIALOG) {
         EnterTextDialog dialog = (EnterTextDialog) d;
         dialog.setEditText("");
      } else if(id == DELETE_RECIPE_DIALOG) {
         EnterTextDialog dialog = (EnterTextDialog) d;

         final int position = bundle.getInt("position", -1);
         final Recipe recipe = adapter.getItem(position);

         // The button listener has to be redone every time so that we can hook it up to the proper recipe
         dialog.setEditHtml("Are you sure you want to remove <b>" + recipe.getName() + "</b> from this category?");
         dialog.setOkListener(new OnClickListener() {
            public void onClick(View v) {
               category.removeRecipe(recipe);
               adapter.remove(position);
            }
         });
      }
   }

   /**
    * Called to create a dialog for the given dialog id
    *
    * @param id     The id for the dialog to create
    * @param bundle Any data required to set up the dialog
    **/
   protected Dialog onCreateDialog(int id, Bundle bundle) {
      Dialog dialog = null;

      if(id == ADD_RECIPE_DIALOG) {
         final RecipePickDialog pickDialog = new RecipePickDialog(this);

         pickDialog.setTitle("Add Recipe To Category");

         //{ Setup the add button handler.
         pickDialog.setOkListener(new OnClickListener() {
            public void onClick(View v) {
               // If an id has been set, attempt to retrieve a recipe with that id
               // then add that recipe to the category and clear the adapter to reload it.
               long recipeId = pickDialog.getSelectedRecipeId();
               if(recipeId >= 0) {
                  Recipe r = util.getRecipeById(recipeId);

                  if(r != null) {
                     category.addRecipe(r);

                     pickDialog.clearSelectedRecipeId();

                     adapter.clear();
                  }
               }
            }
         });
         //}

         dialog = pickDialog;

      } else if(id == DELETE_RECIPE_DIALOG) {
         EnterTextDialog etd = new EnterTextDialog(this, R.layout.show_text_dialog);

         // Set the basic fields. The signal handlers are set up in the prepareDialog() method
         etd.setTitle("Remove Recipe");

         etd.setOkButtonText("Remove");

         dialog = etd;
      }


      return dialog;
   }

   /**
    * Called to create the options menu for this activity
    *
    * @param menu The menu to add items to
    *
    * @return Whether the menu was created
    **/
   public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater mi = getMenuInflater();

      mi.inflate(R.menu.category_menu, menu);

      return true;
   }

   /**
    * Called when an item in the options menu is selected
    *
    * @param item The item that was selected
    *
    * @return Whether the item selection was handled
    **/
   public boolean onOptionsItemSelected(MenuItem item) {
      int id = item.getItemId();

      switch(id) {
         // Toggle the edit state of this activity
         case R.id.edit : {
            setEditing(!edit);
            return true;
         }
         case R.id.export : {
            Intent intent = new Intent(CategoryList.this, Export.class);
            startActivity(intent);
         }
      }

      // See if the super class can handle the item
      return super.onOptionsItemSelected(item);
   }

   /**
    * Creates the dynamic loader for the recipe list
    **/
   private void createDynamicLoadAdapter() {

      DynamicLoadAdapter.Specifics<Recipe> sp = new DynamicLoadAdapter.Specifics<Recipe>() {
         /**
          * Creates a row view for the given recipe
          *
          * @param position The position of the item in the list
          * @param r        The recipe to create a view for
          * @param v        The view to reuse or null
          * @param vg       The group that the view is a part of
          **/
         public View getView(final int position, Recipe r, View v, ViewGroup vg) {
            if(v == null) {
               v = inflate(R.layout.category_item);
            }

            TextView tv = (TextView) v.findViewById(R.id.name_box);
            AppData.getSingleton().useTextFont(tv);

            // If the recipe is null, then we are at the end of the list and need to load more items.
            if(r == null) {
               tv.setText("Loading...");
            // Otherwise list the recipe's name
            } else {
               tv.setText(r.getName());
            }

            // The edit button is not needed in this activity
            v.findViewById(R.id.edit_button).setVisibility(View.GONE);

            //{ Setup the delete button, used for removing the recipe from this category
            View deleteButton = v.findViewById(R.id.delete_button);

            if(edit) {
               /**
                * When the delete button is clicked, show the delete recipe dialog.
                **/
               deleteButton.setOnClickListener(new OnClickListener() {
                  public void onClick(View v) {
                     Bundle b = new Bundle();

                     b.putInt("position", position);

                     showDialog(DELETE_RECIPE_DIALOG, b);
                  }
               });

               deleteButton.setVisibility(View.VISIBLE);
            } else {
               deleteButton.setVisibility(View.GONE);
            }
            //}

            return v;
         }

         /**
          * Get the id for the given recipe
          *
          * @param item The recipe to get the id for
          **/
         public long getItemId(Recipe item) {
            return item.getId();
         }

         /**
          * Retrieve all recipes in the category.
          *
          * @param offset ignored
          * @param max    ignored
          **/
         public List<Recipe> filter(int offset, int max) {
            List<Recipe> nextRecipes = category.getRecipes();
            return nextRecipes;
         }

         /**
          * Stringify the given recipe
          *
          * @param result The recipe to get a string for
          *
          * @return The name of the recipe
          **/
         public String convertResultToString(Recipe result) {
            if(result == null) {
               return "Null";
            } else {
               return result.getName();
            }
         }

         /**
          * Called when a recipe is clicked in the list.
          * Load the recipe that was clicked.
          *
          * @param av The adapter view
          * @param v  The view that was clicked
          * @param position The position of the item in the list
          * @param id       The id of the selected recipe
          * @param item     The selected recipe
          **/
         public void onItemClick(AdapterView av, View v, int position, long id, Recipe item) {
            Intent intent = new Intent(CategoryList.this, RecipeTabs.class);
            intent.putExtra("id", id);
            startActivity(intent);
         }

         /**
          * Create a view for the given id
          *
          * @param layoutId The id of the view to create
          **/
         private View inflate(int layoutId) {
            LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return vi.inflate(layoutId, null);
         }
      };

      // Set the adapter for the class
      this.adapter = new DynamicLoadAdapter<Recipe>(sp);
   }
}
