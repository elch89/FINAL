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

import com.earwormfix.earwormfix.Models.MyFix;
import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.Utilitties.GlideApp;
import com.earwormfix.earwormfix.Utilitties.ItemClickListener;

import java.util.List;

public class MyFixAdapter extends RecyclerView.Adapter<MyFixAdapter.ViewHolder> {
    private static final String URL = "https://www.earwormfix.com/";
    private static final String MY_FIX = "  that's my fix";
    private List<MyFix> myFixList;
    private Context context;
    private ItemClickListener mClickListener;

    public MyFixAdapter(List<MyFix> mData, Context context){
        this.myFixList = mData;
        this.context =context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        // create a new view
        View rootView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.my_list_view, parent, false);
        return new ViewHolder(rootView,mClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(myFixList != null){
            String songDescription = String.valueOf(myFixList.get(position).getId()).concat(". ").concat(myFixList.get(position).getSongName()+MY_FIX);
            holder.name.setText(songDescription);
            // adds a thumbnail for selected video
            GlideApp.with(context)
                    .load(URL.concat(myFixList.get(position).getImgUrl()))
                    .into(holder.snap);
        }

    }

    @Override
    public int getItemCount() {
        if(myFixList != null)
            return myFixList.size();
        return 0;
    }
    public void setClickListener(ItemClickListener itemClickListener) {
        mClickListener = itemClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView name;
        private ImageView snap;
        CardView layout;
        private ItemClickListener clickListener;

        public ViewHolder(View itemView,ItemClickListener mListener){
            super(itemView);
            clickListener = mListener;
            name = itemView.findViewById(R.id.song_name);
            snap = itemView.findViewById(R.id.imageView);
            layout = itemView.findViewById(R.id.single_track);
            // sets selected view as a click listener, for choosing from where to play
            layout.setOnClickListener(this);

        }
        @Override
        public void onClick(View view) {
            if (clickListener != null){
                if(view.getId() == R.id.single_track){
                    clickListener.onItemClick(view,getAdapterPosition());
                }
            }
        }
    }


}
