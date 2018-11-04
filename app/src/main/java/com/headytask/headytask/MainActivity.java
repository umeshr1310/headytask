package com.headytask.headytask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.headytask.headytask.api.ApiInterface;
import com.headytask.headytask.api.Common;
import com.headytask.headytask.localdb.OfflineDBHandler;
import com.headytask.headytask.model.MainJson;
import com.headytask.headytask.model.TypeData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

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

    //ArrayAdapter categoryAdapter = null;

    private OfflineDBHandler offlineDB;
    private boolean connectStatus = false;

    Spinner spCategorySearch,spRanking;
    private RecyclerView recyclerView;
    Button searchBtn;
    String categoryTXT = "",rankingTXT = "";
    protected ProgressDialog mProgressDialog;

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

        offlineDB = new OfflineDBHandler(getApplicationContext());
        SQLiteDatabase db = offlineDB.getWritableDatabase();

        String count = "SELECT count(*) FROM rankings_table";
        Cursor mcursor = db.rawQuery(count, null);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
        System.out.println("Count is icount:"+ icount);

        String count1 = "SELECT count(*) FROM categories_table";
        Cursor mcursor1 = db.rawQuery(count1, null);
        mcursor1.moveToFirst();
        int icount1 = mcursor1.getInt(0);
        System.out.println("Count is icount1:"+ icount1);

        if(connectStatus || ((icount<=0)||(icount1<=0))){
            clearArrayLists();
            getDetailsFromServer();
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
                System.out.println("categoryTXT :"+categoryTXT);
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
                System.out.println("rankingTXT :"+rankingTXT);
                if(Common.isNotNull(rankingTXT)){
                    clearCategoryLists();
                    //get data from local db sort by ranking
                    getCategoryDetailsByRanking(rankingTXT);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spRanking.requestFocus();
            }
        });

        searchBtn.setOnClickListener(this);
        System.out.println("category_name_list.size() :"+category_name_list.size());
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
                if(Common.isNotNull(categoryTXT)){
                    //load category wise data on search button click
                    clearCategoryLists();
                    getCategoryDetailsByCategory(categoryTXT);
                }
                else {
                    Toast.makeText(this,"Please select category",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    private  void clearCategoryLists(){
        if (category_id_list.size() > 0)
            category_id_list.clear();
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
            category_name_list.add(0,"All Categories");
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
    //get all ranking data from local db
    private void getRankingDetailsDB() {
        // Fetching voter details from SQLite
        List<HashMap<String, String>> rankingDir = offlineDB.getRankingDetails();
        getrankingdetailsDB(rankingDir);
    }
    private  void getrankingdetailsDB(List<HashMap<String, String>> rankingDir) {
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
               // System.out.println("ranking_rankings_list.get(i):"+ranking_rankings_list.get(i));
            }
        System.out.println("ranking_rankings_list size:"+ranking_rankings_list.size());
       //remove duplicated from arraylist
       Set<String> hs = new LinkedHashSet<>();
        hs.addAll(ranking_rankings_list);
        ranking_rankings_list.clear();
        ranking_rankings_list.addAll(hs);
            ArrayAdapter rankingAdapter = new ArrayAdapter(MainActivity.this,R.layout.main_spinner_item, ranking_rankings_list);
            rankingAdapter.setDropDownViewResource(R.layout.spinner_item);
            spRanking.setAdapter(rankingAdapter);
            rankingTXT = spRanking.getSelectedItem().toString();
    }

    //get all category data from local db
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
        //remove duplicated from arraylist
        Set<String> hs = new LinkedHashSet<>();
        hs.addAll(category_name_list);
        category_name_list.clear();
        category_name_list.addAll(hs);
        ArrayAdapter categoryAdapter = new ArrayAdapter(MainActivity.this,R.layout.main_spinner_item, category_name_list);
        categoryAdapter.setDropDownViewResource(R.layout.spinner_item);
        spCategorySearch.setAdapter(categoryAdapter);
        categoryTXT = spCategorySearch.getSelectedItem().toString();

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 1);
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<TypeData> titles = prepareData(category_products_name_list,
                category_products_date_added_list,
                category_products_tax_name_list,
                category_products_tax_value_list,
                category_products_variants_color_list,
                category_products_variants_size_list,
                category_products_variants_price_list);
        MyDataAdapter adapter = new MyDataAdapter(getApplicationContext(), titles);
        recyclerView.setAdapter(adapter);
        categoryAdapter.notifyDataSetChanged();
    }
    //get data from local db sort by category
    private void getCategoryDetailsByCategory(String category_name) {
        // Fetching voter details from SQLite
        List<HashMap<String, String>> categoryDir = offlineDB.getCategoriesDetailsByCategory(category_name);
        getcategoriesdetailsDBByCategory(categoryDir);
    }
    private void getcategoriesdetailsDBByCategory(List<HashMap<String, String>> categoryDir) {

        for (int i = 0; i < categoryDir.size(); i++) {
            String cat_id = categoryDir.get(i).get("CATEGORIES_ID");
           // String cat_name = categoryDir.get(i).get("CATEGORIES_NAME");
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
            //category_name_list.add(cat_name.trim());
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
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 1);
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<TypeData> titles = prepareData(category_products_name_list,
                category_products_date_added_list,
                category_products_tax_name_list,
                category_products_tax_value_list,
                category_products_variants_color_list,
                category_products_variants_size_list,
                category_products_variants_price_list);
        MyDataAdapter adapter = new MyDataAdapter(getApplicationContext(), titles);
        recyclerView.setAdapter(adapter);

    }
    //get data from local db sort by ranking
    private void getCategoryDetailsByRanking(String ranking) {
        // Fetching category details from SQLite
        List<HashMap<String, String>> categoryDir = offlineDB.getCategoriesDetailsByRanking(ranking);
        getcategoriesdetailsDBByRanking(categoryDir);
    }
    private void getcategoriesdetailsDBByRanking(List<HashMap<String, String>> categoryDir) {

        for (int i = 0; i < categoryDir.size(); i++) {
            String cat_id = categoryDir.get(i).get("CATEGORIES_ID");
            // String cat_name = categoryDir.get(i).get("CATEGORIES_NAME");
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
            //category_name_list.add(cat_name.trim());
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
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 1);
        recyclerView.setLayoutManager(layoutManager);
        ArrayList<TypeData> titles = prepareData(category_products_name_list,
                category_products_date_added_list,
                category_products_tax_name_list,
                category_products_tax_value_list,
                category_products_variants_color_list,
                category_products_variants_size_list,
                category_products_variants_price_list);
        MyDataAdapter adapter = new MyDataAdapter(getApplicationContext(), titles);
        recyclerView.setAdapter(adapter);
    }

    //put customized data in grid
    private ArrayList<TypeData> prepareData(List<String> category_products_name_list,
                                            List<String> category_products_date_added_list,
                                            List<String> category_products_tax_name_list,
                                            List<String> category_products_tax_value_list,
                                            List<String> category_products_variants_color_list,
                                            List<String> category_products_variants_size_list,
                                            List<String> category_products_variants_price_list
                                            ) {

        System.out.println("Arraylisrt inside preparedate :"+ category_products_name_list.size());

        ArrayList<TypeData> typedata_list = new ArrayList<>();
        for (int i = 0; i < category_products_name_list.size(); i++) {
            TypeData typeData = new TypeData();
            typeData.setProduct_name(category_products_name_list.get(i));
            typeData.setDate_added(category_products_date_added_list.get(i));
            typeData.setTax_name(category_products_tax_name_list.get(i));
            typeData.setTax_value(category_products_tax_value_list.get(i));
            typeData.setVar_color(category_products_variants_color_list.get(i));
            typeData.setVar_size(category_products_variants_size_list.get(i));
            typeData.setVar_price(category_products_variants_price_list.get(i));

            typedata_list.add(typeData);
        }
        return typedata_list;
    }

    /** Load user Data From Server and save to SQLite database **/
    public void getDetailsFromServer() {
        // boolean result = false;
        offlineDB.deleteRanking();
        offlineDB.deleteCategories();
        clearArrayLists();
        mProgressDialog = ProgressDialog.show(this, "Please wait","Loading data...", true);
        Handler h = new Handler(getApplicationContext().getMainLooper());
        // Although you need to pass an appropriate context
        h.post(new Runnable() {
            @Override
            public void run() {
                //  Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                Retrofit retrofit = new Retrofit.Builder().baseUrl(ApiInterface.url)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                ApiInterface apiInterface = retrofit.create(ApiInterface.class);

                Call<MainJson> call = apiInterface.getApiData();
                call.enqueue(new Callback<MainJson>() {
                    @Override
                    public void onResponse(Call<MainJson> call, Response<MainJson> response) {
                        MainJson mainJson = response.body();
                        System.out.println("MainJson :" + mainJson);
                        //Log.d("TAG Success", services.toString());
                        if(Common.isNotNull(mainJson.toString())) {
                            for (int i = 0; i < mainJson.getRankings().size(); i++) {
                                ranking_rankings_list.add(mainJson.getRankings().get(i).getRanking().toString().trim());
                                for(int j=0;j<mainJson.getRankings().get(i).getProducts().size();j++){
                                    ranking_product_id_list.add(String.valueOf(mainJson.getRankings().get(i).getProducts().get(j).getId()).trim());
                                    ranking_product_viewcount_list.add(String.valueOf(mainJson.getRankings().get(i).getProducts().get(j).getViewCount()).trim());
                                    ranking_product_ordercount_list.add(String.valueOf(mainJson.getRankings().get(i).getProducts().get(j).getOrderCount()).trim());
                                    ranking_product_shares_list.add(String.valueOf(mainJson.getRankings().get(i).getProducts().get(j).getShares()).trim());
                                    //save ranking data to local db
                                    offlineDB.addRanking(mainJson.getRankings().get(i).getRanking().toString().trim(),
                                            String.valueOf(mainJson.getRankings().get(i).getProducts().get(j).getId()).trim(),
                                            String.valueOf(mainJson.getRankings().get(i).getProducts().get(j).getViewCount()).trim(),
                                            String.valueOf(mainJson.getRankings().get(i).getProducts().get(j).getOrderCount()).trim(),
                                            String.valueOf(mainJson.getRankings().get(i).getProducts().get(j).getShares()).trim()
                                    );
                                }
                            }
                            for (int i = 0; i < mainJson.getCategories().size(); i++) {
                                category_id_list.add(String.valueOf(mainJson.getCategories().get(i).getId()).trim());
                                category_name_list.add(mainJson.getCategories().get(i).getName().toString().trim());
                                for(int m=0;m<mainJson.getCategories().get(i).getChildCategories().size();m++) {
                                    category_child_category_list.add(String.valueOf(mainJson.getCategories().get(i).getChildCategories().get(m).intValue()).trim());
                                }
                                for(int j=0;j<mainJson.getCategories().get(i).getProducts().size();j++){
                                    category_products_id_list.add(String.valueOf(mainJson.getCategories().get(i).getProducts().get(j).getId()).trim());
                                    category_products_name_list.add(mainJson.getCategories().get(i).getProducts().get(j).getName().toString().trim());
                                    category_products_date_added_list.add(mainJson.getCategories().get(i).getProducts().get(j).getDateAdded().toString().trim());
                                    category_products_tax_name_list.add(mainJson.getCategories().get(i).getProducts().get(j).getTax().getName().toString().trim());
                                    category_products_tax_value_list.add(String.valueOf(mainJson.getCategories().get(i).getProducts().get(j).getTax().getValue()).trim());
                                    for(int k=0;k<mainJson.getCategories().get(i).getProducts().get(j).getVariants().size();k++) {
                                        category_products_variants_id_list.add(String.valueOf(mainJson.getCategories().get(i).getProducts().get(j).getVariants().get(k).getId()).trim());
                                        category_products_variants_color_list.add(mainJson.getCategories().get(i).getProducts().get(j).getVariants().get(k).getColor().toString().trim());
                                        category_products_variants_size_list.add(String.valueOf(mainJson.getCategories().get(i).getProducts().get(j).getVariants().get(k).getSize()).trim());
                                        category_products_variants_price_list.add(String.valueOf(mainJson.getCategories().get(i).getProducts().get(j).getVariants().get(k).getPrice()).trim());
                                        //save category data to local db
                                        offlineDB.addCategories(String.valueOf(mainJson.getCategories().get(i).getId()).trim(),
                                                mainJson.getCategories().get(i).getName().toString().trim(),
                                                String.valueOf(mainJson.getCategories().get(i).getChildCategories()).trim(),
                                                String.valueOf(mainJson.getCategories().get(i).getProducts().get(j).getId()).trim(),
                                                mainJson.getCategories().get(i).getProducts().get(j).getName().toString().trim(),
                                                mainJson.getCategories().get(i).getProducts().get(j).getDateAdded().toString().trim(),
                                                mainJson.getCategories().get(i).getProducts().get(j).getTax().getName().toString().trim(),
                                                String.valueOf(mainJson.getCategories().get(i).getProducts().get(j).getTax().getValue()).trim(),
                                                String.valueOf(mainJson.getCategories().get(i).getProducts().get(j).getVariants().get(k).getId()).trim(),
                                                mainJson.getCategories().get(i).getProducts().get(j).getVariants().get(k).getColor().toString().trim(),
                                                String.valueOf(mainJson.getCategories().get(i).getProducts().get(j).getVariants().get(k).getSize()).trim(),
                                                String.valueOf(mainJson.getCategories().get(i).getProducts().get(j).getVariants().get(k).getPrice()).trim()
                                        );
                                    }
                                }
                            }
                            ArrayAdapter categoryAdapter = new ArrayAdapter(MainActivity.this,R.layout.main_spinner_item, category_name_list);
                            categoryAdapter.setDropDownViewResource(R.layout.spinner_item);
                            spCategorySearch.setAdapter(categoryAdapter);
                            categoryTXT = spCategorySearch.getSelectedItem().toString();

                            ArrayAdapter rankingAdapter = new ArrayAdapter(MainActivity.this,R.layout.main_spinner_item, ranking_rankings_list);
                            rankingAdapter.setDropDownViewResource(R.layout.spinner_item);
                            spRanking.setAdapter(rankingAdapter);
                            rankingTXT = spRanking.getSelectedItem().toString();

                            recyclerView.setHasFixedSize(true);
                            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 1);
                            recyclerView.setLayoutManager(layoutManager);
                            ArrayList<TypeData> titles = prepareData(category_products_name_list,
                                    category_products_date_added_list,
                                    category_products_tax_name_list,
                                    category_products_tax_value_list,
                                    category_products_variants_color_list,
                                    category_products_variants_size_list,
                                    category_products_variants_price_list);
                            MyDataAdapter adapter = new MyDataAdapter(getApplicationContext(), titles);
                            recyclerView.setAdapter(adapter);
                            //categoryAdapter.notifyDataSetChanged();
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"No Data Found",Toast.LENGTH_LONG).show();
                        }
                        mProgressDialog.dismiss();
                    }
                    @Override
                    public void onFailure(Call<MainJson> call, Throwable t) {
                        Log.d("TAG Rankings ERROR", t.toString());
                        System.out.print(t);
                        mProgressDialog.dismiss();
                    }
                });
            }
        });
    }

