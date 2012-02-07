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
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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
	private ArrayAdapter<Instruction> adapter;
	private int moveableItem;

	private static final int EDIT_INSTRUCTION_DIALOG = 1;
	private static final int DELETE_INSTRUCTION_DIALOG = 2;
	private static final int EDIT_DIALOG = 3;
	private static final int CREATE_DIALOG = 4;
	private static final int DELETE_DIALOG = 5;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.instructions_display);

		Intent intent = getIntent();

		r = ((RecipeTabs) getParent()).curRecipe;
		edit = ((RecipeTabs) getParent()).editing;
		moveableItem = -1;

		if(r != null) {
			setTitle(r.getName());

			ListView lv = (ListView) findViewById(R.id.instruction_list);
			adapter = new ArrayAdapter<Instruction>(this, R.layout.instructions_display_item, r.getInstructions()) {
				public View getView(final int position, View convert, ViewGroup group) {
					if(convert == null) {
						LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						convert = vi.inflate(R.layout.instructions_display_item, null);
					}

					final Instruction i = getItem(position);

					TextView tv = (TextView) convert.findViewById(R.id.instruction_number);
					tv.setText((position + 1) + ".");

					tv = (TextView) convert.findViewById(R.id.instruction_text);
					tv.setText(i.getText());

					Button be = (Button) convert.findViewById(R.id.edit_button);
					Button bd = (Button) convert.findViewById(R.id.delete_button);

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

						convert.setOnLongClickListener(new OnLongClickListener() {
							public boolean onLongClick(View v) {
								if(moveableItem != position) {
									moveableItem = position;
								} else {
									moveableItem = -1;
								}
								adapter.notifyDataSetChanged();
								return true;
							}
						});
					} else {
						be.setVisibility(View.GONE);
						bd.setVisibility(View.GONE);
						convert.setOnLongClickListener(null);
					}

					Button mu = (Button) convert.findViewById(R.id.up_button);
					Button md = (Button) convert.findViewById(R.id.down_button);

					if(moveableItem == position) {
						mu.setVisibility(View.VISIBLE);
						mu.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								if(position > 0) {
									r.swapInstructionPositions(adapter.getItem(position), adapter.getItem(position -1));
									Instruction i = adapter.getItem(position);
									moveableItem = position - 1;
									adapter.remove(i);
									adapter.insert(i, position -1);
								}
							}
						});
						md.setVisibility(View.VISIBLE);
						md.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								if(position < adapter.getCount() - 1) {
									r.swapInstructionPositions(adapter.getItem(position), adapter.getItem(position + 1));
									Instruction i = adapter.getItem(position + 1);
									moveableItem = position + 1;
									adapter.remove(i);
									adapter.insert(i, position);
								}
							}
						});
					} else {
						mu.setVisibility(View.GONE);
						md.setVisibility(View.GONE);
					}
					return convert;
				}
			};

			lv.setAdapter(adapter);

			Button add = (Button) findViewById(R.id.add_button);

			add.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Instruction in = r.addInstruction();
					adapter.add(in);

					int position = adapter.getPosition(in);

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
			moveableItem = -1;
		}
		edit = editing;
		adapter.notifyDataSetChanged();

		((RecipeTabs) getParent()).editing = editing;
	}

	protected void onPrepareDialog(int id, Dialog d, Bundle bundle) {

		switch(id) {
			case EDIT_INSTRUCTION_DIALOG : {
				AlertDialog dialog = (AlertDialog) d;
				final Instruction instruction = adapter.getItem(bundle.getInt("position", -1));
				final EditText input = (EditText) dialog.findViewById(R.id.text_edit);
				final boolean deleteOnCancel = bundle.getBoolean("deleteOnCancel", false);
				input.setText(instruction.getText());

				dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Done", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						instruction.setText(input.getText().toString());

						adapter.notifyDataSetChanged();
					}
				});

				dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if(deleteOnCancel) {
							r.removeInstruction(instruction);
							adapter.remove(instruction);
						}
					}
				});
				break;
			}

			case DELETE_INSTRUCTION_DIALOG : {
				AlertDialog dialog = (AlertDialog) d;
				final Instruction instruction = adapter.getItem(bundle.getInt("position", -1));
				dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Delete", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						r.removeInstruction(instruction);

						adapter.remove(instruction);
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
			case EDIT_INSTRUCTION_DIALOG : {
				final Instruction instruction = adapter.getItem(bundle.getInt("position", -1));

				Log.d("Recipebox", "Edit dialog position: " + bundle.getInt("position", -1) + " instruction: " + instruction.getId());

				LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View v = li.inflate(R.layout.enter_text_dialog, null);
				final EditText input = (EditText) v.findViewById(R.id.text_edit);
				builder.setView(v);
				input.setText(instruction.getText());
				input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
				input.setSingleLine(false);

				builder.setTitle("Edit Instruction");
				builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						instruction.setText(input.getText().toString());

						adapter.notifyDataSetChanged();
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
				break;
			}

			case DELETE_INSTRUCTION_DIALOG : {
				final Instruction instruction = adapter.getItem(bundle.getInt("position", -1));
				builder.setTitle("Delete Instruction");
				builder.setMessage("Are you sure you want to delete this instruction?");
				builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						r.removeInstruction(instruction);

						adapter.remove(instruction);
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
				builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Recipe r2 = UtilityImpl.singleton.newRecipe("New Recipe");
						Intent intent = new Intent(InstructionsDisplay.this, RecipeTabs.class);
						intent.putExtra("id", r2.getId());
						intent.putExtra("edit", true);
						intent.putExtra("tab", 2);

						InstructionsDisplay.this.finish();
						startActivity(intent);
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
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
									Intent intent = new Intent(InstructionsDisplay.this, RecipeTabs.class);
									intent.putExtra("id", r2.getId());
									intent.putExtra("edit", true);
									intent.putExtra("tab", 2);

									InstructionsDisplay.this.finish();
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
						InstructionsDisplay.this.finish();
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
