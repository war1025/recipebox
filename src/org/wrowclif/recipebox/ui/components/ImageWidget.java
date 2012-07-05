package org.wrowclif.recipebox.ui.components;

import org.wrowclif.recipebox.R;
import org.wrowclif.recipebox.Recipe;

import static org.wrowclif.recipebox.util.ConstantInitializer.assignId;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageWidget {
	private static final String LOG_TAG = "Recipebox ImageWidget";

	private static final int EDIT_IMAGE_REQUEST = assignId();
	private static final int DELETE_IMAGE_DIALOG = assignId();

	private Recipe recipe;
	private Activity context;
	private View view;
	private ImageView image;
	private Button editButton;
	private Button deleteButton;
	private boolean editing;

	private Uri imageSaveUri;

	public ImageWidget(Recipe recipe, Activity context, ViewStub stub) {
		this.recipe = recipe;
		this.context = context;
		this.editing = false;

		stub.setLayoutResource(R.layout.image_widget);
		this.view = stub.inflate();
		this.editButton = (Button) view.findViewById(R.id.edit_button);
		this.deleteButton = (Button) view.findViewById(R.id.delete_button);
		this.image = (ImageView) view.findViewById(R.id.recipe_image);
		this.image.setVisibility(View.GONE);

		this.editButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				Uri outputUri = getImageOutputUri();
				ImageWidget.this.imageSaveUri = outputUri;
				intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
				ImageWidget.this.context.startActivityForResult(intent, EDIT_IMAGE_REQUEST);
			}
		});

		this.deleteButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ImageWidget.this.context.showDialog(DELETE_IMAGE_DIALOG);
			}
		});

	}

	public void setEditing(boolean editing) {
		this.editing = editing;
		if(editing) {
			editButton.setVisibility(View.VISIBLE);
			if(recipe.getImageUri().length() > 0) {
				deleteButton.setVisibility(View.VISIBLE);
			} else {
				deleteButton.setVisibility(View.GONE);
			}
		} else {
			editButton.setVisibility(View.GONE);
			deleteButton.setVisibility(View.GONE);
		}
	}

	public View getView() {
		refreshImage();
		return this.view;
	}

	public void refreshImage() {
		String uri = recipe.getImageUri();
		if(uri.length() > 0) {
			new LoadImageTask().execute(recipe.getImageUri());
		} else {
			image.setImageBitmap(null);
			image.setVisibility(View.GONE);
		}
		setEditing(editing);
	}

	public Dialog createDialog(int id) {
		Dialog dialog = null;
		if(id == DELETE_IMAGE_DIALOG) {
			EnterTextDialog etd = new EnterTextDialog(context, R.layout.show_text_dialog);

			etd.setTitle("Delete Image");
			etd.setEditText("Are you sure you want to delete this recipe's image?");

			etd.setOkButtonText("Delete");

			etd.setOkListener(new OnClickListener() {
				public void onClick(View v) {
					recipe.setImageUri("");
					refreshImage();
				}
			});

			dialog = etd;
		}
		return dialog;
	}

	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		boolean handled = false;
		if(requestCode == EDIT_IMAGE_REQUEST) {
			handled = true;
			if(resultCode == Activity.RESULT_OK) {
				if(imageSaveUri != null) {
					recipe.setImageUri(imageSaveUri.toString());
					refreshImage();
					imageSaveUri = null;
				}
			}
		}
		return handled;
	}

	private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
		protected Bitmap doInBackground(String... imagePath) {
			File f = new File(Uri.parse(imagePath[0]).getSchemeSpecificPart());
			Log.d(LOG_TAG, "File: " + f.getPath() + " Exists: " + f.exists());
			Log.d(LOG_TAG, "Image uri: " + imagePath[0]);
			return BitmapFactory.decodeFile(f.getPath());
		}

		protected void onPostExecute(Bitmap result) {
			if(result == null) {
				Toast.makeText(context, "Image could not be loaded.", Toast.LENGTH_SHORT).show();
			} else {
				image.setImageBitmap(result);
				image.setVisibility(View.VISIBLE);
			}
		}
	}

	protected LayoutInflater getLayoutInflater() {
		return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	private Uri getImageOutputUri() {
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

		return Uri.fromFile(imageFile);
	}

	public void saveInstanceState(Bundle bundle) {
		bundle.putParcelable("saveUri", this.imageSaveUri);
	}

	public void restoreInstanceState(Bundle bundle) {
		Object saved = bundle.getParcelable("saveUri");
		if(saved != null) {
			this.imageSaveUri = (Uri) saved;
		}
	}
}
