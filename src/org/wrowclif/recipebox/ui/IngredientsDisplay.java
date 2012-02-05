package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Ingredient;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.RecipeIngredient;
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
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ListView;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.text.TextWatcher;
import android.text.Editable;

import java.util.ArrayList;

public class IngredientsDisplay extends Activity {

	private boolean edit;
	private Recipe r;
	private ArrayAdapter<RecipeIngredient> adapter;

	private static final int NEW_INGREDIENT_DIALOG = 0;
	private static final int CONFIRM_NEW_INGREDIENT_DIALOG = 1;
	private static final int INGREDIENT_ALREADY_IN_USE_DIALOG = 2;
	private static final int EDIT_INGREDIENT_DIALOG = 3;
	private static final int DELETE_INGREDIENT_DIALOG = 4;
	private static final int EDIT_DIALOG = 5;
	private static final int CREATE_DIALOG = 6;
	private static final int DELETE_DIALOG = 7;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ingredients_display);

		Intent intent = getIntent();

		r = ((RecipeTabs) getParent()).curRecipe;
		edit = ((RecipeTabs) getParent()).editing;

		if(r != null) {
			setTitle(r.getName());

			ListView lv = (ListView) findViewById(R.id.ingredient_list);
			adapter = new ArrayAdapter<RecipeIngredient>(this, R.layout.ingredient_display_item, r.getIngredients()) {
				public View getView(final int position, View convert, ViewGroup group) {
					if(convert == null) {
						LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						convert = vi.inflate(R.layout.ingredient_display_item, null);
					}

					final RecipeIngredient i = getItem(position);

					TextView tv = (TextView) convert.findViewById(R.id.ingredient_name);
					tv.setText(i.getName());

					tv = (TextView) convert.findViewById(R.id.ingredient_amount);
					tv.setText(i.getAmount());

					Button be = (Button) convert.findViewById(R.id.edit_button);
					Button bd = (Button) convert.findViewById(R.id.delete_button);

					if(edit) {
						be.setVisibility(View.VISIBLE);
						bd.setVisibility(View.VISIBLE);

						be.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								Bundle bundle = new Bundle();
								bundle.putInt("position", position);

								showDialog(EDIT_INGREDIENT_DIALOG, bundle);
							}
						});
						bd.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								Bundle bundle = new Bundle();
								bundle.putInt("position", position);

								showDialog(DELETE_INGREDIENT_DIALOG, bundle);
							}
						});
					} else {
						be.setVisibility(View.GONE);
						bd.setVisibility(View.GONE);
					}
					return convert;
				}
			};

			lv.setAdapter(adapter);

			Button add = (Button) findViewById(R.id.add_button);

			add.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Bundle bundle = new Bundle();
					bundle.putString("ingredient", "");

					showDialog(NEW_INGREDIENT_DIALOG, bundle);
				}
			});

			Button done = (Button) findViewById(R.id.done_button);

			done.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					setEditing(false);
				}
			});

			if(!edit) {
				add.setVisibility(View.GONE);
				done.setVisibility(View.GONE);
			}
		}


    }

	public void onResume() {
		super.onResume();
		boolean editing = ((RecipeTabs) getParent()).editing;
		if(edit != editing) {
			setEditing(editing);
		}
	}

    protected void setEditing(boolean editing) {
		Button add = (Button) findViewById(R.id.add_button);
		Button done = (Button) findViewById(R.id.done_button);
		if(editing) {
			add.setVisibility(View.VISIBLE);
			done.setVisibility(View.VISIBLE);
		} else {
			add.setVisibility(View.GONE);
			done.setVisibility(View.GONE);
		}
		edit = editing;
		adapter.notifyDataSetChanged();

		((RecipeTabs) getParent()).editing = editing;
	}

	protected void onPrepareDialog(int id, Dialog d, final Bundle bundle) {

		switch(id) {
			case NEW_INGREDIENT_DIALOG : {
				AlertDialog dialog = (AlertDialog) d;

				String text = bundle.getString("ingredient");
				TextView input = (TextView) dialog.findViewById(R.id.ingredient_edit);
				Log.d("Recipebox", "text: " + text + " input: " + input);
				input.setText(text);
				break;
			}

			case CONFIRM_NEW_INGREDIENT_DIALOG : {
				AlertDialog dialog = (AlertDialog) d;

				final String ingredient = bundle.getString("ingredient");
				dialog.setMessage("You have never used " + ingredient + " before. " +
					"Are you sure you want to add it to this recipe?");
				dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Add", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Ingredient i = UtilityImpl.singleton.createOrRetrieveIngredient(ingredient);
						RecipeIngredient ri = r.addIngredient(i);

						if(ri == null) {
							showDialog(INGREDIENT_ALREADY_IN_USE_DIALOG);
						} else {
							adapter.add(ri);

							int position = adapter.getPosition(ri);

							Bundle bundle = new Bundle();
							bundle.putInt("position", position);

							showDialog(EDIT_INGREDIENT_DIALOG, bundle);
						}
					}
				});
				dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						showDialog(NEW_INGREDIENT_DIALOG, bundle);
					}
				});
				break;
			}

			case INGREDIENT_ALREADY_IN_USE_DIALOG : {
				AlertDialog dialog = (AlertDialog) d;
				String ingredient = bundle.getString("ingredient");

				dialog.setMessage("Sorry! " + ingredient + " is already used in this recipe. " +
									"Ingredients can only be added once per recipe");
				break;
			}

			case EDIT_INGREDIENT_DIALOG : {
				AlertDialog dialog = (AlertDialog) d;
				final RecipeIngredient ri = adapter.getItem(bundle.getInt("position", -1));
				final TextView input = (TextView) dialog.findViewById(R.id.text_edit);

				dialog.setTitle(String.format("Edit %s Amount", ri.getName()));
				input.setText(ri.getAmount());

				dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Done", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						ri.setAmount(input.getText().toString());

						adapter.notifyDataSetChanged();
					}
				});
				break;
			}

			case DELETE_INGREDIENT_DIALOG : {
				AlertDialog dialog = (AlertDialog) d;
				final RecipeIngredient ri = adapter.getItem(bundle.getInt("position", -1));
				dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Delete", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						r.removeIngredient(ri);

						adapter.remove(ri);
					}
				});
				break;
			}
		}
	}

	protected Dialog onCreateDialog(int id, Bundle bundle) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		switch(id) {
			case NEW_INGREDIENT_DIALOG : {
				LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View v = li.inflate(R.layout.ingredient_edit_dialog, null);
				builder.setView(v);
				final AutoCompleteTextView input = (AutoCompleteTextView) v.findViewById(R.id.ingredient_edit);

				Specifics<Ingredient> sp = new Specifics<Ingredient>() {

					public View getView(int id, Ingredient i, View v, ViewGroup vg) {
						if(v == null) {
							v = inflate(R.layout.autoitem);
						}

						TextView tv = (TextView) v.findViewById(R.id.child_name);

						tv.setText(i.getName());

						return v;
					}

					public long getItemId(Ingredient item) {
						return item.getId();
					}

					public List<Ingredient> filter(CharSequence seq) {
						return UtilityImpl.singleton.searchIngredients(seq.toString(), 5);
					}

					public String convertResultToString(Ingredient result) {
						return result.getName();
					}

					public void onItemClick(AdapterView av, View v, int position, long id, Ingredient item) {

					}

					private View inflate(int layoutId) {
						LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						return vi.inflate(layoutId, null);
					}
				};

				ListAutoCompleteAdapter<Ingredient> acadapter = new ListAutoCompleteAdapter<Ingredient>(sp);

				input.setAdapter(acadapter);

				input.setOnEditorActionListener(new OnEditorActionListener() {
					public boolean onEditorAction(TextView v, int action, KeyEvent event) {
						if((action == EditorInfo.IME_NULL) && (event.getAction() == 0)) {
							Ingredient i = UtilityImpl.singleton.getIngredientByName(v.getText().toString());

							if(i == null) {
								Bundle bundle = new Bundle();
								bundle.putString("ingredient", input.getText().toString());

								showDialog(CONFIRM_NEW_INGREDIENT_DIALOG, bundle);
							} else {
								RecipeIngredient ri = r.addIngredient(i);

								if(ri == null) {
									Bundle bundle = new Bundle();
									bundle.putString("ingredient", i.getName());
									showDialog(INGREDIENT_ALREADY_IN_USE_DIALOG, bundle);
								} else {
									adapter.add(ri);

									int position = adapter.getPosition(ri);

									Bundle bundle = new Bundle();
									bundle.putInt("position", position);

									showDialog(EDIT_INGREDIENT_DIALOG, bundle);
								}
							}
							return true;
						}
						return false;
					}
				});


				builder.setTitle("New Ingredient");
				builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Ingredient i = UtilityImpl.singleton.getIngredientByName(input.getText().toString());

						if(i == null) {
							Bundle bundle = new Bundle();
							bundle.putString("ingredient", input.getText().toString());

							showDialog(CONFIRM_NEW_INGREDIENT_DIALOG, bundle);
						} else {
							RecipeIngredient ri = r.addIngredient(i);

							if(ri == null) {
								Bundle bundle = new Bundle();
								bundle.putString("ingredient", i.getName());
								showDialog(INGREDIENT_ALREADY_IN_USE_DIALOG, bundle);
							} else {
								adapter.add(ri);

								int position = adapter.getPosition(ri);

								Bundle bundle = new Bundle();
								bundle.putInt("position", position);

								showDialog(EDIT_INGREDIENT_DIALOG, bundle);
							}
						}
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});

				break;
			}

			case CONFIRM_NEW_INGREDIENT_DIALOG : {
				builder.setTitle("Confirm Ingredient");

				final String ingredient = bundle.getString("ingredient");
				builder.setMessage("You have never used " + ingredient + " before. " +
					"Are you sure you want to add it to this recipe?");
				builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Ingredient i = UtilityImpl.singleton.createOrRetrieveIngredient(ingredient);
						RecipeIngredient ri = r.addIngredient(i);

						if(ri == null) {
							Bundle bundle = new Bundle();
							bundle.putString("ingredient", i.getName());
							showDialog(INGREDIENT_ALREADY_IN_USE_DIALOG, bundle);
						} else {
							adapter.add(ri);

							int position = adapter.getPosition(ri);

							Bundle bundle = new Bundle();
							bundle.putInt("position", position);

							showDialog(EDIT_INGREDIENT_DIALOG, bundle);
						}
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
				break;
			}

			case INGREDIENT_ALREADY_IN_USE_DIALOG : {
				final String ingredient = bundle.getString("ingredient");
				builder.setTitle("Ingredient In Use");
				builder.setMessage("Sorry! " + ingredient + " is already used in this recipe. " +
									"Ingredients can only be added once per recipe");

				builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
				break;
			}
			case EDIT_INGREDIENT_DIALOG : {
				final RecipeIngredient ri = adapter.getItem(bundle.getInt("position", -1));

				LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View v = li.inflate(R.layout.enter_text_dialog, null);
				final EditText input = (EditText) v.findViewById(R.id.text_edit);
				input.setSingleLine(true);
				builder.setView(v);
				input.setText(ri.getAmount());

				builder.setTitle(String.format("Edit %s Amount", ri.getName()));
				builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						ri.setAmount(input.getText().toString());

						adapter.notifyDataSetChanged();
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
				break;
			}

			case DELETE_INGREDIENT_DIALOG : {
				final RecipeIngredient ri = adapter.getItem(bundle.getInt("position", -1));
				builder.setTitle("Delete Ingredient");
				builder.setMessage("Are you sure you want to delete this ingredient?");
				builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						r.removeIngredient(ri);

						adapter.remove(ri);
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
				break;
			}

			case CREATE_DIALOG : {
				builder.setTitle("Create New Recipe");
				break;
			}

			case EDIT_DIALOG : {
				builder.setTitle("Edit Recipe");
				builder.setItems(new String[] {"Edit This Recipe", "Create Variant", "Cancel"},
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							switch(which) {
								case 0 :
									setEditing(!edit);
									break;
								case 1 :
									Recipe r2 = r.branch(r.getName() + " (branch)");
									Intent intent = new Intent(IngredientsDisplay.this, RecipeTabs.class);
									intent.putExtra("id", r2.getId());
									intent.putExtra("edit", true);
									intent.putExtra("tab", 2);

									IngredientsDisplay.this.finish();
									startActivity(intent);
									break;
							}
						}
				});
				break;
			}

			case DELETE_DIALOG : {
				builder.setTitle("Delete Recipe");
				builder.setMessage("Are you sure you want to delete the recipe?");
				builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						r.delete();
						IngredientsDisplay.this.finish();
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

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.recipe_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.edit :
				if(edit) {
					setEditing(false);
				} else {
					showDialog(EDIT_DIALOG);
				}
				return true;

			case R.id.delete :
				showDialog(DELETE_DIALOG);
				return true;

			case R.id.create :
				showDialog(CREATE_DIALOG);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

    public void onStop() {
		super.onStop();

		AppData.getSingleton().close();
	}
}
