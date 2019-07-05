package com.fullsecurity.common;

import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.fullsecurity.shared.R;
import com.fullsecurity.shoppingcart.ShoppingCart;
import com.fullsecurity.shoppingcart.ShoppingCartViewHolder;
import com.fullsecurity.touch.ItemTouchHelperAdapter;
import com.fullsecurity.touch.OnStartDragListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecyclerListAdapter extends RecyclerView.Adapter<ShoppingCartViewHolder> implements ItemTouchHelperAdapter {

    private final List<ShoppingCart> mItems = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private final OnStartDragListener mDragStartListener;
    View view;

    public RecyclerListAdapter(OnStartDragListener dragStartListener, ArrayList<ShoppingCart> shoppingCarts, LinearLayoutManager layoutManager) {
        mDragStartListener = dragStartListener;
        mItems.addAll(shoppingCarts);
        this.layoutManager = layoutManager;
    }

    @Override
    public ShoppingCartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shopping_cart_main, parent, false);
        ShoppingCartViewHolder itemViewHolder = new ShoppingCartViewHolder(view, layoutManager);
        return itemViewHolder;
    }

    @Override
    public void onBindViewHolder(ShoppingCartViewHolder viewHolder, int position) {
        ShoppingCart shoppingCart = mItems.get(position);
        int indexInView = mItems.get(position).getIndexInView();
        if (indexInView >= 0) mItems.get(position).setIndexInView(position);
        viewHolder.bind(shoppingCart);

        RelativeLayout handleView = (RelativeLayout) view.findViewById(R.id.shoppingCart);

        // Start a drag whenever the handle view it touched
        handleView.setOnTouchListener((View v, MotionEvent event) -> {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(viewHolder);
                }
                return false;
        });
    }

    @Override
    public void onItemDismiss(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mItems, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

}
