package com.fullsecurity.storeitem;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.fullsecurity.shared.databinding.StoreItemMainBinding;

public class StoreItemViewHolder extends RecyclerView.ViewHolder {
    private StoreItemMainBinding binding;

    StoreItemFragment storeItemFragment;
    View view;

    public StoreItemViewHolder(View view, StoreItemFragment storeItemFragment) {
        super(view);
        this.view = view;
        this.storeItemFragment = storeItemFragment;
        binding = DataBindingUtil.bind(view);
    }

    public void bind(StoreItem storeItem) {
        binding.setStoreItem(storeItem);
        binding.setStoreItemClickHandler(storeItemFragment);
        binding.executePendingBindings();
    }
}