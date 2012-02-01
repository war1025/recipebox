package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.Instruction;
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
import android.widget.AdapterView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.TextWatcher;
import android.text.Editable;

import java.util.ArrayList;

public class Main extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        AppData.initialSingleton(this);

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



    }

    public void onStop() {
		super.onStop();

		AppData.getSingleton().close();
	}

}
