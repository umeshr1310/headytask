package com.headytask.headytask.api;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.headytask.headytask.MainActivity;
import com.headytask.headytask.R;
import com.headytask.headytask.localdb.OfflineCategoryDBHandler;
import com.headytask.headytask.localdb.OfflineDBHandler;
import com.headytask.headytask.model.MainJson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BackgroundSyncService extends IntentService {

    public OfflineDBHandler DB = new OfflineDBHandler(this);
    public OfflineCategoryDBHandler catDB = new OfflineCategoryDBHandler(this);
    ArrayList<String> ranking_list = new ArrayList<String>() ;
    ArrayList<String> product_id_list = new ArrayList<String>() ;
    ArrayList<String> view_count_list = new ArrayList<String>() ;
    ArrayList<String> order_count_list = new ArrayList<String>() ;
    ArrayList<String> shares_list = new ArrayList<String>() ;

    private ArrayList<String> category_id_list = new ArrayList<>();
    private ArrayList<String> category_name_list = new ArrayList<>();
    private ArrayList<String> category_child_category_list = new ArrayList<>();
    private ArrayList<String> category_products_id_list = new ArrayList<>();
    private ArrayList<String> category_products_name_list = new ArrayList<>();
    private ArrayList<String> category_products_date_added_list = new ArrayList<>();
    private ArrayList<String> category_products_tax_name_list = new ArrayList<>();
    private ArrayList<String> category_products_tax_value_list = new ArrayList<>();
    private ArrayList<String> category_products_variants_id_list = new ArrayList<>();
    private ArrayList<String> category_products_variants_color_list = new ArrayList<>();
    private ArrayList<String> category_products_variants_size_list = new ArrayList<>();
    private ArrayList<String> category_products_variants_price_list = new ArrayList<>();


    private static final String TAG = "SyncService";
    NotificationManager mNotifyManager;

    boolean statusTag = false, result = false;
    String activityTAG = "";
    public BackgroundSyncService() {
        super(BackgroundSyncService.class.getSimpleName());
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Background Sync Service Started!");
        activityTAG = (String) intent.getExtras().get("TAG");

        // Sync Data in background
        statusTag = getDetailsFromServer();
        if(statusTag){
            if(activityTAG.equals("SYNC")) {
                Intent RTReturn = new Intent(MainActivity.SYNC_DATA);

                RTReturn.putStringArrayListExtra("ranking_list", ranking_list);
                RTReturn.putStringArrayListExtra("product_id_list", product_id_list);
                RTReturn.putStringArrayListExtra("view_count_list", view_count_list);
                RTReturn.putStringArrayListExtra("order_count_list", order_count_list);
                RTReturn.putStringArrayListExtra("shares_list", shares_list);

                RTReturn.putStringArrayListExtra("category_id_list", category_id_list);
                RTReturn.putStringArrayListExtra("category_name_list", category_name_list);
                RTReturn.putStringArrayListExtra("category_child_category_list", category_child_category_list);
                RTReturn.putStringArrayListExtra("category_products_id_list", category_products_id_list);
                RTReturn.putStringArrayListExtra("category_products_name_list", category_products_name_list);
                RTReturn.putStringArrayListExtra("category_products_date_added_list", category_products_date_added_list);
                RTReturn.putStringArrayListExtra("category_products_tax_name_list", category_products_tax_name_list);
                RTReturn.putStringArrayListExtra("category_products_tax_value_list", category_products_tax_value_list);
                RTReturn.putStringArrayListExtra("category_products_variants_id_list", category_products_variants_id_list);
                RTReturn.putStringArrayListExtra("category_products_variants_color_list", category_products_variants_color_list);
                RTReturn.putStringArrayListExtra("category_products_variants_size_list", category_products_variants_size_list);
                RTReturn.putStringArrayListExtra("category_products_variants_price_list", category_products_variants_price_list);

                LocalBroadcastManager.getInstance(BackgroundSyncService.this).sendBroadcast(RTReturn);
            }

            Log.d(TAG, "Service Stopping!");
            BackgroundSyncService.this.stopSelf();
        }

    }
    /** Load user Data From Server and save to SQLite database **/
    public boolean getDetailsFromServer() {

        DB.deleteRanking();
        DB.deleteCategories();
        clearLists();

        Retrofit retrofit = new Retrofit.Builder().baseUrl(ApiInterface.url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);

        //Show notification in notification area
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Product Data")
                .setContentText("Sync in progress...")
                .setSmallIcon(R.drawable.sync).build();



        Call<MainJson> call = apiInterface.getApiData();
        call.enqueue(new Callback<MainJson>() {

            @Override
            public void onResponse(Call<MainJson> call, Response<MainJson> response) {
                MainJson mainJson = response.body();
                System.out.println("MainJson :" + mainJson);
                //Log.d("TAG Success", services.toString());

                if(Common.isNotNull(mainJson.toString())) {

                    for (int i = 0; i < mainJson.getRankings().size(); i++) {
                        ranking_list.add(mainJson.getRankings().get(i).getRanking().toString().trim());
                        //System.out.println("ranking_rankings_list :"+ranking_rankings_list.get(i));

                        for(int j=0;j<mainJson.getRankings().get(i).getProducts().size();j++){
                            product_id_list.add(String.valueOf(mainJson.getRankings().get(i).getProducts().get(j).getId()).trim());
                            // System.out.println("ranking_product_id_list :"+ranking_product_id_list.get(j));
                            view_count_list.add(String.valueOf(mainJson.getRankings().get(i).getProducts().get(j).getViewCount()).trim());
                            // System.out.println("ranking_product_viewcount_list :"+ranking_product_viewcount_list.get(j));
                            order_count_list.add(String.valueOf(mainJson.getRankings().get(i).getProducts().get(j).getOrderCount()).trim());
                            // System.out.println("ranking_product_ordercount_list :"+ranking_product_ordercount_list.get(j));
                            shares_list.add(String.valueOf(mainJson.getRankings().get(i).getProducts().get(j).getShares()).trim());
                            // System.out.println("ranking_product_shares_list :"+ranking_product_shares_list.get(j));
                            DB.addRanking(mainJson.getRankings().get(i).getRanking().toString().trim(),
                                    String.valueOf(mainJson.getRankings().get(i).getProducts().get(j).getId()).trim(),
                                    String.valueOf(mainJson.getRankings().get(i).getProducts().get(j).getViewCount()).trim(),
                                    String.valueOf(mainJson.getRankings().get(i).getProducts().get(j).getOrderCount()).trim(),
                                    String.valueOf(mainJson.getRankings().get(i).getProducts().get(j).getShares()).trim() );
                        }
                    }
                    for (int i = 0; i < mainJson.getCategories().size(); i++) {
                        category_id_list.add(String.valueOf(mainJson.getCategories().get(i).getId()).trim());
                        //System.out.println("category_id_list :"+category_id_list.get(i));
                        category_name_list.add(mainJson.getCategories().get(i).getName().toString().trim());
                        // System.out.println("category_name_list :"+category_name_list.get(i));
                        for(int m=0;m<mainJson.getCategories().get(i).getChildCategories().size();m++) {
                               category_child_category_list.add(String.valueOf(mainJson.getCategories().get(i).getChildCategories().get(m).intValue()).trim());
                            // System.out.println("category_child_category_list :" + category_child_category_list.get(m));
                        }
                        for(int j=0;j<mainJson.getCategories().get(i).getProducts().size();j++){
                            category_products_id_list.add(String.valueOf(mainJson.getCategories().get(i).getProducts().get(j).getId()).trim());
                            //System.out.println("category_products_id_list :"+category_products_id_list.get(j));
                            category_products_name_list.add(mainJson.getCategories().get(i).getProducts().get(j).getName().toString().trim());
                           // System.out.println("category_products_name_list :"+category_products_name_list.get(j));
                            category_products_date_added_list.add(mainJson.getCategories().get(i).getProducts().get(j).getDateAdded().toString().trim());
                            //System.out.println("category_products_date_added_list :"+category_products_date_added_list.get(j));

                            category_products_tax_name_list.add(mainJson.getCategories().get(i).getProducts().get(j).getTax().getName().toString().trim());
                            //System.out.println("category_products_tax_name_list :"+category_products_tax_name_list.get(j));
                            category_products_tax_value_list.add(String.valueOf(mainJson.getCategories().get(i).getProducts().get(j).getTax().getValue()).trim());
                           // System.out.println("category_products_tax_value_list :"+category_products_tax_value_list.get(j));

                            for(int k=0;k<mainJson.getCategories().get(i).getProducts().get(j).getVariants().size();k++) {
                                category_products_variants_id_list.add(String.valueOf(mainJson.getCategories().get(i).getProducts().get(j).getVariants().get(k).getId()).trim());
                               // System.out.println("category_products_variants_id_list :"+category_products_variants_id_list.get(k));
                                category_products_variants_color_list.add(mainJson.getCategories().get(i).getProducts().get(j).getVariants().get(k).getColor().toString().trim());
                                //System.out.println("category_products_variants_color_list :"+category_products_variants_color_list.get(k));
                                category_products_variants_size_list.add(String.valueOf(mainJson.getCategories().get(i).getProducts().get(j).getVariants().get(k).getSize()).trim());
                                //System.out.println("category_products_variants_size_list :"+category_products_variants_size_list.get(k));
                                category_products_variants_price_list.add(String.valueOf(mainJson.getCategories().get(i).getProducts().get(j).getVariants().get(k).getPrice()).trim());
                                //System.out.println("category_products_variants_price_list :"+category_products_variants_price_list.get(k));

                                DB.addCategories(String.valueOf(mainJson.getCategories().get(i).getId()).trim(),
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
                        /*ArrayAdapter cityAdapter = new ArrayAdapter(MainActivity.this,R.layout.main_spinner_item, citiesList);
                        cityAdapter.setDropDownViewResource(R.layout.spinner_item);
                        spCity.setAdapter(cityAdapter);*/
                    result = true;
                }
                else {
                    Toast.makeText(getApplicationContext(),"No Data Found",Toast.LENGTH_LONG).show();
                    result = false;
                }

            }
            @Override
            public void onFailure(Call<MainJson> call, Throwable t) {
                Log.d("TAG Rankings ERROR", t.toString());
                result = false;
                System.out.print(t);
            }

        });
        return result;
    }

    private void clearLists() {
        ranking_list.clear();
        product_id_list.clear();
        view_count_list.clear();
        order_count_list.clear();
        shares_list.clear();
        category_id_list.clear();
        category_name_list.clear();
        category_child_category_list.clear();
        category_products_id_list.clear();
        category_products_name_list.clear();
        category_products_date_added_list.clear();
        category_products_tax_name_list.clear();
        category_products_tax_value_list.clear();
        category_products_variants_id_list.clear();
        category_products_variants_color_list.clear();
        category_products_variants_size_list.clear();
        category_products_variants_price_list.clear();
    }
}
