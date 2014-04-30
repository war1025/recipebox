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

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState)   {
      super.onCreate(savedInstanceState);
      Actions.RECIPE_INFO.showNotifications();

      Intent intent = getIntent();

      r    = ((RecipeTabs) getParent()).curRecipe;
      edit = ((RecipeTabs) getParent()).editing;

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


      Button[] btns = {(Button) findViewById(R.id.name_button),
                       (Button) findViewById(R.id.description_button),
                       (Button) findViewById(R.id.prep_button),
                       (Button) findViewById(R.id.cook_button)};

      int[] dialogs = {NAME_DIALOG, DESCRIPTION_DIALOG, PREP_TIME_DIALOG, COOK_TIME_DIALOG};

      for(int i = 0; i < btns.length; i++) {
         btns[i].setOnClickListener(new EditClickListener(dialogs[i]));
      }

      findViewById(R.id.done_editing).setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            setEditing(false);
         }
      });

      categories = new CategoryListWidget(r, this);
      View categoryView = categories.getView();
      categoryView.setId(9876);

      RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
         ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      params.addRule(RelativeLayout.BELOW, R.id.description_group);
      params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

      ViewGroup info = (ViewGroup) findViewById(R.id.info_group);
      info.addView(categoryView, params);

      related = new RelatedRecipeListWidget(r, this);

      params = new RelativeLayout.LayoutParams(
         ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      params.addRule(RelativeLayout.BELOW, categoryView.getId());
      params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

      info.addView(related.getView(), params);

      image = new ImageWidget(r, this, (ViewStub) info.findViewById(R.id.image_stub));
      image.refreshImage();

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

      this.setupDialogHandlers();
      this.menus.setupMenuHandlers(this.menuManager);
      this.menus.setupDialogHandlers(this.dialogManager);

      setEditing(edit);
   }

   private void setText(int id, String text) {
      TextView tv = (TextView) findViewById(id);
      tv.setText(text);
   }

   public void onResume() {
      super.onResume();
      boolean editing = ((RecipeTabs) getParent()).editing;
      if(edit != editing) {
         setEditing(editing);
      }
   }

   protected void updateRecipeNameUi() {
      String name = r.getName();
      setText(R.id.name_edit, name);
   }

   protected void updateRecipeDescriptionUi() {
      String description = r.getDescription();
      setText(R.id.description_edit, (description.equals("")) ? "Description" : description);
   }

   protected void updatePrepTimeUi() {
      String prepTime = timeFormat(r.getPrepTime());
      setText(R.id.prep_edit, prepTime);
      setText(R.id.prep_button, prepTime);

      this.updateTotalTimeUi();
   }

   protected void updateCookTimeUi() {
      String cookTime = timeFormat(r.getCookTime());
      setText(R.id.cook_edit, cookTime);
      setText(R.id.cook_button, cookTime);

      this.updateTotalTimeUi();
   }

   protected void updateTotalTimeUi() {
      setText(R.id.total_edit, timeFormat(r.getCookTime() + r.getPrepTime()));
   }


   protected void setEditing(boolean editing) {
      TextView[] labels = {(TextView) findViewById(R.id.prep_edit),
                           (TextView) findViewById(R.id.cook_edit),
                           (TextView) findViewById(R.id.total_label),
                           (TextView) findViewById(R.id.total_edit)};

      Button[] btns = {(Button) findViewById(R.id.name_button),
                       (Button) findViewById(R.id.description_button),
                       (Button) findViewById(R.id.prep_button),
                       (Button) findViewById(R.id.cook_button),
                       (Button) findViewById(R.id.done_editing)};

      if(editing) {
         Actions.RECIPE_EDIT.showNotifications();
         Actions.RECIPE_INFO_EDIT.showNotifications();
         for(Button b : btns) {
            b.setVisibility(View.VISIBLE);
         }
         for(TextView label : labels) {
            label.setVisibility(View.GONE);
         }
         findViewById(R.id.description_edit).setVisibility(View.VISIBLE);
      } else {
         for(Button b : btns) {
            b.setVisibility(View.GONE);
         }
         for(TextView label : labels) {
            label.setVisibility(View.VISIBLE);
         }
         if(r.getDescription().equals("")) {
            findViewById(R.id.description_edit).setVisibility(View.GONE);
         }
      }

      categories.setEditing(editing);
      related.setEditing(editing);
      image.setEditing(editing);
      edit = editing;

      ((RecipeTabs) getParent()).editing = editing;
   }

   protected class EditClickListener implements OnClickListener {

      private int dialog;

      protected EditClickListener(int dialog) {
         this.dialog = dialog;
      }

      public void onClick(View v) {
         showDialog(dialog);
      }
   }

   private void setupDialogHandlers() {
      categories.setupDialogHandlers(this.dialogManager);
      related.setupDialogHandlers(this.dialogManager);
      image.setupDialogHandlers(this.dialogManager);

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

         }
      });

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

         }
      });

      this.dialogManager.registerHandler(NAME_DIALOG, new DialogHandler() {

         public Dialog createDialog(Bundle bundle) {
            final EnterTextDialog dialog = new EnterTextDialog(RecipeDisplay.this);

            dialog.setTitle("Edit Name");

            dialog.setEditText(r.getName());

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

         }
      });

      this.dialogManager.registerHandler(DESCRIPTION_DIALOG, new DialogHandler() {

         public Dialog createDialog(Bundle bundle) {
            final EnterTextDialog dialog = new EnterTextDialog(RecipeDisplay.this);

            dialog.setTitle("Edit Description");

            dialog.setEditText(r.getDescription());

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

         }
      });
   }

   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      if(image.onActivityResult(requestCode, resultCode, data)) {
         return;
      }
   }

   private String timeFormat(int minutes) {
      int hours = minutes / 60;
      int min = minutes % 60;

      return String.format("%02d:%02d", hours, min);
   }

   protected void onSaveInstanceState(Bundle bundle) {
      super.onSaveInstanceState(bundle);
      image.saveInstanceState(bundle);
   }

   protected void onRestoreInstanceState(Bundle bundle) {
      super.onRestoreInstanceState(bundle);
      image.restoreInstanceState(bundle);
   }

}
