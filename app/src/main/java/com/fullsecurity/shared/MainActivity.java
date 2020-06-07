package com.fullsecurity.shared;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fullsecurity.categories.CategoriesFragment;
import com.fullsecurity.client.BottomLeftFragment;
import com.fullsecurity.client.BottomRightFragment;
import com.fullsecurity.client.TopLeftFragment;
import com.fullsecurity.client.TopRightFragment;
import com.fullsecurity.server.BaseballDBCreator;
import com.fullsecurity.server.PdfManager;
import com.fullsecurity.server.PersonDBCreator;
import com.fullsecurity.server.PurchaseDBCreator;
import com.fullsecurity.server.SDVServer;
import com.fullsecurity.server.ServerFullDirectoryPathName;
import com.fullsecurity.server.ServerState;
import com.fullsecurity.server.StoreDBCreator;
import com.fullsecurity.shoppingcart.ShoppingCartFragment;
import com.fullsecurity.storeitem.StoreItem;
import com.fullsecurity.storeitem.StoreItemFragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

@SuppressWarnings("all")
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
                                                               FragmentManager.OnBackStackChangedListener {

    private final int WRITE_EXTERNAL_STORAGE_REQUEST = 1;
    private final int READ_EXTERNAL_STORAGE_REQUEST = 2;
    private final String CATEGORY_FRAGMENT_TAG = "categoryfrag";
    private final String STORE_FRAGMENT_TAG = "storefrag";
    private final String SHOPPINGCART_FRAGMENT_TAG = "shoppingcartfrag";
    private TextView clients;
    public String hostName = "localhost";
    public String hostNameParameter;
    private TextView ipButton;
    private EditText ipAddr;
    private Context context;
    private SDVServer sdvServer;
    private final MainActivity mainActivity = this;
    public String packageName;
    public ArrayList<Integer> toolbarOptionsToInclude;
    private ArrayList<Integer> toolbarOptions;
    public boolean testingInProgress;
    private String saveCategoryNameForFragmentBackButton;
    public SQLiteDatabase baseballDatabase;
    public SQLiteDatabase storeDatabase;
    public SQLiteDatabase personDatabase;
    public SQLiteDatabase purchaseDatabase;
    public BaseballDBCreator baseballDBCreator;
    public StoreDBCreator storeDBCreator;    
    public PersonDBCreator personDBCreator;
    public PurchaseDBCreator purchaseDBCreator;

    public TopLeftFragment tlfrg;
    public TopRightFragment trfrg;
    public BottomLeftFragment blfrg;
    public BottomRightFragment brfrg;
    public CategoriesFragment categoriesFragment;
    public StoreItemFragment storeItemFragment;
    public ShoppingCartFragment shoppingCartFragment;

    // To change version number
    //   1. Add one to the versionCode in build.gradle and AndroidManifest.xml
    //   2. Change the versionName in build.gradle and AndroidManifest.xml
    //   3. Change the TextView with id=ipButton in grid_main to reflect the latest version number

    // To add another Microservice group
    //   1. Change number of buttons in SDVClient
    //      STEP 2
    //   2. Add two new TextViews at the end of fragment_common.xml (be sure to get the RelativeLayout references correct)
    //      STEP 3
    //   3. If an EditText is required, add a new "et?" declaration for an EditText (be sure to get the RelativeLayout references correct)
    //   4. Add a new case in the "main" method in SDVClient
    //   5. Add a new case in the "loadFourClientsFragments" method in SDVClient
    //   6. Add a new case in the "buttonAction" method in SDVClient
    //   7. Declare the name of a String variable in SDVClient to hold the received string from the server
    //   8. Add a String receiving processor in SDVClient, like "finalReadFileProcessor" for example
    //   9: Add a new case in clientFinalProcessor in SDVClient
    //  10. Add a new case in method processReturnValue in SDVClient
    //  11. Add a new case in method messageGetter in SDVClient
    //  12. Add new microservices properties in SDVServer
    //  13. Add processor Java classes for the new microservice

    // Measuring inter-microservice communication times
    //   T0: button click (start time)
    //       server processes the client request, connects to microservice A, and then waits for another client request
    //   T1: microservice A starts to read its input parameter
    //       microservice A reads its input parameter
    //   T2: microservice A starts to do its work
    //       microservice A completes its work, connects to microservice B, and then waits for another server request
    //   T3: microservice B starts to read its input parameter
    //       microservice B reads its input parameter
    //   T2: microservice B starts to do its work
    //       microservice B completes its work, connects to microservice C, and then waits for another microservice request
    //   T3: microservice C starts to read its input parameter
    //       microservice C reads its input parameter
    //   T2: microservice C starts to do its work
    //       microservice C completes its work, connects to microservice D, and then waits for another microservice request
    //   T3: microservice D starts to read its input parameter
    //       microservice D reads its input parameter
    //   T2: microservice D starts to do its work
    //       microservice D completes its work, gets ready to write an output back to microservice D, and then waits for another microservice request
    //   T4: microservice C starts reading its return value from microservice D
    //       microservice C reads its return value from microservice D
    //   T4: microservice B starts reading its return value from microservice C
    //       microservice B reads its return value from microservice C
    //   T4: microservice A starts reading its return value from microservice B
    //       microservice A reads its return value from microservice B
    //   T5: the client starts reading its return value from microservice A
    //       the client reads its return value from microservice A


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        boolean permissionGranted = tryPermissions();
        if (permissionGranted) loadFourClientsFragments(false);
    }

    public void loadFourClientsFragments(boolean fourFragmentsRestart) {

        tlfrg = new TopLeftFragment(this);
        trfrg = new TopRightFragment(this);
        blfrg = new BottomLeftFragment(this);
        brfrg = new BottomRightFragment(this);

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.frame11, tlfrg, "tlfrg");
        transaction.add(R.id.frame12, trfrg, "trfrg");
        transaction.add(R.id.frame21, blfrg, "blfrg");
        transaction.add(R.id.frame22, brfrg, "brfrg");
        transaction.commit();
        toolbarOptionsToInclude = new ArrayList<>();
        toolbarOptions = new ArrayList<>();
        toolbarOptions.add(R.id.local_shipping);
        toolbarOptions.add(R.id.credit_card);
        toolbarOptions.add(R.id.add_shopping_cart);
        toolbarOptions.add(R.id.cached);
        ipButton = (TextView) findViewById(R.id.ipButton);
        ipAddr = (EditText) findViewById(R.id.ipAddr);
        clients = (TextView) findViewById(R.id.clients);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        final String defaultValue = getResources().getString(R.string.ipa_default);
        hostNameParameter = sharedPref.getString(getString(R.string.ipa_settings), defaultValue);
        ipAddr.setHint(hostNameParameter);
        TextWatcher watcherIpAddress = (new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                boolean error = checkIPAddressInput();
                if (error)
                    disableIpButton();
                else
                    enableIpButton();
            }

            @Override
            public void onTextChanged(CharSequence a, int b, int c, int d) {
            }

            @Override
            public void beforeTextChanged(CharSequence a, int b, int c, int d) {
            }
        });
        ipAddr.addTextChangedListener(watcherIpAddress);
        ipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testingInProgress = false;
                hostName = hostNameParameter;
                if (!ipAddr.getText().toString().isEmpty()) hostName = ipAddr.getText().toString();
                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.ipa_settings), hostName);
                editor.commit();
                if (!hostName.equals("localhost")) {
                    testingInProgress = true;
                    hostName = "localhost";
                }
                ipAddr.setVisibility(View.GONE);
                ipButton.setVisibility(View.GONE);
                // SDVServer runs on the main thread
                // ServerListener instance is spawned by SDVServer and runs on a new thread
                // SDVServerRequestProcessor instance is spawned by ServerListener every time a new client is accepted, and it runs on a new thread
                if (fourFragmentsRestart)
                    sdvServer.serverStateList = new ArrayList<>();
                else {
                    sdvServer = new SDVServer(mainActivity);
                    sdvServer.startServer();
                }
                tlfrg.sdvClient.enableButton(0);
                trfrg.sdvClient.enableButton(0);
                blfrg.sdvClient.enableButton(0);
                brfrg.sdvClient.enableButton(0);
                if (testingInProgress) {
                    tlfrg.runTest();
                    trfrg.runTest();
                    blfrg.runTest();
                    brfrg.runTest();
                }
            }
        });

        LinearLayout ll = (LinearLayout) findViewById(R.id.categoriesView);
        ll.setVisibility(View.GONE);
        ll = (LinearLayout) findViewById(R.id.itemsView);
        ll.setVisibility(View.GONE);

        LinearLayout mainGridView = (LinearLayout) findViewById(R.id.maingridview);
        mainGridView.setVisibility(View.VISIBLE);

        View toolbarLayout = (View) findViewById(R.id.toolbarLayout);
        toolbarLayout.setVisibility(View.GONE);

        if (fourFragmentsRestart) return;

        ipButton.setEnabled(false);

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progBar);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);

        // initalize baseball baseballDatabase
        Thread dbThread = initializeDatabasesAndPdfFile(progressBar);
        dbThread.start();
    }

    private Thread initializeDatabasesAndPdfFile(ProgressBar progBar) {
        Thread dbThread = new Thread() {
            public void run() {
                Cursor cursor;
                int count;

                // create pdf file
                packageName = "com.fullsecurity.shared";
                ServerFullDirectoryPathName fdpn = new ServerFullDirectoryPathName();
                String fullDirectoryPathName = fdpn.getFullDirectoryPathName(packageName);
                String fullFilePathName = fullDirectoryPathName + "/" + "doc.pdf";
                File f = new File(fullFilePathName);
                if (!f.exists() || f.isDirectory()) new PdfManager(fullFilePathName);
                // This is how you write a file
                //String fff = directoryNameReceive + "/" + "docout.pdf";
                //readFileReceive.write(fff);

                runOnUiThread(new Runnable() {
                    public void run() {
                        progBar.setIndeterminate(false);
                        progBar.getProgressDrawable().setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
                        progBar.setScaleY(2f);
                        progBar.setMax(19106);
                        progBar.setProgress(0);
                    }
                });
                String[] allColumns = {
                        BaseballDBCreator.COLUMN_ID,
                        BaseballDBCreator.COLUMN_PLAYER_ID,
                        BaseballDBCreator.COLUMN_BIRTH_YEAR,
                        BaseballDBCreator.COLUMN_BIRTH_MONTH,
                        BaseballDBCreator.COLUMN_BIRTHDAY,
                        BaseballDBCreator.COLUMN_BIRTH_COUNTRY,
                        BaseballDBCreator.COLUMN_BIRTH_STATE,
                        BaseballDBCreator.COLUMN_BIRTH_CITY,
                        BaseballDBCreator.COLUMN_DEATH_YEAR,
                        BaseballDBCreator.COLUMN_DEATH_MONTH,
                        BaseballDBCreator.COLUMN_DEATH_DAY,
                        BaseballDBCreator.COLUMN_DEATH_COUNTRY,
                        BaseballDBCreator.COLUMN_DEATH_STATE,
                        BaseballDBCreator.COLUMN_DEATH_CITY,
                        BaseballDBCreator.COLUMN_NAME_FIRST,
                        BaseballDBCreator.COLUMN_NAME_LAST,
                        BaseballDBCreator.COLUMN_NAME_GIVEN,
                        BaseballDBCreator.COLUMN_WEIGHT,
                        BaseballDBCreator.COLUMN_HEIGHT,
                        BaseballDBCreator.COLUMN_BATS,
                        BaseballDBCreator.COLUMN_THROWS,
                        BaseballDBCreator.COLUMN_DEBUT,
                        BaseballDBCreator.COLUMN_FINAL_GAME,
                        BaseballDBCreator.COLUMN_RETRO_ID,
                        BaseballDBCreator.COLUMN_BBREF_ID
                };

                baseballDBCreator = new BaseballDBCreator(mainActivity);
                baseballDatabase = baseballDBCreator.getWritableDatabase();
                String countQuery = "SELECT * FROM " + BaseballDBCreator.TABLE_MASTER;
                cursor = baseballDatabase.rawQuery(countQuery, null);
                count = cursor.getCount();
                cursor.close();
                if (count == 0) {
                    BufferedReader input = null;
                    try {
                        InputStream is = getResources().openRawResource(R.raw.master);
                        input = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                        readOneCSVLine(input); // ignore the firt line of Master.csv
                        int lineCount = 0;
                        while (true) {
                            String[] splitCSVLine = readOneCSVLine(input);
                            if (splitCSVLine == null) break;
                            lineCount += 1;
                            ContentValues values = new ContentValues();
                            int splitLength = splitCSVLine.length;
                            int rowLength = allColumns.length - 1;
                            int firstLength = (splitLength < rowLength ? splitLength : rowLength);
                            for (int i = 1; i <= firstLength; i++)
                                values.put(allColumns[i], splitCSVLine[i - 1]);
                            if (splitLength < rowLength)
                                for (int i = splitLength + 1; i <= rowLength; i++)
                                    values.put(allColumns[i], "");
                            long insertId = baseballDatabase.insert(BaseballDBCreator.TABLE_MASTER, null, values);
                            final int lc = lineCount;
                            mainActivity.runOnUiThread(new Runnable() {
                                public void run() {
                                    progBar.setProgress(lc);
                                    clients.setText("" + lc + "/19106");
                                }
                            });
                        }
                    } catch (Exception e) {
                        Log.d("JIM", "EXCEPTION READING DATABASE=" + e.toString());
                    } finally {
                        try {
                            input.close();
                        } catch (Exception g) { }
                    }
                }
                initializeECommerceDatabases(progBar);
            }
        };
        return dbThread;
    }

    private void initializeECommerceDatabases(ProgressBar progBar) {
        Cursor cursor;
        int count;
        int lineCount = 0;
        runOnUiThread(new Runnable() {
            public void run() {
                progBar.setMax(88);
                progBar.setProgress(0);
            }
        });
        String[] allProductColumns = {
                StoreDBCreator.ROWID,
                StoreDBCreator.CATEGORY,
                StoreDBCreator.DESCRIPTION,
                StoreDBCreator.COST,
                StoreDBCreator.WEIGHT
        };
        storeDBCreator = new StoreDBCreator(mainActivity);
        storeDatabase = storeDBCreator.getWritableDatabase();
        String qCount = "SELECT * FROM " + storeDBCreator.TABLE_PRODUCTS;
        cursor = storeDatabase.rawQuery(qCount, null);
        count = cursor.getCount();
        cursor.close();
        if (count == 0) {
            BufferedReader input = null;
            try {
                InputStream is = getResources().openRawResource(R.raw.products);
                input = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while (true) {
                    String[] splitCSVLine = readOneCSVLine(input);
                    if (splitCSVLine == null) break;
                    lineCount += 1;
                    ContentValues values = new ContentValues();
                    int splitLength = splitCSVLine.length;
                    int rowLength = allProductColumns.length-1;
                    int firstLength = (splitLength < rowLength ? splitLength : rowLength);
                    for (int i = 1; i <= firstLength; i++) values.put(allProductColumns[i], splitCSVLine[i-1]);
                    if (splitLength < rowLength) for (int i = splitLength+1; i <= rowLength; i++) values.put(allProductColumns[i], "");
                    long insertId = storeDatabase.insert(storeDBCreator.TABLE_PRODUCTS, null, values);
                    final int lc = lineCount;
                    mainActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            progBar.setProgress(lc);
                            clients.setText("" + lc + "/88");
                        }
                    });
                }
            } catch (Exception e) {
                Log.d("JIM","EXCEPTION READING DATABASE="+e.toString());
                e.printStackTrace();
            } finally {
                try {
                    input.close();
                } catch (Exception g) { }
            }
        }

        String[] allPersonColumns = {
                PersonDBCreator.ROWID,
                PersonDBCreator.NAME,
                PersonDBCreator.CARDS
        };
        personDBCreator = new PersonDBCreator(mainActivity);
        personDatabase = personDBCreator.getWritableDatabase();
        String pCount = "SELECT * FROM " + personDBCreator.TABLE_PERSONS;
        cursor = personDatabase.rawQuery(pCount, null);
        count = cursor.getCount();
        cursor.close();
        if (count == 0) {
            BufferedReader input = null;
            try {
                InputStream is = getResources().openRawResource(R.raw.persons);
                input = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while (true) {
                    String[] splitCSVLine = readOneCSVLine(input);
                    if (splitCSVLine == null) break;
                    lineCount += 1;
                    ContentValues values = new ContentValues();
                    int splitLength = splitCSVLine.length;
                    int rowLength = allPersonColumns.length-1;
                    int firstLength = (splitLength < rowLength ? splitLength : rowLength);
                    for (int i = 1; i <= firstLength; i++) values.put(allPersonColumns[i], splitCSVLine[i-1]);
                    if (splitLength < rowLength) for (int i = splitLength+1; i <= rowLength; i++) values.put(allPersonColumns[i], "");
                    long insertId = personDatabase.insert(personDBCreator.TABLE_PERSONS, null, values);
                    final int lc = lineCount;
                    mainActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            progBar.setProgress(lc);
                            clients.setText("" + lc + "/88");
                        }
                    });
                }
            } catch (Exception e) {
                Log.d("JIM","EXCEPTION READING DATABASE="+e.toString());
                e.printStackTrace();
            } finally {
                try {
                    input.close();
                } catch (Exception g) { }
            }
        }

        String[] allPurchaseColumns = {
                PurchaseDBCreator.ROWID,
                PurchaseDBCreator.NAME,
                PurchaseDBCreator.DESCRIPTION,
                PurchaseDBCreator.STATUS,
                PurchaseDBCreator.COST,
                PurchaseDBCreator.WEIGHT
        };
        purchaseDBCreator = new PurchaseDBCreator(mainActivity);
        purchaseDatabase = purchaseDBCreator.getWritableDatabase();
        String uCount = "SELECT * FROM " + purchaseDBCreator.TABLE_PURCHASES;
        cursor = purchaseDatabase.rawQuery(uCount, null);
        count = cursor.getCount();
        cursor.close();
        if (count == 0) {
            BufferedReader input = null;
            try {
                InputStream is = getResources().openRawResource(R.raw.purchases);
                input = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while (true) {
                    String[] splitCSVLine = readOneCSVLine(input);
                    if (splitCSVLine == null) break;
                    lineCount += 1;
                    ContentValues values = new ContentValues();
                    int splitLength = splitCSVLine.length;
                    int rowLength = allPurchaseColumns.length-1;
                    int firstLength = (splitLength < rowLength ? splitLength : rowLength);
                    for (int i = 1; i <= firstLength; i++) values.put(allPurchaseColumns[i], splitCSVLine[i-1]);
                    if (splitLength < rowLength) for (int i = splitLength+1; i <= rowLength; i++) values.put(allPurchaseColumns[i], "");
                    long insertId = purchaseDatabase.insert(purchaseDBCreator.TABLE_PURCHASES, null, values);
                    final int lc = lineCount;
                    mainActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            progBar.setProgress(lc);
                            clients.setText("" + lc + "/88");
                        }
                    });
                }
            } catch (Exception e) {
                Log.d("JIM","EXCEPTION READING DATABASE="+e.toString());
                e.printStackTrace();
            } finally {
                try {
                    input.close();
                } catch (Exception g) { }
            }
        }
        runOnUiThread(new Runnable() {
            public void run() {
                progBar.setVisibility(View.GONE);
                clients.setText("Initialization Completed");
                ipButton.setEnabled(true);
            }
        });
    }

    private void setCategoryVisibility() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.categoriesView);
        ll.setVisibility(View.VISIBLE);
        ll = (LinearLayout) findViewById(R.id.itemsView);
        ll.setVisibility(View.GONE);
        FrameLayout llf = (FrameLayout) findViewById(R.id.cartView);
        llf.setVisibility(View.GONE);
    }

    private void setStoreItemVisibility() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.itemsView);
        ll.setVisibility(View.VISIBLE);
        ll = (LinearLayout) findViewById(R.id.categoriesView);
        ll.setVisibility(View.GONE);
        FrameLayout llf = (FrameLayout) findViewById(R.id.cartView);
        llf.setVisibility(View.GONE);
    }

    private void setShoppingCartVisibility() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.itemsView);
        ll.setVisibility(View.GONE);
        ll = (LinearLayout) findViewById(R.id.categoriesView);
        ll.setVisibility(View.GONE);
        FrameLayout llf = (FrameLayout) findViewById(R.id.cartView);
        llf.setVisibility(View.VISIBLE);;
    }

    public void loadCategoriesFragment(byte[] key, int userId) {
        setCategoryVisibility();
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        categoriesFragment = new CategoriesFragment(context, key, this, userId);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction
                .replace(R.id.categoriesRecyclerFrame, categoriesFragment, CATEGORY_FRAGMENT_TAG)
                .addToBackStack(null)
                .commit();
    }

    public void loadStoreItemFragment(byte[] key, int userId, String categoryName) {
        saveCategoryNameForFragmentBackButton = categoryName;
        setStoreItemVisibility();
        storeItemFragment = new StoreItemFragment(context, key, this, userId, categoryName);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction
                .replace(R.id.itemsRecyclerFrame, storeItemFragment, STORE_FRAGMENT_TAG)
                .addToBackStack(null)
                .commit();
    }

    public void loadShoppingCartFragment(byte[] key, int userId) {
        setShoppingCartVisibility();
        shoppingCartFragment = new ShoppingCartFragment(context, key, this, userId);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction
                .replace(R.id.cartView, shoppingCartFragment, SHOPPINGCART_FRAGMENT_TAG)
                .addToBackStack(null)
                .commit();
    }

    public void initalizeDecorations(ArrayList<StoreItem> storeItems) {
        // storeItems is a special list of category names
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) toolbar.getLayoutParams();
        final float scale = getResources().getDisplayMetrics().density;
        int pixels = (int) (56 * scale + 0.5f);
        layoutParams.height = pixels;
        toolbar.setLayoutParams(layoutParams);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, R.color.red)));

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toggle.setDrawerIndicatorEnabled(false);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View hdrView = inflater.inflate(R.layout.nav_header_main, null);
        navigationView.addHeaderView(hdrView);
        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();
        for (int k = 0; k < storeItems.size(); k++) menu.add(Menu.NONE, k, Menu.NONE, storeItems.get(k).getCategory());

        LinearLayout mainGridView = (LinearLayout) findViewById(R.id.maingridview);
        mainGridView.setVisibility(View.GONE);

        View toolbarLayout = (View) findViewById(R.id.toolbarLayout);
        toolbarLayout.setVisibility(View.VISIBLE);
    }

    private String[] readOneCSVLine(BufferedReader brd) {
        String delims = "[,]";
        try {
            String s = brd.readLine();
            if (s == null) return null;
            return s.split(delims);
        } catch (Exception e) {
            Log.d("JIM", "Exception when reading one line: "+e.toString());
            return null;
        }
    }

    public long expireTime() {
        return (testingInProgress ? 12L : sdvServer.LIFETIME_OF_CLIENT_STATE_IN_SECS);
    }

    public void updateClientState() {
        StringBuffer sb = new StringBuffer();
        int n = sdvServer.serverStateList.size();
        for (int k = 0; k < n; k++ ) {
            ServerState c = sdvServer.serverStateList.get(k);
            sb.append(c.toString());
            if (c.hasExpired(expireTime())) sb.append(" EXPIRED");
            if (k < n-1) sb.append('\n');
        }
        clients.setText(sb.toString());
    }

    private void disableIpButton() {
        ipButton.setEnabled(false);
        ipButton.setTextColor(ContextCompat.getColor(context, R.color.blue));
    }

    private void enableIpButton() {
        ipButton.setEnabled(true);
        ipButton.setTextColor(ContextCompat.getColor(context, R.color.white));
    }

    private boolean checkIPAddressInput() {
        boolean error = false;
        if (ipAddr.getText().toString().length() == 0 || ipAddr.getText().toString().equals("localhost")) {
            ipAddr.setTextColor(error ? ContextCompat.getColor(context, R.color.red) : ContextCompat.getColor(context, R.color.black));
            return false;
        }
        String fieldText = ipAddr.getText().toString().trim();
        error = !isIPAddress(fieldText);
        ipAddr.setTextColor(error ? ContextCompat.getColor(context, R.color.red) : ContextCompat.getColor(context, R.color.black));
        return error;
    }

    private boolean isIPAddress(String ip) {
        if (ip == null || ip == "") return false;
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;
        for (String s : parts) {
            if (!isNumeric(s)) return false;
            int i = Integer.parseInt(s);
            if (i < 0 || i > 255) return false;
        }
        return true;
    }

    private boolean isNumeric(String s) {
        if (s == null) return false;
        boolean nonNumericFound = false;
        int n = s.length();
        if (n == 0) return false;
        int k = 0;
        while (k < n && !nonNumericFound) { nonNumericFound = (s.charAt(k) < '0' || s.charAt(k) > '9'); k += 1; };
        return !nonNumericFound;
    };

    private boolean tryPermissions() {
        boolean wp = false;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            wp = true;
        else
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST);
        return wp;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_REQUEST: {
                // If request is not granted, the result arrays are empty.
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // permission granted (this path is never taken)
                    } else {
                        // permission denied
                        finish();
                    }
                } else {
                    // permission denied
                    finish();
                }
                return;
            }
        }
    }

    // toolbar and navigation drawer

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.demo_toolbar_main, menu);
        for (int ident : toolbarOptions) if (!toolbarOptionsToInclude.contains(ident)) menu.removeItem(ident);
        toolbarOptionsToInclude.clear();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.local_shipping:
                return true;
            case R.id.credit_card:
                return true;
            case R.id.add_shopping_cart:
                loadShoppingCartFragment(storeItemFragment.key, storeItemFragment.userId);
                return true;
            case R.id.cached:
                if (shoppingCartFragment != null) shoppingCartFragment.processDeleted();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation drawer item clicks here
        loadStoreItemFragment(categoriesFragment.getKey(), categoriesFragment.getUserId(), item.getTitle().toString());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void updateMenuInNavigationView(NavigationView navigationView) {
        // handle items in navigation drawer here
        Menu menu = navigationView.getMenu();
        int menuSize = menu.size();
        for (int k = 0; k < menuSize; k++) {
            menu.removeItem(k);
        }
        for (int k = 0; k < 10; k++) {
            menu.add(Menu.NONE, k, Menu.NONE, "Ticket " + k);
        }
    }

    private void goBackToPreviousFragment(FragmentManager manager) {
        // getBackStackEntryCount() == 1 : category screen (backstack is has entry for 4-fragment)
        // getBackStackEntryCount() == 2 : storeItems shopping screen for selected category (backstack = category)
        // getBackStackEntryCount() == 3 : shopping cart screen (backstack = store items)
        int fragmentBackStackCount = manager.getBackStackEntryCount();
        manager.popBackStackImmediate();
        switch (fragmentBackStackCount) {
            case 1:
                setContentView(R.layout.activity_main);
                loadFourClientsFragments(true);
                break;
            case 2:
                setCategoryVisibility();
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                toolbarOptionsToInclude.clear();
                setSupportActionBar(toolbar);
                toolbar.setTitle("CATEGORIES");
                break;
            case 3:
                setStoreItemVisibility();
                toolbar = (Toolbar) findViewById(R.id.toolbar);
                toolbarOptionsToInclude.add(R.id.add_shopping_cart);
                setSupportActionBar(toolbar);
                toolbar.setTitle(saveCategoryNameForFragmentBackButton);
                break;
            case 4:
                setShoppingCartVisibility();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackStackChanged() { /*showBackStack();*/ }

    public void showBackStack() {
        int numberOfBackStackEntries = getSupportFragmentManager().getBackStackEntryCount();
        Log.d("JIM", "BackStackEntryCount=" + numberOfBackStackEntries);
        for (int i = 0; i < numberOfBackStackEntries; i++)
            Log.d("JIM", "BackStackEntry=" + getSupportFragmentManager().getBackStackEntryAt(i));
    }

    private void killtheAppAndExit() {
        if (baseballDBCreator != null) baseballDBCreator.close();
        if (personDBCreator != null) personDBCreator.close();
        if (storeDBCreator != null) storeDBCreator.close();
        if (purchaseDBCreator != null) purchaseDBCreator.close();
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    @Override
    public void onBackPressed() {
        // back button pressed
        // getBackStackEntryCount() == 0 : initial 4-fragment screen
        // getBackStackEntryCount() == 1 : category screen
        // getBackStackEntryCount() == 2 : storeItems shopping screen for selected category
        // getBackStackEntryCount() == 3 : shopping cart screen
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() == 0)
            killtheAppAndExit();
        else
            goBackToPreviousFragment(manager);
    }

    @Override
    protected void onUserLeaveHint()
    {
        // home button pressed
        // getBackStackEntryCount() == 0 : initial 4-fragment screen
        // getBackStackEntryCount() == 1 : category screen
        // getBackStackEntryCount() == 2 : storeItems shopping screen for selected category
        // getBackStackEntryCount() == 3 : shopping cart screen
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) killtheAppAndExit();
    }

}
