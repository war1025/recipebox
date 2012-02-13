package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.AppData;
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
import android.text.TextWatcher;
import android.text.Editable;

import java.util.ArrayList;

public class Browse extends Activity {

	private Utility util;
	private DynamicLoadAdapter<Recipe> recentAdapter;
	private static final int CREATE_RECIPE_DIALOG = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse);

		util = UtilityImpl.singleton;

		ListView lv = (ListView) findViewById(R.id.browse_recipes);

		createDynamicLoadAdapter();

		recentAdapter.setUpList(lv);
    }

    public void onResume() {
		super.onResume();
		recentAdapter.clear();
	}

	private void createDynamicLoadAdapter() {

		DynamicLoadAdapter.Specifics<Recipe> sp = new DynamicLoadAdapter.Specifics<Recipe>() {

			public View getView(int id, Recipe r, View v, ViewGroup vg) {
				if(v == null) {
					v = inflate(R.layout.autoitem);
				}

				TextView tv = (TextView) v.findViewById(R.id.child_name);

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
				List<Recipe> nextRecipes = UtilityImpl.singleton.getRecipesByName(offset, max);
				if(nextRecipes.size() == max) {
					nextRecipes.add(null);
				}
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
				Intent intent = new Intent(Browse.this, RecipeTabs.class);
				intent.putExtra("id", id);
				startActivity(intent);
			}

			private View inflate(int layoutId) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				return vi.inflate(layoutId, null);
			}
		};

		recentAdapter = new DynamicLoadAdapter<Recipe>(sp);
	}

}
