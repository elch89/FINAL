package com.earwormfix.earwormfix.Utilitties;

import android.view.View;

public interface ItemClickListener {
    void onCommentClick(View view,int position);
    void onFixClick(View view, int position);
    // this listener is used for adding friends, adding to fix list and updating profile
    void onItemClick(View view, int position);
    void onDeleteClick(View view, int position);
}
