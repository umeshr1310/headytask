package com.headytask.headytask.api;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class Common {

    /**Checks for Null String object **/
public static boolean isNotNull(String txt){
    return txt!=null && txt.trim().length()>0 ? true: false;
}

}
