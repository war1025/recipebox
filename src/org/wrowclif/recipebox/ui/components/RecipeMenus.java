package org.wrowclif.recipebox.ui.components;

import org.wrowclif.recipebox.Actions;
import org.wrowclif.recipebox2.R;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.impl.UtilityImpl;
import org.wrowclif.recipebox.ui.RecipeTabs;
import org.wrowclif.recipebox.util.ShareUtil;

import org.wrowclif.recipebox.ui.components.DialogManager.DialogHandler;
import org.wrowclif.recipebox.ui.components.MenuManager.MenuHandler;

import static org.wrowclif.recipebox.util.ConstantInitializer.assignId;

import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class RecipeMenus {

   private Recipe recipe;
   private Activity activity;
   private int tab;
   private EditSwitcher switcher;

   private static final int SHARE_RECIPE_DIALOG = assignId();
   private static final int EDIT_RECIPE_DIALOG = assignId();
   private static final int DELETE_RECIPE_DIALOG = assignId();

   public static int getMenuId() {
      return R.menu.recipe_menu;
   }

   public RecipeMenus(Recipe recipe, Activity activity, int tab, EditSwitcher switcher) {
      this.recipe = recipe;
      this.activity = activity;
      this.tab = tab;
      this.switcher = switcher;
   }

   public void setupMenuHandlers(MenuManager menuManager) {

      menuManager.registerHandler(R.id.share, new MenuHandler() {
         public void itemSelected(MenuItem item) {
            Actions.RECIPE_SHARE.showNotifications();
            ShareUtil.share(activity, recipe);
         }
      });

      menuManager.registerHandler(R.id.edit, new MenuHandler() {
         public void itemSelected(MenuItem item) {
            if(switcher.getEditing()) {
               switcher.setEditing(false);
            } else {
               Actions.RECIPE_EDIT_DIALOG.showNotifications();
               activity.showDialog(EDIT_RECIPE_DIALOG);
            }
         }
      });

      menuManager.registerHandler(R.id.delete, new MenuHandler() {
         public void itemSelected(MenuItem item) {
            activity.showDialog(DELETE_RECIPE_DIALOG);
         }
      });
   }

   public void setupDialogHandlers(DialogManager dialogManager) {

      dialogManager.registerHandler(EDIT_RECIPE_DIALOG, new DialogHandler() {

         public Dialog createDialog(Bundle bundle) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Edit Recipe");
            builder.setItems(new String[] {"Edit This Recipe", "Create Variant", "Cancel"},
               new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int which) {
                     switch(which) {
                        case 0 :
                           switcher.setEditing(true);
                           break;
                        case 1 :
                           Recipe r2 = recipe.branch(recipe.getName() + " (branch)");
                           Intent intent = new Intent(activity, RecipeTabs.class);
                           intent.putExtra("id", r2.getId());
                           intent.putExtra("edit", true);
                           intent.putExtra("tab", tab);

                           activity.finish();
                           activity.startActivity(intent);
                           break;
                     }
                  }
            });
            builder.setCancelable(true);
            return builder.create();
         }

         public void prepareDialog(Dialog d, Bundle bundle) {

         }
      });

      dialogManager.registerHandler(DELETE_RECIPE_DIALOG, new DialogHandler() {

         public Dialog createDialog(Bundle bundle) {
            EnterTextDialog etd = new EnterTextDialog(activity, R.layout.show_text_dialog);

            etd.setTitle("Delete Recipe");

            etd.setEditText("Are you sure you want to delete the recipe?");

            etd.setOkButtonText("Delete");
            etd.setOkListener(new OnClickListener() {
               public void onClick(View v) {
                  recipe.delete();
                  activity.finish();
               }
            });

            return etd;
         }

         public void prepareDialog(Dialog d, Bundle bundle) {

         }
      });
   }

   public interface EditSwitcher {

      public void setEditing(boolean editing);

      public boolean getEditing();

   }
}
