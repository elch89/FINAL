package com.earwormfix.earwormfix.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.earwormfix.earwormfix.Models.Post;
import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.Utilitties.GlideApp;
import com.earwormfix.earwormfix.Utilitties.ItemClickListener;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.exoplayer.ExoPlayerViewHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;
//@SuppressWarnings("WeakerAccess")
public class FeedsViewHolder extends RecyclerView.ViewHolder implements ToroPlayer, View.OnClickListener {
    private static final String FIXED_ICON = "Fixed";
    protected final PlayerView playerView;
    private ItemClickListener clickListener;
    protected ExoPlayerViewHelper helper;
    private Uri videoUri;
    private TextView pDate,userId, desc;
    private ImageView userAvatar;
    private Context context;
    private ImageView volumeOn, volumeOff;
    public RecyclerView comments;
    private TextView mFixed;

    public FeedsViewHolder(ViewGroup parent, LayoutInflater inflater, int layoutRes,Context context, ItemClickListener mListener) {
        super(inflater.inflate(layoutRes, parent, false));
        clickListener = mListener;
        this.context=context;
        AspectRatioFrameLayout aspectRatioFrameLayout = itemView.findViewById(R.id.videoView);
        playerView = itemView.findViewById(R.id.playerView);
        pDate = itemView.findViewById(R.id.post_date);
        desc = itemView.findViewById(R.id.describe_vid);
        userId = itemView.findViewById(R.id.user_id);
        comments = itemView.findViewById(R.id.comment_recycler_view);
        userAvatar = itemView.findViewById(R.id.icon_poster);
        TextView addToList = itemView.findViewById(R.id.add_to_list);
        mFixed = itemView.findViewById(R.id.thumbs_up);
        TextView btnComment =  itemView.findViewById(R.id.cmd_comment);

        // Volume control mute/un mute
        volumeOff =itemView.findViewById(R.id.exo_volume_off);
        volumeOn = itemView.findViewById(R.id.exo_volume_up);
        volumeOff.setVisibility(View.INVISIBLE);
        volumeUp();
        volumeDown();

        TextView delete = itemView.findViewById(R.id.removePost);
        delete.setOnClickListener(this);
        addToList.setOnClickListener(this);
        btnComment.setOnClickListener(this);
        mFixed.setOnClickListener(this);
        aspectRatioFrameLayout.setAspectRatio(4f/3f);
    }

     public void bind(Post item) {
        String baseUrl ="https://earwormfix.com/";
        String mediaName = baseUrl.concat(item.getUrl());
        videoUri = Uri.parse(mediaName);
        pDate.setText(convertStrDate(item.getCreated_at()));
        if(item.getFixed()>0){
            mFixed.setText(String.format("%s  %s", FIXED_ICON, String.valueOf(item.getFixed())));
        }
        else {
            mFixed.setText(String.format("%s", FIXED_ICON));
        }
        desc.setText(item.getDescription());
        if(item.getProfPic().equals("0")){
            userAvatar.setImageResource(R.drawable.avatar_dog);
        }
        else{
            GlideApp.with(context).asBitmap().load(baseUrl.concat(item.getProfPic())).into(userAvatar);
        }

        userId.setText(item.getName());


    }
    /**
     * set up toro player
     * */

    @NonNull @Override public View getPlayerView() {
        return playerView;
    }

    @NonNull @Override public PlaybackInfo getCurrentPlaybackInfo() {
        return helper != null ? helper.getLatestPlaybackInfo() : PlaybackInfo.SCRAP;
    }

    @Override
    public void initialize(@NonNull Container container, @NonNull PlaybackInfo playbackInfo) {
        if (videoUri == null) throw new IllegalStateException("Video is null.");
        if (helper == null) {
            helper = new ExoPlayerViewHelper(this, videoUri);
        }
        helper.initialize(container, playbackInfo);
    }

    @Override
    public void play() {
        if (helper != null) {
            helper.play();
        }
    }

    @Override
    public void pause() {
        if (helper != null) helper.pause();
    }

    @Override
    public boolean isPlaying() {
        return helper != null && helper.isPlaying();
    }

    @Override
    public void release() {
        if (helper != null) {
            helper.release();
            helper = null;
        }
    }

    @Override
    public boolean wantsToPlay() {
        return ToroUtil.visibleAreaOffset(this, itemView.getParent()) >= 0.65;
    }

    @Override
    public int getPlayerOrder() {
        return getAdapterPosition();
    }

    @Override
    public void onClick(View view) {
        if (clickListener != null) {
            if(view.getId() == R.id.cmd_comment) {
                clickListener.onCommentClick(view, getAdapterPosition());
            }
            else if(view.getId() == R.id.thumbs_up){

                clickListener.onFixClick(view,getAdapterPosition());
            }
            else if(view.getId() == R.id.add_to_list){
                clickListener.onItemClick(view,getAdapterPosition());
            }
            else if(view.getId() == R.id.removePost){
                clickListener.onDeleteClick(view,getAdapterPosition());
            }
        }
    }

    // Changes the format of date from database
    private String convertStrDate(String timeDate){
        @SuppressLint("SimpleDateFormat")
        DateFormat frmtIn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        @SuppressLint("SimpleDateFormat")
        DateFormat frmtOut = new SimpleDateFormat("dd/MM/yy HH:mm");
        @SuppressLint("SimpleDateFormat")
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
    private void volumeUp(){
        volumeOn.setOnClickListener(v -> {
            if(helper!=null) {
                helper.setVolume(0.75f);
                volumeOn.setVisibility(View.INVISIBLE);
                volumeOff.setVisibility(View.VISIBLE);
            }
        });
    }
    private void volumeDown(){
        volumeOff.setOnClickListener(v -> {
            if(helper!=null){
                helper.setVolume(0f);
                volumeOn.setVisibility(View.VISIBLE);
                volumeOff.setVisibility(View.INVISIBLE);
            }
        });
    }

}


