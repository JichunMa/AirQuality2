package com.example.mjc.airquality;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.mjc.airquality.util.NewWorkManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by mjc on 2017/4/24.
 * 获取指定城市的Aqi指数
 */

public class GainAqiUtil {

    private Context context;
    private GetDataDeal deal;


    //为了在主线程执行获取数据后的操作
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    if(msg.obj!=null){
                        String toastContent = (String) msg.obj;
                        if(!TextUtils.isEmpty(toastContent)){
                            Toast.makeText(context, toastContent, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
            }
        }
    };

    public GainAqiUtil(Context context, GetDataDeal deal) {
        this.context = context;
        this.deal = deal;
    }

    /**
     * @date create time 2017/4/24
     * @author mjc
     * @Description 请求数据
     * @Param cityPY当前选中城市的拼音
     * @Version v450
     */
    public void sendRequestWithHttpClient(final String cityPY) {
        if (TextUtils.isEmpty(cityPY)) {
            Toast.makeText(context, "当前选择城市有误", Toast.LENGTH_SHORT).show();
            return;
        }
        if (NewWorkManager.checkNetWork(context)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection = null;
                    try {
                        URL url = new URL("https://api.waqi.info/feed/" + cityPY +
                                "/?token=9270562d4d773301f4df547fde38689c17dbc7e5");

                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(5000);
                        connection.setReadTimeout(5000);
                        InputStream in = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }


                        Log.d("mjctest", "cityPY: "+cityPY);
                        Log.d("mjctest", response.toString());

                        final int aqi = parseJsonData(response.toString());
                        if (aqi == -1) {
                            //获取数据有误
                            Log.d(getClass().getSimpleName(), "get data occur error!");
                        } else {
                            //在主线程执行处理操作
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (deal != null) {
                                        deal.deal(aqi);
                                    }
                                }
                            });
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            Log.d("mjc", "no network");
            Toast.makeText(context, "No Network", Toast.LENGTH_SHORT).show();
        }

    }


    public int parseJsonData(String data) {
        int aqi = -1;
        try {
            if (TextUtils.isEmpty(data)) {
                Message msg = new Message();
                msg.what = 1;
                msg.obj = "data is error!";
                handler.sendMessage(msg);
            } else {
                JSONObject jsonObj = new JSONObject(data);
                String status = jsonObj.getString("status");
                if (!TextUtils.isEmpty(status) && status.equals("ok")) {
                    aqi = jsonObj.getJSONObject("data").getInt("aqi");
                } else if (!TextUtils.isEmpty(status) && status.equals("error")) {
                    Message msg = new Message();
                    msg.what = 1;
                    if(jsonObj.getString("data").equals("Unknown station")){
                        msg.obj = "暂不支持该地点!";
                    }else{
                        Log.d("ResponseData", data);
                        msg.obj = "获取数据失败!";
                    }
                    handler.sendMessage(msg);
                }else{
                    Log.d("ResponseData", data);
                    Log.d("ResponseData", data);
                    Message msg = new Message();
                    msg.obj = "获取数据失败!";
                    handler.sendMessage(msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            aqi = -1;
        } finally {
            return aqi;
        }
    }

    //预留接口执行获取AQI数据后的相关操作
    interface GetDataDeal {
        void deal(int aqi);
    }

}
