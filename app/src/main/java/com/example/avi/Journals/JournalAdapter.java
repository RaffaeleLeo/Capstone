package com.example.avi.Journals;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.avi.R;

import java.util.ArrayList;
import java.util.HashMap;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.JournalViewHolder> {

    private ArrayList<Journal> mDataset;
    private JournalAdapter.JournalViewHolder.OnJournalListener onJournalListener;

    public static class JournalViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ViewGroup view;
        public TextView nameView;
        public TextView locationView;
        public TextView frequencyView;
        public ImageView typeView;
        JournalAdapter.JournalViewHolder.OnJournalListener onJournalListener;

        public JournalViewHolder(View v, JournalAdapter.JournalViewHolder.OnJournalListener onJournalListener) {
            super(v);
            view = (ViewGroup) v;
            nameView = (TextView) view.getChildAt(0);
            locationView = (TextView) view.getChildAt(1);
            frequencyView = (TextView) view.getChildAt(2);
            typeView = (ImageView) view.getChildAt(3);

            v.setOnClickListener(this);

            this.onJournalListener = onJournalListener;
        }

        @Override
        public void onClick(View v) {
            onJournalListener.onJournalClick(getAdapterPosition());
        }

        public interface OnJournalListener{
            void onJournalClick(int position);
        }


    }
    // initialize the data set here
    public JournalAdapter(ArrayList<Journal> mDataset, JournalAdapter.JournalViewHolder.OnJournalListener onJournalListener) {
        this.mDataset = mDataset;
        this.onJournalListener = onJournalListener;
    }
    // Create new views (invoked by the layout manager)
    @Override
    public JournalAdapter.JournalViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_journal_view, parent, false);
        JournalAdapter.JournalViewHolder vh = new JournalAdapter.JournalViewHolder(v, onJournalListener);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(JournalAdapter.JournalViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        Journal Journal = mDataset.get(position);
        holder.nameView.setText(Journal.name);

        String location_text = Journal.description;
        String span_text = "";

        holder.locationView.setText(location_text);
        holder.frequencyView.setText(span_text);

        //holder.typeView.setImageResource(R.drawable.journal_icon);
                switch (Journal.type) {
                    case "Skiing":
                        holder.typeView.setImageResource(R.drawable.skiing_icon_2);
                        //holder.typeView.setBackgroundResource(R.drawable.round_button_light_blue);
                        break;
                    case "Snowboarding":
                        holder.typeView.setImageResource(R.drawable.snowboard_icon);
                        //holder.typeView.setBackgroundResource(R.drawable.round_button_light_blue);
                        break;
                    case "Hiking":
                        holder.typeView.setImageResource(R.drawable.hiker_icon);
                        //holder.typeView.setBackgroundResource(R.drawable.round_button_light_blue);
                        break;
                    case "Peak":
                        holder.typeView.setImageResource(R.drawable.peak_icon);
                //holder.typeView.setBackgroundResource(R.drawable.round_button_light_blue);
                break;

        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
