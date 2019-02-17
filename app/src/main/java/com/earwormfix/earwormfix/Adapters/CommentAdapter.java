package com.earwormfix.earwormfix.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
    private Context context;

    public CommentAdapter(int resourceId, List<Comment> items) {
        this.items = items;
//        this.context = context;
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
            Comment item = items.get(position);
//            if(item == null){
//                item = new Comment("1","There are no Comments to show","00:00:00");
//            }
            Log.d("Comment testing","id is:"+item.getId());
            viewHolder.bind(item);

        }

    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setFeeds(List<Comment> comments){
        items = comments;
        notifyDataSetChanged();
    }


}
