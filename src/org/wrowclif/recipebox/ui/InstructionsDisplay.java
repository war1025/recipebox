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
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.TextWatcher;
import android.text.Editable;

import java.util.ArrayList;

public class InstructionsDisplay extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instructions_display);

		Intent intent = getIntent();

		Recipe r = UtilityImpl.singleton.getRecipeById(intent.getLongExtra("id", -1));

		if(r != null) {
			setTitle(r.getName());

			ListView lv = (ListView) findViewById(R.id.instruction_list);
			lv.setAdapter(new ArrayAdapter<Instruction>(this, R.layout.instructions_display_item, r.getInstructions()) {
				public View getView(int position, View convert, ViewGroup group) {
					if(convert == null) {
						LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						convert = vi.inflate(R.layout.instructions_display_item, null);
					}

					Instruction i = getItem(position);

					TextView tv = (TextView) convert.findViewById(R.id.instruction_number);
					tv.setText((position + 1) + ".");

					tv = (TextView) convert.findViewById(R.id.instruction_text);
					tv.setText(i.getText());

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
