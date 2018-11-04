package com.headytask.headytask;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ListingActivity extends AppCompatActivity {
    private TextView tv_product_name, tv_date_added, tv_tax_name, tv_tax_value, tv_var_color, tv_var_size, tv_var_price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);
        tv_product_name = findViewById(R.id.tv_product_name);
        tv_date_added = findViewById(R.id.tv_date_added);
        tv_tax_name =  findViewById(R.id.tv_tax_name);
        tv_tax_value = findViewById(R.id.tv_tax_value);
        tv_var_color = findViewById(R.id.tv_var_color);
        tv_var_size =  findViewById(R.id.tv_var_size);
        tv_var_price = findViewById(R.id.tv_var_price);
        Intent i = getIntent();
        String prod_name = i.getStringExtra("PRODUCT_NAME").toString().trim();
        String date_added = i.getStringExtra("DATE_ADDED").toString().trim();
        String tax_name = i.getStringExtra("TAX_NAME").toString().trim();
        String tax_value = i.getStringExtra("TAX_VALUE").toString().trim();
        String var_color = i.getStringExtra("VAR_COLOR").toString().trim();
        String var_size = i.getStringExtra("VAR_SIZE").toString().trim();
        String var_price = i.getStringExtra("VAR_PRICE").toString().trim();
        tv_product_name.setText("Product Name :"+prod_name);
        tv_date_added.setText("Date Added :\n"+date_added);
        tv_tax_name.setText(tax_name);
        tv_tax_value.setText(tax_value);
        tv_var_color.setText("Color :"+var_color);
        tv_var_size.setText("Size :"+var_size);
        tv_var_price.setText("Price :"+var_price);





    }
}
