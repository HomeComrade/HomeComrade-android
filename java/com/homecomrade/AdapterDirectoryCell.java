package com.homecomrade;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class AdapterDirectoryCell extends ArrayAdapter<EntityDirectoryListing> implements SectionIndexer {
    static private final String LOG_TAG = "mc_DirectoryCellAdapter";

    private final Context mContext;
    private ArrayList<EntityDirectoryListing> mEntityDirectoryListings = new ArrayList<>();

    private String[] mSections;

    public AdapterDirectoryCell(Context context, int resourceId, ArrayList<EntityDirectoryListing> entityDirectoryListings) {
        super(context, resourceId, entityDirectoryListings);

        Log.i(LOG_TAG, "new adapter");

        mEntityDirectoryListings = entityDirectoryListings;
        mContext = context;

        //setup the alpaindexer
        mSections = new String[0];
    }

    @Override
    public void notifyDataSetChanged() {
        ArrayList<String> alphabetIndexer = new ArrayList<String>();

        int size = mEntityDirectoryListings.size();

        for (int i = 0; i < size; i++) {
            EntityDirectoryListing entityDirectoryListing = mEntityDirectoryListings.get(i);

            //get the first letter of the store
            String firstLetter =  entityDirectoryListing.name.substring(0, 1);

            //convert to uppercase otherwise lowercase a -z will be sorted after upper A-Z
            firstLetter = firstLetter.toUpperCase(Locale.getDefault());

            if (!alphabetIndexer.contains(firstLetter)) {
                alphabetIndexer.add(firstLetter);
            }
        }

        mSections = new String[alphabetIndexer.size()];

        for (int i = 0; i < alphabetIndexer.size(); i++) {
            mSections[i] = alphabetIndexer.get(i);
        }

        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = View.inflate(mContext, R.layout.cell_directory_listing, null);
        rowView.setBackgroundColor(Color.TRANSPARENT);

        EntityDirectoryListing entityDirectoryListing = mEntityDirectoryListings.get(position);

        TextView listingNameTextView = (TextView)rowView.findViewById(R.id.directoryListingName);
        listingNameTextView.setText(entityDirectoryListing.name);

        ImageView listingImageView = (ImageView)rowView.findViewById(R.id.directoryListingImage);

        ImageView checkImage = (ImageView)rowView.findViewById(R.id.directoryListingCheckImage);
        checkImage.setVisibility(View.GONE);

        if (entityDirectoryListing.isDir && entityDirectoryListing.name.equals(getContext().getResources().getString(R.string.browse_back))) {
            listingImageView.setImageResource(android.R.drawable.ic_menu_revert);

            listingNameTextView.setTextColor(Color.GRAY);
            listingNameTextView.setTypeface(null, Typeface.BOLD_ITALIC);
        }
        else if (entityDirectoryListing.isDir) {
            listingImageView.setImageResource(android.R.drawable.ic_menu_add);

            listingNameTextView.setTextColor(Color.BLACK);
            listingNameTextView.setTypeface(null, Typeface.BOLD);
        }
        else if (entityDirectoryListing.isPlayable) {
            listingImageView.setImageResource(android.R.drawable.ic_media_play);

            listingNameTextView.setTypeface(null, Typeface.NORMAL);

            checkImage.setVisibility(View.VISIBLE);

            if (entityDirectoryListing.isSelected) {
                checkImage.setImageResource(android.R.drawable.checkbox_on_background);
                listingNameTextView.setTextColor(Color.BLACK);
                rowView.setBackgroundColor(Color.LTGRAY);
            }
            else {
                checkImage.setImageResource(android.R.drawable.checkbox_off_background);
                listingNameTextView.setTextColor(Color.DKGRAY);
                rowView.setBackgroundColor(Color.TRANSPARENT);
            }
        }
        else {
            listingImageView.setVisibility(View.INVISIBLE);

            listingNameTextView.setTextColor(Color.GRAY);
            listingNameTextView.setTypeface(null, Typeface.NORMAL);
        }

        return rowView;
    }

    @Override
    public int getPositionForSection(int section) {
        //return mAlphaIndexer.get(mSections[section]);

        for (int i = 0; i < mEntityDirectoryListings.size(); i++) {
            EntityDirectoryListing item = mEntityDirectoryListings.get(i);

            String firstLetter =  item.name.substring(0, 1);
            if (firstLetter.equals(mSections[section])) {
                return i;
            }
        }

        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public Object[] getSections() {
        return mSections;
    }
}