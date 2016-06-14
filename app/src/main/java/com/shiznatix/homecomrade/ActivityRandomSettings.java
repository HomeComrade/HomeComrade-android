package com.shiznatix.homecomrade;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ActivityRandomSettings extends ExpandableListActivity {
    static private final String LOG_TAG = "mc_RandomSettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_settings);

        ArrayList<EntityRandomShowCategory> showCategories = new ArrayList<>();

        try {
            String showsData = getIntent().getExtras().getString("showsData");

            if (null == showsData) {
                Log.d(LOG_TAG, "Missing showsData for Random Settings Activity");
                finish();
            }

            JSONObject jsonObject = new JSONObject(showsData);
            JSONArray categories = jsonObject.names();
            TableRandomShows tableRandomShows = new TableRandomShows(this);

            Log.d(LOG_TAG, "show categories: "+categories);

            for (int i = 0; i < categories.length(); i++) {
                String categoryName = categories.getString(i);
                JSONArray showsJsonArray = jsonObject.getJSONArray(categoryName);
                ArrayList<EntityRandomShow> shows = new ArrayList<>();

                //add the shows to the array
                for (int y = 0; y < showsJsonArray.length(); y++) {
                    String showTitle = showsJsonArray.getString(y);

                    EntityRandomShow entityRandomShow = tableRandomShows.getShowByTitle(showTitle);

                    if (null == entityRandomShow) {
                        shows.add(new EntityRandomShow(-1, showTitle));
                    }
                    else {
                        shows.add(entityRandomShow);
                    }
                }

                Collections.sort(shows, new Comparator<EntityRandomShow>() {
                    public int compare(EntityRandomShow s1, EntityRandomShow s2) {
                        return s1.title.compareToIgnoreCase(s2.title);
                    }
                });

                //add the shows to the category / map
                showCategories.add(new EntityRandomShowCategory(categoryName, shows));
            }
        }
        catch (JSONException e) {
            Log.d(LOG_TAG, "Invalid JSON for showsData");
            finish();
        }

        Collections.sort(showCategories, new Comparator<EntityRandomShowCategory>() {
            public int compare(EntityRandomShowCategory c1, EntityRandomShowCategory c2) {
                return c1.title.compareToIgnoreCase(c2.title);
            }
        });

        AdapterRandomSettingCell listAdapter = new AdapterRandomSettingCell(this, showCategories);
        setListAdapter(listAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.random_settings, menu);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
        String showTitle = ((TextView)view.findViewById(R.id.randomSettingShow)).getText().toString();

        TableRandomShows tableRandomShows = new TableRandomShows(this);
        EntityRandomShow entityRandomShow = tableRandomShows.getShowByTitle(showTitle);

        TextView randomSettingShow = (TextView)view.findViewById(R.id.randomSettingShow);
        ImageView checkboxImage = (ImageView)view.findViewById(R.id.randomSettingShowCheck);

        if (null == entityRandomShow) {
            tableRandomShows.addShow(showTitle);
            checkboxImage.setImageResource(android.R.drawable.checkbox_on_background);
            randomSettingShow.setTextColor(Color.BLACK);
        }
        else {
            tableRandomShows.deleteShow(entityRandomShow);
            checkboxImage.setImageResource(android.R.drawable.checkbox_off_background);
            randomSettingShow.setTextColor(Color.GRAY);
        }

        return true;
    }
}
