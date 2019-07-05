package com.fullsecurity.storeitem;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fullsecurity.shared.R;

import java.util.ArrayList;

public class StoreItemRecyclerViewAdapter extends RecyclerView.Adapter<StoreItemViewHolder> {

    ArrayList<StoreItem> storeItems;
    View view;
    StoreItemFragment storeItemFragment;

    public StoreItemRecyclerViewAdapter(ArrayList<StoreItem> storeItems, StoreItemFragment storeItemFragment) {
        this.storeItems = storeItems;
        this.storeItemFragment = storeItemFragment;
    }

    @Override
    public StoreItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.store_item_main, viewGroup, false);
        return new StoreItemViewHolder(view, storeItemFragment);
    }

    @Override
    public void onBindViewHolder(StoreItemViewHolder viewHolder, int position) {
        StoreItem storeItem = storeItems.get(position);
        storeItem.setIndexInView(position);
        viewHolder.bind(storeItem);
    }

    @Override
    public int getItemCount() {
        return storeItems.size();
    }

}
