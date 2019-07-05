package com.fullsecurity.demoapplication;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.fullsecurity.shared.R;

import java.util.ArrayList;

public class DemoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Context context;
    Resources resources;
    Activity activity;
    String packageName;

    ArrayList<StoreItem> storeItems;
    RecyclerView recyclerview;
    RecyclerView.LayoutManager RecyclerViewLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity_main);

        context = getApplicationContext();
        resources = getResources();
        activity = this;
        packageName = getApplicationContext().getPackageName();

        initialization();
    }

    private void initialization() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) toolbar.getLayoutParams();
        final float scale = resources.getDisplayMetrics().density;
        int pixels = (int) (56 * scale + 0.5f);
        layoutParams.height = pixels;
        toolbar.setLayoutParams(layoutParams);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, R.color.red)));

        // Setup navigation drawer with list of all tickets
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toggle.setDrawerIndicatorEnabled(false);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View hdrView = inflater.inflate(R.layout.demo_nav_header_main, null);
        navigationView.addHeaderView(hdrView);
        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();
        for (int k = 0; k < 5; k++) {
            menu.add(Menu.NONE, k, Menu.NONE, "Entry " + k);
        }

        recyclerview = (RecyclerView)findViewById(R.id.recyclerview);
        RecyclerViewLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerview.setLayoutManager(RecyclerViewLayoutManager);
        AddItemsToRecyclerViewArrayList();
        recyclerview.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));
        RecyclerView.Adapter adapter = new RecyclerViewAdapter(storeItems);
        recyclerview.setAdapter(adapter);
    }

    public void AddItemsToRecyclerViewArrayList(){
        storeItems = new ArrayList<>();
        storeItems.add(new StoreItem("Clothing", "Dress", 3500));
        storeItems.add(new StoreItem("Clothing", "Pants", 4499));
        storeItems.add(new StoreItem("Clothing", "Shoes", 11900));
        storeItems.add(new StoreItem("Clothing", "Hat", 1599));
        storeItems.add(new StoreItem("Clothing", "Shirt", 3500));
        storeItems.add(new StoreItem("Clothing", "Socks", 599));
        storeItems.add(new StoreItem("Book", "Gone With the Wind", 1499));
        storeItems.add(new StoreItem("Book", "A Tale of Two Cities", 2999));
        storeItems.add(new StoreItem("Book", "Fate is the Hunter", 1995));
        storeItems.add(new StoreItem("Book", "A Samll Death in Lisbon", 1000));
        storeItems.add(new StoreItem("Car", "Ford", 3500000));
        storeItems.add(new StoreItem("Car", "Chevrolet", 2900000));
        storeItems.add(new StoreItem("Car", "Porsche", 8900000));
        storeItems.add(new StoreItem("Car", "Mercedes", 7700000));
        storeItems.add(new StoreItem("Appliance", "Razor", 2567));
        storeItems.add(new StoreItem("Appliance", "Blender", 1495));
        storeItems.add(new StoreItem("Appliance", "Mixer", 4799));
        storeItems.add(new StoreItem("Appliance", "Dishwasher", 28900));
        storeItems.add(new StoreItem("Appliance", "Washer", 35000));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.demo_toolbar_main, menu);
        menu.findItem(R.id.action_clear).setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_loop:
                return true;
            case R.id.action_view_headline:
                return true;
            case R.id.action_arrow_back:
                finish();
                return true;
            case R.id.action_clear:
                activity.invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        String searchName = item.getTitle().toString().substring(7); // Strip off "Ticket "
//        int ndx = getIndexOfTicket(searchName);
//        if (ndx < 0) {
//            Date date = new java.util.Date();
//            //D.logger.write("ERROR LOG " + date + ": Search could not find ticket=" + item.getTitle().toString() + "\n");
//        } else {
//
//        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateMenuInNavigationView(NavigationView navigationView) {
        Menu menu = navigationView.getMenu();
        Log.d("JIM","COUNT="+menu.size());
        int menuSize = menu.size();
        for (int k = 0; k < menuSize; k++) {
            Log.d("JIM","REMOVE="+k);
            menu.removeItem(k);
        }
        for (int k = 0; k < 10; k++) {
            Log.d("JIM","ADD="+k);
            menu.add(Menu.NONE, k, Menu.NONE, "Ticket " + k);
        }
    }
}
