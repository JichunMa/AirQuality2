package com.esaysidebar.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by mjc on 2017/5/15.
 */

public class LocalCityNameUtil {

    private GetLocalCityNameCallback callback;

    //获取城市名称位置
    public void getLocalCityName(Context mContext) {

        final double[] arrayLocation = new double[2];
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                arrayLocation[0] = location.getLatitude();
                arrayLocation[1] = location.getLongitude();
            } else {
                LocationListener locationListener = new LocationListener() {
                    public void onLocationChanged(Location location) {
                    }

                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    public void onProviderEnabled(String provider) {
                    }

                    public void onProviderDisabled(String provider) {
                    }
                };
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    arrayLocation[0] = location.getLatitude();
                    arrayLocation[1] = location.getLongitude();
                }
            }
            Log.d("mjctest", "Lat:" + arrayLocation[0] + ";Lon=" + arrayLocation[1]);
            if (NewWorkManager.checkNetWork(mContext)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection connection;
                        try {
                            URL url = new URL("http://api.map.baidu.com/geocoder?output=json&location="
                                    + arrayLocation[0] + "," + arrayLocation[1] + "&ak=esNPFDwwsXWtsQfw4NMNmur1");

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
                            if (!TextUtils.isEmpty(response)) {
                                JSONObject jsonObj = new JSONObject(response.toString());
                                String status = jsonObj.getString("status");
                                if (!TextUtils.isEmpty(status) && status.equalsIgnoreCase("OK")) {
                                    JSONObject jsonResult = jsonObj.getJSONObject("result");
                                    String cityName = jsonResult.getJSONObject("addressComponent").getString("city");
                                    if (callback != null) {
                                        //回调返回城市名称
                                        callback.setLocalCityName(cityName);
                                    }
                                    Log.d("mjctest", "localtion city name: " + cityName);
                                }
                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } else {
                Log.d("mjc", "no network");
                Toast.makeText(mContext, "No Network", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public LocalCityNameUtil(GetLocalCityNameCallback callback) {
        this.callback = callback;
    }

    //获取城市名称后回调方法
    public interface GetLocalCityNameCallback {
        void setLocalCityName(String cityName);
    }

    public void setCallback(GetLocalCityNameCallback callback) {
        this.callback = callback;
    }
}
