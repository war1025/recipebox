package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.R;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.impl.UtilityImpl;

import android.content.Intent;
import android.os.Bundle;

import android.app.TabActivity;
import android.graphics.Color;
import android.view.View;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

public class RecipeTabs extends TabActivity {

	protected Recipe curRecipe;
	protected boolean editing;

	private static final int INACTIVE_COLOR = Color.parseColor("#439595");
	private static final int ACTIVE_COLOR = Color.parseColor("#7dd3d3");
	private static final int TEXT_COLOR = Color.parseColor("#000000");

	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.recipe_tabs);

		TabHost host = getTabHost();
		TabSpec spec = null;
		Intent intent = getIntent();
		long id = intent.getLongExtra("id", -1);
		boolean edit = intent.getBooleanExtra("edit", false);
		int tab = intent.getIntExtra("tab", 0);

		this.curRecipe = UtilityImpl.singleton.getRecipeById(id);
		this.editing = edit;

		setTitle(curRecipe.getName());

		curRecipe.updateLastViewTime();

		intent = new Intent(this, RecipeDisplay.class);

		spec = host.newTabSpec("info").setIndicator("Info").setContent(intent);

		host.addTab(spec);

		intent = new Intent(this, IngredientsDisplay.class);

		spec = host.newTabSpec("ingredients").setIndicator("Ingredients").setContent(intent);

		host.addTab(spec);

		intent = new Intent(this, InstructionsDisplay.class);

		spec = host.newTabSpec("instructions").setIndicator("Instructions").setContent(intent);

		host.addTab(spec);


		host.setCurrentTab(tab);

		View tabView = null;

		for(int i = 0; i < 3; i++) {
			tabView = host.getTabWidget().getChildAt(i);
			tabView.setBackgroundColor((tab == i) ? ACTIVE_COLOR : INACTIVE_COLOR);
			((TextView) tabView.findViewById(android.R.id.title)).setTextColor(TEXT_COLOR);
		}

		host.getTabWidget().getChildAt(tab).setBackgroundColor(ACTIVE_COLOR);

		host.setOnTabChangedListener(new OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				View tab = null;
				int currentTab = getTabHost().getCurrentTab();
				for(int i = 0; i < 3; i++) {
					tab = getTabHost().getTabWidget().getChildAt(i);
					tab.setBackgroundColor((currentTab == i) ? ACTIVE_COLOR : INACTIVE_COLOR);
					((TextView) tab.findViewById(android.R.id.title)).setTextColor(TEXT_COLOR);
				}
			}
		});
	}

}
