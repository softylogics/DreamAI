package com.softylogics.dreamai;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DreamViewModel extends ViewModel {
    private final MutableLiveData<DreamItem> selectedItem = new MutableLiveData<DreamItem>();


    public MutableLiveData<DreamItem> getSelectedItem() {
        return selectedItem;
    }
    public void setDreamItem(DreamItem item) {
        selectedItem.setValue(item);
    }
}
