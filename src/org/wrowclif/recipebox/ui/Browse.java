package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.Actions;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.Utility;
import org.wrowclif.recipebox2.R;

import org.wrowclif.recipebox.impl.UtilityImpl;

import org.wrowclif.recipebox.ui.components.BaseActivity;
import org.wrowclif.recipebox.ui.components.DynamicLoadAdapter;
import org.wrowclif.recipebox.ui.components.MenuManager.MenuHandler;

import static org.wrowclif.recipebox.util.ConstantInitializer.assignId;

import java.util.List;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Allows the user to browse through all recipes in the system, sorted alphabetically
 *
 * @param util              Refrence to the Utility singleton for querying recipes
 * @param recipeListAdapter Adapter for loading recipes by name
 **/
public class Browse extends BaseActivity {

   private Utility util;
   private DynamicLoadAdapter<Recipe> recipeListAdapter;

   public int getViewId() {
      return R.layout.browse;
   }

   public int getMenuId() {
      return R.menu.browse_menu;
   }

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      Actions.BROWSE.showNotifications();

      util = UtilityImpl.singleton;

      ListView lv = (ListView) findViewById(R.id.browse_recipes);

      this.useHeadingFont(R.id.browse_label);

      createDynamicLoadAdapter();

      recipeListAdapter.setUpList(lv);

      setupMenuHandlers();
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
         public View getView(int id, Recipe r, View v, ViewGroup vg) {
            if(v == null) {
               v = Browse.this.inflate(R.layout.category_item);
            }

            v.findViewById(R.id.edit_group).setVisibility(View.GONE);

            Browse.this.useTextFont(v, R.id.name_box);
            TextView tv = (TextView) v.findViewById(R.id.name_box);

            // If the recipe is null, then we are at the end of the list and need to load more recipes
            if(r == null) {
               tv.setText("Loading...");
             // Otherwise show the name of the recipe
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
          * Retrieves additional recipes to show in the list
          *
          * @param offset How many recipes are currently shown
          * @param max    The maximum number of new recipes to return
          **/
         public List<Recipe> filter(int offset, int max) {
            // Load recipes sorted by name
            List<Recipe> nextRecipes = UtilityImpl.singleton.getRecipesByName(offset, max);
            // If we have the maximum number, then add a null item so we will know to
            // load more when we get to the end of the list
            if(nextRecipes.size() == max) {
               nextRecipes.add(null);
            }
            return nextRecipes;
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
            Intent intent = new Intent(Browse.this, RecipeTabs.class);
            intent.putExtra("id", id);
            startActivity(intent);
         }
      };

      recipeListAdapter = new DynamicLoadAdapter<Recipe>(sp);
   }

   private void setupMenuHandlers() {
      this.menuManager.registerHandler(R.id.export, new MenuHandler() {
         public void itemSelected(MenuItem item) {
            // Create the Export intent
            Intent intent = new Intent(Browse.this, Export.class);
            startActivity(intent);
         }
      });

      this.menuManager.registerHandler(R.id.backup, new MenuHandler() {
         public void itemSelected(MenuItem item) {
            // Create the Export intent
            Intent intent = new Intent(Browse.this, Backup.class);
            startActivity(intent);
         }
      });
   }
}
