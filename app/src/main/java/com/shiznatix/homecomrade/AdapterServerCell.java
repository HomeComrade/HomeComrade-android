package com.shiznatix.homecomrade;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class AdapterServerCell extends ArrayAdapter<EntityServer> {
    private final Context mContext;
    private ArrayList<EntityServer> mEntityServers = new ArrayList<>();

    public AdapterServerCell(Context context, int resourceId, ArrayList<EntityServer> entityServers) {
        super(context, resourceId, entityServers);

        mEntityServers = entityServers;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = View.inflate(mContext, R.layout.cell_server, null);

        EntityServer entityServer = mEntityServers.get(position);

        TextView serverUrlTextView = (TextView)rowView.findViewById(R.id.serverUrlTextView);
        serverUrlTextView.setText(entityServer.url);

        return rowView;
    }
}
