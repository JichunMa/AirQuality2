package com.example.mjc.airquality.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Created by mjc on 2017/4/24.
 */

public class LocalSharedPreferences {

    public LocalSharedPreferences() {
    }

    /**
    * @date create time 2017/4/24
    * @author mjc
    * @Description 保存整型数据
    * @Param
    * @Version v450
    */
    public void saveIntegerData(Context context, String name, int data) {
        if (context == null) {
            throw  new RuntimeException(getClass().getSimpleName()+" saveIntegerData()"+"context is null!");
        }
        if (TextUtils.isEmpty(name)) {
            throw  new RuntimeException(getClass().getSimpleName()+" saveIntegerData()"+"name is empty!");
        }
        SharedPreferences settings = context.getSharedPreferences(context.getPackageName(), 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(name, data);
        editor.commit();
    }

    /**
    * @date create time 2017/4/24
    * @author mjc
    * @Description 保存字符数据
    * @Param
    * @Version v450
    */
    public void saveStringData(Context context, String name, String data) {
        if (context == null) {
            throw  new RuntimeException(getClass().getSimpleName()+" saveIntegerData()"+",context is null!");
        }
        if (TextUtils.isEmpty(name)) {
            throw  new RuntimeException(getClass().getSimpleName()+" saveIntegerData()"+",name is empty!");
        }
        if (TextUtils.isEmpty(data)) {
            throw  new RuntimeException(getClass().getSimpleName()+" saveIntegerData()"+",data is empty!");
        }
        SharedPreferences settings = context.getSharedPreferences(context.getPackageName(), 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(name, data);
        editor.commit();
    }



    /**
    * @date create time 2017/4/24
    * @author mjc
    * @Description 从sh中读取一个String类型
    * @Param
    * @Version v450
    */
    public String readStringData(Context context,String name){
        String data = "";
        if (context == null) {
            throw  new RuntimeException(getClass().getSimpleName()+" readStringData()"+",context is null!");
        }
        if (TextUtils.isEmpty(name)) {
            throw  new RuntimeException(getClass().getSimpleName()+" readStringData()"+",name is empty!");
        }
        SharedPreferences settings = context.getSharedPreferences(context.getPackageName(), 0);
        data = settings.getString(name,"");
        return data;
    }
}
