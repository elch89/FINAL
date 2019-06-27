package com.earwormfix.earwormfix.Adapters;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.earwormfix.earwormfix.Models.MyFix;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<MyFix> selected = new MutableLiveData<MyFix>();

    public void select(MyFix item) {
        selected.setValue(item);
    }

    public LiveData<MyFix> getSelected() {
        return selected;
    }
}
