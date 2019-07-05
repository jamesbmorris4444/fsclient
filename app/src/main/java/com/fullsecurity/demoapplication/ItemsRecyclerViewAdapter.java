package com.fullsecurity.demoapplication;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fullsecurity.shared.R;

import java.util.ArrayList;

public class ItemsRecyclerViewAdapter extends RecyclerView.Adapter<StoreItemViewHolder>  {

    ArrayList<StoreItem> storeItems;
    View view;
    ItemsFragment itemsFragment;

    public ItemsRecyclerViewAdapter(ArrayList<StoreItem> storeItems, ItemsFragment itemsFragment) {
        this.storeItems = storeItems;
        this.itemsFragment = itemsFragment;
    }

    @Override
    public StoreItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_item, viewGroup, false);
        return new StoreItemViewHolder(view, itemsFragment);
    }

    @Override
    public void onBindViewHolder(StoreItemViewHolder viewHolder, int position) {
        StoreItem storeItem = storeItems.get(position);
        storeItem.setIndexInView(position);
        viewHolder.bind(storeItem);
    }

    @Override
    public int getItemCount() {
        int n = storeItems.size();
        return n;
    }

}
