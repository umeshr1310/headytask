package com.headytask.headytask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.headytask.headytask.api.BackgroundSyncService;
import com.headytask.headytask.localdb.OfflineCategoryDBHandler;
import com.headytask.headytask.localdb.OfflineDBHandler;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String SYNC_DATA = "com.headytask.headytask.SYNC_DATA";

    private List<String> ranking_product_id_list = new ArrayList<>();
    private List<String> ranking_product_viewcount_list = new ArrayList<>();
    private List<String> ranking_product_ordercount_list = new ArrayList<>();
    private List<String> ranking_product_shares_list = new ArrayList<>();
    private List<String> ranking_rankings_list = new ArrayList<>();

    private List<String> category_id_list = new ArrayList<>();
    private List<String> category_name_list = new ArrayList<>();
    private List<String> category_child_category_list = new ArrayList<>();
    private List<String> category_products_id_list = new ArrayList<>();
    private List<String> category_products_name_list = new ArrayList<>();
    private List<String> category_products_date_added_list = new ArrayList<>();
    private List<String> category_products_tax_name_list = new ArrayList<>();
    private List<String> category_products_tax_value_list = new ArrayList<>();
    private List<String> category_products_variants_id_list = new ArrayList<>();
    private List<String> category_products_variants_color_list = new ArrayList<>();
    private List<String> category_products_variants_size_list = new ArrayList<>();
    private List<String> category_products_variants_price_list = new ArrayList<>();


    String ranking,product_id, view_count, order_count,shares;

    private OfflineDBHandler offlineDB;
    private OfflineCategoryDBHandler offlineCatDB;
    private boolean connectStatus = false;

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(SYNC_DATA)) {
                ranking_rankings_list = intent.getStringArrayListExtra("ranking_list");
                ranking_product_id_list = intent.getStringArrayListExtra("product_id_list");
                ranking_product_viewcount_list = intent.getStringArrayListExtra("view_count_list");
                ranking_product_ordercount_list = intent.getStringArrayListExtra("order_count_list");
                ranking_product_shares_list = intent.getStringArrayListExtra("shares_list");

                category_id_list = intent.getStringArrayListExtra("category_id_list");
                category_name_list = intent.getStringArrayListExtra("category_name_list");
                category_child_category_list = intent.getStringArrayListExtra("category_child_category_list");
                category_products_id_list = intent.getStringArrayListExtra("category_products_id_list");
                category_products_name_list = intent.getStringArrayListExtra("category_products_name_list");
                category_products_date_added_list = intent.getStringArrayListExtra("category_products_date_added_list");
                category_products_tax_name_list = intent.getStringArrayListExtra("category_products_tax_name_list");
                category_products_tax_value_list = intent.getStringArrayListExtra("category_products_tax_value_list");
                category_products_variants_id_list = intent.getStringArrayListExtra("category_products_variants_id_list");
                category_products_variants_color_list = intent.getStringArrayListExtra("category_products_variants_color_list");
                category_products_variants_size_list = intent.getStringArrayListExtra("category_products_variants_size_list");
                category_products_variants_price_list = intent.getStringArrayListExtra("category_products_variants_price_list");

                System.out.println("Data received SyncActivity by receiver ranking_rankings_list:" + ranking_rankings_list.size());
                System.out.println("Data received SyncActivity by receiver category_products_variants_size_list:" + category_products_variants_size_list.size());
                //Do something with the string
            }
        }
    };
    LocalBroadcastManager bManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectStatus = checkInternetConnection();
        System.out.println("Connection :" + connectStatus);

        bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SYNC_DATA);
        bManager.registerReceiver(bReceiver, intentFilter);

        offlineDB = new OfflineDBHandler(getApplicationContext());
        SQLiteDatabase db = offlineDB.getWritableDatabase();

        String count = "SELECT count(*) FROM rankings_table";
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
        System.out.println("Count is icount:"+ icount);

        //offlineCatDB = new OfflineCategoryDBHandler(getApplicationContext());
        //SQLiteDatabase db1 = offlineCatDB.getWritableDatabase();

        String count1 = "SELECT count(*) FROM categories_table";
        Cursor mcursor1 = db.rawQuery(count1, null);
        mcursor1.moveToFirst();
        int icount1 = mcursor1.getInt(0);
        System.out.println("Count is icount1:"+ icount1);

        if(connectStatus || ((icount<=0)||(icount1<=0))){

            clearArrayLists();
            //Toast.makeText(getApplicationContext(),"service called", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(MainActivity.this, BackgroundSyncService.class);
            i.putExtra("TAG","SYNC");
            startService(i);
        }
        else if((icount > 0)||(icount1>0)){
            clearArrayLists();
            getRankingDetailsDB();
            getCategoriesDetailsDB();
        }
        else {
            Toast.makeText(getApplicationContext(),"Mobile Network/Wifi is Off",Toast.LENGTH_SHORT).show();
        }

    }
    public boolean checkInternetConnection() {
        boolean mobileDataEnabled = false; // Assume disabled
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifi.isConnected()) {
            // If Wi-Fi connected
            System.out.println("Inside WIFI");
            mobileDataEnabled = true;
        }
        if (mobile.isConnected()) {
            // If Internet connected
            System.out.println("Inside mobile netwrk");
            mobileDataEnabled = true;
        }
        return mobileDataEnabled;
    }
    private void clearArrayLists() {

        if (ranking_rankings_list.size() > 0)
            ranking_rankings_list.clear();
        if (ranking_product_id_list.size() > 0)
            ranking_product_id_list.clear();
        if (ranking_product_viewcount_list.size() > 0)
            ranking_product_viewcount_list.clear();
        if (ranking_product_ordercount_list.size() > 0)
            ranking_product_ordercount_list.clear();
        if (ranking_product_shares_list.size() > 0)
            ranking_product_shares_list.clear();

        if (category_id_list.size() > 0)
            category_id_list.clear();
        if (category_name_list.size() > 0)
            category_name_list.clear();
        if (category_child_category_list.size() > 0)
            category_child_category_list.clear();
        if (category_products_id_list.size() > 0)
            category_products_id_list.clear();
        if (category_products_name_list.size() > 0)
            category_products_name_list.clear();
        if (category_products_date_added_list.size() > 0)
            category_products_date_added_list.clear();
        if (category_products_tax_name_list.size() > 0)
            category_products_tax_name_list.clear();
        if (category_products_tax_value_list.size() > 0)
            category_products_tax_value_list.clear();
        if (category_products_variants_id_list.size() > 0)
            category_products_variants_id_list.clear();
        if (category_products_variants_color_list.size() > 0)
            category_products_variants_color_list.clear();
        if (category_products_variants_size_list.size() > 0)
            category_products_variants_size_list.clear();
        if (category_products_variants_price_list.size() > 0)
            category_products_variants_price_list.clear();
    }
    private void getRankingDetailsDB() {
        // Fetching voter details from SQLite
        List<HashMap<String, String>> rankingDir = offlineDB.getRankingDetails();
        getrankingdetailsDB(rankingDir);
    }
    private void getrankingdetailsDB(List<HashMap<String, String>> rankingDir) {

        for (int i = 0; i < rankingDir.size(); i++) {
            String ranking = rankingDir.get(i).get("RANKING");
            String id = rankingDir.get(i).get("PRODUCTS_ID");
            String view_count = rankingDir.get(i).get("VIEW_COUNT");
            String order_count = rankingDir.get(i).get("ORDER_COUNT");
            String shares = rankingDir.get(i).get("SHARES");

            //add data to arraylist
            ranking_rankings_list.add(ranking.trim());
            ranking_product_id_list.add(id.trim());
            ranking_product_viewcount_list.add(view_count.trim());
            ranking_product_ordercount_list.add(order_count.trim());
            ranking_product_shares_list.add(shares.trim());

        }
    }

    private void getCategoriesDetailsDB() {
        // Fetching voter details from SQLite
        List<HashMap<String, String>> categoryDir = offlineDB.getCategoriesDetails();
        getcategoriesdetailsDB(categoryDir);
    }
    private void getcategoriesdetailsDB(List<HashMap<String, String>> categoryDir) {

        for (int i = 0; i < categoryDir.size(); i++) {
            String cat_id = categoryDir.get(i).get("CATEGORIES_ID");
            String cat_name = categoryDir.get(i).get("CATEGORIES_NAME");
            String child_cat = categoryDir.get(i).get("CATEGORIES_CHILD_CATEGORIES");
            String prod_id = categoryDir.get(i).get("CATEGORIES_PRODUCTS_ID");
            String prod_name = categoryDir.get(i).get("CATEGORIES_PRODUCTS_NAME");
            String date_added = categoryDir.get(i).get("CATEGORIES_PRODUCTS_DATE_ADDED");
            String tax_name = categoryDir.get(i).get("CATEGORIES_PRODUCTS_TAX_NAME");
            String tax_value = categoryDir.get(i).get("CATEGORIES_PRODUCTS_TAX_VALUE");
            String var_id = categoryDir.get(i).get("CATEGORIES_PRODUCTS_VARIANTS_ID");
            String var_color = categoryDir.get(i).get("CATEGORIES_PRODUCTS_VARIANTS_COLOR");
            String var_size = categoryDir.get(i).get("CATEGORIES_PRODUCTS_VARIANTS_SIZE");
            String var_price = categoryDir.get(i).get("CATEGORIES_PRODUCTS_VARIANTS_PRICE");

            //add data to arraylist
            category_id_list.add(cat_id.trim());
            category_name_list.add(cat_name.trim());
            category_child_category_list.add(child_cat.trim());
            category_products_id_list.add(prod_id.trim());
            category_products_name_list.add(prod_name.trim());
            category_products_date_added_list.add(date_added.trim());
            category_products_tax_name_list.add(tax_name.trim());
            category_products_tax_value_list.add(tax_value.trim());
            category_products_variants_id_list.add(var_id.trim());
            category_products_variants_color_list.add(var_color.trim());
            category_products_variants_size_list.add(var_size.trim());
            category_products_variants_price_list.add(var_price.trim());

        }
    }

}
