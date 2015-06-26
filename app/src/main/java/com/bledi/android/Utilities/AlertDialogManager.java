package com.bledi.android.Utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;


public class AlertDialogManager 
{
	public void showAlertDialog(Context context, String title, String message, Boolean status) 
	{
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();

		// Setting Dialog Title
		alertDialog.setTitle(title);

		// Setting Dialog Message
		alertDialog.setMessage(message);

		// Setting OK Button
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}
}


