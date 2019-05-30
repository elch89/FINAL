package com.earwormfix.earwormfix.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.Utilitties.ItemClickListener;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {
    private String[] mData;
    private ItemClickListener mClickListener;

    public ProfileAdapter(String[] mData){
        this.mData = mData;
    }
    @NonNull
    @Override
    public ProfileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        // create a new view
        View rootView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.profile_view, parent, false);
        return new ProfileAdapter.ViewHolder(rootView, mClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileAdapter.ViewHolder holder, int position) {
        holder.mTV.setText(mData[position]);
    }

    @Override
    public int getItemCount() {
        return mData.length;
    }
    public void setClickListener(ItemClickListener itemClickListener) {
        mClickListener = itemClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView mTV;
        public Button btnEdit;
        ItemClickListener clickListener;

        public ViewHolder(View itemView, ItemClickListener mListener){
            super(itemView);
            clickListener = mListener;
            mTV = itemView.findViewById(R.id.profile_att);
            btnEdit = itemView. findViewById(R.id.profile_edit);
            btnEdit.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null){
                if(v.getId() == R.id.profile_edit){
                    clickListener.onSubmitEdit(v, getAdapterPosition());
                }
            }

        }
    }
}
