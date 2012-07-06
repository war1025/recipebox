package org.wrowclif.recipebox.util;

import org.wrowclif.recipebox.Recipe;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;

import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

import java.util.Scanner;

public class ZipUtil {
	private static final String LOG_TAG = "Recipebox ZipUtil";

	public static Uri zipRecipes(Context ctx, Recipe... r) {
		if(r.length == 0) {
			return null;
		}

		Uri zipUri = null;
		File tempFile = null;

		ZipOutputStream zos = null;

		try {
			String name = "RecipeCollection";
			if(r.length == 1) {
				name = r[0].getName();
			}
			tempFile = File.createTempFile(name.replaceAll(" ", ""), ".zip", ctx.getCacheDir());
			tempFile.setReadable(true, false);

			zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));

			String recipeJson = JsonUtil.toJson(r);

			ZipEntry recipeEntry = new ZipEntry("recipe.rcpb");
			zos.putNextEntry(recipeEntry);
			zos.write(recipeJson.getBytes());
			zos.closeEntry();

			for(int i = 0; i < r.length; i++) {
				File imageFile = ImageUtil.getImageFile(r[i]);

				if(imageFile != null && imageFile.exists()) {
					ZipEntry imageEntry = new ZipEntry(i + ".jpg");

					zos.putNextEntry(imageEntry);

					FileInputStream in = null;

					try {
						in = new FileInputStream(imageFile);

						int bufferSize = 8 * 1024;
						byte[] buffer = new byte[bufferSize];

						int count = 0;

						while((count = in.read(buffer, 0, bufferSize)) != -1) {
							zos.write(buffer, 0, count);
						}
					} catch(Exception e) {
						Log.e(LOG_TAG, "Exception Zipping image: " + e, e);
					} finally {
						if(in != null) {
							try {
								in.close();
							} catch(Exception ex) {
							}
						}
					}

					zos.closeEntry();
				}
			}

			zipUri = Uri.fromFile(tempFile);
		} catch(Exception e) {
			Log.e(LOG_TAG, "Exception Zipping Recipe: " + e, e);
		} finally {
			if(zos != null) {
				try {
					zos.close();
				} catch(Exception ex) {
				}
			}
		}

		return zipUri;
	}

	public static Recipe[] unzipRecipes(Context ctx, Uri zipLocation) {
		Recipe[] recipes = null;
		ZipInputStream zipStream = null;

		try {
			zipStream = new ZipInputStream(ctx.getContentResolver().openInputStream(zipLocation));

			ZipEntry recipeEntry = null;
			while((recipeEntry = zipStream.getNextEntry()) != null) {
				if(recipeEntry.getName().equals("recipe.rcpb")) {
					Scanner in = new Scanner(zipStream);
					in.useDelimiter("\\A");

					recipes = JsonUtil.fromJson(in.next());
					break;
				}
			}

			try {
				zipStream.close();
			} catch(Exception e) {
				Log.e(LOG_TAG, "Error closing initial zipstream: " + e, e);
			}

			if(recipes != null) {
				zipStream = new ZipInputStream(ctx.getContentResolver().openInputStream(zipLocation));

				ZipEntry imageEntry = null;
				while((imageEntry = zipStream.getNextEntry()) != null) {
					if(imageEntry.getName().matches("[0-9]+\\.jpg")) {
						String entryName = imageEntry.getName();
						int index = Integer.parseInt(entryName.substring(0, entryName.indexOf(".")));
						if(0 <= index && index < recipes.length && recipes[index] != null) {
							File saveAs = ImageUtil.getImageSaveFile(recipes[index]);
							OutputStream out = null;

							try {
								out = new BufferedOutputStream(new FileOutputStream(saveAs));

								int bufferSize = 8 * 1024;
								byte[] buffer = new byte[bufferSize];
								int count = 0;

								while((count = zipStream.read(buffer, 0, bufferSize)) != -1) {
									out.write(buffer, 0, count);
								}

								recipes[index].setImageUri(Uri.fromFile(saveAs).toString());
							} catch(Exception e) {
								Log.e(LOG_TAG, "Exception saving image file: " + index + " " + e, e);
							} finally {
								if(out != null) {
									try {
										out.close();
									} catch(Exception ex) {
									}
								}
							}

							ImageUtil.saveDownsampledImage(recipes[index], 1024);
						}
					}
				}
			}
		} catch(Exception e) {
			Log.e(LOG_TAG, "Error loading recipe: " + e, e);
		} finally {
			if(zipStream != null) {
				try {
					zipStream.close();
				} catch(Exception e) {
				}
			}
		}

		return recipes;
	}
}
