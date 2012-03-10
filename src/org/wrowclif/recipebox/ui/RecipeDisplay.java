package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Category;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.Utility;
import org.wrowclif.recipebox.R;

import org.wrowclif.recipebox.ui.components.RecipeMenus;
import org.wrowclif.recipebox.ui.components.RecipeMenus.EditSwitcher;

import org.wrowclif.recipebox.impl.UtilityImpl;

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
	private boolean edit;
	private RecipeMenus menus;
	private static final int NAME_DIALOG = 0;
	private static final int DESCRIPTION_DIALOG = 1;
	private static final int PREP_TIME_DIALOG = 2;
	private static final int COOK_TIME_DIALOG = 3;
	private static final int EDIT_DIALOG = 5;
	private static final int DELETE_DIALOG = 6;
	private static final int CREATE_DIALOG = 7;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recipe_display);

		Intent intent = getIntent();
		util = UtilityImpl.singleton;

		r = ((RecipeTabs) getParent()).curRecipe;
		edit = ((RecipeTabs) getParent()).editing;

		if(r != null) {
			setTitle(r.getName());

			setText(R.id.name_edit, r.getName());
			setText(R.id.description_edit, r.getDescription());
			setText(R.id.prep_edit, timeFormat(r.getPrepTime()));
			setText(R.id.cook_edit, timeFormat(r.getCookTime()));
			setText(R.id.total_edit, timeFormat(r.getCookTime() + r.getPrepTime()));

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

			findViewById(R.id.category_button).setVisibility(View.GONE);
			ViewGroup categories = (ViewGroup) findViewById(R.id.category_box);

			LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			for(Category c : r.getCategories()) {
				View v = li.inflate(R.layout.autoitem, null);
				TextView ctv = (TextView) v.findViewById(R.id.child_name);
				ctv.setText(c.getName());
				categories.addView(v);
			}

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
		TextView[] labels = {(TextView) findViewById(R.id.name_label), (TextView) findViewById(R.id.description_label),
								(TextView) findViewById(R.id.prep_label), (TextView) findViewById(R.id.cook_label),
								(TextView) findViewById(R.id.total_label), (TextView) findViewById(R.id.total_edit)};

		Button[] btns = {(Button) findViewById(R.id.name_button), (Button) findViewById(R.id.description_button),
							(Button) findViewById(R.id.prep_button), (Button) findViewById(R.id.cook_button),
							(Button) findViewById(R.id.done_editing)};

		if(editing) {
			for(Button b : btns) {
				b.setVisibility(View.VISIBLE);
			}
			for(TextView label : labels) {
				label.setVisibility(View.GONE);
			}
		} else {
			for(Button b : btns) {
				b.setVisibility(View.GONE);
			}
			for(TextView label : labels) {
				label.setVisibility(View.VISIBLE);
			}
		}
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
		if(menus != null) {
			Dialog d = menus.createDialog(id);
			if(d != null) {
				return d;
			}
		}
		switch(id) {
			case PREP_TIME_DIALOG : case COOK_TIME_DIALOG :
				return showTimeDialog(id);
			default :
				return showTextDialog(id);
		}
	}

	protected Dialog showTimeDialog(int id) {
		switch(id) {
			case PREP_TIME_DIALOG : {
				int time = r.getPrepTime();

				return new TimePickerDialog(RecipeDisplay.this, new OnTimeSetListener() {
						public void onTimeSet(TimePicker picker, int hour, int minute) {
							r.setPrepTime(hour * 60 + minute);
							TextView tv = (TextView) findViewById(R.id.prep_edit);
							tv.setText(timeFormat(r.getPrepTime()));

							tv = (TextView) findViewById(R.id.total_edit);
							tv.setText(timeFormat(r.getPrepTime() + r.getCookTime()));
						}
					}, time / 60, time % 60, true);

			}
			case COOK_TIME_DIALOG : {
				int time = r.getCookTime();

				return new TimePickerDialog(RecipeDisplay.this, new OnTimeSetListener() {
						public void onTimeSet(TimePicker picker, int hour, int minute) {
							r.setCookTime(hour * 60 + minute);
							TextView tv = (TextView) findViewById(R.id.cook_edit);
							tv.setText(timeFormat(r.getCookTime()));

							tv = (TextView) findViewById(R.id.total_edit);
							tv.setText(timeFormat(r.getPrepTime() + r.getCookTime()));
						}
					}, time / 60, time % 60, true);
			}
		}

		return null;
	}

	protected Dialog showTextDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String title = null;
		String initialText = null;
		boolean singleLine = false;
		DigitsKeyListener dklistener = null;

		LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = li.inflate(R.layout.enter_text_dialog, null);
		EditText input = (EditText) v.findViewById(R.id.text_edit);
		builder.setView(v);
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});

		switch(id) {
			case NAME_DIALOG : {
				title = "Edit Name";

				initialText = r.getName();

				input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
				input.setSingleLine(true);

				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						EditText input = (EditText) ((Dialog)dialog).findViewById(R.id.text_edit);
						String value = input.getText().toString();
						r.setName(value);
						TextView tv = (TextView) findViewById(R.id.name_edit);
						tv.setText(r.getName());
					}
				});


				break;
			}
			case DESCRIPTION_DIALOG : {
				title = "Edit Description";

				initialText = r.getDescription();

				input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
				input.setSingleLine(false);

				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						EditText input = (EditText) ((Dialog)dialog).findViewById(R.id.text_edit);
						String value = input.getText().toString();
						r.setDescription(value);
						TextView tv = (TextView) findViewById(R.id.description_edit);
						tv.setText(r.getDescription());
					}
				});

				break;
			}

			default :
				break;
		}
		builder.setCancelable(true);
		builder.setTitle(title);
		dialog = builder.create();

		input.setText(initialText);

		return dialog;
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
