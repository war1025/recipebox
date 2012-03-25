package org.wrowclif.recipebox.ui.components;

import org.wrowclif.recipebox.R;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.impl.UtilityImpl;
import org.wrowclif.recipebox.ui.RecipeTabs;

import static org.wrowclif.recipebox.util.ConstantInitializer.assignId;

import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;

public class RecipeMenus {

	private Recipe recipe;
	private Activity activity;
	private int tab;
	private EditSwitcher switcher;

	private static final int CREATE_RECIPE_DIALOG = assignId();
	private static final int EDIT_RECIPE_DIALOG = assignId();
	private static final int DELETE_RECIPE_DIALOG = assignId();

	public RecipeMenus(Recipe recipe, Activity activity, int tab, EditSwitcher switcher) {
		this.recipe = recipe;
		this.activity = activity;
		this.tab = tab;
		this.switcher = switcher;
	}

	public boolean onItemSelect(int id) {
		switch(id) {
			case R.id.create : {
				activity.showDialog(CREATE_RECIPE_DIALOG);
				return true;
			}

			case R.id.edit : {
				if(switcher.getEditing()) {
					switcher.setEditing(false);
				} else {
					activity.showDialog(EDIT_RECIPE_DIALOG);
				}
				return true;
			}

			case R.id.delete : {
				activity.showDialog(DELETE_RECIPE_DIALOG);
				return true;
			}

			default :
				return false;

		}
	}

	public void createMenu(Menu menu) {
		MenuInflater inflater = activity.getMenuInflater();
		inflater.inflate(R.menu.recipe_menu, menu);
	}

	public Dialog createDialog(int id) {
		if(id == CREATE_RECIPE_DIALOG) {
			return createRecipeDialog();

		} else if(id == EDIT_RECIPE_DIALOG) {
			return editRecipeDialog();

		} else if(id == DELETE_RECIPE_DIALOG) {
			return deleteRecipeDialog();

		} else {
			return null;
		}
	}

	private Dialog createRecipeDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Create New Recipe");
		builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Recipe r2 = UtilityImpl.singleton.newRecipe("New Recipe");
				Intent intent = new Intent(activity, RecipeTabs.class);
				intent.putExtra("id", r2.getId());
				intent.putExtra("edit", true);
				intent.putExtra("tab", tab);

				activity.finish();
				activity.startActivity(intent);
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});
		builder.setCancelable(true);
		return builder.create();
	}

	private Dialog editRecipeDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Edit Recipe");
		builder.setItems(new String[] {"Edit This Recipe", "Create Variant", "Cancel"},
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					switch(which) {
						case 0 :
							switcher.setEditing(true);
							break;
						case 1 :
							Recipe r2 = recipe.branch(recipe.getName() + " (branch)");
							Intent intent = new Intent(activity, RecipeTabs.class);
							intent.putExtra("id", r2.getId());
							intent.putExtra("edit", true);
							intent.putExtra("tab", tab);

							activity.finish();
							activity.startActivity(intent);
							break;
					}
				}
		});
		builder.setCancelable(true);
		return builder.create();
	}

	private Dialog deleteRecipeDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Delete Recipe");
		builder.setMessage("Are you sure you want to delete the recipe?");
		builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				recipe.delete();
				activity.finish();
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});
		builder.setCancelable(true);
		return builder.create();
	}

	public interface EditSwitcher {

		public void setEditing(boolean editing);

		public boolean getEditing();

	}
}
