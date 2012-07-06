package org.wrowclif.recipebox.ui.components;

import org.wrowclif.recipebox.R;
import org.wrowclif.recipebox.AppData;
import org.wrowclif.recipebox.Recipe;

import org.wrowclif.recipebox.util.ImageUtil;

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
import android.widget.TextView;
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
	private TextView noImageLabel;
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

		this.noImageLabel = (TextView) view.findViewById(R.id.image_label);
		AppData.getSingleton().useTextFont(this.noImageLabel);

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

		this.image.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(!ImageWidget.this.editing) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.parse(ImageWidget.this.recipe.getImageUri()), "image/*");
					ImageWidget.this.context.startActivity(intent);
				}
			}
		});

	}

	public void setEditing(boolean editing) {
		this.editing = editing;
		if(editing) {
			editButton.setVisibility(View.VISIBLE);
			if(recipe.getImageUri().length() > 0) {
				deleteButton.setVisibility(View.VISIBLE);
				noImageLabel.setVisibility(View.GONE);
			} else {
				deleteButton.setVisibility(View.GONE);
				noImageLabel.setVisibility(View.VISIBLE);
			}
		} else {
			editButton.setVisibility(View.GONE);
			deleteButton.setVisibility(View.GONE);
			noImageLabel.setVisibility(View.GONE);
		}
	}

	public View getView() {
		refreshImage();
		return this.view;
	}

	public void refreshImage() {
		String uri = recipe.getImageUri();
		if(uri.length() > 0) {
			new LoadImageTask().execute(recipe);
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
					File toDelete = ImageUtil.getImageFile(recipe);
					if(toDelete != null && toDelete.exists()) {
						toDelete.delete();
					}
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
					ImageUtil.saveDownsampledImage(recipe, 1024);
					refreshImage();
					imageSaveUri = null;
				}
			}
		}
		return handled;
	}

	private class LoadImageTask extends AsyncTask<Recipe, Void, Bitmap> {
		protected Bitmap doInBackground(Recipe... recipe) {
			return ImageUtil.loadImageAtWidth(recipe[0], context.getWindowManager().getDefaultDisplay().getWidth());
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
		File imageFile = ImageUtil.getImageSaveFile(recipe);
		Uri uri = null;
		if(imageFile != null) {
			uri = Uri.fromFile(imageFile);
		}
		return uri;
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
