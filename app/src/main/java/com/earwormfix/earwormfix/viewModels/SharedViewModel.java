package com.earwormfix.earwormfix.viewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.earwormfix.earwormfix.Models.MyFix;
/**
 * view model for observing live data changes when user adds a video from posts
 * */
public class SharedViewModel extends ViewModel {
    private final MutableLiveData<MyFix> selected = new MutableLiveData<>();
    public void select(MyFix item) {
        selected.setValue(item);
    }
    public LiveData<MyFix> getSelected() {
        return selected;
    }
}
