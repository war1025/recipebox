package org.wrowclif.recipebox.ui.components;

import android.view.MenuItem;

import java.util.Map;
import java.util.HashMap;

public class MenuManager {

   private Map<Integer, MenuHandler> handlers;

   public MenuManager() {
      this.handlers = new HashMap<Integer, MenuHandler>();
   }

   public void registerHandler(int dialogId, MenuHandler handler) {
      this.handlers.put(dialogId, handler);
   }

   public MenuHandler getMenuHandler(int dialogId) {
      return this.handlers.get(dialogId);
   }


   public interface MenuHandler {

      public void itemSelected(MenuItem item);

   }

}
