package com.fullsecurity.shoppingcart;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.fullsecurity.shared.databinding.ShoppingCartMainBinding;
import com.fullsecurity.touch.ItemTouchHelperViewHolder;

public class ShoppingCartViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

    private ShoppingCartMainBinding binding;
    private LinearLayoutManager layoutManager;

    public ShoppingCartViewHolder(View view, LinearLayoutManager layoutManager) {
        super(view);
        binding = DataBindingUtil.bind(view);
        this.layoutManager = layoutManager;
    }

    public void bind(ShoppingCart shoppingCart) {
        binding.setShoppingCart(shoppingCart);
        binding.executePendingBindings();
    }

    @Override
    public void onItemSelected() {
        layoutManager.getChildAt(getAdapterPosition()).setBackgroundColor(Color.LTGRAY);
    }

    @Override
    public void onItemClear() {
        layoutManager.getChildAt(getAdapterPosition()).setBackgroundColor(Color.WHITE);
    }

}

