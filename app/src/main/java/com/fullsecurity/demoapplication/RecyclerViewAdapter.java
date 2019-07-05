package com.fullsecurity.demoapplication;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fullsecurity.shared.R;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>  {

    ArrayList<StoreItem> storeItems;
    View view;

    public RecyclerViewAdapter(ArrayList<StoreItem> storeItems) {
        this.storeItems = storeItems;
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapter.ViewHolder Viewholder, int i) {
        Viewholder.categoryTextView.setText(storeItems.get(i).getCategory());
        String name = storeItems.get(i).getName();
        if (name.length() == 0)
            Viewholder.nameTextView.setVisibility(View.GONE);
        else
            Viewholder.nameTextView.setText(storeItems.get(i).getName());
        int cost = storeItems.get(i).getCost();
        if (cost == 0)
            Viewholder.costTextView.setVisibility(View.GONE);
        else
            Viewholder.costTextView.setText(displayablePrice(storeItems.get(i).getCost()));
    }

    public static String displayablePrice(int price) {
        String dollarString;
        int cents = price % 100;
        int dollars = price / 100;
        if (dollars < 1000)
            dollarString = Integer.toString(dollars);
        else
            dollarString = Integer.toString(dollars / 1000) + "," + String.format("%03d", dollars % 1000);
        return "$" + dollarString + (cents < 10 ? ".0"+cents : "." + cents);
    }

    @Override
    public int getItemCount() {
        int n = storeItems.size();
        return n;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView categoryTextView;
        TextView nameTextView;
        TextView costTextView;
        public ViewHolder(View view) {
            super(view);
            categoryTextView = (TextView) view.findViewById(R.id.category);
            nameTextView = (TextView) view.findViewById(R.id.name);
            costTextView = (TextView) view.findViewById(R.id.cost);
        }
    }

}
