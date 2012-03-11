package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Category;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.RecipeIngredient;
import org.wrowclif.recipebox.Utility;
import org.wrowclif.recipebox.Instruction;
import org.wrowclif.recipebox.R;

import org.wrowclif.recipebox.impl.UtilityImpl;

import org.wrowclif.recipebox.ui.components.ListAutoCompleteAdapter;
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

public class CategoryList extends Activity {

	private Utility util;
	private Category category;
	private DynamicLoadAdapter<Recipe> adapter;
	private boolean edit;

	private static final int ADD_RECIPE_DIALOG = 1;
	private static final int DELETE_RECIPE_DIALOG = 2;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.categories);

		util = UtilityImpl.singleton;

		onNewIntent(getIntent());
	}

	public void onNewIntent(Intent intent) {
		category = util.getCategoryById(intent.getLongExtra("id", -1));

		ListView lv = (ListView) findViewById(R.id.category_list);

		createDynamicLoadAdapter();

		adapter.setUpList(lv);

		TextView label = (TextView) findViewById(R.id.category_label);
		label.setText(category.getName());

		TextView addButton = (TextView) findViewById(R.id.add_button);

		addButton.setText("Add Recipe");

		addButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(ADD_RECIPE_DIALOG);
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
			case ADD_RECIPE_DIALOG : {
				AlertDialog dialog = (AlertDialog) d;
				TextView tv = (TextView) dialog.findViewById(R.id.text_edit);
				tv.setText("");
				break;
			}

			case DELETE_RECIPE_DIALOG : {
				AlertDialog dialog = (AlertDialog) d;
				final int position = bundle.getInt("position", -1);
				final Recipe recipe = adapter.getItem(position);

				dialog.setMessage("Are you sure you want to remove " + recipe.getName() + " from this category?");
				dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Remove", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						category.removeRecipe(recipe);
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

		switch(id) {
			case ADD_RECIPE_DIALOG : {

				LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View v = li.inflate(R.layout.enter_recipe_dialog, null);
				final AutoCompleteTextView input = (AutoCompleteTextView) v.findViewById(R.id.text_edit);
				final long[] idHolder = new long[1];
				builder.setView(v);

				input.setHint("Recipe Name");
				input.setText("");
				input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
				input.setSingleLine(true);

				ListAutoCompleteAdapter.Specifics<Recipe> sp = new ListAutoCompleteAdapter.Specifics<Recipe>() {

					public View getView(int id, Recipe r, View v, ViewGroup vg) {
						if(v == null) {
							v = inflate(R.layout.autoitem);
						}

						TextView tv = (TextView) v.findViewById(R.id.child_name);

						tv.setText(r.getName());

						return v;
					}

					public long getItemId(Recipe item) {
						return item.getId();
					}

					public List<Recipe> filter(CharSequence seq) {
						return util.searchRecipes(seq.toString(), 5);
					}

					public String convertResultToString(Recipe result) {
						return result.getName();
					}

					public void onItemClick(AdapterView av, View v, int position, long id, Recipe item) {
						idHolder[0] = id;
					}

					private View inflate(int layoutId) {
						LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						return vi.inflate(layoutId, null);
					}
				};

				final ListAutoCompleteAdapter<Recipe> acAdapter = new ListAutoCompleteAdapter<Recipe>(sp);


				input.setAdapter(acAdapter);

				input.setOnItemClickListener(acAdapter.onClick);

				builder.setTitle("Add Recipe To Category");
				builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if(idHolder[0] >= 0) {
							Recipe r = util.getRecipeById(idHolder[0]);

							if(r != null) {
								category.addRecipe(r);

								idHolder[0] = -1;

								adapter.clear();
							}
						}
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
				break;
			}

			case DELETE_RECIPE_DIALOG : {
				final int position = bundle.getInt("position", -1);
				final Recipe recipe = adapter.getItem(position);

				builder.setTitle("Remove Recipe");
				builder.setMessage("Are you sure you want to remove " + recipe.getName() + " from this category?");
				builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						category.removeRecipe(recipe);
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

		DynamicLoadAdapter.Specifics<Recipe> sp = new DynamicLoadAdapter.Specifics<Recipe>() {
			public View getView(final int position, Recipe r, View v, ViewGroup vg) {
				if(v == null) {
					v = inflate(R.layout.category_item);
				}

				TextView tv = (TextView) v.findViewById(R.id.name_box);

				if(r == null) {
					tv.setText("Loading...");
				} else {
					tv.setText(r.getName());
				}

				View deleteButton = v.findViewById(R.id.delete_button);

				if(edit) {
					deleteButton.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							Bundle b = new Bundle();

							b.putInt("position", position);

							showDialog(DELETE_RECIPE_DIALOG, b);
						}
					});

					deleteButton.setVisibility(View.VISIBLE);
				} else {
					deleteButton.setVisibility(View.GONE);
				}

				return v;
			}

			public long getItemId(Recipe item) {
				return item.getId();
			}

			public List<Recipe> filter(int offset, int max) {
				List<Recipe> nextRecipes = category.getRecipes();
				return nextRecipes;
			}

			public String convertResultToString(Recipe result) {
				if(result == null) {
					return "Null";
				} else {
					return result.getName();
				}
			}

			public void onItemClick(AdapterView av, View v, int position, long id, Recipe item) {
				Intent intent = new Intent(CategoryList.this, RecipeTabs.class);
				intent.putExtra("id", id);
				startActivity(intent);
			}

			private View inflate(int layoutId) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				return vi.inflate(layoutId, null);
			}
		};

		adapter = new DynamicLoadAdapter<Recipe>(sp);
	}
}
