package com.example.avi.Snapshot;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.avi.R;

import java.util.ArrayList;

public class listviewAdapter extends BaseAdapter {

    public ArrayList<Snapshot> snapshotList;
    Activity activity;

    public listviewAdapter(Activity activity, ArrayList<Snapshot> list) {
        super();
        this.activity = activity;
        this.snapshotList = list;
    }

    @Override
    public int getCount() {
        return snapshotList.size();
    }

    @Override
    public Object getItem(int position) {
        return snapshotList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        TextView name;
        TextView elevation;
        TextView aspect;
        TextView rating;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        LayoutInflater inflater = activity.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_row, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.snapshot_name);
            holder.elevation = (TextView) convertView.findViewById(R.id.snapshot_elevation);
            holder.aspect = (TextView) convertView.findViewById(R.id.snapshot_aspect);
            holder.rating = (TextView) convertView.findViewById(R.id.snapshot_rating);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Snapshot item = snapshotList.get(position);
        holder.name.setText(item.getName());
        holder.elevation.setText(item.getElevation());
        holder.aspect.setText(item.getAspect());
        holder.rating.setText(item.getRating());

        return convertView;
    }
}
