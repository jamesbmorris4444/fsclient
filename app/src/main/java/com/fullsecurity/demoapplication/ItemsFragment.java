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
public class ItemsFragment extends ClientCommunicationsWrapper {

    private Context context;
    private ArrayList<StoreItem> storeItems;
    private String requestedMSName;
    private byte[] key;
    private MainActivity mainActivity;
    private int userId;
    public ItemsClickHandler itemsClickHandler;

    public ItemsFragment(Context context, byte[] key, MainActivity m, int userId) {
        super(key);
        this.context = context;
        this.key = key;
        mainActivity = m;
        this.userId = userId;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.itemsRecyclerView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        View view = inflater.inflate(R.layout.items_main, container, false);
        return view;
    }

    public void initialItemsScreen(String categoryName) {
        // categoryName is the category whose items will be loaded into the view
        StoreDBCreator storeDBCreator = mainActivity.storeDBCreator;
        String category = mainActivity.storeDBCreator.CATEGORY;
        String description = mainActivity.storeDBCreator.DESCRIPTION;
        String cost = mainActivity.storeDBCreator.COST;
        String weight = mainActivity.storeDBCreator.WEIGHT;
        String table = mainActivity.storeDBCreator.TABLE_PRODUCTS;
        String selectQuery = "SELECT " + category + "," + description + "," + cost + "," + weight + " FROM " + table + " WHERE " + category + " = '" + categoryName + "';|";
        storeItems = new ArrayList<>();
        executeMicroservice(selectQuery, "items");
    }

    public void handleItemClick(View view, StoreItem storeItem) {
        String table = mainActivity.purchaseDBCreator.TABLE_PURCHASES;
        String insertQuery = "INSERT INTO " + table + " VALUES(NULL," +
                             "'person" + (userId+1) + "'," +            // name
                             "'" + storeItem.getDescription() + "'," +  // description
                             "'ordered'," +                             // status
                             storeItem.getRawCost() + "," +             // cost
                             storeItem.getWeight() + ");";              // weight
        Log.d("JIM","REMOVE="+insertQuery);
        executeMicroservice(insertQuery, "items");
    }

    private void executeMicroservice(String query, String requestedMSName) {
        byte[] sCipher;
        Rijndael AESalgorithm = new Rijndael();
        try {
            AESalgorithm.makeKey(key, 256, AESalgorithm.DIR_ENCRYPT);
            sCipher = AESalgorithm.encryptArray(query.getBytes());
        } catch (Exception ex) {
            Log.d("JIM", "ItemsFragment: EXCEPTION 2000: " + ex.toString());
            ex.printStackTrace();
            String r = "EXCEPTION DURING AES ENCRYPTION";
            storeItems.add(new StoreItem(r, "ERROR READING CATEGORIES", -1, -1));
            return;
        }
        Payload request = new Payload(1, userId, requestedMSName);
        request.setPayload(0, sCipher);
        getDataFromMicroservice(request);
    }

    private void loadItemsRecyclerView() {
        if (storeItems.size() == 0) storeItems.add(new StoreItem("NO", "DATA", -1, -1));
        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.itemsRecyclerView);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
        RecyclerView.Adapter adapter = new ItemsRecyclerViewAdapter(storeItems, this);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void processResponseFromMicroservice(String result) {
        if (!result.equals("STORED")) {
            String[] s = result.split("[|]");
            StringBuffer sb = new StringBuffer();
            int n = s.length;
            for (int i = 0; i < n; i++) {
                String[] t = s[i].split("[,]");
                storeItems.add(new StoreItem(t[0], t[1], Integer.parseInt(t[2]), Integer.parseInt(t[3])));
            }
            mainActivity.initalizeDecorations(storeItems);
            loadItemsRecyclerView();
            mainActivity.setTitle("ITEMS");
        }
    }

    @Override
    protected void processErrorResponseFromMicroservice(String errorMessage) {
        String errorMicroserviceName = null;
        int errorValueOrNormalReturnValue = -Integer.parseInt(errorMessage.substring(4, 6));
        if (errorMessage.length() > 6) errorMicroserviceName = errorMessage.substring(7);
        storeItems.add(new StoreItem(messageGetter(errorValueOrNormalReturnValue, errorMicroserviceName), "ERROR IN REQUEST", -1, -1));
        mainActivity.initalizeDecorations(storeItems);
        loadItemsRecyclerView();
    }

}
