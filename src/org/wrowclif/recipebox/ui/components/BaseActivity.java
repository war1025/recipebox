package org.wrowclif.recipebox.ui.components;

import org.wrowclif.recipebox.ui.components.DialogManager.DialogHandler;
import org.wrowclif.recipebox.ui.components.MenuManager.MenuHandler;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class BaseActivity extends Activity {

   protected DialogManager dialogManager;
   protected MenuManager   menuManager;

   protected int getViewId() {
      return 0;
   }

   protected int getMenuId() {
      return 0;
   }

   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      dialogManager = new DialogManager();
      menuManager   = new MenuManager();

      setContentView(this.getViewId());

   }

   protected Dialog onCreateDialog(int id, Bundle bundle) {
      DialogHandler handler = this.dialogManager.getDialogHandler(id);

      Dialog dialog = null;

      if(handler != null) {
         dialog = handler.createDialog(bundle);
      }

      return dialog;
   }

   protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {
      DialogHandler handler = this.dialogManager.getDialogHandler(id);

      if(handler != null) {
         handler.prepareDialog(dialog, bundle);
      }
   }

   public boolean onCreateOptionsMenu(Menu menu) {
      int menu_id = this.getMenuId();
      if(menu_id != 0) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(menu_id, menu);
         return true;
      }
      return false;
   }

   public boolean onOptionsItemSelected(MenuItem item) {
      MenuHandler handler = this.menuManager.getMenuHandler(item.getItemId());

      if(handler != null) {
         handler.itemSelected(item);
         return true;
      }
      return false;
   }

}
