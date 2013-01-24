package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.Actions;
import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Category;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.RecipeIngredient;
import org.wrowclif.recipebox.Utility;
import org.wrowclif.recipebox.Instruction;
import org.wrowclif.recipebox.R;
import org.wrowclif.recipebox.impl.UtilityImpl;

import org.wrowclif.recipebox.ui.components.EnterTextDialog;
import org.wrowclif.recipebox.ui.components.DynamicLoadAdapter;

import static org.wrowclif.recipebox.util.ConstantInitializer.assignId;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.text.TextWatcher;
import android.text.Editable;
import android.text.InputType;

import java.util.ArrayList;

/**
 * Shows the user all categories in the system
 *
 * @param util    Reference to the Utility singleton for querying recipes
 * @param appData Reference to the AppData singleton for retrieving application settings
 * @param adapter Adapter for loading the category list
 * @param edit    Flag indicating whether we are in edit mode
 *
 * @param CREATE_CATEGORY_DIALOG Dialog ID for creating a new category
 * @param EDIT_CATEGORY_DIALOG   Dialog ID for editing an existing category
 * @param DELETE_CATEGORY_DIALOG Dialog ID for removing a category
 **/
public class BrowseCategories extends Activity {

	private Utility util;
	private AppData appData;
	private DynamicLoadAdapter<Category> adapter;
	private boolean edit;

