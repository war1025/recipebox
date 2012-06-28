package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.Actions;
import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Recipe;
import org.wrowclif.recipebox.Instruction;
import org.wrowclif.recipebox.R;

import org.wrowclif.recipebox.ui.components.EnterTextDialog;
import org.wrowclif.recipebox.ui.components.RecipeMenus;
import org.wrowclif.recipebox.ui.components.RecipeMenus.EditSwitcher;
import org.wrowclif.recipebox.ui.components.DynamicLoadAdapter;
import org.wrowclif.recipebox.ui.components.ReorderableItemDecorator;
import org.wrowclif.recipebox.ui.components.ReorderableItemDecorator.ItemSwap;

import org.wrowclif.recipebox.impl.UtilityImpl;

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
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ListView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.text.TextWatcher;
import android.text.Editable;
import android.text.InputType;

import java.util.ArrayList;

public class InstructionsDisplay extends Activity {

	private boolean edit;
	private Recipe r;
	private DynamicLoadAdapter<Instruction> adapter;
	private ReorderableItemDecorator<Instruction> reorderDecorator;
	private RecipeMenus menus;

	private static final int EDIT_INSTRUCTION_DIALOG = assignId();
	private static final int DELETE_INSTRUCTION_DIALOG = assignId();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.instructions_display);
		Actions.RECIPE_INSTRUCTIONS.showNotifications();

		Intent intent = getIntent();

		r = ((RecipeTabs) getParent()).curRecipe;
		edit = ((RecipeTabs) getParent()).editing;

		createDynamicLoadAdapter();

		ItemSwap swapper = new ItemSwap() {
			public void swapItems(int a, int b) {
				Actions.RECIPE_INSTRUCTIONS_REORDER.showNotifications();
				r.swapInstructionPositions(adapter.getItem(a), adapter.getItem(b));
			}
		};

		reorderDecorator = new ReorderableItemDecorator(adapter, swapper);

		ListView lv = (ListView) findViewById(R.id.instruction_list);
		lv.setAdapter(adapter);

		Button add = (Button) findViewById(R.id.add_button);

		add.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Instruction in = r.addInstruction();

				int position = adapter.getCount();
				adapter.add(position, in);

				Bundle bundle = new Bundle();
				bundle.putInt("position", position);
				bundle.putBoolean("deleteOnCancel", true);

				showDialog(EDIT_INSTRUCTION_DIALOG, bundle);
			}
		});

		Button done = (Button) findViewById(R.id.done_button);

		done.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setEditing(false);
			}
		});

		setEditing(edit);
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
			Actions.RECIPE_EDIT.showNotifications();
			if(adapter.getCount() >= 2) {
				Actions.RECIPE_INSTRUCTIONS_PRE_REORDER.showNotifications();
			}
			add.setVisibility(View.VISIBLE);
			done.setVisibility(View.VISIBLE);
		} else {
			add.setVisibility(View.GONE);
			done.setVisibility(View.GONE);
		}
		edit = editing;
		reorderDecorator.setEditing(editing);
		adapter.notifyDataSetChanged();

		((RecipeTabs) getParent()).editing = editing;
	}

	protected void onPrepareDialog(int id, Dialog d, Bundle bundle) {

		if(id == EDIT_INSTRUCTION_DIALOG) {
			final EnterTextDialog dialog = (EnterTextDialog) d;
			final int position = bundle.getInt("position", -1);
			final Instruction instruction = adapter.getItem(position);
			final boolean deleteOnCancel = bundle.getBoolean("deleteOnCancel", false);

			dialog.setEditText(instruction.getText());

			dialog.setOkListener(new OnClickListener() {
				public void onClick(View v) {
					instruction.setText(dialog.getEditText());

					adapter.notifyDataSetChanged();
					if(adapter.getCount() >= 2) {
						Actions.RECIPE_INSTRUCTIONS_PRE_REORDER.showNotifications();
					}
				}
			});

			dialog.setCancelListener(new OnClickListener() {
				public void onClick(View v) {
					if(deleteOnCancel) {
						r.removeInstruction(instruction);
						adapter.remove(position);
					}
				}
			});
		} else if(id == DELETE_INSTRUCTION_DIALOG) {
			EnterTextDialog dialog = (EnterTextDialog) d;

			final int position = bundle.getInt("position", -1);
			final Instruction instruction = adapter.getItem(position);

			dialog.setOkListener(new OnClickListener() {
				public void onClick(View v) {
					r.removeInstruction(instruction);

					adapter.remove(position);
				}
			});
		}
	}

	protected Dialog onCreateDialog(int id, Bundle bundle) {
		if(menus != null) {
			Dialog d = menus.createDialog(id);
			if(d != null) {
				return d;
			}
		}
		Dialog dialog = null;

		if(id == EDIT_INSTRUCTION_DIALOG) {
			final Instruction instruction = adapter.getItem(bundle.getInt("position", -1));

			Log.d("Recipebox", "Edit dialog position: " + bundle.getInt("position", -1) + " instruction: " + instruction.getId());

			EnterTextDialog etd = new EnterTextDialog(this);

			etd.setEditText(instruction.getText());

			etd.getEditView().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
			etd.getEditView().setSingleLine(false);

			etd.setTitle("Edit Instruction");

			etd.setOkButtonText("Done");

			dialog = etd;

		} else if(id == DELETE_INSTRUCTION_DIALOG) {
			EnterTextDialog etd = new EnterTextDialog(this, R.layout.show_text_dialog);

			etd.setTitle("Delete Instruction");
			etd.setEditText("Are you sure you want to delete this instruction?");

			etd.setOkButtonText("Delete");

			dialog = etd;
		}

		return dialog;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		EditSwitcher es = new EditSwitcher() {
			public void setEditing(boolean editing) {
				InstructionsDisplay.this.setEditing(editing);
			}

			public boolean getEditing() {
				return InstructionsDisplay.this.edit;
			}
		};
		menus = new RecipeMenus(r, this, 2, es);

		menus.createMenu(menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		boolean menuHandled = menus.onItemSelect(item.getItemId());

		if(!menuHandled) {
			return super.onOptionsItemSelected(item);
		} else {
			return true;
		}
	}

	private void createDynamicLoadAdapter() {

		DynamicLoadAdapter.Specifics<Instruction> sp = new DynamicLoadAdapter.Specifics<Instruction>() {
			public View getView(final int position, Instruction i, View convert, ViewGroup vg) {
				if(convert == null) {
					LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convert = vi.inflate(R.layout.instructions_display_item, null);
				}

				TextView tv = (TextView) convert.findViewById(R.id.instruction_number);
				tv.setText((position + 1) + ".");
				AppData.getSingleton().useHeadingFont(tv);

				tv = (TextView) convert.findViewById(R.id.instruction_text);
				AppData.getSingleton().useTextFont(tv);

				Button be = (Button) convert.findViewById(R.id.edit_button);
				Button bd = (Button) convert.findViewById(R.id.delete_button);

				if(i == null) {
					tv.setText("Loading...");
					be.setVisibility(View.GONE);
					bd.setVisibility(View.GONE);
					return convert;
				} else {
					tv.setText(i.getText());
				}


				if(edit) {
					be.setVisibility(View.VISIBLE);
					bd.setVisibility(View.VISIBLE);

					be.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							Bundle bundle = new Bundle();
							bundle.putInt("position", position);

							showDialog(EDIT_INSTRUCTION_DIALOG, bundle);
						}
					});
					bd.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							Bundle bundle = new Bundle();
							bundle.putInt("position", position);

							showDialog(DELETE_INSTRUCTION_DIALOG, bundle);
						}
					});
				} else {
					be.setVisibility(View.GONE);
					bd.setVisibility(View.GONE);
				}

				reorderDecorator.decorateItem(convert, position);

				return convert;
			}

			public long getItemId(Instruction item) {
				return item.getId();
			}

			public List<Instruction> filter(int offset, int max) {
				List<Instruction> instructions = r.getInstructions();
				return instructions;
			}

			public String convertResultToString(Instruction result) {
				return "Instruction";
			}

			public void onItemClick(AdapterView av, View v, int position, long id, Instruction item) {

			}

			private View inflate(int layoutId) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				return vi.inflate(layoutId, null);
			}
		};

		adapter = new DynamicLoadAdapter<Instruction>(sp);
	}
}
