package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.Actions;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox2.R;

import org.wrowclif.recipebox.ui.components.BaseActivity;
import org.wrowclif.recipebox.ui.components.DialogManager.DialogHandler;
import org.wrowclif.recipebox.ui.components.EnterTextDialog;
import org.wrowclif.recipebox.ui.components.RecipeMenus;
import org.wrowclif.recipebox.ui.components.RecipeMenus.EditSwitcher;
import org.wrowclif.recipebox.ui.components.CategoryListWidget;
import org.wrowclif.recipebox.ui.components.RelatedRecipeListWidget;
import org.wrowclif.recipebox.ui.components.ImageWidget;

import static org.wrowclif.recipebox.util.ConstantInitializer.assignId;

import java.util.List;

import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.text.InputType;


/**
 * View showing the general information for a recipe.
 *
 * @param r          The recipe we are displaying.
 * @param edit       Whether or not we are in edit mode.
 * @param menus      Helper for showing menu items and responding to them.
 * @param categories Widget showing the categories that the recipe is in.
 * @param related    Widget showing related recipes
 * @param image      Widget for showing the recipe's image
 **/
public class RecipeDisplay extends BaseActivity {

   private Recipe                  r;
   private boolean                 edit;
   private RecipeMenus             menus;
   private CategoryListWidget      categories;
   private RelatedRecipeListWidget related;
   private ImageWidget             image;

   private static final int NAME_DIALOG        = assignId();
   private static final int DESCRIPTION_DIALOG = assignId();
   private static final int PREP_TIME_DIALOG   = assignId();
   private static final int COOK_TIME_DIALOG   = assignId();
   private static final int EDIT_DIALOG        = assignId();
   private static final int DELETE_DIALOG      = assignId();
   private static final int CREATE_DIALOG      = assignId();

   protected int getViewId() {
      return R.layout.recipe_display;
   }

   protected int getMenuId() {
      return RecipeMenus.getMenuId();
   }

