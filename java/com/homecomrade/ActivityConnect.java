package com.homecomrade;

import java.util.ArrayList;

import org.json.JSONObject;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ActivityConnect extends ActivityAbstract {
    static private final String LOG_TAG = "mc_ActivityConnect";

    private ProgressDialog mConnectingDialog;

    private BroadcastReceiver mCommandStartReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG, "command started");

            Bundle bundle = intent.getExtras();

            mConnectingDialog = new ProgressDialog(ActivityConnect.this);
            mConnectingDialog.setTitle(R.string.connections_connecting);
            mConnectingDialog.setMessage(String.format(getResources().getString(R.string.connections_connecting_to), bundle.getString("url")));
            mConnectingDialog.show();
        }
    };

    private BroadcastReceiver mCommandResponseReceivedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG, "command success");
            mConnectingDialog.dismiss();

            Bundle bundle = intent.getExtras();
            String url = bundle.getString("url");
            String data = bundle.getString("data");

            Log.i(LOG_TAG, "data: "+data);

            if (null != url && null != data) {
                String serverType = null;
                ArrayList<String> serverOptions = new ArrayList<String>();

                try {
                    JSONObject jsonObject = new JSONObject(data);
                    serverType = jsonObject.getString("serverType");

                    ModelServerTypes modelServerTypes = new ModelServerTypes();

                    if (!modelServerTypes.isValidServerType(serverType)) {
                        throw new Exception("Invalid server type");
                    }

                    try {
                        boolean irRemote = jsonObject.getBoolean("irRemote");

                        if (irRemote) {
                            serverOptions.add("irRemote");
                        }
                    }
                    catch (Exception e) {
                    }

                    Log.i(LOG_TAG, "serverType: "+serverType);
                }
                catch (Exception e) {
                    //default to totem
                    serverType = ModelServerTypes.TOTEM_SERVER;
                }

                Intent remoteActivityIntent = new Intent(ActivityConnect.this, ActivityRemote.class);
                remoteActivityIntent.putExtra("url", url);
                remoteActivityIntent.putExtra("serverType", serverType);
                remoteActivityIntent.putStringArrayListExtra("serverOptions", serverOptions);
                startActivity(remoteActivityIntent);
            }
        }
    };

    private BroadcastReceiver mCommandFailureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG, "command failure");
            mConnectingDialog.dismiss();

            Bundle bundle = intent.getExtras();
            String error = bundle.getString("error");

            if (null != error) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ActivityConnect.this);
                dialogBuilder.setTitle(R.string.connections_connection_failed)
                        .setMessage(String.format(getResources().getString(R.string.connections_could_not_connect_to), error))
                        .setNeutralButton(R.string.ok, null)
                        .show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        //get the saved servers
        final TableServers tableServers = new TableServers(this);
        final ArrayList<EntityServer> savedEntityServers = tableServers.getAllServers();

        //our views
        final EditText serverEditText = (EditText)findViewById(R.id.serverEditText);
        Button connectButton = (Button)findViewById(R.id.connectButton);
        final ListView serverListView = (ListView)findViewById(R.id.serverListView);

        //connect button functions
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(serverEditText.getWindowToken(), 0);

                final String url = serverEditText.getText().toString();

                EntityServer entityServer = tableServers.getServerByUrl(url);

                if (null == entityServer) {
                    Log.i(LOG_TAG, "server does not exist, ask if we would like it to be saved");

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ActivityConnect.this);
                    dialogBuilder.setMessage(R.string.connections_save_or_connect)
                            .setTitle(url)
                            .setPositiveButton(R.string.connections_save, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    tableServers.addServer(url);

                                    EntityServer newEntityServer = tableServers.getServerByUrl(url);

                                    AdapterServerCell adapter = ((AdapterServerCell)serverListView.getAdapter());
                                    adapter.add(newEntityServer);
                                    adapter.notifyDataSetChanged();

                                    connect(tableServers.getServerByUrl(url));
                                }
                            })
                            .setNegativeButton(R.string.connections_connect, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    connect(new EntityServer(-1, url));
                                }
                            })
                            .show();
                }
                else {
                    connect(entityServer);
                }
            }
        });

        //List view functions
        serverListView.setAdapter(new AdapterServerCell(this, R.id.serverListView, savedEntityServers));

        serverListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(serverEditText.getWindowToken(), 0);

                final EntityServer entityServer = savedEntityServers.get(position);

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ActivityConnect.this);
                dialogBuilder.setMessage(R.string.connections_connect_or_delete)
                        .setTitle(entityServer.url)
                        .setPositiveButton(R.string.connections_connect, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                connect(entityServer);
                            }
                        })
                        .setNegativeButton(R.string.connections_delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                tableServers.deleteServer(entityServer);

                                AdapterServerCell adapter = ((AdapterServerCell)serverListView.getAdapter());
                                adapter.remove(entityServer);
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        //setup our broadcast receivers
        LocalBroadcastManager.getInstance(this).registerReceiver(mCommandStartReceiver, new IntentFilter(ModelConnection.COMMAND_START_KEY));
        LocalBroadcastManager.getInstance(this).registerReceiver(mCommandResponseReceivedReceiver, new IntentFilter(ModelConnection.COMMAND_RESPONSE_RECEIVED_KEY));
        LocalBroadcastManager.getInstance(this).registerReceiver(mCommandFailureReceiver, new IntentFilter(ModelConnection.COMMAND_FAILURE_KEY));
    }

    @Override
    public void onPause() {
        super.onPause();

        //remove our broadcast receivers
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCommandStartReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCommandResponseReceivedReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCommandFailureReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ModelServerTypes modelServerTypes = new ModelServerTypes();

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.connect, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionChooseColor:

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void connect(EntityServer entityServer) {
        ModelConnection modelConnection = new ModelConnection(this, entityServer.url);

        try {
            modelConnection.setCommand(ModelCommands.COMMAND_HEARTBEAT);
            modelConnection.clearPostParams();
            modelConnection.sendCommand();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}