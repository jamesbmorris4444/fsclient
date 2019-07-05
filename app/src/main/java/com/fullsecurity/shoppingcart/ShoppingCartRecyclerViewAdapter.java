package com.fullsecurity.shoppingcart;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fullsecurity.shared.R;
import com.fullsecurity.shared.databinding.ShoppingCartMainBinding;

import java.util.ArrayList;

@SuppressWarnings("all")
public class ShoppingCartRecyclerViewAdapter extends RecyclerView.Adapter<ShoppingCartRecyclerViewAdapter.ShoppingCartViewHolder> {

    private ArrayList<ShoppingCart> shoppingCarts;
    private ShoppingCartFragment shoppingCartFragment;
    private RecyclerView recyclerView;
    private View view;

    public ShoppingCartRecyclerViewAdapter(ArrayList<ShoppingCart> shoppingCarts,
                                           ShoppingCartFragment shoppingCartFragment, RecyclerView recyclerView) {
        this.shoppingCarts = shoppingCarts;
        this.shoppingCartFragment = shoppingCartFragment;
        this.recyclerView = recyclerView;
    }

    @Override
    public ShoppingCartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shopping_cart_main, parent, false);
        ShoppingCartViewHolder itemViewHolder = new ShoppingCartViewHolder(view);
        return itemViewHolder;
    }

    @Override
    public void onBindViewHolder(ShoppingCartViewHolder viewHolder, int position) {
        ShoppingCart shoppingCart = shoppingCarts.get(position);
        if (shoppingCart.getIndex() < 0) viewHolder.view.setEnabled(false);
        viewHolder.bind(shoppingCart);
    }

    @Override
    public int getItemCount() {
        return shoppingCarts.size();
    }

    public class ShoppingCartViewHolder extends RecyclerView.ViewHolder {

        private ShoppingCartMainBinding binding;
        private View view;

        public ShoppingCartViewHolder(View view) {
            super(view);
            this.view = view;
            binding = DataBindingUtil.bind(view);
        }

        public void bind(ShoppingCart shoppingCart) {
            binding.setShoppingCart(shoppingCart);
            binding.setShoppingCartClickHandler(shoppingCartFragment);
            binding.executePendingBindings();
        }

    }

}