//inner Adapter class for showing data in recyclerview
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
            viewHolder.tv_product_name.setText(list.get(i).getProduct_name());
            viewHolder.tv_date_added.setText(list.get(i).getDate_added());
            viewHolder.tv_tax_name.setText(list.get(i).getTax_name());
            viewHolder.tv_tax_value.setText(list.get(i).getTax_value());
            viewHolder.tv_var_color.setText(list.get(i).getVar_color());
            viewHolder.tv_var_size.setText(list.get(i).getVar_size());
            viewHolder.tv_var_price.setText(list.get(i).getVar_price());

            viewHolder.setClickListener(new ItemClickListener() {
                @Override
                public void onClick(View view, int position, boolean isLongClick) {
                    if (isLongClick) {
                        Intent i = new Intent(MainActivity.this, ListingActivity.class);
                        i.putExtra("PRODUCT_NAME",list.get(position).getProduct_name());
                        i.putExtra("DATE_ADDED", list.get(position).getDate_added());
                        i.putExtra("TAX_NAME", list.get(position).getTax_name());
                        i.putExtra("TAX_VALUE",list.get(position).getTax_value());
                        i.putExtra("VAR_COLOR", list.get(position).getVar_color());
                        i.putExtra("VAR_SIZE", list.get(position).getVar_size());
                        i.putExtra("VAR_PRICE", list.get(position).getVar_price());
                        startActivity(i);

                    } else {
                        Intent i = new Intent(MainActivity.this, ListingActivity.class);
                        i.putExtra("PRODUCT_NAME",list.get(position).getProduct_name());
                        i.putExtra("DATE_ADDED", list.get(position).getDate_added());
                        i.putExtra("TAX_NAME", list.get(position).getTax_name());
                        i.putExtra("TAX_VALUE",list.get(position).getTax_value());
                        i.putExtra("VAR_COLOR", list.get(position).getVar_color());
                        i.putExtra("VAR_SIZE", list.get(position).getVar_size());
                        i.putExtra("VAR_PRICE", list.get(position).getVar_price());
                        startActivity(i);
                    }
                }
            });
        }
        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
            private TextView tv_product_name, tv_date_added, tv_tax_name, tv_tax_value, tv_var_color, tv_var_size, tv_var_price;
            private ItemClickListener clickListener;

            public ViewHolder(View view) {
                super(view);
                tv_product_name = (TextView) view.findViewById(R.id.tv_product_name);
                tv_date_added = (TextView) view.findViewById(R.id.tv_date_added);
                tv_tax_name = (TextView) view.findViewById(R.id.tv_tax_name);
                tv_tax_value = (TextView) view.findViewById(R.id.tv_tax_value);
                tv_var_color = (TextView) view.findViewById(R.id.tv_var_color);
                tv_var_size = (TextView) view.findViewById(R.id.tv_var_size);
                tv_var_price = (TextView) view.findViewById(R.id.tv_var_price);

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
