package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.Ingredient;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.RecipeIngredient;
import org.wrowclif.recipebox.R;
import org.wrowclif.recipebox.impl.UtilityImpl;
import org.wrowclif.recipebox.ui.ListAutoCompleteAdapter;
import org.wrowclif.recipebox.ui.ListAutoCompleteAdapter.Specifics;

import java.util.List;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;

public class IngredientDialog extends Dialog {

	private EditText amountInput;
	private AutoCompleteTextView ingredientInput;
	private TextView messageView;
	private Button okButton;
	private Button cancelButton;

	private Recipe recipe;
	private ArrayAdapter<RecipeIngredient> adapter;
	private RecipeIngredient ingredient;
	private int position;

	private View.OnClickListener editOkOnClick;
	private View.OnClickListener editCancelOnClick;
	private OnEditorActionListener editOnIngredientAction;
	private View.OnClickListener	newOkOnClick;
	private View.OnClickListener newCancelOnClick;
	private OnEditorActionListener newOnIngredientAction;
	private View.OnClickListener confirmOkOnClick;
	private View.OnClickListener confirmCancelOnClick;
	private View.OnClickListener inUseOkOnClick;

	public IngredientDialog(Context context) {
		super(context);
		setContentView(R.layout.ingredient_edit_dialog);

		getWindow().setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		setCancelable(true);

		amountInput = (EditText) findViewById(R.id.amount_edit);
		ingredientInput = (AutoCompleteTextView) findViewById(R.id.ingredient_edit);
		messageView = (TextView) findViewById(R.id.message_box);
		okButton = (Button) findViewById(R.id.ok_button);
		cancelButton = (Button) findViewById(R.id.cancel_button);

		connectAutoComplete();

		editOkOnClick = new EditOkOnClick();
		editCancelOnClick = new CancelOnClick();
		editOnIngredientAction = new EditOnIngredientAction();

		newOkOnClick = new NewOkOnClick();
		newCancelOnClick = editCancelOnClick;
		newOnIngredientAction = new NewOnIngredientAction();

		confirmOkOnClick = new ConfirmOkOnClick();
		confirmCancelOnClick = new CancelReturnToEditOnClick();

		inUseOkOnClick = confirmCancelOnClick;
	}

	public void prepareNew(Recipe recipe, ArrayAdapter<RecipeIngredient> adapter) {
		this.recipe = recipe;
		this.adapter = adapter;
		this.ingredient = null;
		this.position = -1;

		showNew();
	}

	public void prepareExisiting(Recipe r, ArrayAdapter<RecipeIngredient> adapter, RecipeIngredient ingredient, int position) {
		this.recipe = r;
		this.adapter = adapter;
		this.ingredient = ingredient;
		this.position = position;

		showEdit();
	}

	public void showNew() {
		reshowNew();

		ingredientInput.dismissDropDown();

		amountInput.setText("");
		ingredientInput.setText("");
	}

	public void reshowNew() {
		setTitle("Add Ingredient");

		amountInput.setVisibility(View.VISIBLE);
		ingredientInput.setVisibility(View.VISIBLE);
		okButton.setVisibility(View.VISIBLE);
		cancelButton.setVisibility(View.VISIBLE);
		messageView.setVisibility(View.GONE);

		ingredientInput.setOnEditorActionListener(newOnIngredientAction);

		okButton.setText("Add");
		okButton.setOnClickListener(newOkOnClick);

		cancelButton.setText("Cancel");
		cancelButton.setOnClickListener(newCancelOnClick);
	}

	public void showEdit() {
		reshowEdit();

		ingredientInput.dismissDropDown();

		amountInput.setText(ingredient.getAmount());
		ingredientInput.setText(ingredient.getName());
	}

	public void reshowEdit() {
		setTitle("Edit Ingredient");

		amountInput.setVisibility(View.VISIBLE);
		ingredientInput.setVisibility(View.VISIBLE);
		okButton.setVisibility(View.VISIBLE);
		cancelButton.setVisibility(View.VISIBLE);
		messageView.setVisibility(View.GONE);

		ingredientInput.setOnEditorActionListener(editOnIngredientAction);

		okButton.setText("Done");
		okButton.setOnClickListener(editOkOnClick);

		cancelButton.setText("Cancel");
		cancelButton.setOnClickListener(editCancelOnClick);
	}

