package com.earwormfix.earwormfix.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.earwormfix.earwormfix.Models.Comment;
import com.earwormfix.earwormfix.Views.CommentViewHolder;

import java.util.List;

public class CommentAdapter extends
        RecyclerView.Adapter<CommentViewHolder> {
    private List<Comment> items;
    private int resourceId;

    public CommentAdapter(int resourceId, List<Comment> items) {
        this.items = items;
        this.resourceId = resourceId;

    }
    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View rootView = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(resourceId, viewGroup, false);

       // context = viewGroup.getContext();

        return new CommentViewHolder(rootView);
    }
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder viewHolder, int position) {
        if(items!=null) {
            if(items.get(position)!=null) {
                Comment item = items.get(position);
                viewHolder.bind(item);
            }

        }

    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }



}
