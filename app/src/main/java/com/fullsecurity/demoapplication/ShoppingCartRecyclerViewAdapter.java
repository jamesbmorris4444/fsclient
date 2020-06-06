package com.fullsecurity.demoapplication;

import android.graphics.Color;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fullsecurity.shared.R;

import java.util.ArrayList;
import java.util.Collections;

public class ShoppingCartRecyclerViewAdapter extends RecyclerView.Adapter<ShoppingCartViewHolder> implements ItemTouchHelperAdapter, ItemTouchHelperViewHolder  {

    private ArrayList<ShoppingCart> shoppingCarts;
    private View view;
    private ShoppingCartFragment shoppingCartFragment;
    private LinearLayoutManager linearLayoutManager;

    public ShoppingCartRecyclerViewAdapter(ArrayList<ShoppingCart> shoppingCarts, ShoppingCartFragment shoppingCartFragment) {
        this.shoppingCarts = shoppingCarts;
        this.shoppingCartFragment = shoppingCartFragment;
    }

    public void setLayoutManager(LinearLayoutManager linearLayoutManager) {
        this.linearLayoutManager = linearLayoutManager;
    }

    @Override
    public ShoppingCartViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.shopping_cart_main, viewGroup, false);
        return new ShoppingCartViewHolder(view, shoppingCartFragment, this);
    }

    @Override
    public void onBindViewHolder(ShoppingCartViewHolder viewHolder, int position) {
        ShoppingCart shoppingCart = shoppingCarts.get(position);
        shoppingCart.setIndexInView(position);
        viewHolder.bind(shoppingCart);
    }

    @Override
    public int getItemCount() {
        return shoppingCarts.size();
    }

    // added
    @Override
    public void onItemDismiss(int position) {
        shoppingCartFragment.deleted.add(shoppingCarts.get(position));
        shoppingCarts.remove(position);
        for (int k = position; k < shoppingCarts.size(); k++) shoppingCarts.get(k).setIndexInView(k);
        notifyItemRemoved(position);
    }

    // added
    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(shoppingCarts, fromPosition, toPosition);
        int startPosition = (fromPosition < toPosition ? fromPosition : toPosition);
        for (int k = startPosition; k < shoppingCarts.size(); k++) shoppingCarts.get(k).setIndexInView(k);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemSelected(int position) {
        Log.d("JIM","REMOVE pos="+position);
        View itemView = linearLayoutManager.findViewByPosition(position);
        itemView.setBackgroundColor(Color.CYAN);
    }

    @Override
    public void onItemClear(int position) {
        View itemView = linearLayoutManager.findViewByPosition(position);
        if (itemView != null) itemView.setBackgroundColor(Color.WHITE);
    }
}
