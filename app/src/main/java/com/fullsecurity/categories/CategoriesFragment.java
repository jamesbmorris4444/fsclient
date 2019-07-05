package com.fullsecurity.categories;

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
import com.fullsecurity.server.StoreDBCreator;
import com.fullsecurity.shared.MainActivity;
import com.fullsecurity.shared.R;
import com.fullsecurity.storeitem.StoreItem;

import java.util.ArrayList;

@SuppressWarnings("all")
public class CategoriesFragment extends ClientCommunicationsWrapper {

    private Context context;
    private ArrayList<StoreItem> storeItems;
    private String requestedMSName;
    public byte[] key;
    private MainActivity mainActivity;
    public int userId;

    public CategoriesFragment(Context context, byte[] key, MainActivity m, int userId) {
        super(key);
        this.context = context;
        this.key = key;
        mainActivity = m;
        this.userId = userId;
        storeItems = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) mainActivity.findViewById(R.id.categoriesRecyclerView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        Toolbar toolbar = (Toolbar) mainActivity.findViewById(R.id.toolbar);
        mainActivity.setSupportActionBar(toolbar);
        toolbar.setTitle("CATEGORIES");
        initialCategoriesScreen();
        View view = inflater.inflate(R.layout.categories_list_main, container, false);
        return view;
    }

    public void initialCategoriesScreen() {
        StoreDBCreator storeDBCreator = mainActivity.storeDBCreator;
        String category = mainActivity.storeDBCreator.CATEGORY;
        String description = mainActivity.storeDBCreator.DESCRIPTION;
        String cost = mainActivity.storeDBCreator.COST;
        String table = mainActivity.storeDBCreator.TABLE_PRODUCTS;
        String selectQuery = "SELECT DISTINCT " + category + " FROM " + table + ";|";
        getDataFromMicroservice(selectQuery, "CategoriesFragment", userId, "categories");
    }

    @Override
    public void processNormalResponseFromMicroservice(String result) {
        String[] s = result.split("[|]");
        StringBuffer sb = new StringBuffer();
        int n = s.length;
        for (int i = 0; i < n; i++) storeItems.add(new StoreItem(s[i], "", 0, 0));
        mainActivity.initalizeDecorations(storeItems); // storeItems contains only unique categories
        loadCategoriesRecyclerView();
    }

    @Override
    public void processErrorResponseFromMicroservice(String errorMessage) {
        String errorMicroserviceName = null;
        int errorValueOrNormalReturnValue = -Integer.parseInt(errorMessage.substring(4, 6));
        if (errorMessage.length() > 6) errorMicroserviceName = errorMessage.substring(7);
        storeItems.add(new StoreItem(messageGetter(errorValueOrNormalReturnValue, errorMicroserviceName), "ERROR IN REQUEST", -1, -1));
        mainActivity.initalizeDecorations(storeItems);
        loadCategoriesRecyclerView();
    }

    private void loadCategoriesRecyclerView() {
        if (storeItems.size() == 0) storeItems.add(new StoreItem("NO","DATA", -1, -1));
        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.categoriesRecyclerView);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
        RecyclerView.Adapter adapter = new CategoriesRecyclerViewAdapter(storeItems);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

}
