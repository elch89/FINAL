package com.earwormfix.earwormfix.Views;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.earwormfix.earwormfix.Models.Feed;
import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.Utilitties.ItemClickListener;
import com.google.android.exoplayer2.ui.PlayerView;

import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.exoplayer.ExoPlayerViewHelper;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;
//@SuppressWarnings("WeakerAccess")
public class FeedsViewHolder extends RecyclerView.ViewHolder implements ToroPlayer, View.OnClickListener {
    private static final String VIDEO_SAMPLE = "tacoma_narrows";
    private static final String FIXED_ICON = "Fixed";

    protected final PlayerView playerView;
    private ItemClickListener clickListener;
    protected ExoPlayerViewHelper helper;
    private Uri videoUri;
    private TextView pDate,userId;
    private ImageView userAvatar;

    /////
    public RecyclerView comments;
    private TextView btnComment ,mFixed;
    public FeedsViewHolder(ViewGroup parent, LayoutInflater inflater, int layoutRes, ItemClickListener mListener) {
        super(inflater.inflate(layoutRes, parent, false));
        clickListener = mListener;
        playerView = itemView.findViewById(R.id.playerView);
        pDate = itemView.findViewById(R.id.post_date);
        userId = itemView.findViewById(R.id.user_id);
        comments = itemView.findViewById(R.id.comment_recycler_view);
        userAvatar = itemView.findViewById(R.id.icon_poster);

        mFixed = itemView.findViewById(R.id.thumbs_up);
        btnComment =  itemView.findViewById(R.id.cmd_comment);

        btnComment.setOnClickListener(this);
        mFixed.setOnClickListener(this);

    }

     public void bind(Feed item) {
        videoUri = Uri.parse("file:///android_asset/video/video.mp4");
        pDate.setText(item.getTop());
        if(item.getFixed()>0){
            mFixed.setText(String.format("%s  %s", FIXED_ICON, String.valueOf(item.getFixed())));
        }
        userAvatar.setImageResource(item.getProfile_pic());
        userId.setText(item.getUid());


    }

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

    @Override public void play() {
        if (helper != null) {
            helper.play();
            //helper.setVolume(1.0f);// Added by me

        }
    }

    @Override public void pause() {
        if (helper != null) helper.pause();
    }

    @Override public boolean isPlaying() {
        return helper != null && helper.isPlaying();
    }

    @Override public void release() {
        if (helper != null) {
            helper.release();
            helper = null;
        }
    }

    @Override public boolean wantsToPlay() {
        return ToroUtil.visibleAreaOffset(this, itemView.getParent()) >= 0.65;
    }

    @Override public int getPlayerOrder() {
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
        }
    }

}