	public void showConfirmNew() {
		setTitle("Confirm New Ingredient");

		amountInput.setVisibility(View.GONE);
		ingredientInput.setVisibility(View.GONE);
		okButton.setVisibility(View.VISIBLE);
		cancelButton.setVisibility(View.VISIBLE);
		messageView.setVisibility(View.VISIBLE);

		messageView.setText("You have never used " + ingredientInput.getText() + " before. " +
					"Are you sure you want to add it to this recipe?");

		okButton.setText("Add");
		okButton.setOnClickListener(confirmOkOnClick);

		cancelButton.setText("Cancel");
		cancelButton.setOnClickListener(confirmCancelOnClick);
	}

	public void showAlreadyInUse() {
		setTitle("Ingredient Already In Use");

		amountInput.setVisibility(View.GONE);
		ingredientInput.setVisibility(View.GONE);
		okButton.setVisibility(View.VISIBLE);
		cancelButton.setVisibility(View.GONE);
		messageView.setVisibility(View.VISIBLE);

		messageView.setText("Sorry! " + ingredientInput.getText() + " is already used in this recipe. " +
									"Ingredients can only be added once per recipe");

		okButton.setText("Ok");
		okButton.setOnClickListener(inUseOkOnClick);
	}

	private void connectAutoComplete() {
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
				LayoutInflater vi = getLayoutInflater();
				return vi.inflate(layoutId, null);
			}
		};

		ListAutoCompleteAdapter<Ingredient> acadapter = new ListAutoCompleteAdapter<Ingredient>(sp);

		ingredientInput.setAdapter(acadapter);
	}

	protected class NewOkOnClick implements View.OnClickListener {
		public void onClick(View v) {
			Ingredient i = UtilityImpl.singleton.getIngredientByName(ingredientInput.getText().toString());
			String amountText = amountInput.getText().toString();

			if(i == null) {
				showConfirmNew();
			} else {
				RecipeIngredient ri = recipe.addIngredient(i);

				if(ri == null) {
					showAlreadyInUse();
				} else {
					ri.setAmount(amountText);
					adapter.add(ri);
					IngredientDialog.this.dismiss();
				}
			}
		}
	}

	protected class NewOnIngredientAction implements OnEditorActionListener {
		public boolean onEditorAction(TextView v, int action, KeyEvent event) {
			if((action == EditorInfo.IME_NULL) && (event.getAction() == 0)) {
				newOkOnClick.onClick(null);
				return true;
			}
			return false;
		}
	}

	protected class CancelOnClick implements View.OnClickListener {
		public void onClick(View v) {
			IngredientDialog.this.dismiss();
		}
	}

	protected class EditOkOnClick implements View.OnClickListener {
		public void onClick(View v) {
			Ingredient currentIngredient = UtilityImpl.singleton.getIngredientByName(ingredientInput.getText().toString());

			boolean sameIngredient = (currentIngredient != null) &&
										(currentIngredient.getId() == ingredient.getIngredient().getId());

			if(sameIngredient) {
				ingredient.setAmount(amountInput.getText().toString());
				adapter.notifyDataSetChanged();
				IngredientDialog.this.dismiss();
			} else if(currentIngredient == null) {
				showConfirmNew();
			} else {
				RecipeIngredient added = recipe.addIngredient(currentIngredient);

				if(added == null) {
					showAlreadyInUse();
				} else {
					List<RecipeIngredient> order = new ArrayList<RecipeIngredient>(adapter.getCount());
					for(int i = 0; i < adapter.getCount(); i++) {
						if(i == position) {
							order.add(added);
						} else {
							order.add(adapter.getItem(i));
						}
					}
					recipe.removeIngredient(ingredient);
					recipe.reorderIngredients(order);

					added.setAmount(amountInput.getText().toString());

					adapter.remove(ingredient);
					adapter.insert(added, position);

					IngredientDialog.this.dismiss();
				}
			}
		}
	}

	protected class EditOnIngredientAction implements OnEditorActionListener {
		public boolean onEditorAction(TextView v, int action, KeyEvent event) {
			if((action == EditorInfo.IME_NULL) && (event.getAction() == 0)) {
				editOkOnClick.onClick(null);
				return true;
			}
			return false;
		}
	}

	protected class ConfirmOkOnClick implements View.OnClickListener {
		public void onClick(View v) {
			Ingredient i = UtilityImpl.singleton.createOrRetrieveIngredient(ingredientInput.getText().toString());

			if(ingredient == null) {
				newOkOnClick.onClick(null);
			} else {
				editOkOnClick.onClick(null);
			}
		}
	}

	protected class CancelReturnToEditOnClick implements View.OnClickListener {
		public void onClick(View v) {
			if(ingredient == null) {
				reshowNew();
			} else {
				reshowEdit();
			}
		}
	}
}