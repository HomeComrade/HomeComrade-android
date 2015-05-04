package com.homecomrade;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class DialogInsertIntoPlaylistAt {
    static private final String LOG_TAG = "mc_InsertIntoDialog";

    protected AlertDialog mAlertDialog;
    protected int mSelectedIndex = -1;

    public DialogInsertIntoPlaylistAt(final Context context, String data, final ArrayList<String> selectedFiles, final EntityServer entityServer) {
        try {
            JSONArray showsArray = new JSONArray(data);

            final int showsArrayLength = showsArray.length() + 1;

            CharSequence[] entriesArray = new CharSequence[showsArrayLength];

            entriesArray[0] = context.getResources().getString(R.string.browse_start_of_playlist);

            for (int i = 0; i < showsArray.length(); i++) {
                entriesArray[i + 1] = showsArray.getString(i);
            }

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setTitle(R.string.browse_insert_after)
                    .setSingleChoiceItems(entriesArray, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSelectedIndex = which;
                        }
                    })
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            String selectedIndex = Integer.toString(mSelectedIndex);

                            if ((showsArrayLength - 1) < mSelectedIndex || 0 > mSelectedIndex) {
                                return;
                            }

                            Log.i(LOG_TAG, "selected show position: " + selectedIndex);

                            try {
                                JSONArray filePaths = new JSONArray(selectedFiles);

                                ModelConnection modelConnection = new ModelConnection(context, entityServer.url);
                                modelConnection.clearPostParams();
                                modelConnection.setPostParam("position", selectedIndex);
                                modelConnection.setPostParam("filePaths", filePaths.toString());
                                modelConnection.setCommand(ModelCommands.COMMAND_INSERT_AT);
                                modelConnection.sendCommand();
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }

                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null);

            mAlertDialog = alertDialogBuilder.create();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void show() {
        if (null != mAlertDialog) {
            mAlertDialog.show();
        }
    }
}