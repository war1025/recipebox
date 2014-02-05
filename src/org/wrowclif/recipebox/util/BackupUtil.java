package org.wrowclif.recipebox.util;

import org.wrowclif.recipebox.AppData;

import android.os.Environment;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.nio.channels.FileChannel;

public class BackupUtil {

   private static final String LOG_TAG = "Recipebox BackupUtil";

   public static boolean createBackup(final String backupName) {
      boolean success = false;

      final String currentDBPath = "//data//org.wrowclif.recipebox2//databases//RECIPEBOX";
      final String backupDirPath = "//org.wrowclif.recipebox2//backups";

      String sanitized_name = backupName.replaceAll("\\.", "_").replaceAll("/", "");
      try {
         File sd = Environment.getExternalStorageDirectory();
         File data = Environment.getDataDirectory();

         if (sd.canWrite()) {
            File current_db = new File(data, currentDBPath);
            File backup_dir = new File(sd, backupDirPath);
            backup_dir.mkdirs();

            File backup_db = new File(backup_dir, sanitized_name);

            FileChannel src = null;
            FileChannel dst = null;

            try {
               src = new FileInputStream(current_db).getChannel();
               dst = new FileOutputStream(backup_db).getChannel();
               dst.transferFrom(src, 0, src.size());

               success = true;

            } catch(Exception e) {
               Log.e(LOG_TAG, "Failed to export database:" + e);
            } finally {
               try {
                  src.close();
               } catch(Exception e) {}

               try {
                  dst.close();
               } catch(Exception e) {}
            }
         }
      } catch (Exception e) {
         Log.e(LOG_TAG, "Failed to export database:" + e);
      }
      return success;
   }

   public static List<String> getAvailableBackups() {
      final String backupDirPath = "//org.wrowclif.recipebox2//backups";

      List<String> backups = new ArrayList<String>();

      try {
         File sd = Environment.getExternalStorageDirectory();
         File backup_dir = new File(sd, backupDirPath);

         if (backup_dir.canRead() && backup_dir.isDirectory()) {
            for(String file : backup_dir.list()) {
               backups.add(file);
            }
         }
      } catch (Exception e) {
         Log.e(LOG_TAG, "Failed to list backups");
      }

      return backups;
   }

   public static boolean loadBackup(final String backupName) {
      boolean success = false;

		AppData.getSingleton().getOpenHelper().close();

      final String currentDBPath = "//data//org.wrowclif.recipebox2//databases//RECIPEBOX";
      final String backupDirPath = "//org.wrowclif.recipebox2//backups";

      try {
         File sd = Environment.getExternalStorageDirectory();
         File backup_dir = new File(sd, backupDirPath);
         File backup_db = new File(backup_dir, backupName);

         File data = Environment.getDataDirectory();

         if (backup_db.exists() && backup_db.isFile() && backup_db.canRead()) {
            File current_db = new File(data, currentDBPath);
            current_db.delete();

            FileChannel src = null;
            FileChannel dst = null;

            try {
               src = new FileInputStream(backup_db).getChannel();
               dst = new FileOutputStream(current_db).getChannel();
               dst.transferFrom(src, 0, src.size());

               success = true;

            } catch(Exception e) {
               Log.e(LOG_TAG, "Failed to export database:" + e);
            } finally {
               try {
                  src.close();
               } catch(Exception e) {}

               try {
                  dst.close();
               } catch(Exception e) {}
            }
         }
      } catch (Exception e) {
         Log.e(LOG_TAG, "Failed to export database:" + e);
      }
      return success;
   }
}
