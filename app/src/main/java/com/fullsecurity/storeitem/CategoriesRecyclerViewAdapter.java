package com.fullsecurity.storeitem;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fullsecurity.shared.R;

import java.util.ArrayList;

public class CategoriesRecyclerViewAdapter extends RecyclerView.Adapter<CategoriesRecyclerViewAdapter.ViewHolder>  {

    ArrayList<StoreItem> storeItems;
    View view;

    public CategoriesRecyclerViewAdapter(ArrayList<StoreItem> storeItems) {
        this.storeItems = storeItems;
    }

    @Override
    public CategoriesRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.store_item_main, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoriesRecyclerViewAdapter.ViewHolder Viewholder, int i) {
        Viewholder.categoryTextView.setText(storeItems.get(i).getCategory());
    }

    @Override
    public int getItemCount() {
        int n = storeItems.size();
        return n;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView categoryTextView;
        TextView descriptionTextView;
        TextView costTextView;
        public ViewHolder(View view) {
            super(view);
            categoryTextView = (TextView) view.findViewById(R.id.category);
            descriptionTextView = (TextView) view.findViewById(R.id.description);
            costTextView = (TextView) view.findViewById(R.id.cost);
            descriptionTextView.setVisibility(View.GONE);
            costTextView.setVisibility(View.GONE);
        }
    }

}
