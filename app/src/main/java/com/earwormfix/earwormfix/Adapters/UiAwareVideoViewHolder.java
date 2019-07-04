/*
 * Copyright (c) 2018 Nam Nguyen, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.earwormfix.earwormfix.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.earwormfix.earwormfix.Utilitties.ItemClickListener;
import com.google.android.exoplayer2.ControlDispatcher;
import com.google.android.exoplayer2.Player;

import java.util.concurrent.atomic.AtomicInteger;

import im.ene.toro.exoplayer.ExoPlayerViewHelper;
import im.ene.toro.exoplayer.ui.ToroControlView;


/**
 * This ViewHolder extends {@link FeedsViewHolder}, provide the mechanism to effectively play/pause
 * the Player from UI. Because the {@link ExoPlayerViewHelper} has nothing to do with
 * {@link ToroControlView} directly, but the {@link ToroControlView} plays an important role in our
 * setup, we need to make them work together.
 *
 * @author eneim (2018/03/19).
 */

class UiAwareVideoViewHolder extends FeedsViewHolder {

  @SuppressWarnings("WeakerAccess") final FeedAdapter adapter;

  UiAwareVideoViewHolder(FeedAdapter adapter, ViewGroup parent, LayoutInflater inflater,
                                int layoutRes, Context context, ItemClickListener mListener) {
    super(parent, inflater, layoutRes,context,mListener);
    this.adapter = adapter;

    // PlayerViewHelper will lazily prepare MediaSource: it will prepare in the first time
    // ToroPlayer#play() is called. ToroControlView cannot know about that, and if User
    // clicks to 'Play' button to start a playback, the traditional way will not work because
    // at that time, the MediaSource may not be prepared. To fix this, we use helper to deal
    // with the dispatcher.
    this.playerView.setControlDispatcher(new ControlDispatcher() {
      // Just to have a local reference.
      final AtomicInteger order = UiAwareVideoViewHolder.this.adapter.lastUserPause;
      @Override
      public boolean dispatchSetPlayWhenReady(Player player, boolean playWhenReady) {
        // We ask helper to play/pause instead. This way we make sure the MediaSource will be
        // correctly prepared.
        if (helper == null) return false; // not ready yet, do nothing.
        if (!playWhenReady) {
          helper.pause();
          order.set(getPlayerOrder());
        } else {
          helper.play();

          if (order.get() == getPlayerOrder()) order.set(-1);
        }

        return true;
      }

      @Override
      public boolean dispatchSeekTo(Player player, int windowIndex, long positionMs) {
        player.seekTo(windowIndex, positionMs);
        return true;
      }
      @Override
      public boolean dispatchSetRepeatMode(Player player, int repeatMode) {
        return false;
      }
      @Override
      public boolean dispatchSetShuffleModeEnabled(Player player, boolean shuffleModeEnabled) {
        return false;
      }
      @Override
      public boolean dispatchStop(Player player, boolean reset) {
        return false;
      }
    });
  }
}
