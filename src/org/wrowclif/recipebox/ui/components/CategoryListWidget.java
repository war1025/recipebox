package org.wrowclif.recipebox.ui.components;

import org.wrowclif.recipebox.R;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.Category;
import org.wrowclif.recipebox.Utility;

import org.wrowclif.recipebox.impl.UtilityImpl;

import org.wrowclif.recipebox.ui.CategoryList;

import static org.wrowclif.recipebox.util.ConstantInitializer.assignId;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class CategoryListWidget {

	private static final int DELETE_CATEGORY_DIALOG = assignId();
	private static final int ADD_CATEGORY_DIALOG = assignId();

	private Recipe recipe;
	private Utility util;
	private Activity context;
	private View listWidget;
	private Button addButton;
	private ViewGroup categories;
	private CategoryDialog categoryDialog;
	private boolean editing;

	public CategoryListWidget(Recipe recipe, Activity context) {
		this.recipe = recipe;
		this.context = context;
		this.listWidget = getLayoutInflater().inflate(R.layout.list_widget, null);
		this.categories = (ViewGroup) listWidget.findViewById(R.id.category_list);
		this.editing = false;
		this.util = UtilityImpl.singleton;

		AppData.getSingleton().useHeadingFont((TextView) listWidget.findViewById(R.id.category_label));

		this.addButton = (Button) listWidget.findViewById(R.id.category_button);

		addButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CategoryListWidget.this.context.showDialog(ADD_CATEGORY_DIALOG);
			}
		});
	}

	public View getView() {
		refresh();
		return listWidget;
	}

	public void refresh() {
		categories.removeAllViews();
		for(Category c : recipe.getCategories()) {
			categories.addView(createCategoryEntry(c));
		}
		categories.invalidate();
	}

	protected View createCategoryEntry(final Category c) {
		View v = getLayoutInflater().inflate(R.layout.list_widget_item, null);
		TextView ctv = (TextView) v.findViewById(R.id.name_box);
		AppData.getSingleton().useTextFont(ctv);
		ctv.setText(c.getName());

		TextView idBox = (TextView) v.findViewById(R.id.edit_button);
		idBox.setText(c.getId() + "");
		idBox.setVisibility(View.GONE);

		v.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(context, CategoryList.class);
				i.putExtra("id", c.getId());
				i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				context.startActivity(i);
			}
		});

		View deleteButton = v.findViewById(R.id.delete_button);
		deleteButton.setVisibility(View.VISIBLE);

		deleteButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Bundle bundle = new Bundle();

				bundle.putLong("id", c.getId());
				context.showDialog(DELETE_CATEGORY_DIALOG, bundle);
			}
		});

		return v;
	}

	public void setEditing(boolean editing) {
		this.editing = editing;
		if(editing) {
			listWidget.setVisibility(View.VISIBLE);
		} else if(categories.getChildCount() == 0) {
			listWidget.setVisibility(View.GONE);
		}
		for(int i = 0; i < categories.getChildCount(); i++) {
			View child = categories.getChildAt(i);
			View editGroup = child.findViewById(R.id.edit_group);
			editGroup.setVisibility((editing) ? View.VISIBLE : View.GONE);
		}
		addButton.setVisibility((editing) ? View.VISIBLE : View.GONE);
	}

	public Dialog createDialog(int id) {
		Dialog retDialog = null;
		if((id == ADD_CATEGORY_DIALOG) || (id == DELETE_CATEGORY_DIALOG)) {
			if(categoryDialog == null) {
				categoryDialog = new CategoryDialog(context, recipe, this);
			}
			retDialog = categoryDialog;
		}
		return retDialog;
	}

	public boolean prepareDialog(int id, Dialog dialog, Bundle bundle) {
		boolean handled = false;
		if(id == ADD_CATEGORY_DIALOG) {
			((CategoryDialog) dialog).prepareNew();
			handled = true;
		} else if(id == DELETE_CATEGORY_DIALOG) {
			long category = bundle.getLong("id", -1);
			((CategoryDialog) dialog).prepareDelete(util.getCategoryById(category));
			handled = true;
		}
		return handled;
	}

	protected LayoutInflater getLayoutInflater() {
		return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
}
