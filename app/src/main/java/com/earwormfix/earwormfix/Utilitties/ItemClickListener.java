package com.earwormfix.earwormfix.Utilitties;

import android.view.View;

public interface ItemClickListener {
    void onCommentClick(View view,int position);
    void onFixClick(View view, int position);
    void onSubmitEdit(View view, int position);
}
