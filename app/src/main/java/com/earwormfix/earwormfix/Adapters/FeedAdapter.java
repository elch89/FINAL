package com.earwormfix.earwormfix.Adapters;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.earwormfix.earwormfix.Models.Comment;
import com.earwormfix.earwormfix.Models.Post;
import com.earwormfix.earwormfix.R;
import com.earwormfix.earwormfix.Utilitties.ItemClickListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import im.ene.toro.CacheManager;
import im.ene.toro.PlayerSelector;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.widget.Container;

/** display the data in a RecyclerView*/
public class FeedAdapter extends PagedListAdapter<Post, FeedsViewHolder> implements PlayerSelector, CacheManager {

    private ItemClickListener clickListener;
    private LayoutInflater inflater;
    private Context context;


    public FeedAdapter(PlayerSelector origin,Context context) {
        super(Post.CALLBACK);
        this.origin = ToroUtil.checkNotNull(origin);
        this.context = context;
    }

    public FeedAdapter() {//getAplicationContext()
        this(PlayerSelector.DEFAULT,null);
    }


    @NonNull @Override
    public FeedsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (inflater == null || inflater.getContext() != parent.getContext()) {
            inflater = LayoutInflater.from(parent.getContext());
        }
        return new UiAwareVideoViewHolder(this, parent, inflater, R.layout.feed_view, context ,clickListener);
    }

    @Override public void onBindViewHolder(@NonNull FeedsViewHolder holder, int position) {
        List<Comment> mComments = new ArrayList<>();
        Post current = getItem(position);
        if(current!=null){
            if(Objects.requireNonNull(current).getComments() != null ){
                // adds comment associated to post
                mComments.addAll(Arrays.asList(current.getComments()));
            }
            holder.bind(current);
            // populate with a list of comments inside
            LinearLayoutManager lln = new LinearLayoutManager(context);
            holder.comments.setLayoutManager(lln);

            if (mComments.isEmpty()) {
                holder.comments.setMinimumHeight(200);
            }
            CommentAdapter commentAdapter = new CommentAdapter(R.layout.comment_view, mComments);
            holder.comments.setAdapter(commentAdapter);
        }
    }


    /// PlayerSelector implementation

    @SuppressWarnings("WeakerAccess")
    final PlayerSelector origin;
    // Keep a cache of the Playback order that is manually paused by User.
    // So that if User scroll to it again, it will not start play.
    // Value will be updated by the ViewHolder.
    final AtomicInteger lastUserPause = new AtomicInteger(-1);

    @NonNull
    @Override public Collection<ToroPlayer> select(@NonNull Container container,
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

    @NonNull
    @Override public PlayerSelector reverse() {
        return origin.reverse();
    }
    @Nullable
    @Override public Object getKeyForOrder(int order) {
        return order;
    }

    @Nullable @Override public Integer getOrderForKey(@NonNull Object key) {
        return key instanceof Integer ? (Integer) key : null;
    }

    public Post getFeedAt(int position){
        return getItem(position);
    }

    @Override
    public int getItemCount() {
        if(getCurrentList()!=null)
            return getCurrentList().size();
        return 0;
    }
    public void setClickListener(ItemClickListener itemClickListener) {
        clickListener = itemClickListener;
    }

}
