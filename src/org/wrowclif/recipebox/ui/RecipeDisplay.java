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
	private ViewGroup categories;

	private static final int NAME_DIALOG = 0;
	private static final int DESCRIPTION_DIALOG = 1;
	private static final int PREP_TIME_DIALOG = 2;
	private static final int COOK_TIME_DIALOG = 3;
	private static final int EDIT_DIALOG = 5;
	private static final int DELETE_DIALOG = 6;
	private static final int CREATE_DIALOG = 7;
	private static final int DELETE_CATEGORY_DIALOG = 8;

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

			String name = r.getName();
			setText(R.id.name_edit, name);
			setText(R.id.name_button, name.equals("") ? "Edit Name" : name);

			String description = r.getDescription();
			setText(R.id.description_edit, description);
			setText(R.id.description_button, description.equals("") ? "Edit Description" : description);

			String prepTime = timeFormat(r.getPrepTime());
			setText(R.id.prep_edit, prepTime);
			setText(R.id.prep_button, prepTime);

			String cookTime = timeFormat(r.getCookTime());
			setText(R.id.cook_edit, cookTime);
			setText(R.id.cook_button, cookTime);

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
			this.categories = (ViewGroup) findViewById(R.id.category_box);

			LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			for(final Category c : r.getCategories()) {
				View v = li.inflate(R.layout.category_item, null);
				TextView ctv = (TextView) v.findViewById(R.id.name_box);
				ctv.setText(c.getName());

				TextView idBox = (TextView) v.findViewById(R.id.edit_button);
				idBox.setText(c.getId() + "");

				v.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						Intent i = new Intent(RecipeDisplay.this, CategoryList.class);
						i.putExtra("id", c.getId());
						i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						startActivity(i);
					}
				});

				View deleteButton = v.findViewById(R.id.delete_button);
				deleteButton.setVisibility(View.VISIBLE);

				deleteButton.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						Bundle bundle = new Bundle();

						bundle.putLong("id", c.getId());
						showDialog(DELETE_CATEGORY_DIALOG, bundle);
					}
				});

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
		TextView[] labels = {(TextView) findViewById(R.id.name_edit), (TextView) findViewById(R.id.description_edit),
								(TextView) findViewById(R.id.prep_edit), (TextView) findViewById(R.id.cook_edit),
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
			case DELETE_CATEGORY_DIALOG :
				return showConfirmDialog(id);
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

							String prepTime = timeFormat(r.getPrepTime());
							setText(R.id.prep_edit, prepTime);
							setText(R.id.prep_button, prepTime);

							setText(R.id.total_edit, timeFormat(r.getPrepTime() + r.getCookTime()));
						}
					}, time / 60, time % 60, true);

			}
			case COOK_TIME_DIALOG : {
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

						String name = r.getName();
						setText(R.id.name_edit, name);
						setText(R.id.name_button, name.equals("") ? "Edit Name" : name);

						((RecipeTabs) getParent()).setTitle(name.equals("") ? "Recipe Box" : name);
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

						String description = r.getDescription();
						setText(R.id.description_edit, description);
						setText(R.id.description_button, description.equals("") ? "Edit Description" : description);
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

	protected Dialog showConfirmDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Remove Category");
		builder.setMessage("");
		builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});
		builder.setCancelable(true);
		return builder.create();
	}

	protected void onPrepareDialog(int id, Dialog d, Bundle bundle) {

		switch(id) {
			case DELETE_CATEGORY_DIALOG : {
				AlertDialog dialog = (AlertDialog) d;
				final long categoryId = bundle.getLong("id", -1);
				final Category category = util.getCategoryById(categoryId);
				dialog.setMessage("Are you sure you want to remove "  + r.getName() +
										" from the " + category.getName() + " category?");

				dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Remove", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						r.removeCategory(category);

						for(int i = 1; i < categories.getChildCount(); i++) {
							View child = categories.getChildAt(i);
							TextView idBox = (TextView) child.findViewById(R.id.edit_button);
							long childId = Long.parseLong(idBox.getText().toString());
							if(childId == categoryId) {
								categories.removeView(child);
								categories.invalidate();
								break;
							}
						}
					}
				});
				break;
			}
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
