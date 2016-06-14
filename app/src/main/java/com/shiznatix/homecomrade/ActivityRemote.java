package com.shiznatix.homecomrade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityRemote extends ActivityAbstract {
    static private final String LOG_TAG = "mc_RemoteActivity";

    private EntityServer mEntityServer;
    private String mServerType;

    protected int mCommandsQueue = 0;
    protected Toast mToast;
    protected ImageView mOneTimeSelectShowsButton;
    protected ProgressDialog mProgressDialog;
    protected ArrayList<String> mOneTimeRandomShows = new ArrayList<>();

    private BroadcastReceiver mCommandStartReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG, "command started");

            mCommandsQueue++;

            Bundle bundle = intent.getExtras();

            toast(String.format(getResources().getString(R.string.sending_command), bundle.getString("command"), bundle.getString("url")));
        }
    };

    private BroadcastReceiver mCommandSuccessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG, "command success");

            mCommandsQueue--;

            String command = intent.getExtras().getString("command");

            if (!command.equals(ModelCommands.COMMAND_CURRENT_PLAYLIST)) {
                toast(String.format(getResources().getString(R.string.command_success), command));
            }
        }
    };

    private BroadcastReceiver mCommandResponseReceivedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG, "command response received");

            Bundle bundle = intent.getExtras();

            String type = bundle.getString("type");
            String data = bundle.getString("data");

            try {
                if (null != type && null != data) {
                    if (type.equals(ModelCommands.COMMAND_RANDOM_SETTINGS)) {
                        Intent randomSettingsActivityIntent = new Intent(ActivityRemote.this, ActivityRandomSettings.class);
                        randomSettingsActivityIntent.putExtra("showsData", data);
                        startActivity(randomSettingsActivityIntent);
                    }
                    else if (type.equals(ModelCommands.COMMAND_ALL_SHOWS)) {
                        try {
                            JSONArray showsArray = new JSONArray(data);
                            String[] allShowsList = new String[showsArray.length()];

                            for (int i = 0; i < showsArray.length(); i++) {
                                allShowsList[i] = showsArray.getString(i);
                            }

                            Arrays.sort(allShowsList);

                            selectOneTimeRandomShows(allShowsList);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else if (type.equals(ModelCommands.COMMAND_LAST_PLAYLIST) || type.equals(ModelCommands.COMMAND_RANDOM)) {
                        try {
                            JSONArray showsArray = new JSONArray(data);

                            if (showsArray.length() < 1) {
                                throw new Exception(getResources().getString(R.string.empty_playlist_error));
                            }

                            String[] toToastArray = new String[showsArray.length()];

                            for (int i = 0; i < showsArray.length(); i++) {
                                toToastArray[i] = showsArray.getString(i);
                            }

                            String toastString = TextUtils.join("\n", toToastArray);
                            toast(toastString);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else if (type.equals(ModelCommands.COMMAND_CURRENT_PLAYLIST)) {
                        try {
                            JSONArray showsArray = new JSONArray(data);

                            if (showsArray.length() < 1) {
                                throw new Exception(getResources().getString(R.string.empty_playlist_error));
                            }

                            DialogCurrentPlaylist dialogCurrentPlaylist = new DialogCurrentPlaylist(ActivityRemote.this, showsArray, mEntityServer);
                            dialogCurrentPlaylist.show();
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            catch (Exception e) {
                toast(e.getMessage());
            }

            if (null != mProgressDialog) {
                mProgressDialog.cancel();
            }
        }
    };

    private BroadcastReceiver mCommandFailureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG, "command failure");

            if (null != mProgressDialog) {
                mProgressDialog.cancel();
            }

            mCommandsQueue--;

            String command = intent.getExtras().getString("command");

            toast(String.format(getResources().getString(R.string.command_failed), command));
        }
    };

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);

        String url = getIntent().getExtras().getString("url");
        String serverType = getIntent().getExtras().getString("serverType");
        ArrayList<String> serverOptions = getIntent().getExtras().getStringArrayList("serverOptions");

        if (null == url) {
            Log.d(LOG_TAG, "Missing url for Remote Activity");
            finish();
        }

        TableServers tableServers = new TableServers(this);
        mEntityServer = tableServers.getServerByUrl(url);
        if (null == mEntityServer) {
            mEntityServer = new EntityServer(-1, url);
        }

        //set our options
        mEntityServer.options = serverOptions;

        ModelServerTypes modelServerTypes = new ModelServerTypes();
        if (modelServerTypes.isValidServerType(serverType)) {
            mServerType = serverType;
        }
        else {
            mServerType = ModelServerTypes.TOTEM_SERVER;
        }

        mProgressDialog = new ProgressDialog(this);
        mToast = Toast.makeText(this, null, Toast.LENGTH_LONG);
        TextView view = (TextView)mToast.getView().findViewById(android.R.id.message);

        if (null != view) {
            view.setGravity(Gravity.CENTER);
        }

        //main server title
        TextView connectedServerTitle = (TextView)findViewById(R.id.connectedServerTitle);
        connectedServerTitle.setText(mEntityServer.url);

        //ir remote options
        if (!mEntityServer.hasOption("irRemote")) {
            Button volumeDownTvButton = (Button)findViewById(R.id.buttonVolDownTv);
            Button volumeUpTvButton = (Button)findViewById(R.id.buttonVolUpTv);
            Button volumeMuteTvButton = (Button)findViewById(R.id.buttonMuteTv);
            Button sourceTvButton = (Button)findViewById(R.id.buttonSourceTv);

            volumeDownTvButton.setVisibility(View.GONE);
            volumeUpTvButton.setVisibility(View.GONE);
            volumeMuteTvButton.setVisibility(View.GONE);
            sourceTvButton.setVisibility(View.GONE);
        }

        setupLayoutForServerType();
    }

    @Override
    public void onResume() {
        super.onResume();

        //setup our broadcast receivers
        LocalBroadcastManager.getInstance(this).registerReceiver(mCommandStartReceiver, new IntentFilter(ModelConnection.COMMAND_START_KEY));
        LocalBroadcastManager.getInstance(this).registerReceiver(mCommandSuccessReceiver, new IntentFilter(ModelConnection.COMMAND_SUCCESS_KEY));
        LocalBroadcastManager.getInstance(this).registerReceiver(mCommandResponseReceivedReceiver, new IntentFilter(ModelConnection.COMMAND_RESPONSE_RECEIVED_KEY));
        LocalBroadcastManager.getInstance(this).registerReceiver(mCommandFailureReceiver, new IntentFilter(ModelConnection.COMMAND_FAILURE_KEY));
    }

    @Override
    public void onPause() {
        super.onPause();

        //remove our broadcast receivers
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCommandStartReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCommandSuccessReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCommandResponseReceivedReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCommandFailureReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ModelServerTypes modelServerTypes = new ModelServerTypes();

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(modelServerTypes.getMenuForRemote(mServerType), menu);

        for (int i = 0; i < menu.size(); i++) {
            if (!mEntityServer.hasOption("irRemote")) {
                if (menu.getItem(i).getItemId() == R.id.actionPowerTv) {
                    menu.getItem(i).setVisible(false);
                }
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionDisconnect:
                Intent intent = new Intent(this, ActivityConnect.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            case R.id.actionRandomSettings:
                sendBasicCommand(ModelCommands.COMMAND_RANDOM_SETTINGS);
                return true;
            case R.id.actionShowControls:
                sendBasicCommand(ModelCommands.COMMAND_TOGGLE_CONTROLS);
                return true;
            case R.id.actionLastPlaylist:
                sendBasicCommand(ModelCommands.COMMAND_LAST_PLAYLIST);
                return true;
            case R.id.actionQuit:
                sendBasicCommand(ModelCommands.COMMAND_QUIT);
                return true;
            case R.id.actionShowCurrentPlaylist:
                sendBasicCommand(ModelCommands.COMMAND_CURRENT_PLAYLIST);
                return true;
            case R.id.actionPowerTv:
                sendBasicCommand(ModelCommands.COMMAND_POWER_TV);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void buttonBasicCommandClicked(View view) {
        String command = null;

        switch (view.getId()) {
            case R.id.buttonPrevious:
                command = ModelCommands.COMMAND_PREVIOUS;
                break;
            case R.id.buttonNext:
                command = ModelCommands.COMMAND_NEXT;
                break;
            case R.id.buttonPlayPause:
                command = ModelCommands.COMMAND_PLAY_PAUSE;
                break;
            case R.id.buttonBack:
                command = ModelCommands.COMMAND_SEEK_BWD;
                break;
            case R.id.buttonForward:
                command = ModelCommands.COMMAND_SEEK_FWD;
                break;
            case R.id.buttonSkipTitleSequence:
                command = ModelCommands.COMMAND_SKIP_TITLE_SEQUENCE;
                break;
            case R.id.buttonVolDown:
                command = ModelCommands.COMMAND_VOLUME_DOWN;
                break;
            case R.id.buttonVolUp:
                command = ModelCommands.COMMAND_VOLUME_UP;
                break;
            case R.id.buttonVolDownTv:
                command = ModelCommands.COMMAND_VOLUME_DOWN_TV;
                break;
            case R.id.buttonVolUpTv:
                command = ModelCommands.COMMAND_VOLUME_UP_TV;
                break;
            case R.id.buttonMuteTv:
                command = ModelCommands.COMMAND_MUTE_TV;
                break;
            case R.id.buttonSourceTv:
                command = ModelCommands.COMMAND_SOURCE_TV;
                break;
            case R.id.buttonFullScreen:
                command = ModelCommands.COMMAND_FULLSCREEN;
                break;
            case R.id.buttonPlayFile:
                command = ModelCommands.COMMAND_CURRENT_PLAYLIST;
                break;
        }

        if (null != command) {
            sendBasicCommand(command);
        }
    }

    public void buttonRandomClick(View view) {
        if (view.getId() != R.id.buttonRandom) {
            return;
        }

        View titleView = getLayoutInflater().inflate(R.layout.alert_dialog_random_title, null);

        mOneTimeSelectShowsButton = (ImageView)titleView.findViewById(R.id.randomDialogSelectShowsButton);
        mOneTimeSelectShowsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG_TAG, "fetch and show all shows");

                mProgressDialog = new ProgressDialog(ActivityRemote.this);
                mProgressDialog.setTitle(R.string.remote_loading);
                mProgressDialog.setMessage(getString(R.string.remote_fetching_available_shows));
                mProgressDialog.show();

                sendBasicCommand(ModelCommands.COMMAND_ALL_SHOWS);
            }
        });

        final String[] playlistSizes = {"1", "2", "5", "10"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCustomTitle(titleView)
                .setSingleChoiceItems(playlistSizes, 2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                    }
                })
                .setPositiveButton(R.string.remote_enqueue, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();

                        sendRandomShowsCommand(playlistSizes[selectedPosition], ModelCommands.COMMAND_ENQUEUE);
                    }
                })
                .setNegativeButton(R.string.remote_play, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();

                        sendRandomShowsCommand(playlistSizes[selectedPosition], ModelCommands.COMMAND_PLAY);
                    }
                })
                .create()
                .show();
    }

    public void selectOneTimeRandomShows(final String[] allShowsList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.remote_select_shows)
                .setMultiChoiceItems(allShowsList, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            mOneTimeRandomShows.add(allShowsList[which]);
                        }
                        else if (mOneTimeRandomShows.contains(allShowsList[which])) {
                            mOneTimeRandomShows.remove(allShowsList[which]);
                        }
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mOneTimeRandomShows.isEmpty()) {
                            mOneTimeSelectShowsButton.setImageResource(android.R.drawable.btn_star_big_off);
                        }
                        else {
                            mOneTimeSelectShowsButton.setImageResource(android.R.drawable.btn_star_big_on);
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mOneTimeSelectShowsButton.setImageResource(android.R.drawable.btn_star_big_off);
                        mOneTimeRandomShows.clear();

                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    public void buttonBrowseClick(View view) {
        if (view.getId() != R.id.buttonBrowse) {
            return;
        }

        Intent browseActivityIntent = new Intent(ActivityRemote.this, ActivityBrowse.class);
        browseActivityIntent.putExtra("url", mEntityServer.url);
        startActivity(browseActivityIntent);
    }

    private void sendBasicCommand(String command) {
        try {
            ModelConnection modelConnection = new ModelConnection(this, mEntityServer.url);
            modelConnection.clearPostParams();
            modelConnection.setCommand(command);
            modelConnection.sendCommand();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPostCommand(String command, HashMap<String, String> postParams) {
        try {
            ModelConnection modelConnection = new ModelConnection(this, mEntityServer.url);
            modelConnection.setPostParams(postParams);
            modelConnection.setCommand(command);
            modelConnection.sendCommand();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendRandomShowsCommand(String playlistSize, String playMethod) {
        JSONArray allowedShows;

        if (!mOneTimeRandomShows.isEmpty()) {
            allowedShows = new JSONArray(mOneTimeRandomShows);
            mOneTimeRandomShows.clear();
        }
        else {
            TableRandomShows tableRandomShows = new TableRandomShows(ActivityRemote.this);
            allowedShows = new JSONArray(tableRandomShows.getAllShowTitles());
        }

        HashMap<String, String> postParams = new HashMap<>();
        postParams.put("playlistSize", playlistSize);
        postParams.put("allowedShows", allowedShows.toString());
        postParams.put("playMethod", playMethod);

        Log.i(LOG_TAG, "sending json: "+postParams);

        sendPostCommand(ModelCommands.COMMAND_RANDOM, postParams);
    }

    private void setupLayoutForServerType() {
        if (mServerType.equals(ModelServerTypes.OMXPLAYER_SERVER)) {
            Button skipTitleSequenceButton = (Button)findViewById(R.id.buttonSkipTitleSequence);
            Button fullScreenButton = (Button)findViewById(R.id.buttonFullScreen);

            skipTitleSequenceButton.setVisibility(View.GONE);
            fullScreenButton.setVisibility(View.GONE);
        }
    }

    private void toast(String text) {
        String toastText;

        if (0 > mCommandsQueue) {
            mCommandsQueue = 0;
        }

        if (null == text) {
            toastText = String.format(getResources().getString(R.string.remote_command_queue), mCommandsQueue);
        }
        else {
            toastText = String.format(getResources().getString(R.string.remote_command_status), mCommandsQueue, text);
        }

        mToast.setText(toastText);
        mToast.show();
    }
}