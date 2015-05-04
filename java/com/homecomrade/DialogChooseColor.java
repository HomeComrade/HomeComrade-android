package com.homecomrade;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

public class DialogChooseColor {
    static private final String LOG_TAG = "mc_DialogChooseColor";

    protected AlertDialog mAlertDialog;
    protected int mSelectedIndex = -1;

    public DialogChooseColor(final Context context) {
        CharSequence[] entriesArray = new CharSequence[5];
        entriesArray[0] = "Red";
        entriesArray[1] = "Green";
        entriesArray[2] = "Blue";
        entriesArray[3] = "White";
        entriesArray[4] = "Off";

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(R.string.current_playlist)
                .setSingleChoiceItems(entriesArray, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSelectedIndex = which;
                    }
                })
                .setCancelable(false)
                .setPositiveButton(R.string.play, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String selectedIndex = Integer.toString(mSelectedIndex);

                        Log.i(LOG_TAG, "selected color position: " + selectedIndex);

                        /*
                        try {
                            ConnectionModel connectionModel = new ConnectionModel(context, server.url);
                            connectionModel.clearPostParams();
                            connectionModel.setPostParam("position", selectedIndex);
                            connectionModel.setCommand(CommandsModel.COMMAND_PLAY_AT);
                            connectionModel.sendCommand();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        */

                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, null);

        mAlertDialog = alertDialogBuilder.create();
    }

    public void show() {
        if (null != mAlertDialog) {
            mAlertDialog.show();
        }
    }
}