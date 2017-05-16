package com.example.mjc.airquality;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.esaysidebar.EasySideBarBuilder;
import com.example.mjc.airquality.util.LocalCityNameUtil;
import com.example.mjc.airquality.util.LocalSharedPreferences;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    private TextView txtAqi;
    private TextView txtContent;
    private TextView txtCity;
    int currentAqi = -1;
    private final String[] mIndexItems = {"定位"};//头部额外的索引
    String cityNamePinYin = "beijing";//当前选中城市的拼音
    String localCityName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindView();
        currentAqi = getLastAQIData();//显示上一次数据的指数
        loadCityName();//从本地加载城市名称
        getDataFromWeb();

        //获取定位城市
        getLocalCityName();

    }


    public void bindView() {
        txtAqi = (TextView) findViewById(R.id.txtAqi);
        txtContent = (TextView) findViewById(R.id.txtContent);
        txtCity = (TextView) findViewById(R.id.txtCity);
        //选择城市
        txtCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseCity();
            }
        });
    }


    //加载上次数据
    public int getLastAQIData() {
        int aqi;
        SharedPreferences settings = getSharedPreferences(getPackageName(), 0);
        aqi = settings.getInt("aqi", -1);
        return aqi;
    }

    //展示PM2.5指数
    public void displayAQI(int aqi) {
        if (aqi != -1) {
            //展示默认文字
            txtAqi.setText(String.valueOf(aqi));
            if (aqi < 50) {
                txtAqi.append("\n一级（优）");
                txtContent.setText("空气质量令人满意，基本无空气污染");
                txtAqi.setBackgroundColor(Color.parseColor("#009966"));
            } else if (aqi < 100) {
                txtAqi.append("\n二级（良）");
                txtContent.setText("空气质量可接受，但某些污染物可能对极少数异常敏感人群健康有较弱影响");
                txtAqi.setBackgroundColor(Color.parseColor("#FFDE33"));
            } else if (aqi < 150) {
                txtAqi.append("\n三级（轻度污染）");
                txtContent.setText("易感人群症状有轻度加剧，健康人群出现刺激症状");
                txtAqi.setBackgroundColor(Color.parseColor("#FF9933"));
            } else if (aqi < 200) {
                txtAqi.append("\n四级（中度污染");
                txtContent.setText("进一步加剧易感人群症状，可能对健康人群心脏、呼吸系统有影响");
                txtAqi.setBackgroundColor(Color.parseColor("#CC0033"));
            } else if (aqi < 300) {
                txtAqi.append("\n五级（重度污染）");
                txtContent.setText("心脏病和肺病患者症状显著加剧，运动耐受力降低，健康人群普遍出现症状");
                txtAqi.setBackgroundColor(Color.parseColor("#660099"));
            } else {
                txtAqi.append("\n六级（严重污染）");
                txtContent.setText("健康人群运动耐受力降低，有明显强烈症状，提前出现某些疾病");
                txtAqi.setBackgroundColor(Color.parseColor("#7E0023"));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case EasySideBarBuilder.CODE_SIDEREQUEST:
                if (data != null) {
                    String city = data.getStringExtra("selected");
                    cityNamePinYin = data.getStringExtra("cityNamePinYin");
                    if (!TextUtils.isEmpty(city) && !TextUtils.isEmpty(cityNamePinYin)) {
                        txtCity.setText(city);
                        saveCityNameToLocal(city, cityNamePinYin);
                        Toast.makeText(this, "选择的城市：" + city + ",拼音为：" + cityNamePinYin,
                                Toast.LENGTH_SHORT).show();
                        getDataFromWeb();
                    } else {
                        Toast.makeText(this, "选择的城市为空", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    //选择城市
    public void chooseCity() {
        //初始化以及配置
        if(TextUtils.isEmpty(localCityName)){
            //未定位成功显示定位中，定位成功显示定位城市名称
            localCityName = "定位中";
        }
        new EasySideBarBuilder(MainActivity.this)
                .setTitle("城市选择")
                .setIndexColor(Color.BLUE)
                .setIndexColor(0xFF0095EE)
                .isLazyRespond(true) //懒加载模式
                //.setHotCityList(hotCityList)//热门城市列表
                .setIndexItems(mIndexItems)//索引字母
                .setLocationCity(localCityName)//定位城市
                .setMaxOffset(60)//索引的最大偏移量
                .start();
    }


    public void saveAQIToLocal(int aqi) {
        new LocalSharedPreferences().saveIntegerData(getApplicationContext(), "aqi", aqi);
    }

    public void saveCityNameToLocal(String city, String cityNamePinYin) {
        new LocalSharedPreferences().saveStringData(getApplicationContext(), "city", city);
        new LocalSharedPreferences().saveStringData(getApplicationContext(), "cityPY", cityNamePinYin);
    }

    public void getDataFromWeb() {
        //先显示默认文字
        displayAQI(-1);
        GainAqiUtil gainAqiUtil = new GainAqiUtil(this, new GainAqiUtil.GetDataDeal() {
            @Override
            public void deal(int aqi) {
                currentAqi = aqi;
                displayAQI(aqi);
                saveAQIToLocal(aqi);
            }
        });
        gainAqiUtil.sendRequestWithHttpClient(cityNamePinYin);
    }

    //从本地加载城市名称
    public void loadCityName() {
        String cityName = new LocalSharedPreferences().readStringData(this, "city");
        String cityNamePinYinLocal = new LocalSharedPreferences().readStringData(this, "cityPY");
        if (!TextUtils.isEmpty(cityName) && !TextUtils.isEmpty(cityNamePinYinLocal)) {
            txtCity.setText(cityName);
            cityNamePinYin = cityNamePinYinLocal;
        }
    }

    //定位获取城市名称
    @AfterPermissionGranted(65)
    public void getLocalCityName() {
        String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            //具有相关权限
            LocalCityNameUtil cityNameUtil  = new LocalCityNameUtil(new LocalCityNameUtil.GetLocalCityNameCallback() {
                @Override
                public void setLocalCityName(String cityName) {
                    localCityName = cityName;
                }
            });
            cityNameUtil.getLocalCityName(MainActivity.this);
        }else{
            //未拥有权限，去申请权限
            EasyPermissions.requestPermissions(this, "", 65, perms);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Toast.makeText(this, "授予权限成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

}
