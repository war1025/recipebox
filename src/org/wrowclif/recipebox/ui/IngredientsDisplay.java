package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Ingredient;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.RecipeIngredient;
import org.wrowclif.recipebox.R;

import org.wrowclif.recipebox.ui.components.IngredientDialog;
import org.wrowclif.recipebox.ui.components.RecipeMenus;
import org.wrowclif.recipebox.ui.components.RecipeMenus.EditSwitcher;
import org.wrowclif.recipebox.ui.components.DynamicLoadAdapter;
import org.wrowclif.recipebox.ui.components.ReorderableItemDecorator;
import org.wrowclif.recipebox.ui.components.ReorderableItemDecorator.ItemSwap;

import org.wrowclif.recipebox.impl.UtilityImpl;

import java.util.List;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ListView;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.text.TextWatcher;
import android.text.Editable;


public class IngredientsDisplay extends Activity {

	private boolean edit;
	private Recipe r;
	private DynamicLoadAdapter<RecipeIngredient> adapter;
	private ReorderableItemDecorator reorderDecorator;
	private RecipeMenus menus;

	private static final int NEW_INGREDIENT_DIALOG = 0;
	private static final int DELETE_INGREDIENT_DIALOG = 1;
	private static final int EDIT_DIALOG = 2;
	private static final int CREATE_DIALOG = 3;
	private static final int DELETE_DIALOG = 4;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ingredients_display);

		Intent intent = getIntent();

		r = ((RecipeTabs) getParent()).curRecipe;
		edit = ((RecipeTabs) getParent()).editing;

		setTitle(r.getName());

		createDynamicLoadAdapter();

		ItemSwap swapper = new ItemSwap() {
			public void swapItems(int a, int b) {
				r.swapIngredientPositions(adapter.getItem(a), adapter.getItem(b));
			}
		};

		reorderDecorator = new ReorderableItemDecorator(adapter, swapper);

		ListView lv = (ListView) findViewById(R.id.ingredient_list);

		lv.setAdapter(adapter);

		Button add = (Button) findViewById(R.id.add_button);

		add.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Bundle bundle = new Bundle();

				showDialog(NEW_INGREDIENT_DIALOG, bundle);
			}
		});

		Button done = (Button) findViewById(R.id.done_button);

		done.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setEditing(false);
			}
		});

		setEditing(edit);
    }

	public void onResume() {
		super.onResume();
		boolean editing = ((RecipeTabs) getParent()).editing;
		if(edit != editing) {
			setEditing(editing);
		}
	}

    protected void setEditing(boolean editing) {
		Button add = (Button) findViewById(R.id.add_button);
		Button done = (Button) findViewById(R.id.done_button);
		if(editing) {
			add.setVisibility(View.VISIBLE);
			done.setVisibility(View.VISIBLE);
		} else {
			add.setVisibility(View.GONE);
			done.setVisibility(View.GONE);
		}
		edit = editing;
		reorderDecorator.setEditing(editing);
		adapter.notifyDataSetChanged();

		((RecipeTabs) getParent()).editing = editing;
	}

	protected void onPrepareDialog(int id, Dialog d, final Bundle bundle) {

		switch(id) {
			case NEW_INGREDIENT_DIALOG : {
				IngredientDialog iDialog = (IngredientDialog) d;

				int position = bundle.getInt("position", -1);
				if(position < 0) {
					iDialog.prepareNew(r, adapter);
				} else {
					iDialog.prepareExisiting(r, adapter, adapter.getItem(position), position);
				}
				break;
			}

			case DELETE_INGREDIENT_DIALOG : {
				AlertDialog dialog = (AlertDialog) d;
				final int position = bundle.getInt("position", -1);
				final RecipeIngredient ri = adapter.getItem(position);
				dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Delete", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						r.removeIngredient(ri);

						adapter.remove(position);
					}
				});
				break;
			}
		}
	}

	protected Dialog onCreateDialog(int id, Bundle bundle) {
		if(menus != null) {
			Dialog d = menus.createDialog(id);
			if(d != null) {
				return d;
			}
		}
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		switch(id) {
			case NEW_INGREDIENT_DIALOG : {
				return new IngredientDialog(this);
			}
			case DELETE_INGREDIENT_DIALOG : {
				final int position = bundle.getInt("position", -1);
				final RecipeIngredient ri = adapter.getItem(position);
				builder.setTitle("Delete Ingredient");
				builder.setMessage("Are you sure you want to delete this ingredient?");
				builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						r.removeIngredient(ri);

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
		EditSwitcher es = new EditSwitcher() {
			public void setEditing(boolean editing) {
				IngredientsDisplay.this.setEditing(editing);
			}

			public boolean getEditing() {
				return IngredientsDisplay.this.edit;
			}
		};
		menus = new RecipeMenus(r, this, 1, es);

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

	private void createDynamicLoadAdapter() {

		DynamicLoadAdapter.Specifics<RecipeIngredient> sp = new DynamicLoadAdapter.Specifics<RecipeIngredient>() {
			public View getView(final int position, RecipeIngredient i, View convert, ViewGroup group) {
				if(convert == null) {
					LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convert = vi.inflate(R.layout.ingredient_display_item, null);
				}

				TextView tv = (TextView) convert.findViewById(R.id.ingredient_box);

				Button be = (Button) convert.findViewById(R.id.edit_button);
				Button bd = (Button) convert.findViewById(R.id.delete_button);

				if(i == null) {
					tv.setText("Loading...");
					be.setVisibility(View.GONE);
					bd.setVisibility(View.GONE);
					return convert;
				} else {
					tv.setText(i.getAmount() + " " + i.getName());
				}

				if(edit) {
					be.setVisibility(View.VISIBLE);
					bd.setVisibility(View.VISIBLE);

					be.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							Bundle bundle = new Bundle();
							bundle.putInt("position", position);

							showDialog(NEW_INGREDIENT_DIALOG, bundle);
						}
					});
					bd.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							Bundle bundle = new Bundle();
							bundle.putInt("position", position);

							showDialog(DELETE_INGREDIENT_DIALOG, bundle);
						}
					});
				} else {
					be.setVisibility(View.GONE);
					bd.setVisibility(View.GONE);
				}

				reorderDecorator.decorateItem(convert, position);
				return convert;
			}

			public long getItemId(RecipeIngredient item) {
				return item.getIngredient().getId();
			}

			public List<RecipeIngredient> filter(int offset, int max) {
				List<RecipeIngredient> ingredients = r.getIngredients();
				return ingredients;
			}

			public String convertResultToString(RecipeIngredient result) {
				return "Ingredient";
			}

			public void onItemClick(AdapterView av, View v, int position, long id, RecipeIngredient item) {

			}

			private View inflate(int layoutId) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				return vi.inflate(layoutId, null);
			}
		};

		adapter = new DynamicLoadAdapter<RecipeIngredient>(sp);
	}
}
