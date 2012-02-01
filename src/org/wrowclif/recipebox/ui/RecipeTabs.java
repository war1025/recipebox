package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.R;

import android.content.Intent;
import android.os.Bundle;

import android.app.TabActivity;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class RecipeTabs extends TabActivity {

	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(R.layout.recipe_tabs);

		TabHost host = getTabHost();
		TabSpec spec = null;
		Intent intent = getIntent();
		long id = intent.getLongExtra("id", -1);

		intent = new Intent(this, RecipeDisplay.class);
		intent.putExtra("id", id);

		spec = host.newTabSpec("info").setIndicator("Info").setContent(intent);

		host.addTab(spec);

		intent = new Intent(this, IngredientsDisplay.class);
		intent.putExtra("id", id);

		spec = host.newTabSpec("ingredients").setIndicator("Ingredients").setContent(intent);

		host.addTab(spec);

		intent = new Intent(this, InstructionsDisplay.class);
		intent.putExtra("id", id);

		spec = host.newTabSpec("instructions").setIndicator("Instructions").setContent(intent);

		host.addTab(spec);


		host.setCurrentTab(0);

	}
}
