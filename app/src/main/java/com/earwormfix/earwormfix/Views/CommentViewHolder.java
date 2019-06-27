package com.earwormfix.earwormfix.Views;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.earwormfix.earwormfix.Models.Comment;
import com.earwormfix.earwormfix.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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
        comment.setText(comm.getUser_input());
        cid.setText(comm.getUid());
        time.setText(convertStrDate(comm.getCreated_at()));
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
}
