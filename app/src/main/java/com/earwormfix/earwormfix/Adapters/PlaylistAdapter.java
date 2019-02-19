package com.earwormfix.earwormfix.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.earwormfix.earwormfix.R;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {
    private String[] mData;

    public PlaylistAdapter(String[] mData){
        this.mData = mData;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        // create a new view
        View rootView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.my_list_view, parent, false);
        return new ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mTV.setText(mData[position]);
    }

    @Override
    public int getItemCount() {
        return mData.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView mTV;

        public ViewHolder(View itemView){
            super(itemView);
            mTV = itemView.findViewById(R.id.song_demo);
        }
    }

}
