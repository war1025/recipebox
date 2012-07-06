package org.wrowclif.recipebox.util;

import org.wrowclif.recipebox.Recipe;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ImageUtil {
	private static final String LOG_TAG = "Recipebox ImageUtil";

	public static void deleteImage(Recipe r) {
		File f = getImageFile(r);
		f.delete();
	}

	public static String copyImage(Recipe from, Recipe to) {
		File fromFile = getImageFile(from);
		File toFile = getImageSaveFile(to);
		if(fromFile != null && fromFile.exists()) {
			FileInputStream fromStream = null;
			FileOutputStream toStream = null;
			try {
				fromStream = new FileInputStream(fromFile);
				toStream = new FileOutputStream(toFile);

				int numRead = 0;
				int bufferSize = 8 * 1024;
				byte[] buffer = new byte[bufferSize];
				while((numRead = fromStream.read(buffer, 0, bufferSize)) != -1) {
					toStream.write(buffer, 0, numRead);
				}
			} catch(Exception e) {
				Log.e(LOG_TAG, "Exception: " + e, e);
			} finally {
				if(fromStream != null) {
					try {
						fromStream.close();
					} catch(Exception e) {
					}
				}
				if(toStream != null) {
					try {
						toStream.close();
					} catch(Exception e) {
					}
				}
			}
		}
		return Uri.fromFile(toFile).toString();
	}

	public static File getImageFile(Recipe r) {
		File imageFile = null;
		String uriString = r.getImageUri();
		if(uriString.length() > 0) {
			Uri uri = Uri.parse(uriString);
			if(uri != null) {
				imageFile = new File(uri.getSchemeSpecificPart());
			}
		}
		return imageFile;
	}

	public static File getImageSaveFile(Recipe recipe) {
		File imageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
																										"org.wrowclif.recipebox");
		if(!imageDir.exists()){
			if(!imageDir.mkdirs()){
				Log.d(LOG_TAG, "Could not access image storage directory: " + imageDir);
				Log.d(LOG_TAG, "External Storage State: " + Environment.getExternalStorageState());
				return null;
			}
		}

		File imageFile = new File(imageDir.getPath() + File.separator + "IMG_" + recipe.getId() + ".jpg");

		return imageFile;
	}

	public static Bitmap loadImageAtWidth(Recipe recipe, int maxWidth) {
		File imageFile = getImageFile(recipe);
		if(imageFile == null) {
			return null;
		}

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(imageFile.getPath(), options);

		int width = options.outWidth;

		int inSampleSize = 1;

		if (width > maxWidth) {
			inSampleSize = Math.round((float) width / (float) maxWidth);
		}

		options.inSampleSize = inSampleSize;
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(imageFile.getPath(), options);
	}

	public static void saveDownsampledImage(Recipe recipe, int maxWidth) {
		Bitmap downsampledImage = loadImageAtWidth(recipe, maxWidth);

		if(downsampledImage != null) {
			File saveAs = getImageFile(recipe);
			FileOutputStream out = null;

			try {
				out = new FileOutputStream(saveAs);

				downsampledImage.compress(Bitmap.CompressFormat.JPEG, 90, out);

			} catch(Exception e) {
				Log.e(LOG_TAG, "Exception Downsampling image: " + e, e);
			} finally {
				if(out != null) {
					try {
						out.close();
					} catch(Exception ex) {
					}
				}
			}
		}
	}

}
