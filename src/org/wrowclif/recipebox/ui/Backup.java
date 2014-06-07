package org.wrowclif.recipebox.ui;

import org.wrowclif.recipebox.Actions;
import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox2.R;

import org.wrowclif.recipebox.util.BackupUtil;

import org.wrowclif.recipebox.ui.components.BaseActivity;
import org.wrowclif.recipebox.ui.components.DialogManager.DialogHandler;
import org.wrowclif.recipebox.ui.components.EnterTextDialog;
import org.wrowclif.recipebox.ui.components.DynamicLoadAdapter;

import static org.wrowclif.recipebox.util.ConstantInitializer.assignId;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * View for managing Recipe backups.
 **/
public class Backup extends BaseActivity {

   private static final String LOG_TAG = "Recipebox Backup";

   private DynamicLoadAdapter<File> backupListAdapter;

   private static final int CREATE_BACKUP_DIALOG = assignId();
   private static final int REMOVE_BACKUP_DIALOG = assignId();
   private static final int USE_BACKUP_DIALOG    = assignId();

   protected int getViewId() {
      return R.layout.categories;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      Actions.BACKUP.showNotifications();

      ListView lv = (ListView) findViewById(R.id.category_list);

      createDynamicLoadAdapter();

      backupListAdapter.setUpList(lv);

      TextView label = (TextView) findViewById(R.id.category_label);
      AppData.getSingleton().useHeadingFont(label);
      label.setText("Manage Backups");

      //{ Create backup button
      TextView add_button = (TextView) findViewById(R.id.add_button);

      add_button.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            showDialog(CREATE_BACKUP_DIALOG);
         }
      });
      //}

      //{ Done editing button
      View done_button = findViewById(R.id.done_button);
      done_button.setVisibility(View.GONE);
      //}

      this.setupDialogHandlers();
    }

   /**
    * Called when this activity resumes execution
    **/
    public void onResume() {
      super.onResume();
      // Clear the backup list so it refreshes
      backupListAdapter.clear();
   }

   /**
    * Sets up the dynamic load adapter for the backup list
    **/
   private void createDynamicLoadAdapter() {

      DynamicLoadAdapter.Specifics<File> sp = new DynamicLoadAdapter.Specifics<File>() {

         /**
          * Gets the view to show for the given file
          *
          * @param id   The id of the recipe
          * @param file The file to create a row view for
          * @param v    The view to reuse
          * @param vg   The group the view belongs to
          **/
         public View getView(final int position, File file, View v, ViewGroup vg) {
            if(v == null) {
               v = inflate(R.layout.category_item);
               ((ViewGroup) v).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            }

            TextView tv = (TextView) v.findViewById(R.id.name_box);
            AppData.getSingleton().useTextFont(tv);

            // If the file is null, then we are at the end of the list and need to load more items.
            if(file == null) {
               tv.setText("Loading...");
            // Otherwise list the file's name
            } else {
               tv.setText(file.getName());
            }

            // The edit button is not needed in this activity
            v.findViewById(R.id.edit_button).setVisibility(View.GONE);

            //{ Setup the delete button, used for removing the recipe from this category
            View delete_button = v.findViewById(R.id.delete_button);

            delete_button.setOnClickListener(new OnClickListener() {
               public void onClick(View v) {
                  Bundle b = new Bundle();

                  b.putInt("position", position);

                  showDialog(REMOVE_BACKUP_DIALOG, b);
               }
            });
            //}

            return v;
         }

         /**
          * Gets the id for the given file
          *
          * @param item The item to get the id for
          **/
         public long getItemId(File item) {
            return item.hashCode();
         }

         /**
          * Retrieves additional files to show in the list
          *
          * @param offset How many recipes are currently shown
          * @param max    The maximum number of new recipes to return
          **/
         public List<File> filter(int offset, int max) {
            return BackupUtil.getAvailableBackups();
         }

         /**
          * Return a string representation of the file (Its name)
          *
          * @param result The file to get a string for
          **/
         public String convertResultToString(File result) {
            if(result == null) {
               return "Null";
            } else {
               return result.getName();
            }
         }

         /**
          * Called when a row in the list is clicked.
          *
          * @param av       The parent view
          * @param v        The view that was selected
          * @param position The position of the item in the list
          * @param id       The id of the selected file
          * @param item     The file that was selected
          **/
         public void onItemClick(AdapterView av, View v, int position, long id, File item) {
            Bundle b = new Bundle();
            b.putInt("position", position);

            showDialog(USE_BACKUP_DIALOG, b);
         }

         /**
          * Create a view for the given id
          *
          * @param layoutId The id for the layout to create
          **/
         private View inflate(int layoutId) {
            LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return vi.inflate(layoutId, null);
         }
      };

      backupListAdapter = new DynamicLoadAdapter<File>(sp);
   }

   private void setupDialogHandlers() {

      //{ Add handler
      DialogHandler backup_handler = new DialogHandler() {
         private DateFormat format = new SimpleDateFormat("MMM-dd-yyyy");

         public Dialog createDialog(Bundle bundle) {
            final EnterTextDialog backup_dialog = new EnterTextDialog(Backup.this);

            backup_dialog.setTitle("Create Backup");
            backup_dialog.setOkButtonText("Backup");

            //{ Setup the add button handler.
            backup_dialog.setOkListener(new OnClickListener() {
               public void onClick(View v) {
                  String backup_name = backup_dialog.getEditText();
                  boolean success = BackupUtil.createBackup(backup_name);
                  String msg = "";
                  if(success) {
                     msg = "Backup saved to external storage";
                  } else {
                     msg = "Could not create backup";
                  }
                  Toast.makeText(Backup.this, msg, Toast.LENGTH_LONG).show();
                  Backup.this.backupListAdapter.clear();
               }
            });
            //}

            return backup_dialog;
         }

         public void prepareDialog(Dialog d, Bundle bundle) {
            EnterTextDialog dialog = (EnterTextDialog) d;
            dialog.setEditText(format.format(new Date()));
         }
      };
      this.dialogManager.registerHandler(CREATE_BACKUP_DIALOG, backup_handler);
      //}

      //{ Remove handler
      DialogHandler remove_handler = new DialogHandler() {
         public Dialog createDialog(Bundle bundle) {
            EnterTextDialog etd = new EnterTextDialog(Backup.this, R.layout.show_text_dialog);

            etd.setTitle("Remove Backup");

            etd.setOkButtonText("Remove");

            return etd;
         }

         public void prepareDialog(Dialog d, Bundle bundle) {
            EnterTextDialog dialog = (EnterTextDialog) d;

            final int position = bundle.getInt("position", -1);
            final File file = Backup.this.backupListAdapter.getItem(position);

            dialog.setEditHtml("Are you sure you want to remove <b>" +
                               file.getName() + "</b> from the available backups?");
            dialog.setOkListener(new OnClickListener() {
               public void onClick(View v) {
                  BackupUtil.removeBackup(file);
                  Backup.this.backupListAdapter.clear();
               }
            });
         }
      };
      this.dialogManager.registerHandler(REMOVE_BACKUP_DIALOG, remove_handler);
      //}

      //{ Load backup handler
      DialogHandler load_handler = new DialogHandler() {
         public Dialog createDialog(Bundle bundle) {
            EnterTextDialog etd = new EnterTextDialog(Backup.this, R.layout.show_text_dialog);

            etd.setTitle("Restore Backup");

            etd.setOkButtonText("Restore");

            return etd;
         }

         public void prepareDialog(Dialog d, Bundle bundle) {
            EnterTextDialog dialog = (EnterTextDialog) d;

            final int position = bundle.getInt("position", -1);
            final File file = Backup.this.backupListAdapter.getItem(position);

            dialog.setEditHtml("Are you sure you want to restore RecipeBox to the " +
                               "state it was when you created <b>" +
                               file.getName() + "</b>?");
            dialog.setOkListener(new OnClickListener() {
               public void onClick(View v) {
                  boolean success = BackupUtil.loadBackup(file);
                  String msg = "";
                  if(success) {
                     msg = "Restore succeeded";
                  } else {
                     msg = "Could not load backup";
                  }
                  Toast.makeText(Backup.this, msg, Toast.LENGTH_LONG).show();
               }
            });
         }
      };
      this.dialogManager.registerHandler(USE_BACKUP_DIALOG, load_handler);
      //}
   }
}
