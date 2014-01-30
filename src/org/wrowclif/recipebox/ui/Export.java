package org.wrowclif.recipebox.ui;

import java.util.List;
import java.util.ArrayList;

import org.wrowclif.recipebox.Actions;
import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.Utility;
import org.wrowclif.recipebox2.R;

import org.wrowclif.recipebox.impl.UtilityImpl;

import org.wrowclif.recipebox.util.ShareUtil;

import org.wrowclif.recipebox.ui.components.EnterTextDialog;
import org.wrowclif.recipebox.ui.components.RecipePickDialog;
import org.wrowclif.recipebox.ui.components.DynamicLoadAdapter;

import static org.wrowclif.recipebox.util.ConstantInitializer.assignId;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Builds a list of recipies to export as a single bundle.
 **/
public class Export extends Activity {

   private static final String LOG_TAG = "Recipebox Export";

   private Utility util;
   private DynamicLoadAdapter<Recipe> recipeListAdapter;
   private List<Long> recipeIds;

   private static final int ADD_RECIPE_DIALOG = assignId();
   private static final int DELETE_RECIPE_DIALOG = assignId();

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.categories);
      Actions.EXPORT.showNotifications();

      util = UtilityImpl.singleton;

      ListView lv = (ListView) findViewById(R.id.category_list);

      createDynamicLoadAdapter();

      recipeListAdapter.setUpList(lv);

      TextView label = (TextView) findViewById(R.id.category_label);
      AppData.getSingleton().useHeadingFont(label);
      label.setText("Export");

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
            List<Recipe> recipes = Export.this.util.getRecipesForIds(Export.this.recipeIds);
            if(recipes.size() > 0) {
               ShareUtil.export(Export.this, recipes.toArray(new Recipe[0]));
            }
            Export.this.finish();
         }
      });
      //}

      recipeIds = new ArrayList<Long>();
    }

   /**
    * Called when this activity resumes execution
    **/
    public void onResume() {
      super.onResume();
      // Clear the recipe list so it refreshes
      recipeListAdapter.clear();
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

         pickDialog.setTitle("Add Recipe To Export");

         //{ Setup the add button handler.
         pickDialog.setOkListener(new OnClickListener() {
            public void onClick(View v) {
               // If an id has been set, attempt to retrieve a recipe with that id
               // then add that recipe to the category and clear the adapter to reload it.
               long recipeId = pickDialog.getSelectedRecipeId();
               if(recipeId >= 0) {
                  Recipe r = util.getRecipeById(recipeId);

                  if(r != null) {
                     Export.this.recipeIds.add(recipeId);

                     pickDialog.clearSelectedRecipeId();

                     Export.this.recipeListAdapter.clear();
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
         final Recipe recipe = Export.this.recipeListAdapter.getItem(position);

         // The button listener has to be redone every time so that we can hook it up to the proper recipe
         dialog.setEditHtml("Are you sure you want to remove <b>" +
                            recipe.getName() + "</b> from the recipes being exported?");
         dialog.setOkListener(new OnClickListener() {
            public void onClick(View v) {
               Export.this.recipeIds.remove(recipe.getId());
               Export.this.recipeListAdapter.remove(position);
            }
         });
      }
   }

   /**
    * Sets up the dynamic load adapter for the recipe list
    **/
   private void createDynamicLoadAdapter() {

      DynamicLoadAdapter.Specifics<Recipe> sp = new DynamicLoadAdapter.Specifics<Recipe>() {

         /**
          * Gets the view to show for the given recipe
          *
          * @param id The id of the recipe
          * @param r  The recipe to create a row view for
          * @param v  The view to reuse
          * @param vg The group the view belongs to
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
            //}

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
          * Retrieves additional recipes to show in the list
          *
          * @param offset How many recipes are currently shown
          * @param max    The maximum number of new recipes to return
          **/
         public List<Recipe> filter(int offset, int max) {
            // Load recipes sorted by name
            List<Recipe> recipes = UtilityImpl.singleton.getRecipesForIds(recipeIds);

            return recipes;
         }

         /**
          * Return a string representation of the recipe (Its name)
          *
          * @param result The recipe to get a string for
          **/
         public String convertResultToString(Recipe result) {
            if(result == null) {
               return "Null";
            } else {
               return result.getName();
            }
         }

         /**
          * Called when a row in the list is clicked.
          * Open the selected recipe
          *
          * @param av       The parent view
          * @param v        The view that was selected
          * @param position The position of the item in the list
          * @param id       The id of the selected recipe
          * @param item     The recipe that was selected
          **/
         public void onItemClick(AdapterView av, View v, int position, long id, Recipe item) {
         }

         /**
          * Create a view for the given id
          *
          * @param layoutId The id for the layout to create
          **/
         private View inflate(int layoutId) {
            LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return vi.inflate(layoutId, null);
         }
      };

      recipeListAdapter = new DynamicLoadAdapter<Recipe>(sp);
   }
}
