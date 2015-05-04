package com.homecomrade;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

public class DialogCurrentPlaylist {
    static private final String LOG_TAG = "mc_CurrentDialog";

    protected AlertDialog mAlertDialog;
    protected int mSelectedIndex = -1;

    public DialogCurrentPlaylist(final Context context, final JSONArray showsArray, final EntityServer entityServer) {
        try {
            CharSequence[] entriesArray = new CharSequence[showsArray.length()];

            for (int i = 0; i < showsArray.length(); i++) {
                entriesArray[i] = showsArray.getString(i);
            }

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

                            if ((showsArray.length() - 1) < mSelectedIndex || 0 > mSelectedIndex) {
                                return;
                            }

                            Log.i(LOG_TAG, "selected show position: " + selectedIndex);

                            try {
                                ModelConnection modelConnection = new ModelConnection(context, entityServer.url);
                                modelConnection.clearPostParams();
                                modelConnection.setPostParam("position", selectedIndex);
                                modelConnection.setCommand(ModelCommands.COMMAND_PLAY_AT);
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
