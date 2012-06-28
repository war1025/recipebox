package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.Actions;
import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Category;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.Utility;
import org.wrowclif.recipebox.R;

import org.wrowclif.recipebox.ui.components.EnterTextDialog;
import org.wrowclif.recipebox.ui.components.RecipeMenus;
import org.wrowclif.recipebox.ui.components.RecipeMenus.EditSwitcher;
import org.wrowclif.recipebox.ui.components.CategoryListWidget;
import org.wrowclif.recipebox.ui.components.RelatedRecipeListWidget;

import org.wrowclif.recipebox.impl.UtilityImpl;

import static org.wrowclif.recipebox.util.ConstantInitializer.assignId;

import java.util.List;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.text.TextWatcher;
import android.text.Editable;
import android.text.method.DigitsKeyListener;
import android.text.InputType;

import java.util.ArrayList;

public class RecipeDisplay extends Activity {

	private Recipe r;
	private Utility util;
	private AppData appData;
	private boolean edit;
	private RecipeMenus menus;
	private CategoryListWidget categories;
	private RelatedRecipeListWidget related;

	private static final int NAME_DIALOG = assignId();
	private static final int DESCRIPTION_DIALOG = assignId();
	private static final int PREP_TIME_DIALOG = assignId();
	private static final int COOK_TIME_DIALOG = assignId();
	private static final int EDIT_DIALOG = assignId();
	private static final int DELETE_DIALOG = assignId();
	private static final int CREATE_DIALOG = assignId();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recipe_display);
		Actions.RECIPE_INFO.showNotifications();

		Intent intent = getIntent();
		util = UtilityImpl.singleton;
		appData = AppData.getSingleton();

		r = ((RecipeTabs) getParent()).curRecipe;
		edit = ((RecipeTabs) getParent()).editing;

		if(r != null) {

			String name = r.getName();
			setText(R.id.name_edit, name);
			appData.useHeadingFont((TextView) findViewById(R.id.name_edit));

			String description = r.getDescription();
			setText(R.id.description_edit, (description.equals("")) ? "Description" : description);
			appData.useTextFont((TextView) findViewById(R.id.description_edit));

			String prepTime = timeFormat(r.getPrepTime());
			setText(R.id.prep_edit, prepTime);
			setText(R.id.prep_button, prepTime);
			appData.useHeadingFont((TextView) findViewById(R.id.prep_label));
			appData.useTextFont((TextView) findViewById(R.id.prep_edit));
			appData.useTextFont((TextView) findViewById(R.id.prep_button));

			String cookTime = timeFormat(r.getCookTime());
			setText(R.id.cook_edit, cookTime);
			setText(R.id.cook_button, cookTime);
			appData.useHeadingFont((TextView) findViewById(R.id.cook_label));
			appData.useTextFont((TextView) findViewById(R.id.cook_edit));
			appData.useTextFont((TextView) findViewById(R.id.cook_button));

			setText(R.id.total_edit, timeFormat(r.getCookTime() + r.getPrepTime()));
			appData.useHeadingFont((TextView) findViewById(R.id.total_label));
			appData.useTextFont((TextView) findViewById(R.id.total_edit));

			Button[] btns = {(Button) findViewById(R.id.name_button), (Button) findViewById(R.id.description_button),
								(Button) findViewById(R.id.prep_button), (Button) findViewById(R.id.cook_button)};

			int[] dialogs = {NAME_DIALOG, DESCRIPTION_DIALOG, PREP_TIME_DIALOG, COOK_TIME_DIALOG};

			for(int i = 0; i < btns.length; i++) {
				btns[i].setOnClickListener(new EditClickListener(dialogs[i]));
			}

			Button doneButton = (Button) findViewById(R.id.done_editing);

			doneButton.setOnClickListener(new OnClickListener() {
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

			setEditing(edit);
		}
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

	protected void setEditing(boolean editing) {
		TextView[] labels = {(TextView) findViewById(R.id.prep_edit), (TextView) findViewById(R.id.cook_edit),
								(TextView) findViewById(R.id.total_label), (TextView) findViewById(R.id.total_edit)};

		Button[] btns = {(Button) findViewById(R.id.name_button), (Button) findViewById(R.id.description_button),
							(Button) findViewById(R.id.prep_button), (Button) findViewById(R.id.cook_button),
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

	protected Dialog onCreateDialog(int id) {
		Dialog d = null;
		if(menus != null) {
			d = menus.createDialog(id);
			if(d != null) {
				return d;
			}
		}
		d = categories.createDialog(id);
		if(d != null) {
			return d;
		}

		d = related.createDialog(id);
		if(d != null) {
			return d;
		}

		if((id == PREP_TIME_DIALOG) || (id == COOK_TIME_DIALOG)) {
			return showTimeDialog(id);
		} else {
			return showTextDialog(id);
		}

	}

	protected Dialog showTimeDialog(int id) {
		if(id == PREP_TIME_DIALOG) {
			int time = r.getPrepTime();

			return new TimePickerDialog(RecipeDisplay.this, new OnTimeSetListener() {
					public void onTimeSet(TimePicker picker, int hour, int minute) {
						r.setPrepTime(hour * 60 + minute);

						String prepTime = timeFormat(r.getPrepTime());
						setText(R.id.prep_edit, prepTime);
						setText(R.id.prep_button, prepTime);

						setText(R.id.total_edit, timeFormat(r.getPrepTime() + r.getCookTime()));
					}
				}, time / 60, time % 60, true);

		} else if(id == COOK_TIME_DIALOG) {
			int time = r.getCookTime();

			return new TimePickerDialog(RecipeDisplay.this, new OnTimeSetListener() {
					public void onTimeSet(TimePicker picker, int hour, int minute) {
						r.setCookTime(hour * 60 + minute);

						String cookTime = timeFormat(r.getCookTime());
						setText(R.id.cook_edit, cookTime);
						setText(R.id.cook_button, cookTime);

						setText(R.id.total_edit, timeFormat(r.getPrepTime() + r.getCookTime()));
					}
				}, time / 60, time % 60, true);
		}

		return null;
	}

	protected Dialog showTextDialog(int id) {
		final EnterTextDialog dialog = new EnterTextDialog(this);

		if(id == NAME_DIALOG) {
			dialog.setTitle("Edit Name");

			dialog.setEditText(r.getName());

			dialog.getEditView().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
			dialog.getEditView().setSingleLine(true);

			dialog.setOkListener(new OnClickListener() {
				public void onClick(View v) {
					String value = dialog.getEditText();

					r.setName(value);

					String name = r.getName();
					setText(R.id.name_edit, name.equals("") ? "Name" : name);

					((RecipeTabs) getParent()).setTitle(name.equals("") ? "Recipe Box" : name);
				}
			});

		} else if(id == DESCRIPTION_DIALOG) {
			dialog.setTitle("Edit Description");

			dialog.setEditText(r.getDescription());

			dialog.getEditView().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
			dialog.getEditView().setSingleLine(false);

			dialog.setOkListener(new OnClickListener() {
				public void onClick(View v) {
					String value = dialog.getEditText();

					r.setDescription(value);

					String description = r.getDescription();
					setText(R.id.description_edit, description.equals("") ? "Description" : description);
				}
			});
		}

		dialog.setCancelable(true);

		return dialog;
	}

	protected void onPrepareDialog(int id, Dialog d, Bundle bundle) {

		if(categories.prepareDialog(id, d, bundle)) {
			return;
		} else if(related.prepareDialog(id, d, bundle)) {
			return;
		} else {
			super.onPrepareDialog(id, d, bundle);
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		EditSwitcher es = new EditSwitcher() {
			public void setEditing(boolean editing) {
				RecipeDisplay.this.setEditing(editing);
			}

			public boolean getEditing() {
				return RecipeDisplay.this.edit;
			}
		};

		menus = new RecipeMenus(r, this, 0, es);

		menus.createMenu(menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		boolean menuHandled = menus.onItemSelect(item.getItemId());

		if(!menuHandled) {
			return super.onOptionsItemSelected(item);
		} else {
			return true;
		}
	}

	private String timeFormat(int minutes) {
		int hours = minutes / 60;
		int min = minutes % 60;

		return String.format("%02d:%02d", hours, min);
	}

}
