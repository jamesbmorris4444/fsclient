package com.fullsecurity.demoapplication;

import android.view.View;

public class ShoppingCartClickHandler {

    ShoppingCartFragment shoppingCartFragment;

    public ShoppingCartClickHandler(ShoppingCartFragment shoppingCartFragment) {
        this.shoppingCartFragment = shoppingCartFragment;
    }

    public void handleShoppingCartClick(View view, ShoppingCart shoppingCart) {
        shoppingCartFragment.handleShoppingCartClick(view, shoppingCart);
    }
}