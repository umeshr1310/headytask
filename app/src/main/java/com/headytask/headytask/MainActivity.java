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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.headytask.headytask.api.BackgroundSyncService;
import com.headytask.headytask.api.Common;
import com.headytask.headytask.localdb.OfflineCategoryDBHandler;
import com.headytask.headytask.localdb.OfflineDBHandler;
import com.headytask.headytask.model.TypeData;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String SYNC_DATA = "com.headytask.headytask.SYNC_DATA";

    private List<String> ranking_product_id_list = new ArrayList<>();
    private List<String> ranking_product_viewcount_list = new ArrayList<>();
    private List<String> ranking_product_ordercount_list = new ArrayList<>();
    private List<String> ranking_product_shares_list = new ArrayList<>();
    private List<String> ranking_rankings_list = new ArrayList<>();

    private ArrayList<String> category_id_list = new ArrayList<>();
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

    Spinner spCategorySearch,spRanking;
    private RecyclerView recyclerView;
    Button searchBtn;
    String categoryTXT = "",rankingTXT = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spCategorySearch = findViewById(R.id.sp_category);
        spRanking = findViewById(R.id.sp_ranking);
        searchBtn = findViewById(R.id.btn_search);
        recyclerView = findViewById(R.id.recyclerview);

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
        spCategorySearch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoryTXT = parent.getItemAtPosition(position).toString().trim();
                InputMethodManager imm = (InputMethodManager)spCategorySearch.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(spCategorySearch.getWindowToken(), 0);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spCategorySearch.requestFocus();
            }
        });
        spRanking.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                rankingTXT = parent.getItemAtPosition(position).toString().trim();
                InputMethodManager imm = (InputMethodManager)spRanking.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(spRanking.getWindowToken(), 0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spRanking.requestFocus();
            }
        });

        searchBtn.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_search :
                if(Common.isNotNull(categoryTXT) && Common.isNotNull(rankingTXT)){
                   /* Intent i = new Intent(MainActivity.this, ListingActivity.class);
                    i.putExtra("CATEGORY",categoryTXT);
                    i.putExtra("RANKING", rankingTXT);
                    startActivity(i);*/
                }
                else {
                    Toast.makeText(this,"Please select service",Toast.LENGTH_SHORT).show();
                }
                break;
        }
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
        ArrayAdapter categoryAdapter = new ArrayAdapter(MainActivity.this,R.layout.main_spinner_item, category_id_list);
        categoryAdapter.setDropDownViewResource(R.layout.spinner_item);
        spCategorySearch.setAdapter(categoryAdapter);

        categoryTXT = spCategorySearch.getSelectedItem().toString();

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<TypeData> titles = prepareData(category_id_list);
        MyDataAdapter adapter = new MyDataAdapter(getApplicationContext(), titles);
        recyclerView.setAdapter(adapter);

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
    //put customized data in grid
    private ArrayList<TypeData> prepareData(ArrayList<String> servicesList) {

        System.out.println("Arraylisrt inside preparedate :"+ servicesList.size());

        ArrayList<TypeData> title_list = new ArrayList<>();
        for (int i = 0; i < servicesList.size(); i++) {
            TypeData title = new TypeData();
            title.setType(servicesList.get(i));
            /*if(i<image_urls.length) {
                title.setImg(image_urls[i]);
            }
            else {
                title.setImg(R.drawable.gift);
            }*/
            title_list.add(title);
        }
        return title_list;
    }
    class MyDataAdapter extends RecyclerView.Adapter<MyDataAdapter.ViewHolder> {
        private ArrayList<TypeData> list;
        private Context context;

        public MyDataAdapter(Context context, ArrayList<TypeData> list) {
            this.list = list;
            this.context = context;
        }

        @Override
        public MyDataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_item, viewGroup, false);
            return new MyDataAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyDataAdapter.ViewHolder viewHolder, int i) {
            viewHolder.tv_title.setText(list.get(i).getType());
            viewHolder.img_icon.setImageResource(list.get(i).getImg());
            viewHolder.setClickListener(new ItemClickListener() {
                @Override
                public void onClick(View view, int position, boolean isLongClick) {
                    if (isLongClick) {
                        //    CallIntent(position);
                        //  Toast.makeText(context, "lClick", Toast.LENGTH_SHORT).show();
                       /* Intent i = new Intent(MainActivity.this, ListingActivity.class);
                        i.putExtra("SERVICE",list.get(position).getType());
                        i.putExtra("CITY", cityTXT);
                        i.putExtra("TALUKA", talukaTXT);
                        startActivity(i);*/

                    } else {
                        //  CallIntent(position);
                        //  Toast.makeText(context, "sClick", Toast.LENGTH_SHORT).show();
                       /* Intent i = new Intent(MainActivity.this, ListingActivity.class);
                        i.putExtra("SERVICE",list.get(position).getType());
                        i.putExtra("CITY", cityTXT);
                        i.putExtra("TALUKA", talukaTXT);
                        startActivity(i);*/

                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
            private TextView tv_title;
            private ImageView img_icon;
            private ItemClickListener clickListener;

            public ViewHolder(View view) {
                super(view);
                tv_title = (TextView) view.findViewById(R.id.tv_title);
                img_icon = (ImageView) view.findViewById(R.id.img_icon);
                view.setOnClickListener(this);
                view.setOnLongClickListener(this);
            }

            public void setClickListener(ItemClickListener itemClickListener) {
                this.clickListener = itemClickListener;
            }

            @Override
            public void onClick(View v) {
                clickListener.onClick(v, getPosition(), false);
            }

            @Override
            public boolean onLongClick(View v) {
                clickListener.onClick(v, getPosition(), true);
                return true;
            }
        }
    }
    public interface ItemClickListener {
        void onClick(View view, int position, boolean isLongClick);
    }
}
