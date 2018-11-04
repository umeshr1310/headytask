package com.headytask.headytask.model;

/**
 * Created by umesh on 29/03/17.
 */

public class TypeData {
    /*public TypeData(String product_name, String date_added, String tax_name, String tax_value, String var_color, String var_size, String var_price) {
        this.product_name = product_name;
        this.date_added = date_added;
        this.tax_name = tax_name;
        this.tax_value = tax_value;
        this.var_color = var_color;
        this.var_size = var_size;
        this.var_price = var_price;
    }*/

    private String product_name, date_added, tax_name, tax_value, var_color, var_size, var_price;

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getDate_added() {
        return date_added;
    }

    public void setDate_added(String date_added) {
        this.date_added = date_added;
    }

    public String getTax_name() {
        return tax_name;
    }

    public void setTax_name(String tax_name) {
        this.tax_name = tax_name;
    }

    public String getTax_value() {
        return tax_value;
    }

    public void setTax_value(String tax_value) {
        this.tax_value = tax_value;
    }

    public String getVar_color() {
        return var_color;
    }

    public void setVar_color(String var_color) {
        this.var_color = var_color;
    }

    public String getVar_size() {
        return var_size;
    }

    public void setVar_size(String var_size) {
        this.var_size = var_size;
    }

    public String getVar_price() {
        return var_price;
    }

    public void setVar_price(String var_price) {
        this.var_price = var_price;
    }
}
