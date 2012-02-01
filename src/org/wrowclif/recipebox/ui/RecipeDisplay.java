package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Recipe;
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
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.TextWatcher;
import android.text.Editable;

import java.util.ArrayList;

public class RecipeDisplay extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_display);

		Intent intent = getIntent();

		Recipe r = UtilityImpl.singleton.getRecipeById(intent.getLongExtra("id", -1));

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
