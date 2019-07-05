package com.fullsecurity.demoapplication;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.fullsecurity.common.Utilities;

public class StoreItem extends BaseObservable {
    private String category;
    private String description;
    private int cost;
    private int weight;
    private int indexInView;

    public StoreItem(String category, String description, int cost, int weight) {
        this.category = category;
        this.description = description;
        this.cost = cost;
        this.weight = weight;
        indexInView = 0;
    }

    @Bindable
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Bindable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Bindable
    public String getCost() {
        return Utilities.displayablePrice(cost);
    }

    public int getRawCost() { return cost; }



    public void setCost(int cost) {
        this.cost = cost;
        //notifyPropertyChanged(BR.location);
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getIndexInView() {
        return indexInView;
    }

    public void setIndexInView(int indexInView) {
        this.indexInView = indexInView;
    }
}
