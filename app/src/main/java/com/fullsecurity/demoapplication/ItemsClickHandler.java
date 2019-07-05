package com.fullsecurity.demoapplication;

import android.view.View;

public class ItemsClickHandler {

    ItemsFragment itemsFragment;

    public ItemsClickHandler(ItemsFragment itemsFragment) {
        this.itemsFragment = itemsFragment;
    }

    public void handleItemClick(View view, StoreItem storeItem) {
        itemsFragment.handleItemClick(view, storeItem);
    }
}
