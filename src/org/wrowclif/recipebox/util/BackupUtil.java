package org.wrowclif.recipebox.util;

import org.wrowclif.recipebox.AppData;

import android.os.Environment;
import android.util.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.nio.channels.FileChannel;

public class BackupUtil {

   private static final String LOG_TAG = "Recipebox BackupUtil";

   public static final String DB_PATH    = "/data/org.wrowclif.recipebox2/databases/RECIPEBOX";
   public static final String BACKUP_DIR = "/org.wrowclif.recipebox2/backups";

   private static File getBackupDirectory() {
      File backup_dir = null;

      File sd = Environment.getExternalStorageDirectory();
      if(sd.canWrite()) {
         backup_dir = new File(sd, BACKUP_DIR);
         backup_dir.mkdirs();
      }
      return backup_dir;
   }

   private static File getDatabaseFile() {
      File data_dir = Environment.getDataDirectory();
      File db_file = new File(data_dir, DB_PATH);

      return db_file;
   }

   public static boolean createBackup(final String backupName) {
      boolean success = false;

      String sanitized_name = backupName.replaceAll("\\.", "_").replaceAll("/", "");
      try {
         File backup_dir = getBackupDirectory();
         File db_file    = getDatabaseFile();

         if (backup_dir != null && db_file.exists()) {
            File backup_db = new File(backup_dir, sanitized_name);

            FileChannel src = null;
            FileChannel dst = null;

            try {
               src = new FileInputStream(db_file).getChannel();
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

   public static List<File> getAvailableBackups() {
      List<File> backups = new ArrayList<File>();

      try {
         File backup_dir = getBackupDirectory();

         if (backup_dir.canRead() && backup_dir.isDirectory()) {
            for(File file : backup_dir.listFiles()) {
               backups.add(file);
            }
         }
      } catch (Exception e) {
         Log.e(LOG_TAG, "Failed to list backups");
      }

      Collections.sort(backups, new Comparator<File>() {
         public int compare(File left, File right) {
            long left_modified  = left.lastModified();
            long right_modified = right.lastModified();
            return (left_modified > right_modified) ? -1 :
                     ((left_modified == right_modified) ? 0 : 1);
         }
      });

      return backups;
   }

   public static boolean loadBackup(final File backup_db) {
      boolean success = false;

      File backup_dir = getBackupDirectory();

      // Cannot load a backup if we cannot get at the backup directory.or the file isn't a backup
      if(backup_dir == null || !backup_db.getParentFile().equals(backup_dir)) {
         return false;
      }

      try {

         if (backup_db.exists() && backup_db.isFile() && backup_db.canRead()) {
            File db_file = getDatabaseFile();

            // Close the database so we can replace the file
            AppData.getSingleton().getOpenHelper().close();
            db_file.delete();

            FileChannel src = null;
            FileChannel dst = null;

            try {
               src = new FileInputStream(backup_db).getChannel();
               dst = new FileOutputStream(db_file).getChannel();
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

   public static boolean removeBackup(File backup) {
      boolean success = false;

      File backup_dir = getBackupDirectory();

      if(backup_dir != null && backup.getParentFile().equals(backup_dir)) {
         backup.delete();
         success = true;
      }

      return success;
   }

}
