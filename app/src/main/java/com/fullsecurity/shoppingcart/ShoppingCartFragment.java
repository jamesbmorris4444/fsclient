package com.fullsecurity.shoppingcart;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fullsecurity.demoapplication.ClientCommunicationsWrapper;
import com.fullsecurity.demoapplication.SimpleDividerItemDecoration;
import com.fullsecurity.server.PurchaseDBCreator;
import com.fullsecurity.shared.MainActivity;
import com.fullsecurity.shared.R;

import java.util.ArrayList;

@SuppressWarnings("all")
public class ShoppingCartFragment extends ClientCommunicationsWrapper  implements ShoppingCartClickHandler {

    private Context context;
    public ArrayList<ShoppingCart> shoppingCarts;
    private String requestedMSName;
    public byte[] key;
    private MainActivity mainActivity;
    public int userId;
    private RecyclerView recyclerView;
    private ShoppingCartRecyclerViewAdapter adapter;
    public ArrayList<ShoppingCart> deleted;

    public ShoppingCartFragment(Context context, byte[] key, MainActivity m, int userId) {
        super(key);
        this.context = context;
        this.key = key;
        mainActivity = m;
        this.userId = userId;
        shoppingCarts = new ArrayList<>();
        deleted = new ArrayList<>();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        shoppingCarts = new ArrayList<>();
        recyclerView = (RecyclerView) getActivity().findViewById(R.id.cartRecyclerView);
        adapter = new ShoppingCartRecyclerViewAdapter(shoppingCarts, this, recyclerView);
        recyclerView.setAdapter(adapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        Toolbar toolbar = (Toolbar) mainActivity.findViewById(R.id.toolbar);
        mainActivity.toolbarOptionsToInclude.add(R.id.local_shipping);
        mainActivity.toolbarOptionsToInclude.add(R.id.credit_card);
        mainActivity.setSupportActionBar(toolbar);
        toolbar.setTitle("SHOPPING CART");
        View view = inflater.inflate(R.layout.shopping_cart_list_main, container, false);
        initialShoppingCartScreen(userId);
        return view;
    }

    public void initialShoppingCartScreen(int userId) {
        // userId is used to determine the person name, as in person<userId>
        PurchaseDBCreator purchaseDBCreator = mainActivity.purchaseDBCreator;
        String name = purchaseDBCreator.NAME;
        String description = purchaseDBCreator.DESCRIPTION;
        String status = purchaseDBCreator.STATUS;
        String cost = purchaseDBCreator.COST;
        String weight = purchaseDBCreator.WEIGHT;
        String rowid = purchaseDBCreator.ROWID;
        String table = purchaseDBCreator.TABLE_PURCHASES;
        String selectQuery = "SELECT " + name + "," +  description + "," + status + "," + cost + "," + weight + "," + rowid + " FROM " + table +
                             " WHERE " + name + " = 'person" + (userId+1) + "' AND " + status + " MATCHES 'ordered*';|";
        executeMicroservice(selectQuery, "shoppingcart");
    }

    private void executeMicroservice(String query, String requestedMSName) {
        getDataFromMicroservice(query, "ShoppingCartFragment", userId, requestedMSName);
    }

    public void enableDeleteIcon() {
        Toolbar toolbar = (Toolbar) mainActivity.findViewById(R.id.toolbar);
        mainActivity.toolbarOptionsToInclude.add(R.id.cached);
        mainActivity.setSupportActionBar(toolbar);
    }

    public void handleItemClick(View view, ShoppingCart shoppingCart) {
        int position = shoppingCart.getIndex();
        if (position < 0) return;
        if (deleted.size() == 0) enableDeleteIcon();
        deleted.add(shoppingCarts.get(position));
        shoppingCarts.remove(position);
        for (int i = position; i < shoppingCarts.size(); i++) shoppingCarts.get(i).setIndex(i);
        adapter.notifyItemRemoved(position);
    }

    @Override
    public void processNormalResponseFromMicroservice(String result) {
        if (result.startsWith("DELETED")) {
            deleted.clear();
            shoppingCarts.clear();
            initialShoppingCartScreen(userId);
            Toolbar toolbar = (Toolbar) mainActivity.findViewById(R.id.toolbar);
            mainActivity.toolbarOptionsToInclude.add(R.id.local_shipping);
            mainActivity.toolbarOptionsToInclude.add(R.id.credit_card);
            mainActivity.setSupportActionBar(toolbar);
        } else {
            String[] s = result.split("[|]");
            StringBuffer sb = new StringBuffer();
            int n = s.length;
            for (int i = 0; i < n; i++) {
                String[] t = s[i].split("[,]");
                int rowid = Integer.parseInt(t[5]);
                shoppingCarts.add(new ShoppingCart(t[0], t[1], t[2], Integer.parseInt(t[3]), Integer.parseInt(t[4]), rowid, (rowid < 0 ? rowid : i)));
            }
            if (shoppingCarts.size() == 0) shoppingCarts.add(new ShoppingCart("","NO DATA", "", -1, -1, -1, -1));
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void processErrorResponseFromMicroservice(String errorMessage) {
        String errorMicroserviceName = null;
        int errorValueOrNormalReturnValue = -Integer.parseInt(errorMessage.substring(4, 6));
        if (errorMessage.length() > 6) errorMicroserviceName = errorMessage.substring(7);
        shoppingCarts.add(new ShoppingCart("", messageGetter(errorValueOrNormalReturnValue, errorMicroserviceName),"",-1, -1, -1, -1));
        adapter.notifyDataSetChanged();
    }

    public void processDeleted() {
        if (deleted.size() > 0) {
            String deleteQuery = "DELETE";
            for (int i = 0; i < deleted.size(); i++) deleteQuery += "," + deleted.get(i).getRowid();
            executeMicroservice(deleteQuery, "shoppingcart");
        }
    }

}
