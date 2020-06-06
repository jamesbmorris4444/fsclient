package com.fullsecurity.shoppingcart;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.fullsecurity.common.Utilities;

public class ShoppingCart extends BaseObservable {
    private String name;
    private String description;
    private String status;
    private int cost;
    private int weight;
    private int rowid;
    private int index;

    public ShoppingCart(String name, String description, String status, int cost, int weight, int rowid, int index) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.cost = cost;
        this.weight = weight;
        this.rowid = rowid;
        this.index = index;
    }

    public int getRowid() {
        return rowid;
    }

    public void setRowid(int rowid) {
        this.rowid = rowid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Bindable
    public String getDescription() {
        if (weight == 0 || cost < 0)
            return ellipsize(description);
        else
            return ellipsize(description) + " (" + weight + " lbs)";
    }

    private String ellipsize(String s) {
        int len = s.length();
        if (len <= 16 || cost < 0) return s;
        return s.substring(0,16).trim() + "...";
    }

    public String getRawDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Bindable
    public String getCost() {
        return (cost < 0 ? "" : Utilities.displayablePrice(cost));
    }

    public int getRawCost() { return cost; }

    public void setCost(int cost) {  this.cost = cost; }

    public int getWeight() { return weight; }

    public int getRawWeight() { return weight; }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String toString() {
        return "["+name+", "+description +", "+status+", "+getRawCost() + ", " + weight + ",rowid=" + rowid + ",index=" + index + "]";
    }
}
