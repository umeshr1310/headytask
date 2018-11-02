package com.headytask.headytask.localdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OfflineDBHandler extends SQLiteOpenHelper {

    private static final String TAG = OfflineDBHandler.class.getSimpleName();
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "products_db";

    private static final String TABLE_RANKINGS = "rankings_table";
    private static final String KEY_RANKING = "ranking";
    private static final String KEY_PRODUCTS_ID = "rankings_products_id";
    private static final String KEY_PRODUCTS_VIEW_COUNT = "rankings_products_view_count";
    private static final String KEY_PRODUCTS_ORDER_COUNT = "rankings_products_order_count";
    private static final String KEY_PRODUCTS_SHARES = "rankings_products_shares";

    private static final String TABLE_CATEGORIES = "categories_table";
    private static final String KEY_CATEGORIES_ID = "categories_id";
    private static final String KEY_CATEGORIES_NAME = "categories_name";
    private static final String KEY_CATEGORIES_CHILD_CATEGORIES = "categories_child_categories";
    private static final String KEY_CATEGORIES_PRODUCTS_ID = "categories_products_id";
    private static final String KEY_CATEGORIES_PRODUCTS_NAME = "categories_products_name";
    private static final String KEY_CATEGORIES_PRODUCTS_DATE_ADDED = "categories_products_date_added";
    private static final String KEY_CATEGORIES_PRODUCTS_TAX_NAME = "categories_products_tax_name";
    private static final String KEY_CATEGORIES_PRODUCTS_TAX_VALUE = "categories_products_tax_value";
    private static final String KEY_CATEGORIES_PRODUCTS_VARIANTS_ID = "categories_products_variants_id";
    private static final String KEY_CATEGORIES_PRODUCTS_VARIANTS_COLOR = "categories_products_variants_color";
    private static final String KEY_CATEGORIES_PRODUCTS_VARIANTS_SIZE = "categories_products_variants_size";
    private static final String KEY_CATEGORIES_PRODUCTS_VARIANTS_PRICE = "categories_products_variants_price";



    public OfflineDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_RANKINGS_TABLE = "CREATE TABLE " + TABLE_RANKINGS + "("
                + KEY_RANKING + " TEXT,"
                + KEY_PRODUCTS_ID + " TEXT,"
                + KEY_PRODUCTS_VIEW_COUNT + " TEXT,"
                + KEY_PRODUCTS_ORDER_COUNT + " TEXT,"
                + KEY_PRODUCTS_SHARES + " TEXT"+ ")";
        db.execSQL(CREATE_RANKINGS_TABLE);
        Log.d(TAG, "SqliteDb RANKINGS table created");

        String CREATE_CATEGORIES_TABLE = "CREATE TABLE " + TABLE_CATEGORIES + "("
                + KEY_CATEGORIES_ID + " TEXT,"
                + KEY_CATEGORIES_NAME + " TEXT,"
                + KEY_CATEGORIES_CHILD_CATEGORIES + " TEXT,"
                + KEY_CATEGORIES_PRODUCTS_ID + " TEXT,"
                + KEY_CATEGORIES_PRODUCTS_NAME + " TEXT,"
                + KEY_CATEGORIES_PRODUCTS_DATE_ADDED + " TEXT,"
                + KEY_CATEGORIES_PRODUCTS_TAX_NAME + " TEXT,"
                + KEY_CATEGORIES_PRODUCTS_TAX_VALUE + " TEXT,"
                + KEY_CATEGORIES_PRODUCTS_VARIANTS_ID + " TEXT,"
                + KEY_CATEGORIES_PRODUCTS_VARIANTS_COLOR + " TEXT,"
                + KEY_CATEGORIES_PRODUCTS_VARIANTS_SIZE + " TEXT,"
                + KEY_CATEGORIES_PRODUCTS_VARIANTS_PRICE + " TEXT"+ ")";
        db.execSQL(CREATE_CATEGORIES_TABLE);
        Log.d(TAG, "SqliteDb CATEGORIES table created");

    }
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RANKINGS);
        // Create tables again
        onCreate(db);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        // Create tables again
        onCreate(db);
    }
    /**
     * Storing ranking details in database
     * */
    public void addRanking(String ranking, String product_id, String view_count, String order_count , String shares) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // code
        values.put(KEY_RANKING, ranking); // Name
        values.put(KEY_PRODUCTS_ID, product_id);
        values.put(KEY_PRODUCTS_VIEW_COUNT, view_count);
        values.put(KEY_PRODUCTS_ORDER_COUNT, order_count);
        values.put(KEY_PRODUCTS_SHARES, shares);

        // Inserting Row
        long id = db.insert(TABLE_RANKINGS, null, values);
        db.close(); // Closing database connection
        Log.d(TAG, "New rankings inserted into sqlite: " + id);
    }

    /**
     * Storing categories details in database
     * */
    public void addCategories(String categories_id, String categories_name, String categories_child_categories,
                              String categories_products_id, String categories_products_name,String categories_products_date_added,
                              String categories_products_tax_name,String categories_products_tax_value,String categories_products_variants_id,
                              String categories_products_variants_color,String categories_products_variants_size,String categories_products_variants_price
                              ) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // code
        values.put(KEY_CATEGORIES_ID, categories_id); // Name
        values.put(KEY_CATEGORIES_NAME, categories_name);
        values.put(KEY_CATEGORIES_CHILD_CATEGORIES, categories_child_categories);
        values.put(KEY_CATEGORIES_PRODUCTS_ID, categories_products_id);
        values.put(KEY_CATEGORIES_PRODUCTS_NAME, categories_products_name);
        values.put(KEY_CATEGORIES_PRODUCTS_DATE_ADDED, categories_products_date_added); // Name
        values.put(KEY_CATEGORIES_PRODUCTS_TAX_NAME, categories_products_tax_name);
        values.put(KEY_CATEGORIES_PRODUCTS_TAX_VALUE, categories_products_tax_value);
        values.put(KEY_CATEGORIES_PRODUCTS_VARIANTS_ID, categories_products_variants_id);
        values.put(KEY_CATEGORIES_PRODUCTS_VARIANTS_COLOR, categories_products_variants_color);
        values.put(KEY_CATEGORIES_PRODUCTS_VARIANTS_SIZE, categories_products_variants_size);
        values.put(KEY_CATEGORIES_PRODUCTS_VARIANTS_PRICE, categories_products_variants_price);

        // Inserting Row
        long id = db.insert(TABLE_CATEGORIES, null, values);
        db.close(); // Closing database connection
        Log.d(TAG, "New categories inserted into sqlite: " + id);
    }

    /**
     * Getting ranking data from database
     * */
    public List<HashMap<String, String>> getRankingDetails() {

        List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

         //HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_RANKINGS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor != null && cursor.moveToFirst()) {
            do {

                HashMap<String, String> user = new HashMap<String, String>();

                user.put("RANKING", cursor.getString(0));
                user.put("PRODUCTS_ID", cursor.getString(1));
                user.put("VIEW_COUNT", cursor.getString(2));
                user.put("ORDER_COUNT", cursor.getString(3));
                user.put("SHARES", cursor.getString(4));

                Log.d(TAG, "Fetching RANKING from Sqlite: " + user.toString());

             //   list.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
      //   return user;

        return list;
    }


    /**
     * Getting categories data from database
     * */
    public List<HashMap<String, String>> getCategoriesDetails() {

        List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        //HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORIES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor != null && cursor.moveToFirst()) {
            do {

                HashMap<String, String> user = new HashMap<String, String>();

                user.put("CATEGORIES_ID", cursor.getString(0));
                user.put("CATEGORIES_NAME", cursor.getString(1));
                user.put("CATEGORIES_CHILD_CATEGORIES", cursor.getString(2));
                user.put("CATEGORIES_PRODUCTS_ID", cursor.getString(3));
                user.put("CATEGORIES_PRODUCTS_NAME", cursor.getString(4));
                user.put("CATEGORIES_PRODUCTS_DATE_ADDED", cursor.getString(5));
                user.put("CATEGORIES_PRODUCTS_TAX_NAME", cursor.getString(6));
                user.put("CATEGORIES_PRODUCTS_TAX_VALUE", cursor.getString(7));
                user.put("CATEGORIES_PRODUCTS_VARIANTS_ID", cursor.getString(8));
                user.put("CATEGORIES_PRODUCTS_VARIANTS_COLOR", cursor.getString(9));
                user.put("CATEGORIES_PRODUCTS_VARIANTS_SIZE", cursor.getString(10));
                user.put("CATEGORIES_PRODUCTS_VARIANTS_PRICE", cursor.getString(11));

                Log.d(TAG, "Fetching CATEGORIES from Sqlite: " + user.toString());

                //   list.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        //   return user;

        return list;
    }


    /**
     * Re create database Delete all tables and create them again
     * */
    public void deleteRanking() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_RANKINGS, null, null);
        db.close();
        Log.d(TAG, "Deleted all RANKING info from sqlite");
    }

    /**
     * Re create database Delete all tables and create them again
     * */
    public void deleteCategories() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_CATEGORIES, null, null);
        db.close();
        Log.d(TAG, "Deleted all CATEGORIES info from sqlite");
    }
}
