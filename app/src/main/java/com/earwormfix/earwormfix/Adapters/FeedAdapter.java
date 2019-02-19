package com.earwormfix.earwormfix.Adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.earwormfix.earwormfix.Activities.LoginActivity;
import com.earwormfix.earwormfix.Models.Comment;
import com.earwormfix.earwormfix.Models.Feed;
import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.Utilitties.ItemClickListener;
import com.earwormfix.earwormfix.Views.FeedsViewHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import im.ene.toro.CacheManager;
import im.ene.toro.PlayerSelector;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.widget.Container;

/** display the data in a RecyclerView*/
public class FeedAdapter extends RecyclerView.Adapter<FeedsViewHolder> implements PlayerSelector, CacheManager {

    private ItemClickListener clickListener;

    public FeedAdapter(PlayerSelector origin,Context context) {
        this.origin = ToroUtil.checkNotNull(origin);
        this.context = context;
    }

    public FeedAdapter() {//getAplicationContext()
        this(PlayerSelector.DEFAULT,null);
    }
    private List<Feed> mFeeds; // Cached copy of feeds
    private List<Comment> mComments; // Cached copy of comments
    private LayoutInflater inflater;
    private Context context;


    @NonNull @Override
    public FeedsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (inflater == null || inflater.getContext() != parent.getContext()) {
            inflater = LayoutInflater.from(parent.getContext());
        }

        return new UiAwareVideoViewHolder(this, parent, inflater, R.layout.feed_view,clickListener);
    }

    @Override public void onBindViewHolder(@NonNull FeedsViewHolder holder, int position) {
        if (mFeeds != null ) {
            // populate with a list of feeds
            Feed current = mFeeds.get(position);
            holder.bind(current);
            // populate with a list of comments inside
            if(mComments!=null ){
                LinearLayoutManager lln = new LinearLayoutManager(context);
                holder.comments.setLayoutManager(lln);

                // get matching comments to feed chosen
                List<Comment> sortedById = getMatchingComments(current);
                if(sortedById.isEmpty()){
                    holder.comments.setMinimumHeight(200);
                }
                CommentAdapter commentAdapter = new CommentAdapter(R.layout.comment_view,sortedById);
                holder.comments.setAdapter(commentAdapter);
            }

        }
    }
    // comments are sorted in database - we get the matching comments to the specified feed by ID
    private List<Comment> getMatchingComments(Feed currentFeed){
        List<Comment> sortedById = new ArrayList<>();
        if(!mComments.isEmpty()) {
            for (Comment comm : mComments) {
                if(comm.getFeedId() == currentFeed.getId()){
                    sortedById.add(comm);
                }
            }
        }
        return sortedById;
    }


    /// PlayerSelector implementation

    @SuppressWarnings("WeakerAccess") //
    final PlayerSelector origin;
    // Keep a cache of the Playback order that is manually paused by User.
    // So that if User scroll to it again, it will not start play.
    // Value will be updated by the ViewHolder.
    final AtomicInteger lastUserPause = new AtomicInteger(-1);

    @NonNull @Override public Collection<ToroPlayer> select(@NonNull Container container,
                                                            @NonNull List<ToroPlayer> items) {
        Collection<ToroPlayer> originalResult = origin.select(container, items);
        ArrayList<ToroPlayer> result = new ArrayList<>(originalResult);
        if (lastUserPause.get() >= 0) {
            for (Iterator<ToroPlayer> it = result.iterator(); it.hasNext(); ) {
                if (it.next().getPlayerOrder() == lastUserPause.get()) {
                    it.remove();
                    break;
                }
            }
        }
        return result;
    }

    @NonNull @Override public PlayerSelector reverse() {
        return origin.reverse();
    }

    /// CacheManager implementation

    @Nullable
    @Override public Object getKeyForOrder(int order) {
        return order;
    }

    @Nullable @Override public Integer getOrderForKey(@NonNull Object key) {
        return key instanceof Integer ? (Integer) key : null;
    }


    public void setFeeds(List<Feed> feeds){
        mFeeds = feeds;
        notifyDataSetChanged();
    }
    public void setComments(List<Comment> comments){
        mComments = comments;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mWords has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mFeeds != null)
            return mFeeds.size();
        else return 0;
    }



    private Uri getMedia(String mediaName) {
        return Uri.parse("android.resource://" + LoginActivity.PACKAGE_NAME +
                "/raw/" + mediaName);
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        clickListener = itemClickListener;
    }
}
