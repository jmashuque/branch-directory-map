package com.example.branchdirectorymap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogUtils {

    public void showYesNoDialog(Context context, String title, String message,
                                DialogInterface.OnClickListener onYesClickListener,
                                DialogInterface.OnClickListener onNoClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);

        builder.setPositiveButton(R.string.yes, (dialog, id) -> {
            if (onYesClickListener != null) {
                onYesClickListener.onClick(dialog, id);
            }
        });

        builder.setNegativeButton(R.string.no, (dialog, id) -> {
            if (onNoClickListener != null) {
                onNoClickListener.onClick(dialog, id);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showOkDialog(Context context, String title, String message, DialogInterface.OnClickListener onOkClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);

        builder.setPositiveButton(R.string.ok, (dialog, id) -> {
            if (onOkClickListener != null) {
                onOkClickListener.onClick(dialog, id);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}