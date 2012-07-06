package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.RecipeIngredient;
import org.wrowclif.recipebox.Utility;
import org.wrowclif.recipebox.Instruction;
import org.wrowclif.recipebox.R;
import org.wrowclif.recipebox.impl.UtilityImpl;

import org.wrowclif.recipebox.ui.components.EnterTextDialog;
import org.wrowclif.recipebox.ui.components.ListAutoCompleteAdapter;
import org.wrowclif.recipebox.ui.components.DynamicLoadAdapter;

import static org.wrowclif.recipebox.util.ConstantInitializer.assignId;
import org.wrowclif.recipebox.util.ShareUtil;

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
	private DynamicLoadAdapter<Recipe> recentAdapter;
	private static final int CREATE_RECIPE_DIALOG = assignId();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		AppData.initialSingleton(this);

		util = UtilityImpl.singleton;

		Intent intent = getIntent();

		if(intent.getData() != null) {
			Log.d("Recipebox", "Intent data not null!");
			if("text/rcpb".equals(intent.getType())) {
				ShareUtil.loadRecipe(this, intent.getData());
			} else if("application/zip".equals(intent.getType())) {
				ShareUtil.loadZipRecipe(this, intent.getData());
			}
		}

		AppData.getSingleton().useHeadingFont((TextView) findViewById(R.id.recipe_label));

		AutoCompleteTextView tv = (AutoCompleteTextView) findViewById(R.id.recipesearch);
		AppData.getSingleton().useTextFont(tv);

		ListAutoCompleteAdapter.Specifics<Recipe> sp = new ListAutoCompleteAdapter.Specifics<Recipe>() {

			public View getView(int id, Recipe r, View v, ViewGroup vg) {
				if(v == null) {
					v = inflate(R.layout.autoitem);
				}

				TextView tv = (TextView) v.findViewById(R.id.child_name);
				AppData.getSingleton().useTextFont(tv);

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

		adapter.setUpView(tv);

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

		TextView addRecipe = (TextView) findViewById(R.id.addrecipe);
		AppData.getSingleton().useHeadingFont(addRecipe);

		addRecipe.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Recipe r = util.newRecipe("New Recipe");
				Intent intent = new Intent(Main.this, RecipeTabs.class);
				intent.putExtra("id", r.getId());
				intent.putExtra("edit", true);
				startActivity(intent);
			}
		});

		TextView browseRecipe = (TextView) findViewById(R.id.browse);
		AppData.getSingleton().useHeadingFont(browseRecipe);

		browseRecipe.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(Main.this, Browse.class);
				startActivity(intent);
			}
		});

		TextView browseCategories = (TextView) findViewById(R.id.categories);
		AppData.getSingleton().useHeadingFont(browseCategories);

		browseCategories.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(Main.this, BrowseCategories.class);
				startActivity(intent);
			}
		});

		ListView lv = (ListView) findViewById(R.id.recent_recipes);

		DynamicLoadAdapter.Specifics<Recipe> sp2 = new DynamicLoadAdapter.Specifics<Recipe>() {

			public View getView(int id, Recipe r, View v, ViewGroup vg) {
				if(v == null) {
					v = inflate(R.layout.category_item);
				}

				v.findViewById(R.id.edit_group).setVisibility(View.GONE);

				TextView tv = (TextView) v.findViewById(R.id.name_box);
				AppData.getSingleton().useTextFont(tv);

				if(r == null) {
					tv.setText("Loading...");
				} else {
					tv.setText(r.getName());
				}

				return v;
			}

			public long getItemId(Recipe item) {
				return item.getId();
			}

			public List<Recipe> filter(int offset, int max) {
				List<Recipe> list = UtilityImpl.singleton.getRecentlyViewedRecipes(offset, max);
				if(list.size() == max) {
					list.add(null);
				}
				return list;
			}

			public String convertResultToString(Recipe result) {
				return (result == null) ? "Null" : result.getName();
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

		recentAdapter = new DynamicLoadAdapter<Recipe>(sp2);

		recentAdapter.setUpList(lv);
		lv.requestFocus();

    }

    public void onResume() {
		super.onResume();
		recentAdapter.clear();
	}

    protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;

		if(id == CREATE_RECIPE_DIALOG) {
			EnterTextDialog etd = new EnterTextDialog(this, R.layout.show_text_dialog);

			etd.setTitle("Create New Recipe");
			etd.setEditText("There are no recipes by that name.\n\n" +
								"Would you like to create a new recipe?");

			etd.setOkButtonText("Create");
			etd.setOkListener(new OnClickListener() {
				public void onClick(View v) {
					TextView tv = (TextView) findViewById(R.id.recipesearch);

					String name = tv.getText().toString();
					Recipe r = util.newRecipe(name);
					Intent intent = new Intent(Main.this, RecipeTabs.class);

					intent.putExtra("id", r.getId());
					intent.putExtra("edit", true);

					startActivity(intent);
				}
			});

			dialog = etd;
		}

		return dialog;
	}

}
