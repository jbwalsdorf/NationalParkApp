package com.jeffwalsdorf.nationalparksapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ParkNameAdapter extends CursorAdapter {

    public ParkNameAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }


    public static class ViewHolder {
        public final TextView parkName;
        public final ImageView parkImage;

        public ViewHolder(View view) {
            parkName = (TextView) view.findViewById(R.id.list_item_place_name);
            parkImage = (ImageView) view.findViewById(R.id.list_item_park_image);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int layoutId = R.layout.list_item_parks;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String parkName = cursor.getString(ParkNameFragment.COL_PARK_DATA_NAME);
        viewHolder.parkName.setText(parkName);

        Uri uri = Uri.parse(cursor.getString(ParkNameFragment.COL_PARK_DATA_URL));
        Picasso.with(context).load(uri).placeholder(R.drawable.usfs).into(viewHolder.parkImage);

    }


}
