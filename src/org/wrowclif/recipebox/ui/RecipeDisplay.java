package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.Utility;
import org.wrowclif.recipebox.R;
import org.wrowclif.recipebox.impl.UtilityImpl;
import org.wrowclif.recipebox.ui.ListAutoCompleteAdapter;
import org.wrowclif.recipebox.ui.ListAutoCompleteAdapter.Specifics;

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
import android.widget.Button;
import android.widget.EditText;
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

import java.util.ArrayList;

public class RecipeDisplay extends Activity {

	private Recipe r;
	private Utility util;
	private boolean edit;
	private static final int NAME_DIALOG = 0;
	private static final int DESCRIPTION_DIALOG = 1;
	private static final int PREP_TIME_DIALOG = 2;
	private static final int COOK_TIME_DIALOG = 3;
	private static final int COST_DIALOG = 4;
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

		r = util.getRecipeById(intent.getLongExtra("id", -1));
		edit = intent.getBooleanExtra("edit", false);

		if(r != null) {
			setTitle(r.getName());

			TextView tv = (TextView) findViewById(R.id.name_edit);
			tv.setText(r.getName());

			tv = (TextView) findViewById(R.id.description_edit);
			tv.setText(r.getDescription());

			tv = (TextView) findViewById(R.id.prep_edit);
			tv.setText(timeFormat(r.getPrepTime()));

			tv = (TextView) findViewById(R.id.cook_edit);
			tv.setText(timeFormat(r.getCookTime()));

			tv = (TextView) findViewById(R.id.cost_edit);
			tv.setText("$" + r.getCost());

			TextView[] labels = {(TextView) findViewById(R.id.name_label), (TextView) findViewById(R.id.description_label),
								(TextView) findViewById(R.id.prep_label), (TextView) findViewById(R.id.cook_label),
								(TextView) findViewById(R.id.cost_label)};

			Button[] btns = {(Button) findViewById(R.id.name_button), (Button) findViewById(R.id.description_button),
								(Button) findViewById(R.id.prep_button), (Button) findViewById(R.id.cook_button),
								(Button) findViewById(R.id.cost_button)};

			int[] dialogs = {NAME_DIALOG, DESCRIPTION_DIALOG, PREP_TIME_DIALOG, COOK_TIME_DIALOG, COST_DIALOG};

			for(int i = 0; i < btns.length; i++) {
				btns[i].setOnClickListener(new EditClickListener(dialogs[i]));
			}

			Button doneButton = (Button) findViewById(R.id.done_editing);

			doneButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					setEditing(false);
				}
			});


			if(edit) {
				for(TextView label : labels) {
					label.setVisibility(View.GONE);
				}
			} else {
				for(Button b : btns) {
					b.setVisibility(View.GONE);
				}
				doneButton.setVisibility(View.GONE);
			}
		}
    }

    protected void setEditing(boolean editing) {
		TextView[] labels = {(TextView) findViewById(R.id.name_label), (TextView) findViewById(R.id.description_label),
								(TextView) findViewById(R.id.prep_label), (TextView) findViewById(R.id.cook_label),
								(TextView) findViewById(R.id.cost_label)};

		Button[] btns = {(Button) findViewById(R.id.name_button), (Button) findViewById(R.id.description_button),
							(Button) findViewById(R.id.prep_button), (Button) findViewById(R.id.cook_button),
							(Button) findViewById(R.id.cost_button), (Button) findViewById(R.id.done_editing)};

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
		switch(id) {
			case CREATE_DIALOG : case EDIT_DIALOG : case DELETE_DIALOG :
				return showMenuDialog(id);
			case PREP_TIME_DIALOG : case COOK_TIME_DIALOG :
				return showTimeDialog(id);
			default :
				return showTextDialog(id);
		}
	}

	protected Dialog showMenuDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		switch(id) {
			case CREATE_DIALOG : {
				builder.setTitle("Create New Recipe");
				break;
			}

			case EDIT_DIALOG : {
				builder.setTitle("Edit Recipe");
				builder.setItems(new String[] {"Edit This Recipe", "Create Variant", "Cancel"},
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							switch(which) {
								case 0 :
									setEditing(!edit);
									break;
								case 1 :
									Recipe r2 = r.branch(r.getName() + " (branch)");
									Intent intent = getIntent();
									intent.putExtra("id", r2.getId());
									intent.putExtra("edit", true);

									RecipeDisplay.this.finish();
									startActivity(intent);
									break;
							}
						}
				});
				break;
			}

			case DELETE_DIALOG : {
				builder.setTitle("Delete Recipe");
				builder.setMessage("Are you sure you want to delete the recipe?");
				builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						r.delete();
						RecipeDisplay.this.finish();
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
				break;
			}
		}
		builder.setCancelable(true);

		dialog = builder.create();

		return dialog;
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

		final EditText input = new EditText(this);
		builder.setView(input);
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});

		switch(id) {
			case NAME_DIALOG : {
				title = "Edit Name";

				input.setSingleLine(true);
				initialText = r.getName();

				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
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

				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String value = input.getText().toString();
						r.setDescription(value);
						TextView tv = (TextView) findViewById(R.id.description_edit);
						tv.setText(r.getDescription());
					}
				});

				break;
			}

			case COST_DIALOG : {
				title = "Edit Cost";

				initialText = r.getCost() + "";
				input.setSingleLine(true);
				input.setKeyListener(new DigitsKeyListener(false, false));

				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						int value = Integer.parseInt(input.getText().toString());
						r.setCost(value);
						TextView tv = (TextView) findViewById(R.id.cost_edit);
						tv.setText("$" + r.getCost());
					}
				});

				break;
			}
			default :
				break;
		}
		builder.setCancelable(true);
		builder.setTitle(title);
		input.setText(initialText);
		dialog = builder.create();

		return dialog;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.recipe_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.edit :
				if(edit) {
					setEditing(false);
				} else {
					showDialog(EDIT_DIALOG);
				}
				return true;

			case R.id.delete :
				showDialog(DELETE_DIALOG);
				return true;

			case R.id.create :
				showDialog(CREATE_DIALOG);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

    private String timeFormat(int minutes) {
		int hours = minutes / 60;
		int min = minutes % 60;

		return String.format("%02d:%02d", hours, min);
	}

    public void onStop() {
		super.onStop();

		AppData.getSingleton().close();
	}

}
