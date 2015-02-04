package com.bradon.justinprather.chattytimerapp;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by justinprather on 2015-01-05.
 */
public class TimeObectAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<TimeObject> mTimeObject;
    private SparseBooleanArray mSelectedItemsIds;

    public TimeObectAdapter(Context context, List<TimeObject> timeObjectList) {
        mInflater = LayoutInflater.from(context);
        mTimeObject = timeObjectList;
        mSelectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public int getCount() {
        return mTimeObject.size();
    }

    @Override
    public Object getItem(int position) {
        return mTimeObject.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        long millis;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.time_element_layout, parent, false);
            holder = new ViewHolder();

            holder.hours = (TextView) view.findViewById(R.id.timeHours);
            holder.minutes = (TextView) view.findViewById(R.id.timeMinutes);
            holder.seconds = (TextView) view.findViewById(R.id.timeSeconds);
            holder.comment = (TextView) view.findViewById(R.id.timeComment);

            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        TimeObject currentTimeObject = mTimeObject.get(position);
        millis = currentTimeObject.getTimeMillis();

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        holder.hours.setText(String.format("%02d", hours));
        holder.minutes.setText(String.format("%02d", minutes));
        holder.seconds.setText(String.format("%02d", seconds));
        holder.comment.setText( currentTimeObject.getTimeComment() );

        view.setBackgroundColor(mSelectedItemsIds.get(position) ? 0x9934B5E4
                : Color.TRANSPARENT);

        return view;
    }
        private class ViewHolder {
            public TextView hours, minutes, seconds, comment;
        }
    public void add(TimeObject object){
        mTimeObject.add(object);
    }

    public void remove(TimeObject object) {
        // super.remove(object);
        mTimeObject.remove(object);
        notifyDataSetChanged();
    }

    public List<TimeObject> getLaptops() {
        return mTimeObject;
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);

        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }
}

