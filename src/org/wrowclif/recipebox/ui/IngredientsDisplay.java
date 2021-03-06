package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.Actions;
import org.wrowclif.recipebox.Ingredient;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.RecipeIngredient;
import org.wrowclif.recipebox2.R;

import org.wrowclif.recipebox.ui.components.BaseActivity;
import org.wrowclif.recipebox.ui.components.DialogManager.DialogHandler;
import org.wrowclif.recipebox.ui.components.EnterTextDialog;
import org.wrowclif.recipebox.ui.components.IngredientDialog;
import org.wrowclif.recipebox.ui.components.RecipeMenus;
import org.wrowclif.recipebox.ui.components.RecipeMenus.EditSwitcher;
import org.wrowclif.recipebox.ui.components.DynamicLoadAdapter;
import org.wrowclif.recipebox.ui.components.ReorderableItemDecorator;
import org.wrowclif.recipebox.ui.components.ReorderableItemDecorator.ItemSwap;

import static org.wrowclif.recipebox.util.ConstantInitializer.assignId;

import java.util.List;
import java.util.ArrayList;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ListView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;


/**
 * Displays the ingredients in the recipe.
 *
 * @param edit             Whether or not the view is in edit mode
 * @param r                The recipe being shown
 * @param adapter          The adapter used to load the ingredients
 * @param reorderDecorator Decorator to use for reordering ingredients
 * @param menus            The menus to show for this activity
 *
 * @param NEW_INGREDIENT_DIALOG    Dialog Id for adding a new ingredient to the recipe
 * @param DELETE_INGREDIENT_DIALOG Dialog Id for removing an ingredient from this recipe
 **/
public class IngredientsDisplay extends BaseActivity {

   private boolean edit;
   private Recipe r;
   private DynamicLoadAdapter<RecipeIngredient> adapter;
   private ReorderableItemDecorator<RecipeIngredient> reorderDecorator;
   private RecipeMenus menus;

   private static final int NEW_INGREDIENT_DIALOG    = assignId();
   private static final int DELETE_INGREDIENT_DIALOG = assignId();

   protected int getViewId() {
      return R.layout.ingredients_display;
   }

