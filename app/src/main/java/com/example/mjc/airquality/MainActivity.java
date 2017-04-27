package com.example.mjc.airquality;

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
import com.example.mjc.airquality.util.LocalSharedPreferences;

public class MainActivity extends AppCompatActivity {

    private TextView txtAqi;
    private TextView txtContent;
    private TextView txtCity;
    int currentAqi = -1;
    private final String[] mIndexItems = {"定位"};//头部额外的索引
    String cityNamePinYin = "beijing";//当前选中城市的拼音
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindView();
        currentAqi = getLastAQIData();//显示上一次数据的指数
        loadCityName();//从本地加载城市名称
        getDataFromWeb();
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


    public int getLastAQIData() {
        int aqi = -1;
        SharedPreferences settings = getSharedPreferences(getPackageName(), 0);
        aqi = settings.getInt("aqi", -1);
        return aqi;
    }

    public void displayAQI(int aqi) {
        if(aqi == -1){
            //展示默认文字
        }else{
            txtAqi.setText("");
            txtAqi.setText("" + aqi);
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
                        saveCityNameToLocal(city,cityNamePinYin);
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
        new EasySideBarBuilder(MainActivity.this)
                .setTitle("城市选择")
                .setIndexColor(Color.BLUE)
                .setIndexColor(0xFF0095EE)
                .isLazyRespond(true) //懒加载模式
                //.setHotCityList(hotCityList)//热门城市列表
                .setIndexItems(mIndexItems)//索引字母
                .setLocationCity("北京")//定位城市
                .setMaxOffset(60)//索引的最大偏移量
                .start();
    }

    /**
     * @date create time 2017/4/24
     * @author mjc
     * @Description 保存数据到本地
     * @Param
     * @Version v450
     */
    public void saveAQIToLocal(int aqi) {
        new LocalSharedPreferences().saveIntegerData(getApplicationContext(), "aqi", aqi);
    }

    /**
     * @date create time 2017/4/24
     * @author mjc
     * @Description 保存城市名称到本地
     * @Param cityNamePinYin城市名称拼音
     * @Version v450
     */
    public void saveCityNameToLocal(String city ,String cityNamePinYin) {
        new LocalSharedPreferences().saveStringData(getApplicationContext(), "city", city);
        new LocalSharedPreferences().saveStringData(getApplicationContext(), "cityPY", cityNamePinYin);
    }

    /**
     * @date create time 2017/4/24
     * @author mjc
     * @Description 从接口中获取数据
     * @Param
     * @Version v450
     */
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
    public void loadCityName(){
        String cityName = new LocalSharedPreferences().readStringData(this,"city");
        String cityNamePinYinLocal = new LocalSharedPreferences().readStringData(this,"cityPY");
        if(!TextUtils.isEmpty(cityName)&&!TextUtils.isEmpty(cityNamePinYinLocal)){
            txtCity.setText(cityName);
            cityNamePinYin = cityNamePinYinLocal;
        }
    }
}
