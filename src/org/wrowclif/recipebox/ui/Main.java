package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.RecipeIngredient;
import org.wrowclif.recipebox.Utility;
import org.wrowclif.recipebox.Instruction;
import org.wrowclif.recipebox.R;
import org.wrowclif.recipebox.impl.UtilityImpl;
import org.wrowclif.recipebox.ui.ListAutoCompleteAdapter;
import org.wrowclif.recipebox.ui.ListAutoCompleteAdapter.Specifics;

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
import android.text.TextWatcher;
import android.text.Editable;

import java.util.ArrayList;

public class Main extends Activity {

	private Utility util;
	private ListAutoCompleteAdapter<Recipe> recentAdapter;
	private static final int CREATE_RECIPE_DIALOG = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		AppData.initialSingleton(this);

		util = UtilityImpl.singleton;

		AutoCompleteTextView tv = (AutoCompleteTextView) findViewById(R.id.recipesearch);

		Specifics<Recipe> sp = new Specifics<Recipe>() {

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
				return UtilityImpl.singleton.searchRecipes(seq.toString(), 5);
			}

			public String convertResultToString(Recipe result) {
				return result.getName();
			}

			public void onItemClick(AdapterView av, View v, int position, long id, Recipe item) {
				Intent intent = new Intent(Main.this, RecipeTabs.class);
				intent.putExtra("id", id);
				startActivity(intent);
			}

			private View inflate(int layoutId) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				return vi.inflate(layoutId, null);
			}
		};

		ListAutoCompleteAdapter<Recipe> adapter = new ListAutoCompleteAdapter<Recipe>(sp);


		tv.setAdapter(adapter);

		tv.setOnItemClickListener(adapter.onClick);

		tv.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int action, KeyEvent event) {
				if((action == EditorInfo.IME_NULL) && (event.getAction() == 0)) {
					String text = v.getText().toString();
					List<Recipe> recipes = util.searchRecipes(text, 1);
					if(recipes.size() > 0) {
						Intent intent = new Intent(Main.this, RecipeTabs.class);
						intent.putExtra("id", recipes.get(0).getId());
						startActivity(intent);
					} else {
						showDialog(CREATE_RECIPE_DIALOG);
					}
					return true;
				}
				return false;
			}
		});

		Button addRecipe = (Button) findViewById(R.id.addrecipe);

		addRecipe.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Recipe r = util.newRecipe("New Recipe");
				Intent intent = new Intent(Main.this, RecipeTabs.class);
				intent.putExtra("id", r.getId());
				intent.putExtra("edit", true);
				startActivity(intent);
			}
		});

		ListView lv = (ListView) findViewById(R.id.recent_recipes);

		Specifics<Recipe> sp2 = new Specifics<Recipe>() {

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
				return UtilityImpl.singleton.getRecentlyViewedRecipes(5);
			}

			public String convertResultToString(Recipe result) {
				return result.getName();
			}

			public void onItemClick(AdapterView av, View v, int position, long id, Recipe item) {
				Intent intent = new Intent(Main.this, RecipeTabs.class);
				intent.putExtra("id", id);
				startActivity(intent);
			}

			private View inflate(int layoutId) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				return vi.inflate(layoutId, null);
			}
		};

		recentAdapter = new ListAutoCompleteAdapter<Recipe>(sp2);

		lv.setAdapter(recentAdapter);
		lv.setOnItemClickListener(recentAdapter.onClick);

    }

    public void onResume() {
		super.onResume();
		recentAdapter.getFilter().filter("");
	}

    protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		switch(id) {
			case CREATE_RECIPE_DIALOG : {
				builder.setTitle("Create New Recipe");
				builder.setMessage("There are no recipes by that name. Would you like to create a new recipe?");
				builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						TextView tv = (TextView) findViewById(R.id.recipesearch);

						String name = tv.getText().toString();
						Recipe r = util.newRecipe(name);
						Intent intent = new Intent(Main.this, RecipeTabs.class);

						intent.putExtra("id", r.getId());
						intent.putExtra("edit", true);

						startActivity(intent);
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

    public void onStop() {
		super.onStop();

		AppData.getSingleton().close();
	}

}