   /**
    * Called when the activity is first created.
    **/
   public void onCreate(Bundle savedInstanceState)   {
      super.onCreate(savedInstanceState);
      Actions.RECIPE_INFO.showNotifications();

      Intent intent = getIntent();

      // The recipe and edit mode are stored in our parent so
      // that they are the same among all three tabs.
      r    = ((RecipeTabs) getParent()).curRecipe;
      edit = ((RecipeTabs) getParent()).editing;

      //{ Update Ui
      this.useHeadingFont(R.id.name_edit,
                          R.id.prep_label,
                          R.id.cook_label,
                          R.id.total_label);

      this.useTextFont(R.id.description_edit,
                       R.id.prep_edit,
                       R.id.prep_button,
                       R.id.cook_edit,
                       R.id.cook_button,
                       R.id.total_edit);


      this.updateRecipeNameUi();
      this.updateRecipeDescriptionUi();
      this.updatePrepTimeUi();
      this.updateCookTimeUi();
      //}

      //{ Setup these buttons to show the proper dialogs
      Button[] btns = {(Button) findViewById(R.id.name_button),
                       (Button) findViewById(R.id.description_button),
                       (Button) findViewById(R.id.prep_button),
                       (Button) findViewById(R.id.cook_button)};

      int[] dialogs = {NAME_DIALOG, DESCRIPTION_DIALOG, PREP_TIME_DIALOG, COOK_TIME_DIALOG};

      for(int i = 0; i < btns.length; i++) {
         btns[i].setOnClickListener(new EditClickListener(dialogs[i]));
      }
      //}

      findViewById(R.id.done_editing).setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            setEditing(false);
         }
      });

      //{ Recipe categories
      categories = new CategoryListWidget(r, this);
      View categoryView = categories.getView();
      categoryView.setId(9876);

      RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
         ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      params.addRule(RelativeLayout.BELOW, R.id.description_group);
      params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

      ViewGroup info = (ViewGroup) findViewById(R.id.info_group);
      info.addView(categoryView, params);
      //}

      //{ Related recipes
      related = new RelatedRecipeListWidget(r, this);

      params = new RelativeLayout.LayoutParams(
         ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      params.addRule(RelativeLayout.BELOW, categoryView.getId());
      params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

      info.addView(related.getView(), params);
      //}

      //{ Recipe image
      image = new ImageWidget(r, this, (ViewStub) info.findViewById(R.id.image_stub));
      image.refreshImage();
      //}

      //{ Menus and dialogs
      this.setupMenu();
      this.setupDialogHandlers();
      //}

      setEditing(edit);
   }


   public void onResume() {
      super.onResume();
      boolean editing = ((RecipeTabs) getParent()).editing;
      if(edit != editing) {
         setEditing(editing);
      }
   }

   /**
    * Switch in or out of edit mode.
    **/
   protected void setEditing(boolean editing) {
      // These views are only shown when we ARE NOT IN edit mode
      View[] read_only = {findViewById(R.id.prep_edit),
                          findViewById(R.id.cook_edit),
                          findViewById(R.id.total_label),
                          findViewById(R.id.total_edit)};

      // These views are shown only when we ARE IN edit mode.
      View[] edit_only = {findViewById(R.id.name_button),
                          findViewById(R.id.description_button),
                          findViewById(R.id.prep_button),
                          findViewById(R.id.cook_button),
                          findViewById(R.id.done_editing),
                          findViewById(R.id.description_edit)};

      if(editing) {
         Actions.RECIPE_EDIT.showNotifications();
         Actions.RECIPE_INFO_EDIT.showNotifications();
         for(View v : edit_only) {
            v.setVisibility(View.VISIBLE);
         }
         for(View v : read_only) {
            v.setVisibility(View.GONE);
         }
      } else {
         for(View v : edit_only) {
            v.setVisibility(View.GONE);
         }
         for(View v : read_only) {
            v.setVisibility(View.VISIBLE);
         }

         // Only show the description edit if there is a description to show
         if(r.getDescription().length() > 0) {
            findViewById(R.id.description_edit).setVisibility(View.VISIBLE);
         }
      }

      categories.setEditing(editing);
      related.setEditing(editing);
      image.setEditing(editing);

      //{ Update the edit state, both here and in the parent view
      edit = editing;

      ((RecipeTabs) getParent()).editing = editing;
      //}
   }

   /**
    * Listener that will show a dialog when called.
    *
    * @param dialog The id of the dialog to show.
    **/
   protected class EditClickListener implements OnClickListener {

      private int dialog;

      protected EditClickListener(int dialog) {
         this.dialog = dialog;
      }

      public void onClick(View v) {
         showDialog(dialog);
      }
   }

   /**
    * Set up the menu used in this view.
    **/
   private void setupMenu() {

      /**
       * Helper methods to determine / modify the editing state of this view
       **/
      EditSwitcher es = new EditSwitcher() {
         public void setEditing(boolean editing) {
            RecipeDisplay.this.setEditing(editing);
         }

         public boolean getEditing() {
            return RecipeDisplay.this.edit;
         }
      };
      this.menus = new RecipeMenus(r, this, 0, es);

      this.menus.setupMenuHandlers(this.menuManager);
      this.menus.setupDialogHandlers(this.dialogManager);
   }

   /**
    * Set up the dialogs used in this view.
    **/
   private void setupDialogHandlers() {
      //{ These sub views have dialogs that they display
      categories.setupDialogHandlers(this.dialogManager);
      related.setupDialogHandlers(this.dialogManager);
      image.setupDialogHandlers(this.dialogManager);
      //}

      //{ Dialog for setting the prep time
      this.dialogManager.registerHandler(PREP_TIME_DIALOG, new DialogHandler() {

         public Dialog createDialog(Bundle bundle) {
            int time = r.getPrepTime();

            return new TimePickerDialog(RecipeDisplay.this, new OnTimeSetListener() {
                  public void onTimeSet(TimePicker picker, int hour, int minute) {
                     r.setPrepTime(hour * 60 + minute);

                     RecipeDisplay.this.updatePrepTimeUi();
                  }
               }, time / 60, time % 60, true);
         }

         public void prepareDialog(Dialog d, Bundle bundle) {
            // No preparation needed before showing.
         }
      });
      //}

      //{ Dialog for setting the cook time
      this.dialogManager.registerHandler(COOK_TIME_DIALOG, new DialogHandler() {

         public Dialog createDialog(Bundle bundle) {
            int time = r.getCookTime();

            return new TimePickerDialog(RecipeDisplay.this, new OnTimeSetListener() {
                  public void onTimeSet(TimePicker picker, int hour, int minute) {
                     r.setCookTime(hour * 60 + minute);

                     RecipeDisplay.this.updateCookTimeUi();
                  }
               }, time / 60, time % 60, true);
         }

         public void prepareDialog(Dialog d, Bundle bundle) {
            // No preparation needed before showing.
         }
      });
      //}

      //{ Dialog for editing the name of the recipe
      this.dialogManager.registerHandler(NAME_DIALOG, new DialogHandler() {

         public Dialog createDialog(Bundle bundle) {
            final EnterTextDialog dialog = new EnterTextDialog(RecipeDisplay.this);

            dialog.setTitle("Edit Name");

            dialog.getEditView().setInputType(InputType.TYPE_CLASS_TEXT
                                              | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            dialog.getEditView().setSingleLine(true);

            dialog.setOkListener(new OnClickListener() {
               public void onClick(View v) {
                  String value = dialog.getEditText();

                  r.setName(value);

                  RecipeDisplay.this.updateRecipeNameUi();

                  String name = r.getName();
                  ((RecipeTabs) getParent()).setTitle(name.equals("") ? "Recipe Box" : name);
               }
            });

            dialog.setCancelable(true);

            return dialog;
         }

         public void prepareDialog(Dialog d, Bundle bundle) {
            EnterTextDialog dialog = (EnterTextDialog) d;
            String name = r.getName();

            // Clear out "New Recipe" so the user doesn't have to.
            if ("New Recipe".equals(name)) {
               dialog.setEditText("");
            } else {
               dialog.setEditText(r.getName());
            }
         }
      });
      //}

      //{ Dialog for editing the description of the recipe
      this.dialogManager.registerHandler(DESCRIPTION_DIALOG, new DialogHandler() {

         public Dialog createDialog(Bundle bundle) {
            final EnterTextDialog dialog = new EnterTextDialog(RecipeDisplay.this);

            dialog.setTitle("Edit Description");

            dialog.getEditView().setInputType(InputType.TYPE_CLASS_TEXT
                                              | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            dialog.getEditView().setSingleLine(false);

            dialog.setOkListener(new OnClickListener() {
               public void onClick(View v) {
                  String value = dialog.getEditText();

                  r.setDescription(value);

                  RecipeDisplay.this.updateRecipeDescriptionUi();
               }
            });

            dialog.setCancelable(true);

            return dialog;
         }

         public void prepareDialog(Dialog d, Bundle bundle) {
            EnterTextDialog dialog = (EnterTextDialog) d;

            dialog.setEditText(r.getDescription());
         }
      });
      //}
   }

   /**
    * Called when an activity that we started returns its result.
    *
    * @param requestCode The code identifying which activity was started.
    * @param resultCode  The code indicating whether the activity succeeded.
    * @param data        Any data returned as a result of the activity.
    **/
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      if(image.onActivityResult(requestCode, resultCode, data)) {
         return;
      }
   }

   /**
    * Called when the activity is being killed so that we can save our state.
    **/
   protected void onSaveInstanceState(Bundle bundle) {
      super.onSaveInstanceState(bundle);
      image.saveInstanceState(bundle);
   }

   /**
    * Called when we are restoring a previous instance of this activity.
    **/
   protected void onRestoreInstanceState(Bundle bundle) {
      super.onRestoreInstanceState(bundle);
      image.restoreInstanceState(bundle);
   }

   /**
    * Update the name of the recipe in the ui.
    **/
   protected void updateRecipeNameUi() {
      String name = r.getName();
      setText(R.id.name_edit, name);
   }

   /**
    * Update the description in the ui.
    **/
   protected void updateRecipeDescriptionUi() {
      String description = r.getDescription();
      setText(R.id.description_edit, (description.equals("")) ? "Description" : description);
   }

   /**
    * Update the prep time in the ui.
    **/
   protected void updatePrepTimeUi() {
      String prepTime = timeFormat(r.getPrepTime());
      setText(R.id.prep_edit, prepTime);
      setText(R.id.prep_button, prepTime);

      this.updateTotalTimeUi();
   }

   /**
    * Update the cook time in the ui.
    **/
   protected void updateCookTimeUi() {
      String cookTime = timeFormat(r.getCookTime());
      setText(R.id.cook_edit, cookTime);
      setText(R.id.cook_button, cookTime);

      this.updateTotalTimeUi();
   }

   /**
    * Update the total time in the ui.
    **/
   protected void updateTotalTimeUi() {
      setText(R.id.total_edit, timeFormat(r.getCookTime() + r.getPrepTime()));
   }

   /**
    * Helper for setting the test of the label with the given id to the given text.
    *
    * @param id   Id of the label to set the text on.
    * @param text The text to use.
    **/
   private void setText(int id, String text) {
      TextView tv = (TextView) findViewById(id);
      tv.setText(text);
   }

   /**
    * Format the given number of minutes into a HH:MM string.
    *
    * @param minutes The number of minutes.
    **/
   private String timeFormat(int minutes) {
      int hours = minutes / 60;
      int min = minutes % 60;

      return String.format("%02d:%02d", hours, min);
   }
}