   protected int getMenuId() {
      return RecipeMenus.getMenuId();
   }

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState)   {
      super.onCreate(savedInstanceState);
      Actions.RECIPE_INGREDIENTS.showNotifications();

      Intent intent = getIntent();

      // Get the current recipe and our edit state from our parent
      r = ((RecipeTabs) getParent()).curRecipe;
      edit = ((RecipeTabs) getParent()).editing;

      // Create our adapter
      createDynamicLoadAdapter();

      //{ Setup the reorder code for the ingredient list
      ItemSwap swapper = new ItemSwap() {
         public void swapItems(int a, int b) {
            Actions.RECIPE_INGREDIENTS_REORDER.showNotifications();
            r.swapIngredientPositions(adapter.getItem(a), adapter.getItem(b));
         }
      };

      reorderDecorator = new ReorderableItemDecorator<RecipeIngredient>(adapter, swapper);
      //}

      ListView lv = (ListView) findViewById(R.id.ingredient_list);

      lv.setAdapter(adapter);

      //{ Add button
      Button add = (Button) findViewById(R.id.add_button);

      add.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            Bundle bundle = new Bundle();

            showDialog(NEW_INGREDIENT_DIALOG, bundle);
         }
      });
      //}

      //{ Done button
      Button done = (Button) findViewById(R.id.done_button);

      done.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            setEditing(false);
         }
      });
      //}

      /**
       * Helper methods to determine / modify the editing state of this view
       **/
      EditSwitcher es = new EditSwitcher() {
         public void setEditing(boolean editing) {
            IngredientsDisplay.this.setEditing(editing);
         }

         public boolean getEditing() {
            return IngredientsDisplay.this.edit;
         }
      };
      this.menus = new RecipeMenus(r, this, 1, es);

      this.setupDialogHandlers();
      this.menus.setupMenuHandlers(this.menuManager);
      this.menus.setupDialogHandlers(this.dialogManager);

      setEditing(edit);
    }

   /**
    * Called when this activity is about to resume execution
    **/
   public void onResume() {
      super.onResume();
      // Set the proper edit mode based on what the edit mode for the entire recipe is.
      boolean editing = ((RecipeTabs) getParent()).editing;
      if(edit != editing) {
         setEditing(editing);
      }
   }

   /**
    * Set the edit mode for the view
    *
    * @param editing The editing state that we should transition into
    **/
    protected void setEditing(boolean editing) {
      // Show / hide the add and done buttons
      Button add = (Button) findViewById(R.id.add_button);
      Button done = (Button) findViewById(R.id.done_button);
      if(editing) {
         Actions.RECIPE_EDIT.showNotifications();
         if(adapter.getCount() >= 2) {
            Actions.RECIPE_INGREDIENTS_PRE_REORDER.showNotifications();
         }
         add.setVisibility(View.VISIBLE);
         done.setVisibility(View.VISIBLE);
      } else {
         add.setVisibility(View.GONE);
         done.setVisibility(View.GONE);
      }
      edit = editing;
      reorderDecorator.setEditing(editing);
      // Notify the adapter that things have changed so the view reloads
      adapter.notifyDataSetChanged();

      // Update the editing state of our parent widget
      ((RecipeTabs) getParent()).editing = editing;
   }

   private void setupDialogHandlers() {

      this.dialogManager.registerHandler(NEW_INGREDIENT_DIALOG, new DialogHandler() {

         public Dialog createDialog(Bundle bundle) {
            Actions.ADD_RECIPE_INGREDIENT.showNotifications();
            return new IngredientDialog(IngredientsDisplay.this);
         }

         public void prepareDialog(Dialog d, Bundle bundle) {
            IngredientDialog dialog = (IngredientDialog) d;

            // If a position was passed in with the bundle,
            // then we are editing an existing ingredient.
            int position = bundle.getInt("position", -1);
            if(position < 0) {
               dialog.prepareNew(r, adapter);
            } else {
               dialog.prepareExisiting(r, adapter, adapter.getItem(position), position);
            }
         }
      });

      this.dialogManager.registerHandler(DELETE_INGREDIENT_DIALOG, new DialogHandler() {

         public Dialog createDialog(Bundle bundle) {
            EnterTextDialog etd = new EnterTextDialog(IngredientsDisplay.this,
                                                      R.layout.show_text_dialog);

            etd.setTitle("Delete Ingredient");
            etd.setOkButtonText("Delete");

            return etd;
         }

         public void prepareDialog(Dialog d, Bundle bundle) {
            EnterTextDialog dialog = (EnterTextDialog) d;

            // The position should always be included.
            final int position = bundle.getInt("position", -1);
            final RecipeIngredient ri = adapter.getItem(position);

            dialog.setEditHtml("Are you sure you want to remove <b>" +
                               ri.getName() + "</b> from this recipe?");

            dialog.setOkListener(new OnClickListener() {
               public void onClick(View v) {
                  r.removeIngredient(ri);

                  adapter.remove(position);
               }
            });
         }
      });
   }

   /**
    * Create the adapter for loading the ingredients in the recipe
    **/
   private void createDynamicLoadAdapter() {

      DynamicLoadAdapter.Specifics<RecipeIngredient> sp =
            new DynamicLoadAdapter.Specifics<RecipeIngredient>() {

         /**
          * Create a view for an ingredient in the recipe.
          * This includes the logic for the different edit buttons.
          *
          * @param position The position of the row in the list
          * @param i        The ingredient we are showing
          * @param convert  The view to resuse or null
          * @param group    The group the view belongs to
          **/
         public View getView(final int position, RecipeIngredient i,
                                                 View convert, ViewGroup group) {
            if(convert == null) {
               convert = IngredientsDisplay.this.inflate(R.layout.ingredient_display_item);
            }

            IngredientsDisplay.this.useTextFont(convert, R.id.ingredient_box);
            TextView tv = (TextView) convert.findViewById(R.id.ingredient_box);

            Button be = (Button) convert.findViewById(R.id.edit_button);
            Button bd = (Button) convert.findViewById(R.id.delete_button);

            // If the ingredient is null, then we are loading still loading the ingredients.
            if(i == null) {
               tv.setText("Loading...");
               be.setVisibility(View.GONE);
               bd.setVisibility(View.GONE);
               return convert;
            } else {
               tv.setText(i.getAmount() + " " + i.getName());
            }

            // If we are in edit mode, connect the button listeners
            if(edit) {
               be.setVisibility(View.VISIBLE);
               bd.setVisibility(View.VISIBLE);

               be.setOnClickListener(new OnClickListener() {
                  public void onClick(View v) {
                     Bundle bundle = new Bundle();
                     bundle.putInt("position", position);

                     // Using the NEW_INGREDIENT dialog but including this ingredient's position
                     // will show the edit dialog
                     showDialog(NEW_INGREDIENT_DIALOG, bundle);
                  }
               });
               bd.setOnClickListener(new OnClickListener() {
                  public void onClick(View v) {
                     Bundle bundle = new Bundle();
                     bundle.putInt("position", position);

                     showDialog(DELETE_INGREDIENT_DIALOG, bundle);
                  }
               });
            // Otherwise just hide everything
            } else {
               be.setVisibility(View.GONE);
               bd.setVisibility(View.GONE);
            }

            // Hook up the reorder buttons on the view.
            reorderDecorator.decorateItem(convert, position);
            return convert;
         }

         /**
          * Get the id for the item
          *
          * @param item The item to get the id for
          **/
         public long getItemId(RecipeIngredient item) {
            return item.getIngredient().getId();
         }

         /**
          * Load the ingredient's that are in the recipe.
          * We use the dynamic loader so that the load will happen asynchronously.
          *
          * @param offset ignored
          * @param max    ignored
          **/
         public List<RecipeIngredient> filter(int offset, int max) {
            List<RecipeIngredient> ingredients = r.getIngredients();
            return ingredients;
         }

         /**
          * Create a string representation for the ingredient
          *
          * @param result The ingredient to get a string for
          **/
         public String convertResultToString(RecipeIngredient result) {
            return "Ingredient";
         }

         /**
          * Do nothing when the item is clicked
          **/
         public void onItemClick(AdapterView av, View v, int position,
                                                         long id, RecipeIngredient item) {

         }
      };

      this.adapter = new DynamicLoadAdapter<RecipeIngredient>(sp);
   }
}
