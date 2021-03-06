package com.earwormfix.earwormfix.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.earwormfix.earwormfix.Models.Connectivity;
import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.Utilitties.GlideApp;
import com.earwormfix.earwormfix.Utilitties.ItemClickListener;

import java.util.ArrayList;

public class AddFriendAdapter extends RecyclerView.Adapter<AddFriendAdapter.ViewHolder> {
    private static  final String BASE_URL ="https://earwormfix.com/";
    private ArrayList<Connectivity> connectivityList;
    private ItemClickListener mClickListener;
    private Context context;
    public AddFriendAdapter(ArrayList<Connectivity> connectivityList, Context context){
        this.connectivityList = connectivityList;
        this.context = context;
    }
    @NonNull
    @Override
    public AddFriendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View rootView = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.connections, viewGroup, false);
        return new AddFriendAdapter.ViewHolder(rootView, mClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AddFriendAdapter.ViewHolder viewHolder, int i) {
        if(connectivityList.get(i).isFriends()){
            viewHolder.mAdd.setClickable(false);
            viewHolder.mAdd.setBackgroundColor(Color.DKGRAY);
            viewHolder.mAdd.setText(R.string.friendsAlready);
        }
        viewHolder.mName.setText(connectivityList.get(i).getFull_name());
        viewHolder.mNick.setText(connectivityList.get(i).getName());
        viewHolder.mEmail.setText(connectivityList.get(i).getEmail());

        if(connectivityList.get(i).getPhoto().equals("0")){
            viewHolder.userAvatar.setImageResource(R.drawable.avatar_dog);
        }
        else{
            GlideApp.with(context).load(BASE_URL+connectivityList.get(i).getPhoto()).into(viewHolder.userAvatar);
        }

    }
    public void setClickListener(ItemClickListener itemClickListener) {
        mClickListener = itemClickListener;
    }
    @Override
    public int getItemCount() {
        return connectivityList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView mName,mNick,mEmail;
        private Button mAdd;
        private ImageView userAvatar;
        private ItemClickListener clickListener;

        private ViewHolder(View itemView, ItemClickListener mListener){
            super(itemView);
            clickListener = mListener;
            userAvatar = itemView.findViewById(R.id.imgAvatar);
            mName = itemView.findViewById(R.id.name_fr);
            mNick = itemView.findViewById(R.id.nickname_fr);
            mEmail = itemView.findViewById(R.id.email_fr);
            mAdd = itemView.findViewById(R.id.add_friend);
            mAdd.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null){
                if(v.getId() == R.id.add_friend){
                    clickListener.onItemClick(v, getAdapterPosition());
                }
            }
        }
    }
}
