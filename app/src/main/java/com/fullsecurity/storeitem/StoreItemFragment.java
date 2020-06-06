package com.fullsecurity.storeitem;

import android.content.Context;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fullsecurity.demoapplication.ClientCommunicationsWrapper;
import com.fullsecurity.demoapplication.SimpleDividerItemDecoration;
import com.fullsecurity.server.StoreDBCreator;
import com.fullsecurity.shared.MainActivity;
import com.fullsecurity.shared.R;

import java.util.ArrayList;

@SuppressWarnings("all")
public class StoreItemFragment extends ClientCommunicationsWrapper implements StoreItemClickHandler {

    private Context context;
    private ArrayList<StoreItem> storeItems;
    private String requestedMSName;
    public byte[] key;
    private MainActivity mainActivity;
    public int userId;
    public StoreItemClickHandler storeItemClickHandler;
    private RecyclerView recyclerView;
    private StoreItemRecyclerViewAdapter adapter;
    private String categoryName;

    public StoreItemFragment(Context context, byte[] key, MainActivity m, int userId, String categoryName) {
        super(key);
        this.context = context;
        this.key = key;
        mainActivity = m;
        this.userId = userId;
        this.categoryName = categoryName;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        storeItems = new ArrayList<>();
        recyclerView = (RecyclerView) getActivity().findViewById(R.id.itemsRecyclerView);
        adapter = new StoreItemRecyclerViewAdapter(storeItems, this);
        recyclerView.setAdapter(adapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        Toolbar toolbar = (Toolbar) mainActivity.findViewById(R.id.toolbar);
        mainActivity.toolbarOptionsToInclude.add(R.id.add_shopping_cart);
        mainActivity.setSupportActionBar(toolbar);
        toolbar.setTitle(categoryName);
        initialItemsScreen(categoryName);
        View view = inflater.inflate(R.layout.store_item_list_main, container, false);
        return view;
    }

    private void initialItemsScreen(String categoryName) {
        // categoryName is the category whose items will be loaded into the view
        StoreDBCreator storeDBCreator = mainActivity.storeDBCreator;
        String category = storeDBCreator.CATEGORY;
        String description = storeDBCreator.DESCRIPTION;
        String cost = storeDBCreator.COST;
        String weight = storeDBCreator.WEIGHT;
        String table = storeDBCreator.TABLE_PRODUCTS;
        String selectQuery = "SELECT " + category + "," + description + "," + cost + "," + weight + " FROM " + table + " WHERE " + category + " = '" + categoryName + "';|";
        executeMicroservice(selectQuery, "items");
    }

    public void handleItemClick(View view, StoreItem storeItem) {
        String table = mainActivity.purchaseDBCreator.TABLE_PURCHASES;
        String rowid = mainActivity.purchaseDBCreator.ROWID;
        String name = mainActivity.purchaseDBCreator.NAME;
        String description = mainActivity.purchaseDBCreator.DESCRIPTION;
        String status = mainActivity.purchaseDBCreator.STATUS;
        String cost = mainActivity.purchaseDBCreator.COST;
        String weight = mainActivity.purchaseDBCreator.WEIGHT;
        String insertQuery = "INSERT:person" + (userId+1) + ":" + storeItem.getDescription() + ":ordered:" +  storeItem.getRawCost() + ":" + storeItem.getWeight();
        executeMicroservice(insertQuery, "items");
    }

    private void executeMicroservice(String query, String requestedMSName) {
        byte[] sCipher;
        storeItems.clear();
        getDataFromMicroservice(query, "StoreItemFragment", userId, requestedMSName);
    }

    @Override
    public void processNormalResponseFromMicroservice(String result) {
        if (!result.startsWith("STORED")) {
            String[] s = result.split("[|]");
            StringBuffer sb = new StringBuffer();
            int n = s.length;
            for (int i = 0; i < n; i++) {
                String[] t = s[i].split("[,]");
                storeItems.add(new StoreItem(t[0], t[1], Integer.parseInt(t[2]), Integer.parseInt(t[3])));
            }
            if (storeItems.size() == 0) storeItems.add(new StoreItem("NO", "DATA", -1, -1));
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void processErrorResponseFromMicroservice(String errorMessage) {
        String errorMicroserviceName = null;
        int errorValueOrNormalReturnValue = -Integer.parseInt(errorMessage.substring(4, 6));
        if (errorMessage.length() > 6) errorMicroserviceName = errorMessage.substring(7);
        storeItems.add(new StoreItem(messageGetter(errorValueOrNormalReturnValue, errorMicroserviceName), "ERROR IN REQUEST", -1, -1));
        adapter.notifyDataSetChanged();
    }

}
