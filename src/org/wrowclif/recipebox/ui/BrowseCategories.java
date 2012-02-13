package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Category;
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
import android.text.TextWatcher;
import android.text.Editable;
import android.text.InputType;

import java.util.ArrayList;

public class BrowseCategories extends Activity {

	private Utility util;
	private ListAutoCompleteAdapter<Category> recentAdapter;
	private DynamicBrowse dynamic;
	private static final int CREATE_CATEGORY_DIALOG = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.categories);

		util = UtilityImpl.singleton;

		ListView lv = (ListView) findViewById(R.id.category_list);

		dynamic = new DynamicBrowse();
		recentAdapter = new ListAutoCompleteAdapter<Category>(dynamic);

		dynamic.setFilter(recentAdapter.getFilter());

		lv.setAdapter(recentAdapter);
		lv.setOnItemClickListener(recentAdapter.onClick);

		View addButton = findViewById(R.id.category_add);

		addButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(CREATE_CATEGORY_DIALOG);
			}
		});

    }

    public void onResume() {
		super.onResume();
		dynamic.clear();
		recentAdapter.clear();
		recentAdapter.getFilter().filter("");
	}

	protected void onPrepareDialog(int id, Dialog d, Bundle bundle) {

		switch(id) {
			case CREATE_CATEGORY_DIALOG : {
				AlertDialog dialog = (AlertDialog) d;
				final EditText input = (EditText) dialog.findViewById(R.id.text_edit);
				input.setText("");
				break;
			}
		}
	}

	protected Dialog onCreateDialog(int id, Bundle bundle) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		switch(id) {
			case CREATE_CATEGORY_DIALOG : {

				LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
						dynamic.clear();
						recentAdapter.clear();
						recentAdapter.getFilter().filter("");
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

	protected class DynamicBrowse implements Specifics<Category> {

		private int size;
		private Filter filter;

		public DynamicBrowse() {
			this.size = 0;
			this.filter = null;
		}

		public void setFilter(Filter f) {
			this.filter = f;
		}

		public void clear() {
			this.size = 0;
		}

		public View getView(int id, Category c, View v, ViewGroup vg) {
			if(v == null) {
				v = inflate(R.layout.autoitem);
			}

			TextView tv = (TextView) v.findViewById(R.id.child_name);

			if(c == null) {
				filter.filter("");
				tv.setText("Loading...");
			} else {
				tv.setText(c.getName());
			}

			return v;
		}

		public long getItemId(Category item) {
			return item.getId();
		}

		public List<Category> filter(CharSequence seq) {
			List<Category> nextRecipes = UtilityImpl.singleton.getCategoriesByName(size, 5);
			if(nextRecipes.size() == 5) {
				nextRecipes.add(null);
			}
			return nextRecipes;
		}

		public List<Category> publishFilter(CharSequence seq, List<Category> oldData, List<Category> newData) {
			if(oldData != null) {
				if(oldData.size() > 0) {
					oldData.remove(oldData.size() - 1);
				}
				oldData.addAll(newData);
			} else {
				oldData = newData;
			}
			size += (newData.size() < 5) ? newData.size() : 5;
			return oldData;
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
			Log.d("Recipebox", "Category: " + item + " " + id + " " + item.getId());
			intent.putExtra("id", id);
			startActivity(intent);
		}

		private View inflate(int layoutId) {
			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return vi.inflate(layoutId, null);
		}
	}

}
