package com.fullsecurity.demoapplication;

import android.view.View;

public class StoreItemClickHandler {

    private StoreItemFragment storeItemFragment;

    public StoreItemClickHandler(StoreItemFragment storeItemFragment) {
        this.storeItemFragment = storeItemFragment;
    }

    public void handleItemClick(View view, StoreItem storeItem) {
        storeItemFragment.handleItemClick(view, storeItem);
    }

}
