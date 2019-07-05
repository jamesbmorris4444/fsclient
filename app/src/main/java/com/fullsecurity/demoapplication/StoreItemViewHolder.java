package com.fullsecurity.demoapplication;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.fullsecurity.shared.databinding.StoreItemMainBinding;

public class StoreItemViewHolder extends RecyclerView.ViewHolder {
    private StoreItemMainBinding binding;

    StoreItemFragment storeItemFragment;

    public StoreItemViewHolder(View view, StoreItemFragment storeItemFragment) {
        super(view);
        this.storeItemFragment = storeItemFragment;
        binding = DataBindingUtil.bind(view);
    }

    public void bind(StoreItem storeItem) {
        binding.setStoreItem(storeItem);
        binding.setStoreItemClickHandler(new StoreItemClickHandler(storeItemFragment));
        binding.executePendingBindings();
    }
}