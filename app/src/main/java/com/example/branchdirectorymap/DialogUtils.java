package com.example.branchdirectorymap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogUtils {

    private Context context;

    public DialogUtils(Context context) {
        this.context = context;
    }

    public void showYesNoDialog(String title, String message,
                                DialogInterface.OnClickListener onYesClickListener,
                                DialogInterface.OnClickListener onNoClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (onYesClickListener != null) {
                    onYesClickListener.onClick(dialog, id);
                }
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (onNoClickListener != null) {
                    onNoClickListener.onClick(dialog, id);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showOkDialog(String title, String message, DialogInterface.OnClickListener onOkClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (onOkClickListener != null) {
                    onOkClickListener.onClick(dialog, id);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}