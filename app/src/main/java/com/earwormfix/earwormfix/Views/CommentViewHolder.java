package com.earwormfix.earwormfix.Views;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.earwormfix.earwormfix.Models.Comment;
import com.earwormfix.earwormfix.R;

public class CommentViewHolder extends RecyclerView.ViewHolder {
    private TextView comment;
    private TextView cid;
    private TextView time;
    public CommentViewHolder(@NonNull View itemView) {
        super(itemView);
        comment =  itemView.findViewById(R.id.comment);
        cid = itemView.findViewById(R.id.cid);
        time = itemView.findViewById(R.id.time);
    }
    public void bind(Comment comm){
        comment.setText(comm.getComment());
        cid.setText(String.valueOf(comm.getId()));
        time.setText(comm.getToc());
    }
}
