package com.fullsecurity.demoapplication;

import android.graphics.Canvas;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.fullsecurity.shoppingcart.ShoppingCart;
import com.fullsecurity.shoppingcart.ShoppingCartRecyclerViewAdapter;
import com.fullsecurity.shoppingcart.ShoppingCartViewHolder;

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

    public static final float ALPHA_FULL = 1.0f;

    private ShoppingCartRecyclerViewAdapter recyclerViewAdapter;

    @Override
    public boolean isLongPressDragEnabled() { return true; }

    @Override
    public boolean isItemViewSwipeEnabled() { return true; }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        recyclerViewAdapter = (ShoppingCartRecyclerViewAdapter) recyclerView.getAdapter();
        ShoppingCart shoppingCart = recyclerViewAdapter.getShoppingCart(viewHolder.getAdapterPosition());
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            final int swipeFlags = 0;
            return (shoppingCart.getIndexInView() >= 0 ? makeMovementFlags(dragFlags, swipeFlags) : 0);
        } else {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return (shoppingCart.getIndexInView() >= 0 ? makeMovementFlags(dragFlags, swipeFlags) : 0);
        }
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
        recyclerViewAdapter = (ShoppingCartRecyclerViewAdapter) recyclerView.getAdapter();
        if (source.getItemViewType() != target.getItemViewType()) { return false; }
        recyclerViewAdapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
        recyclerViewAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            final float alpha = ALPHA_FULL - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
            viewHolder.itemView.setAlpha(alpha);
            viewHolder.itemView.setTranslationX(dX);
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof ShoppingCartViewHolder) {
                // Let the view holder know that this item is being moved or dragged
                ShoppingCartViewHolder itemViewHolder = ((ShoppingCartViewHolder) viewHolder);
                itemViewHolder.getAdapter().onItemSelected(viewHolder.getAdapterPosition());
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        viewHolder.itemView.setAlpha(ALPHA_FULL);
        if (viewHolder instanceof ShoppingCartViewHolder) {
            // Tell the view holder it's time to restore the idle state
            ShoppingCartViewHolder itemViewHolder = ((ShoppingCartViewHolder) viewHolder);
            itemViewHolder.getAdapter().onItemClear(viewHolder.getAdapterPosition());
        }
    }
}
