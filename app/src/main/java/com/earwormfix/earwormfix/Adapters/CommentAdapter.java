package com.earwormfix.earwormfix.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.earwormfix.earwormfix.Models.Comment;
import com.earwormfix.earwormfix.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CommentAdapter extends
        RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> items;
    private int resourceId;

    public CommentAdapter(int resourceId, List<Comment> items) {
        this.items = items;
        this.resourceId = resourceId;

    }
    @NonNull
    @Override
    public CommentAdapter.CommentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View rootView = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(resourceId, viewGroup, false);

       // context = viewGroup.getContext();

        return new CommentAdapter.CommentViewHolder(rootView);
    }
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.CommentViewHolder viewHolder, int position) {
        if(items!=null) {
            if(items.get(position)!=null) {
                Comment item = items.get(position);
                viewHolder.comment.setText(item.getUser_input());
                viewHolder.cid.setText(item.getUid());
                viewHolder.time.setText(convertStrDate(item.getCreated_at()));
            }

        }

    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }
    // Changes the format of date from database
    private String convertStrDate(String timeDate){
        DateFormat frmtIn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat frmtOut = new SimpleDateFormat("dd/MM/yy HH:mm");
        DateFormat frmtEdit = new SimpleDateFormat("HH:mm");
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jerusalem"));
        Calendar cal = Calendar.getInstance();
        try {
            Date date = frmtIn.parse(timeDate);
            cal.setTime(date);

            if(now.get(Calendar.DATE)== cal.get(Calendar.DATE) ){
                return "Today at " + frmtEdit.format(date);
            }
            return frmtOut.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "Not available";
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        private TextView comment;
        private TextView cid;
        private TextView time;
        private CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            comment =  itemView.findViewById(R.id.comment);
            cid = itemView.findViewById(R.id.cid);
            time = itemView.findViewById(R.id.time);
        }


    }



}
