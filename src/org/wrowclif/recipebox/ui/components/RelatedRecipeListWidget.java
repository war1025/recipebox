package org.wrowclif.recipebox.ui.components;

import org.wrowclif.recipebox2.R;

import org.wrowclif.recipebox.Actions;
import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.Suggestion;
import org.wrowclif.recipebox.Utility;

import org.wrowclif.recipebox.impl.UtilityImpl;

import org.wrowclif.recipebox.ui.RecipeTabs;

import org.wrowclif.recipebox.ui.components.DialogManager.DialogHandler;

import static org.wrowclif.recipebox.util.ConstantInitializer.assignId;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class RelatedRecipeListWidget {

   private static final int REMOVE_RELATED_RECIPE_DIALOG = assignId();
   private static final int ADD_RELATED_RECIPE_DIALOG    = assignId();

   private Recipe recipe;
   private Utility util;
   private Activity context;
   private View listWidget;
   private Button addButton;
   private ViewGroup recipes;
   private RelatedRecipeDialog relatedDialog;
   private boolean editing;

   public RelatedRecipeListWidget(Recipe recipe, Activity context) {
      this.recipe = recipe;
      this.context = context;
      this.listWidget = getLayoutInflater().inflate(R.layout.list_widget, null);
      this.recipes = (ViewGroup) listWidget.findViewById(R.id.category_list);
      this.editing = false;
      this.util = UtilityImpl.singleton;

      TextView title = (TextView) listWidget.findViewById(R.id.category_label);
      title.setText("Related Recipes");
      AppData.getSingleton().useHeadingFont(title);

      this.addButton = (Button) listWidget.findViewById(R.id.category_button);

      addButton.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            Actions.RECIPE_ADD_RELATED.showNotifications();
            RelatedRecipeListWidget.this.context.showDialog(ADD_RELATED_RECIPE_DIALOG);
         }
      });
   }

   public View getView() {
      refresh();
      return listWidget;
   }

   public void refresh() {
      recipes.removeAllViews();
      for(Suggestion s : recipe.getSuggestedWith()) {
         recipes.addView(createRecipeEntry(s));
      }
      recipes.invalidate();
   }

   protected View createRecipeEntry(Suggestion s) {

      final Recipe suggested = s.getSuggestedRecipe();
      final long id = suggested.getId();

      View v = getLayoutInflater().inflate(R.layout.list_widget_item, null);
      TextView ctv = (TextView) v.findViewById(R.id.name_box);
      AppData.getSingleton().useTextFont(ctv);
      ctv.setText(suggested.getName());

      TextView idBox = (TextView) v.findViewById(R.id.edit_button);
      idBox.setText(id + "");
      idBox.setVisibility(View.GONE);

      v.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            Intent i = new Intent(context, RecipeTabs.class);
            i.putExtra("id", id);
            context.startActivity(i);
         }
      });

      View deleteButton = v.findViewById(R.id.delete_button);
      deleteButton.setVisibility(View.VISIBLE);

      deleteButton.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            Bundle bundle = new Bundle();

            bundle.putLong("id", id);
            context.showDialog(REMOVE_RELATED_RECIPE_DIALOG, bundle);
         }
      });

      return v;
   }

   public void setEditing(boolean editing) {
      this.editing = editing;
      if(editing) {
         listWidget.setVisibility(View.VISIBLE);
      } else if(recipes.getChildCount() == 0) {
         listWidget.setVisibility(View.GONE);
      }
      for(int i = 0; i < recipes.getChildCount(); i++) {
         View child = recipes.getChildAt(i);
         View editGroup = child.findViewById(R.id.edit_group);
         editGroup.setVisibility((editing) ? View.VISIBLE : View.GONE);
      }
      addButton.setVisibility((editing) ? View.VISIBLE : View.GONE);
   }

   public void setupDialogHandlers(DialogManager dialogManager) {

      dialogManager.registerHandler(ADD_RELATED_RECIPE_DIALOG, new DialogHandler() {

         public Dialog createDialog(Bundle bundle) {
            return new RelatedRecipeDialog(context, recipe, RelatedRecipeListWidget.this);
         }

         public void prepareDialog(Dialog d, Bundle bundle) {
            ((RelatedRecipeDialog) d).prepareNew();
         }
      });

      dialogManager.registerHandler(REMOVE_RELATED_RECIPE_DIALOG, new DialogHandler() {

         public Dialog createDialog(Bundle bundle) {
            return new RelatedRecipeDialog(context, recipe, RelatedRecipeListWidget.this);
         }

         public void prepareDialog(Dialog d, Bundle bundle) {
            long rid = bundle.getLong("id", -1);
            ((RelatedRecipeDialog) d).prepareDelete(util.getRecipeById(rid));
         }
      });
   }

   protected LayoutInflater getLayoutInflater() {
      return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
   }
}
