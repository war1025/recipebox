package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Category;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.RecipeIngredient;
import org.wrowclif.recipebox.Utility;
import org.wrowclif.recipebox.Instruction;
import org.wrowclif.recipebox.R;
import org.wrowclif.recipebox.impl.UtilityImpl;

import org.wrowclif.recipebox.ui.components.DynamicLoadAdapter;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.text.TextWatcher;
import android.text.Editable;
import android.text.InputType;

import java.util.ArrayList;

public class BrowseCategories extends Activity {

	private Utility util;
	private DynamicLoadAdapter<Category> adapter;
	private boolean edit;

	private static final int CREATE_CATEGORY_DIALOG = 1;
	private static final int EDIT_CATEGORY_NAME_DIALOG = 2;
	private static final int DELETE_CATEGORY_DIALOG = 3;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.categories);

		util = UtilityImpl.singleton;

		ListView lv = (ListView) findViewById(R.id.category_list);

		createDynamicLoadAdapter();

		adapter.setUpList(lv);

		View addButton = findViewById(R.id.add_button);

		addButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(CREATE_CATEGORY_DIALOG);
			}
		});

		View doneButton = findViewById(R.id.done_button);

		doneButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setEditing(false);
			}
		});

		setEditing(false);
    }

    public void onResume() {
		super.onResume();
	}

	protected void setEditing(boolean editing) {
		View[] views = {findViewById(R.id.add_button), findViewById(R.id.done_button)};

		if(editing) {
			for(View v : views) {
				v.setVisibility(View.VISIBLE);
			}
		} else {
			for(View v : views) {
				v.setVisibility(View.GONE);
			}
		}

		this.edit = editing;
		adapter.notifyDataSetChanged();
	}

	protected void onPrepareDialog(int id, Dialog d, Bundle bundle) {

		switch(id) {
			case CREATE_CATEGORY_DIALOG : {
				AlertDialog dialog = (AlertDialog) d;
				final EditText input = (EditText) dialog.findViewById(R.id.text_edit);
				input.setText("");
				break;
			}

			case EDIT_CATEGORY_NAME_DIALOG : {
				AlertDialog dialog = (AlertDialog) d;
				final int position = bundle.getInt("position", -1);
				final Category category = adapter.getItem(position);
				final EditText input = (EditText) dialog.findViewById(R.id.text_edit);

				input.setText(category.getName());

				dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Done", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						category.setName(input.getText().toString());
						adapter.notifyDataSetChanged();
					}
				});

				break;
			}

			case DELETE_CATEGORY_DIALOG : {
				AlertDialog dialog = (AlertDialog) d;
				final int position = bundle.getInt("position", -1);
				final Category category = adapter.getItem(position);

				dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Delete", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						category.delete();
						adapter.remove(position);
					}
				});
				break;
			}
		}
	}

	protected Dialog onCreateDialog(int id, Bundle bundle) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		switch(id) {
			case CREATE_CATEGORY_DIALOG : {
				View v = li.inflate(R.layout.enter_text_dialog, null);
				final EditText input = (EditText) v.findViewById(R.id.text_edit);
				builder.setView(v);

				input.setHint("Category Name");
				input.setText("");
				input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
				input.setSingleLine(true);

				builder.setTitle("Create Category");
				builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						util.createOrRetrieveCategory(input.getText().toString());
						adapter.clear();
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
				break;
			}

			case EDIT_CATEGORY_NAME_DIALOG : {
				final int position = bundle.getInt("position", -1);
				final Category category = adapter.getItem(position);

				View v = li.inflate(R.layout.enter_text_dialog, null);
				final EditText input = (EditText) v.findViewById(R.id.text_edit);
				builder.setView(v);

				input.setText(category.getName());
				input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
				input.setSingleLine(true);

				builder.setTitle("Edit Category Name");
				builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						category.setName(input.getText().toString());
						adapter.notifyDataSetChanged();
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
				break;
			}

			case DELETE_CATEGORY_DIALOG : {
				final int position = bundle.getInt("position", -1);
				final Category category = adapter.getItem(position);

				builder.setTitle("Delete Category");
				builder.setMessage("Are you sure you want to delete this category?");
				builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						category.delete();
						adapter.remove(position);
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

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mi = getMenuInflater();

		mi.inflate(R.menu.category_menu, menu);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch(id) {
			case R.id.edit : {
				setEditing(!edit);
				return true;
			}
		}

		return super.onOptionsItemSelected(item);
	}


	private void createDynamicLoadAdapter() {

		DynamicLoadAdapter.Specifics<Category> sp = new DynamicLoadAdapter.Specifics<Category>() {

			public View getView(final int position, Category c, View v, ViewGroup vg) {
				if(v == null) {
					v = inflate(R.layout.category_item);
				}

				TextView tv = (TextView) v.findViewById(R.id.name_box);

				if(c == null) {
					tv.setText("Loading...");
				} else {
					tv.setText(c.getName());
				}

				View editButton = v.findViewById(R.id.edit_button);
				View deleteButton = v.findViewById(R.id.delete_button);

				if(edit) {
					editButton.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							Bundle b = new Bundle();

							b.putInt("position", position);

							showDialog(EDIT_CATEGORY_NAME_DIALOG, b);
						}
					});

					deleteButton.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							Bundle b = new Bundle();

							b.putInt("position", position);

							showDialog(DELETE_CATEGORY_DIALOG, b);
						}
					});

					editButton.setVisibility(View.VISIBLE);
					deleteButton.setVisibility(View.VISIBLE);
				} else {
					editButton.setVisibility(View.GONE);
					deleteButton.setVisibility(View.GONE);
				}

				return v;
			}

			public long getItemId(Category item) {
				return item.getId();
			}

			public List<Category> filter(int offset, int max) {
				List<Category> nextRecipes = UtilityImpl.singleton.getCategoriesByName(offset, max);
				if(nextRecipes.size() == max) {
					nextRecipes.add(null);
				}
				return nextRecipes;
			}

			public String convertResultToString(Category result) {
				if(result == null) {
					return "Null";
				} else {
					return result.getName();
				}
			}

			public void onItemClick(AdapterView av, View v, int position, long id, Category item) {
				Intent intent = new Intent(BrowseCategories.this, CategoryList.class);
				intent.putExtra("id", id);
				startActivity(intent);
			}

			private View inflate(int layoutId) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				return vi.inflate(layoutId, null);
			}
		};

		adapter = new DynamicLoadAdapter<Category>(sp);
	}

}
