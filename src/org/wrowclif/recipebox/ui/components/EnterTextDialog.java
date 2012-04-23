package org.wrowclif.recipebox.ui.components;

import org.wrowclif.recipebox.R;

import org.wrowclif.recipebox.AppData;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;

public class EnterTextDialog extends Dialog {

	private TextView titleView;
	private EditText textView;
	private Button okButton;
	private Button cancelButton;

	private View.OnClickListener okClick;
	private View.OnClickListener cancelClick;

	public EnterTextDialog(Context context) {
		this(context, R.layout.enter_text_dialog);
	}

	public EnterTextDialog(Context context, int layout) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(layout);

		getWindow().setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		setCancelable(true);

		AppData appData = AppData.getSingleton();

		titleView = (TextView) findViewById(R.id.title);
		textView = (EditText) findViewById(R.id.text_edit);
		okButton = (Button) findViewById(R.id.ok_button);
		cancelButton = (Button) findViewById(R.id.cancel_button);

		appData.useHeadingFont(titleView);
		appData.useTextFont(textView);
		appData.useHeadingFont(okButton);
		appData.useHeadingFont(cancelButton);

		okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(okClick != null) {
					okClick.onClick(v);
				}
				EnterTextDialog.this.dismiss();
			}
		});

		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(cancelClick != null) {
					cancelClick.onClick(v);
				}
				EnterTextDialog.this.dismiss();
			}
		});

	}

	public void setTitle(String title) {
		titleView.setText(title);
	}

	public void setEditText(String text) {
		textView.setText(text);
	}

	public EditText getEditView() {
		return textView;
	}

	public String getEditText() {
		return textView.getText().toString();
	}

	public void setOkButtonText(String buttonText) {
		okButton.setText(buttonText);
	}

	public void setCancelButtonText(String buttonText) {
		cancelButton.setText(buttonText);
	}

	public void setOkListener(View.OnClickListener listener) {
		okClick = listener;
	}

	public void setCancelListener(View.OnClickListener listener) {
		cancelClick = listener;
	}
}
