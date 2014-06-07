package org.wrowclif.recipebox.ui.components;

import org.wrowclif.recipebox.AppData;

import org.wrowclif.recipebox.ui.components.DialogManager.DialogHandler;
import org.wrowclif.recipebox.ui.components.MenuManager.MenuHandler;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class BaseActivity extends Activity {

   protected DialogManager dialogManager;
   protected MenuManager   menuManager;

   protected int getViewId() {
      return 0;
   }

   protected int getMenuId() {
      return 0;
   }

   protected void useHeadingFont(View baseView, int... viewIds) {
      AppData app_data = AppData.getSingleton();

      for(int view_id : viewIds) {
         TextView view = (TextView) baseView.findViewById(view_id);
         app_data.useHeadingFont(view);
      }
   }

   protected void useHeadingFont(int... viewIds) {
      AppData app_data = AppData.getSingleton();

      for(int view_id : viewIds) {
         TextView view = (TextView) findViewById(view_id);
         app_data.useHeadingFont(view);
      }
   }

   protected void useTextFont(View baseView, int... viewIds) {
      AppData app_data = AppData.getSingleton();

      for(int view_id : viewIds) {
         TextView view = (TextView) baseView.findViewById(view_id);
         app_data.useTextFont(view);
      }
   }

   protected void useTextFont(int... viewIds) {
      AppData app_data = AppData.getSingleton();

      for(int view_id : viewIds) {
         TextView view = (TextView) findViewById(view_id);
         app_data.useTextFont(view);
      }
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

   public View inflate(int layoutId) {
      LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      return inflater.inflate(layoutId, null);
   }
}
