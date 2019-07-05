package com.fullsecurity.demoapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fullsecurity.common.Payload;
import com.fullsecurity.common.Rijndael;
import com.fullsecurity.server.StoreDBCreator;
import com.fullsecurity.shared.MainActivity;
import com.fullsecurity.shared.R;

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
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        storeItems = new ArrayList<>();
        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.categoriesRecyclerView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        View view = inflater.inflate(R.layout.categories_list_main, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        initialCategoriesScreen();
    }

    public void initialCategoriesScreen() {
        // This is only called once
        // It initizializes the navigation drawer for clicking on categories in later screens
        // It initializes the toolbar and the navigation drawer, so should only be called once
        StoreDBCreator storeDBCreator = mainActivity.storeDBCreator;
        String category = mainActivity.storeDBCreator.CATEGORY;
        String description = mainActivity.storeDBCreator.DESCRIPTION;
        String cost = mainActivity.storeDBCreator.COST;
        String table = mainActivity.storeDBCreator.TABLE_PRODUCTS;
        String selectQuery = "SELECT DISTINCT " + category + " FROM " + table + ";|";
        byte[] sCipher;
        Rijndael AESalgorithm = new Rijndael();
        try {
            AESalgorithm.makeKey(key, 256, AESalgorithm.DIR_ENCRYPT);
            sCipher = AESalgorithm.encryptArray(selectQuery.getBytes());
        } catch (Exception ex) {
            Log.d("JIM", "CategoriesFragment: EXCEPTION 2000: "+ ex.toString());
            ex.printStackTrace();
            String r = "EXCEPTION DURING AES ENCRYPTION";
            storeItems.add(new StoreItem(r,"ERROR READING CATEGORIES", -1, -1));
            return;
        }
        requestedMSName = "categories";
        Payload request = new Payload(1, userId, requestedMSName);
        request.setPayload(0, sCipher);
        getDataFromMicroservice(request);
    }

    private void loadCategoriesRecyclerView() {
        if (storeItems.size() == 0) storeItems.add(new StoreItem("NO","DATA", -1, -1));
        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.categoriesRecyclerView);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
        RecyclerView.Adapter adapter = new CategoriesRecyclerViewAdapter(storeItems);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void processResponseFromMicroservice(String result) {
        String[] s = result.split("[|]");
        StringBuffer sb = new StringBuffer();
        int n = s.length;
        for (int i = 0; i < n; i++) storeItems.add(new StoreItem(s[i], "", 0, 0));
        mainActivity.initalizeDecorations(storeItems);
        loadCategoriesRecyclerView();
        mainActivity.setTitle("CATEGORIES");
    }

    @Override
    protected void processErrorResponseFromMicroservice(String errorMessage) {
        String errorMicroserviceName = null;
        int errorValueOrNormalReturnValue = -Integer.parseInt(errorMessage.substring(4, 6));
        if (errorMessage.length() > 6) errorMicroserviceName = errorMessage.substring(7);
        storeItems.add(new StoreItem(messageGetter(errorValueOrNormalReturnValue, errorMicroserviceName), "ERROR IN REQUEST", -1, -1));
        mainActivity.initalizeDecorations(storeItems);
        loadCategoriesRecyclerView();
    }

}
