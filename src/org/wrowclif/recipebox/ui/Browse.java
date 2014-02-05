package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.Actions;
import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.Utility;
import org.wrowclif.recipebox2.R;

import org.wrowclif.recipebox.impl.UtilityImpl;
import org.wrowclif.recipebox.util.BackupUtil;

import org.wrowclif.recipebox.ui.components.BaseActivity;
import org.wrowclif.recipebox.ui.components.DialogManager.DialogHandler;
import org.wrowclif.recipebox.ui.components.MenuManager.MenuHandler;
import org.wrowclif.recipebox.ui.components.EnterTextDialog;
import org.wrowclif.recipebox.ui.components.DynamicLoadAdapter;

import static org.wrowclif.recipebox.util.ConstantInitializer.assignId;

import java.util.List;
import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Allows the user to browse through all recipes in the system, sorted alphabetically
 *
 * @param util              Refrence to the Utility singleton for querying recipes
 * @param recipeListAdapter Adapter for loading recipes by name
 **/
public class Browse extends BaseActivity {

   private Utility util;
   private DynamicLoadAdapter<Recipe> recipeListAdapter;

   private static final int BACKUP_DB_DIALOG = assignId();

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

      AppData.getSingleton().useHeadingFont((TextView) findViewById(R.id.browse_label));

      createDynamicLoadAdapter();

      recipeListAdapter.setUpList(lv);

      setupDialogHandlers();
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
               v = inflate(R.layout.category_item);
            }

            v.findViewById(R.id.edit_group).setVisibility(View.GONE);

            TextView tv = (TextView) v.findViewById(R.id.name_box);
            tv.setTypeface(AppData.getSingleton().getTextFont());

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

   private void setupDialogHandlers() {

      //{ Add handler
      DialogHandler backup_handler = new DialogHandler() {
         public Dialog createDialog(Bundle bundle) {
            final EnterTextDialog backup_dialog = new EnterTextDialog(Browse.this);

            backup_dialog.setTitle("Create Backup");
            backup_dialog.setOkButtonText("Backup");

            //{ Setup the add button handler.
            backup_dialog.setOkListener(new OnClickListener() {
               public void onClick(View v) {
                  String backup_name = backup_dialog.getEditText();
                  boolean success = BackupUtil.createBackup(backup_name);
                  String msg = "";
                  if(success) {
                     msg = "Backup saved to external storage";
                  } else {
                     msg = "Could not create backup";
                  }
                  Toast.makeText(Browse.this, msg, Toast.LENGTH_LONG).show();
               }
            });
            //}

            return backup_dialog;
         }

         public void prepareDialog(Dialog d, Bundle bundle) {
            EnterTextDialog dialog = (EnterTextDialog) d;
            dialog.setEditText("");
         }
      };
      this.dialogManager.registerHandler(BACKUP_DB_DIALOG, backup_handler);
      //}
   }

   private void setupMenuHandlers() {
      MenuHandler export_handler = new MenuHandler() {
         public void itemSelected(MenuItem item) {
            // Create the Export intent
            Intent intent = new Intent(Browse.this, Export.class);
            startActivity(intent);
         }
      };
      this.menuManager.registerHandler(R.id.export, export_handler);

      MenuHandler backup_handler = new MenuHandler() {
         public void itemSelected(MenuItem item) {
            showDialog(BACKUP_DB_DIALOG);
         }
      };
      this.menuManager.registerHandler(R.id.backup, backup_handler);
   }
}
