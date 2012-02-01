package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.RecipeIngredient;
import org.wrowclif.recipebox.R;
import org.wrowclif.recipebox.impl.UtilityImpl;
import org.wrowclif.recipebox.ui.ListAutoCompleteAdapter;
import org.wrowclif.recipebox.ui.ListAutoCompleteAdapter.Specifics;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.TextWatcher;
import android.text.Editable;

import java.util.ArrayList;

public class IngredientsDisplay extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ingredients_display);

		Intent intent = getIntent();

		Recipe r = UtilityImpl.singleton.getRecipeById(intent.getLongExtra("id", -1));

		if(r != null) {
			setTitle(r.getName());

			ListView lv = (ListView) findViewById(R.id.ingredient_list);
			lv.setAdapter(new ArrayAdapter<RecipeIngredient>(this, R.layout.ingredient_display_item, r.getIngredients()) {
				public View getView(int position, View convert, ViewGroup group) {
					if(convert == null) {
						LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						convert = vi.inflate(R.layout.ingredient_display_item, null);
					}

					RecipeIngredient i = getItem(position);

					TextView tv = (TextView) convert.findViewById(R.id.ingredient_name);
					tv.setText(i.getName());

					tv = (TextView) convert.findViewById(R.id.ingredient_amount);
					tv.setText(i.getUnits().getStringForAmount(i.getAmount()));

					tv = (TextView) convert.findViewById(R.id.ingredient_unit);
					tv.setText(i.getUnits().getAbbreviation());

					return convert;
				}
			});
		}


    }

    public void onStop() {
		super.onStop();

		AppData.getSingleton().close();
	}

}
