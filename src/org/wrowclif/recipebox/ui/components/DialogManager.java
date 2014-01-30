package org.wrowclif.recipebox.ui.components;

import android.app.Dialog;
import android.os.Bundle;

import java.util.Map;
import java.util.HashMap;

public class DialogManager {

   private Map<Integer, DialogHandler> handlers;

   public DialogManager() {
      this.handlers = new HashMap<Integer, DialogHandler>();
   }

   public void registerHandler(int dialogId, DialogHandler handler) {
      this.handlers.put(dialogId, handler);
   }

   public DialogHandler getDialogHandler(int dialogId) {
      return this.handlers.get(dialogId);
   }


   public interface DialogHandler {

      public Dialog createDialog(Bundle bundle);

      public void prepareDialog(Dialog dialog, Bundle bundle);

   }

}
