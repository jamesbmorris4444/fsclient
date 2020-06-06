package com.fullsecurity.demoapplication;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.fullsecurity.shared.databinding.ShoppingCartMainBinding;

public class ShoppingCartViewHolder extends RecyclerView.ViewHolder {

    private ShoppingCartMainBinding binding;
    private ShoppingCartFragment shoppingCartFragment;
    private ShoppingCartRecyclerViewAdapter adapter;

    public ShoppingCartViewHolder(View view, ShoppingCartFragment shoppingCartFragment, ShoppingCartRecyclerViewAdapter adapter) {
        super(view);
        this.shoppingCartFragment = shoppingCartFragment;
        binding = DataBindingUtil.bind(view);
        this.adapter = adapter;
    }

    public void bind(ShoppingCart shoppingCart) {
        binding.setShoppingCart(shoppingCart);
        binding.executePendingBindings();
    }

    public ShoppingCartRecyclerViewAdapter getAdapter() {
        return adapter;
    }

}

