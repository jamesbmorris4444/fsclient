package com.fullsecurity.demoapplication;

import android.content.Context;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fullsecurity.common.Payload;
import com.fullsecurity.common.Rijndael;
import com.fullsecurity.server.PurchaseDBCreator;
import com.fullsecurity.shared.MainActivity;
import com.fullsecurity.shared.R;

import java.util.ArrayList;

@SuppressWarnings("all")
public class ShoppingCartFragment extends ClientCommunicationsWrapper {

    private Context context;
    private ArrayList<ShoppingCart> shoppingCarts;
    private String requestedMSName;
    public byte[] key;
    private MainActivity mainActivity;
    public int userId;
    private RecyclerView recyclerView;
    private ShoppingCartRecyclerViewAdapter adapter;
    private ItemTouchHelper mItemTouchHelper;
    public ArrayList<ShoppingCart> deleted;

    public ShoppingCartFragment(Context context, byte[] key, MainActivity m, int userId) {
        super(key);
        this.context = context;
        this.key = key;
        mainActivity = m;
        this.userId = userId;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        shoppingCarts = new ArrayList<>();
        recyclerView = (RecyclerView) getActivity().findViewById(R.id.cartRecyclerView);
        adapter = new ShoppingCartRecyclerViewAdapter(shoppingCarts, this);
        recyclerView.setAdapter(adapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        deleted = new ArrayList<>();

        View view = inflater.inflate(R.layout.shopping_cart_list_main, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        initialShoppingCartScreen(userId);
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
                             " WHERE " + name + " = 'person" + (userId+1) + "' AND " + status + " = 'ordered';|";
        executeMicroservice(selectQuery, "shoppingcart");
    }

    private void executeMicroservice(String query, String requestedMSName) {
        byte[] sCipher;
        shoppingCarts.clear();
        Rijndael AESalgorithm = new Rijndael();
        try {
            AESalgorithm.makeKey(key, 256, AESalgorithm.DIR_ENCRYPT);
            sCipher = AESalgorithm.encryptArray(query.getBytes());
        } catch (Exception ex) {
            Log.d("JIM", "shoppingCartFragment: EXCEPTION 2000: " + ex.toString());
            ex.printStackTrace();
            String r = "EXCEPTION DURING AES ENCRYPTION";
            shoppingCarts.add(new ShoppingCart("", r, "", -1, -1, -1));
            return;
        }
        Payload request = new Payload(1, userId, requestedMSName);
        request.setPayload(0, sCipher);
        getDataFromMicroservice(request);
    }

    @Override
    protected void processResponseFromMicroservice(String result) {
        if (!result.startsWith("DELETED")) {
            String[] s = result.split("[|]");
            StringBuffer sb = new StringBuffer();
            int n = s.length;
            for (int i = 0; i < n; i++) {
                String[] t = s[i].split("[,]");
                Log.d("JIM","REMOVE len="+t.length+"  |  "+s[i]);
                shoppingCarts.add(new ShoppingCart(t[0], t[1], t[2], Integer.parseInt(t[3]), Integer.parseInt(t[4]), Integer.parseInt(t[5])));
            }
            if (shoppingCarts.size() == 0) shoppingCarts.add(new ShoppingCart("","NO DATA", "", -1, -1, -1));
            adapter.notifyDataSetChanged();
            mainActivity.setTitle("SHOPPING CART");
        }
    }

    @Override
    protected void processErrorResponseFromMicroservice(String errorMessage) {
        String errorMicroserviceName = null;
        int errorValueOrNormalReturnValue = -Integer.parseInt(errorMessage.substring(4, 6));
        if (errorMessage.length() > 6) errorMicroserviceName = errorMessage.substring(7);
        shoppingCarts.add(new ShoppingCart("", messageGetter(errorValueOrNormalReturnValue, errorMicroserviceName),"",-1, -1, -1));
        adapter.notifyDataSetChanged();
    }

    public void processDeleted() {
        if (deleted.size() > 0) {
            String deleteQuery = "DELETE";
            for (int i = 0; i < deleted.size(); i++) deleteQuery += "," + deleted.get(i).getRowid();
            Log.d("JIM", "REMOVE delete=" + deleteQuery);
            executeMicroservice(deleteQuery, "shoppingcart");
        } else
            Log.d("JIM", "REMOVE delete NOTHING TO DELETE");
        for (ShoppingCart d : deleted) Log.d("JIM", "REMOVE="+d.toString());
    }

}
