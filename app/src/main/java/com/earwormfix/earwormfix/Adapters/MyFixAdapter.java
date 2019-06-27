package com.earwormfix.earwormfix.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.earwormfix.earwormfix.GlideApp;
import com.earwormfix.earwormfix.Models.MyFix;
import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.Utilitties.ItemClickListener;

import java.util.List;

public class MyFixAdapter extends RecyclerView.Adapter<MyFixAdapter.ViewHolder> {
    private List<MyFix> mData;
    private Context context;
    private ItemClickListener mclickListener;

    public MyFixAdapter(List<MyFix> mData, Context context){
        this.mData = mData;
        this.context =context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        // create a new view
        View rootView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.my_list_view, parent, false);
        return new ViewHolder(rootView,mclickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(mData != null){
            String songDescription = String.valueOf(mData.get(position).getId()).concat(".  ").concat(mData.get(position).getSongName());
            holder.name.setText(songDescription);
            GlideApp.with(context)
                    .load("https://www.earwormfix.com/"+mData.get(position).getImgUrl())
                    //.override(300, 200)
                    .into(holder.snap);
        }

    }

    @Override
    public int getItemCount() {
        if(mData != null)
            return mData.size();
        return 0;
    }
    public void setClickListener(ItemClickListener itemClickListener) {
        mclickListener = itemClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView songnum, name;
        public ImageView snap;
        CardView layout;
        private ItemClickListener clickListener;

        public ViewHolder(View itemView,ItemClickListener mListener){
            super(itemView);
            clickListener = mListener;
            name = itemView.findViewById(R.id.song_name);
            snap = itemView.findViewById(R.id.imageView);
            layout = itemView.findViewById(R.id.single_track);
            /*songnum.setOnClickListener(this);
            name.setOnClickListener(this);*/
            layout.setOnClickListener(this);


        }
        @Override
        public void onClick(View view) {
            if (clickListener != null){
                if(view.getId() == R.id.single_track){
                    clickListener.onSubmitEdit(view,getAdapterPosition());
                }
            }

        }
    }


}
