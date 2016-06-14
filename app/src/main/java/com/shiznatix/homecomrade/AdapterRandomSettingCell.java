package com.shiznatix.homecomrade;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class AdapterRandomSettingCell extends BaseExpandableListAdapter {
    private Context mContext;
    private ArrayList<EntityRandomShowCategory> mShowCategories;

    public AdapterRandomSettingCell(Context context, ArrayList<EntityRandomShowCategory> showCategories) {
        mContext = context;
        mShowCategories = showCategories;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mShowCategories.get(groupPosition).shows.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, final ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = View.inflate(mContext, R.layout.cell_random_settings_show, null);
        }
        else {
            view = convertView;
        }

        EntityRandomShow entityRandomShow = mShowCategories.get(groupPosition).shows.get(childPosition);

        TextView randomSettingShow = (TextView)view.findViewById(R.id.randomSettingShow);
        randomSettingShow.setText(entityRandomShow.title);

        ImageView randomSettingShowCheck = (ImageView)view.findViewById(R.id.randomSettingShowCheck);
        TableRandomShows tableRandomShows = new TableRandomShows(mContext);

        if (null == tableRandomShows.getShowByTitle(entityRandomShow.title)) {
            randomSettingShowCheck.setImageResource(android.R.drawable.checkbox_off_background);
            randomSettingShow.setTextColor(Color.GRAY);
        }
        else {
            randomSettingShowCheck.setImageResource(android.R.drawable.checkbox_on_background);
            randomSettingShow.setTextColor(Color.BLACK);
        }

        return view;
    }

    @Override
    public int getChildrenCount(int position) {
        return mShowCategories.get(position).shows.size();
    }

    @Override
    public Object getGroup(int position) {
        return mShowCategories.get(position).title;
    }

    @Override
    public int getGroupCount() {
        return mShowCategories.size();
    }

    @Override
    public long getGroupId(int position) {
        return position;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = View.inflate(mContext, R.layout.cell_random_settings_category, null);
        }
        else {
            view = convertView;
        }

        EntityRandomShowCategory entityRandomShowCategory = mShowCategories.get(groupPosition);

        TextView textLabel = (TextView)view.findViewById(R.id.randomSettingCategory);
        textLabel.setText(entityRandomShowCategory.title);

        return view;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}