	private static final int CREATE_CATEGORY_DIALOG = assignId();
	private static final int EDIT_CATEGORY_NAME_DIALOG = assignId();
	private static final int DELETE_CATEGORY_DIALOG = assignId();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.categories);
		Actions.CATEGORIES.showNotifications();

		// Singleton references
		util = UtilityImpl.singleton;
		appData = AppData.getSingleton();

		appData.useHeadingFont((TextView) findViewById(R.id.category_label));

		//{ Setup the category list
		ListView lv = (ListView) findViewById(R.id.category_list);

		createDynamicLoadAdapter();

		adapter.setUpList(lv);
		//}

		//{ Add category button
		View addButton = findViewById(R.id.add_button);

		addButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(CREATE_CATEGORY_DIALOG);
			}
		});
		//}

		//{ Done editing button
		View doneButton = findViewById(R.id.done_button);

		doneButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setEditing(false);
			}
		});
		//}

		// Start out not in edit mode
		setEditing(false);
    }

	/**
	 * Called when this activity resumes execution
	 **/
    public void onResume() {
		super.onResume();
	}

	/**
	 * Switch the editing state of this view
	 *
	 * @param editing Whether or not we are editing
	 **/
	protected void setEditing(boolean editing) {
		// The views that are affected by edit mode
		View[] views = {findViewById(R.id.add_button), findViewById(R.id.done_button)};

		// Show them when we are editing
		if(editing) {
			Actions.CATEGORIES_EDIT.showNotifications();
			for(View v : views) {
				v.setVisibility(View.VISIBLE);
			}
		// Hide them otherwise
		} else {
			for(View v : views) {
				v.setVisibility(View.GONE);
			}
		}

		// Reload the list
		this.edit = editing;
		adapter.notifyDataSetChanged();
	}

	/**
	 * Called when reusing an existing dialog
	 *
	 * @param id     The id of the dialog being created
	 * @param d      The dialog being reused
	 * @param bundle Any information to use for updating the dialog
	 **/
	protected void onPrepareDialog(int id, Dialog d, Bundle bundle) {

		// When creating a new category, just clear out the text field
		if(id == CREATE_CATEGORY_DIALOG) {
			EnterTextDialog dialog = (EnterTextDialog) d;
			dialog.setEditText("");
		// When editing a category, fill the text field with the category's current name.
		} else if(id == EDIT_CATEGORY_NAME_DIALOG) {
			final EnterTextDialog dialog = (EnterTextDialog) d;
			final int position = bundle.getInt("position", -1);
			final Category category = adapter.getItem(position);

			dialog.setEditText(category.getName());

			// When the user accepts the change, update the category and refresh the category list
			dialog.setOkListener(new OnClickListener() {
				public void onClick(View v) {
					category.setName(dialog.getEditText());
					adapter.notifyDataSetChanged();
				}
			});

		// When deleting a category, show a confirmation dialog with the category's name placed in it
		} else if(id == DELETE_CATEGORY_DIALOG) {
			EnterTextDialog dialog = (EnterTextDialog) d;

			final int position = bundle.getInt("position", -1);
			final Category category = adapter.getItem(position);

			dialog.setEditHtml("Are you sure you want to delete the <b>" + category.getName() + "</b> category?");

			// If the user accepts, delete the category and remove it from the list
			dialog.setOkListener(new OnClickListener() {
				public void onClick(View v) {
					category.delete();
					adapter.remove(position);
				}
			});
		}
	}

	/**
	 * Called when creating a dialog for the first time.
	 * Note: Prepare dialog is called after this method and before the dialog is shown,
	 *       so item specifc data can be set there.
	 *
	 * @param id     The id of the dialog to create
	 * @param bundle Any additional information needed to create the dialog
	 **/
	protected Dialog onCreateDialog(int id, Bundle bundle) {
		Dialog dialog = null;

		if(id == CREATE_CATEGORY_DIALOG) {
			final EnterTextDialog etd = new EnterTextDialog(this);

			// Setup the text field
			etd.getEditView().setHint("Category Name");
			etd.setEditText("");
			etd.getEditView().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
			etd.getEditView().setSingleLine(true);

			etd.setTitle("Create Category");

			// Connect the accept listener since we don't need any item specific information.
			etd.setOkButtonText("Create");
			etd.setOkListener(new OnClickListener() {
				public void onClick(View v) {
					// Make a new category with the name the user entered, if that category doesn't yet exist
					util.createOrRetrieveCategory(etd.getEditText());
					// Clear the adapter so the new category will be shown in the list
					adapter.clear();
				}
			});

			dialog = etd;

		} else if(id == EDIT_CATEGORY_NAME_DIALOG) {
			final EnterTextDialog etd = new EnterTextDialog(this);

			// Setup the text field
			etd.getEditView().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
			etd.getEditView().setSingleLine(true);

			etd.setTitle("Edit Category Name");

			etd.setOkButtonText("Done");

			dialog = etd;
		} else if(id == DELETE_CATEGORY_DIALOG) {
			EnterTextDialog etd = new EnterTextDialog(this, R.layout.show_text_dialog);

			etd.setTitle("Delete Category");

			etd.setOkButtonText("Delete");

			dialog = etd;
		}


		return dialog;
	}

	/**
	 * Called to create the options menu for this activity
	 *
	 * @param menu The options menu to add items to
	 **/
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mi = getMenuInflater();

		mi.inflate(R.menu.category_menu, menu);

		return true;
	}

	/**
	 * Called when an item in the options menu is selected
	 *
	 * @param item The item that was selected
	 **/
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch(id) {
			// If the edit option was selected, toggle edit mode
			case R.id.edit : {
				setEditing(!edit);
				return true;
			}
		}

		return super.onOptionsItemSelected(item);
	}


	/**
	 * Sets up the dynamic loader for the category list
	 **/
	private void createDynamicLoadAdapter() {

		DynamicLoadAdapter.Specifics<Category> sp = new DynamicLoadAdapter.Specifics<Category>() {

			/**
			 * Gets the row view for the given category
			 *
			 * @param position The position of the category in the list
			 * @param c        The category to show
			 * @param v        The view to reuse or null
			 * @param vg       The parent of the given view
			 **/
			public View getView(final int position, Category c, View v, ViewGroup vg) {
				if(v == null) {
					v = inflate(R.layout.category_item);
				}

				TextView tv = (TextView) v.findViewById(R.id.name_box);
				appData.useTextFont((TextView) v.findViewById(R.id.name_box));

				// If the category is null then we are at the end of the list and need to load more items
				if(c == null) {
					tv.setText("Loading...");
			    // Otherwise show the category's name
				} else {
					tv.setText(c.getName());
				}

				View editButton = v.findViewById(R.id.edit_button);
				View deleteButton = v.findViewById(R.id.delete_button);

				// If we are in edit mode add handlers for the edit and delete buttons and show the buttons
				if(edit) {
					editButton.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							Bundle b = new Bundle();

							b.putInt("position", position);

							showDialog(EDIT_CATEGORY_NAME_DIALOG, b);
						}
					});

					deleteButton.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							Bundle b = new Bundle();

							b.putInt("position", position);

							showDialog(DELETE_CATEGORY_DIALOG, b);
						}
					});

					editButton.setVisibility(View.VISIBLE);
					deleteButton.setVisibility(View.VISIBLE);
				// Otherwise hide the buttons and don't bother showing anything
				} else {
					editButton.setVisibility(View.GONE);
					deleteButton.setVisibility(View.GONE);
				}

				return v;
			}

			/**
			 * Gets the id for the given category
			 *
			 * @param item The category to get the id of
			 **/
			public long getItemId(Category item) {
				return item.getId();
			}

			/**
			 * Loads more categories to show in the list
			 *
			 * @param offset How many categories are currently shown
			 * @param max    The maximum number of new categories to load
			 **/
			public List<Category> filter(int offset, int max) {
				List<Category> nextCategories = UtilityImpl.singleton.getCategoriesByName(offset, max);
				if(nextCategories.size() == max) {
					nextCategories.add(null);
				}
				return nextCategories;
			}

			/**
			 * Get a string representation for the given category (Its name)
			 *
			 * @param result The category to get the name of
			 **/
			public String convertResultToString(Category result) {
				if(result == null) {
					return "Null";
				} else {
					return result.getName();
				}
			}

			/**
			 * Called when a category is clicked in the list.
			 * Opens that category
			 *
			 * @param av       The list view
			 * @param v        The view that was clicked
			 * @param position The position of the clicked item in the list
			 * @param id       The id of the selected category
			 * @param item     The category that was selected
			 **/
			public void onItemClick(AdapterView av, View v, int position, long id, Category item) {
				Intent intent = new Intent(BrowseCategories.this, CategoryList.class);
				intent.putExtra("id", id);
				startActivity(intent);
			}

			/**
			 * Creates a view for the given id
			 *
			 * @param layoutId The id of the view to load
			 **/
			private View inflate(int layoutId) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				return vi.inflate(layoutId, null);
			}
		};

		adapter = new DynamicLoadAdapter<Category>(sp);
	}

}
