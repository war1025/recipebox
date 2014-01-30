package org.wrowclif.recipebox.ui.components;

import java.util.List;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.Utility;
import org.wrowclif.recipebox2.R;

import org.wrowclif.recipebox.impl.UtilityImpl;

import org.wrowclif.recipebox.ui.components.EnterTextDialog;
import org.wrowclif.recipebox.ui.components.ListAutoCompleteAdapter;

import android.content.Context;
import android.text.InputType;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RecipePickDialog extends EnterTextDialog {

   private Utility util;
   private long selectedRecipeId;
   private ListAutoCompleteAdapter<Recipe> autoCompleteAdapter;

   public RecipePickDialog(Context context) {
      this(context, R.layout.enter_recipe_dialog);
   }

   public RecipePickDialog(Context context, int layout) {
      super(context, layout);
      this.getEditView().setHint("Recipe Name");
      this.setEditText("");
      this.getEditView().setInputType(InputType.TYPE_CLASS_TEXT |
                                      InputType.TYPE_TEXT_FLAG_CAP_WORDS);
      this.getEditView().setSingleLine(true);
      this.setOkButtonText("Add");

      util = UtilityImpl.singleton;

      setupAutoCompleteAdapter();
   }

   public long getSelectedRecipeId() {
      return selectedRecipeId;
   }

   public void clearSelectedRecipeId() {
      this.selectedRecipeId = -1;
   }

   private void setupAutoCompleteAdapter() {
      //{ Set up the name field autocomplete
      ListAutoCompleteAdapter.Specifics<Recipe> sp =
            new ListAutoCompleteAdapter.Specifics<Recipe>() {

         /**
          * Gets a row view for the given recipe.
          *
          * @param id The id of the recipe
          * @param r  The recipe that is being shown
          * @param v  The view to reuse if it isn't null
          * @param vg The group that the view is in
          **/
         public View getView(int id, Recipe r, View v, ViewGroup vg) {
            if(v == null) {
               v = inflate(R.layout.autoitem);
            }

            TextView tv = (TextView) v.findViewById(R.id.child_name);
            AppData.getSingleton().useTextFont(tv);

            // Just show the recipe's name
            tv.setText(r.getName());

            return v;
         }

         /**
          * Returns the id for the given recipe
          *
          * @param item The recipe to get the id from
          **/
         public long getItemId(Recipe item) {
            return item.getId();
         }

         /**
          * Loads the top 5 recipe matches for the given text
          *
          * @param seq The text to search with
          **/
         public List<Recipe> filter(CharSequence seq) {
            return util.searchRecipes(seq.toString(), 5);
         }

         /**
          * Get a string representation of the given recipe.
          *
          * @param result The recipe to stringify
          *
          * @return The recipe's name
          **/
         public String convertResultToString(Recipe result) {
            return result.getName();
         }

         /**
          * Called when an item is clicked in the autocomplete list
          *
          * @param av       The adapter
          * @param v        The view that was clicked
          * @param position The position of the selected item in the list
          * @param id       The id of the selected recipe
          * @param item     The selected recipe
          **/
         public void onItemClick(AdapterView av, View v, int position, long id, Recipe item) {
            RecipePickDialog.this.selectedRecipeId = id;
         }

         /**
          * Create a view from the given layoutId
          *
          * @param layoutId The id of the view to create
          **/
         private View inflate(int layoutId) {
            Context ctx = AppData.getSingleton().getContext();
            LayoutInflater vi = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return vi.inflate(layoutId, null);
         }
      };

      // Create an autocomplete adapter
      this.autoCompleteAdapter = new ListAutoCompleteAdapter<Recipe>(sp);

      AutoCompleteTextView acView = (AutoCompleteTextView) this.getEditView();

      // Get the edit view out of the dialog and set the auto complete adapter that we just made
      acView.setAdapter(this.autoCompleteAdapter);

      // Setup the onclick listener
      acView.setOnItemClickListener(this.autoCompleteAdapter.onClick);
      //}
   }
}
