package com.mvsee.myapplication;

import com.dl.playfun.app.AppContext;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;

/**
 * Author: 彭石林
 * Time: 2022/1/8 11:49
 * Description: This is MyContext
 */
public class MyContext extends AppContext {
    public void onCreate() {
        super.onCreate();
//        FacebookSdk.setApplicationId(getString(R.string.playfun_facebook_app_id));
//        //初始化Facebook SDK
//        FacebookSdk.sdkInitialize(getApplicationContext());
//        if(BuildConfig.DEBUG){
//            FacebookSdk.setIsDebugEnabled(true);
//            FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
//        }
    }
}
