package com.example.mjc.airquality;

import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.mjc.airquality.util.LocalSharedPreferences;

import java.lang.reflect.Method;

/**
 * Created by mjc on 2017/4/10.
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public class MyAppTileService extends TileService {
    @Override
    public void onClick() {
        super.onClick();
        Log.d("mjc", "onclick!");
        collapseStatusBar();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    //收起通知栏
    private void collapseStatusBar() {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        try {
            Object service = getSystemService("statusbar");
            Class<?> statusbarManager = Class
                    .forName("android.app.StatusBarManager");
            Method collapse = null;
            if (service != null) {
                if (currentApiVersion <= 16) {
                    collapse = statusbarManager.getMethod("collapse");
                } else {
                    collapse = statusbarManager.getMethod("collapsePanels");
                }
                collapse.setAccessible(true);
                collapse.invoke(service);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Log.d("mjc", "onStartListening");

        getDataFromWeb();
    }

    /**
     * @date create time 2017/4/24
     * @author mjc
     * @Description 从接口中获取数据
     * @Param
     * @Version v450
     */
    public void getDataFromWeb() {
        GainAqiUtil gainAqiUtil = new GainAqiUtil(this, new GainAqiUtil.GetDataDeal() {
            @Override
            public void deal(int aqi) {
                updateTile(aqi);
                saveDataToLocal(aqi);
            }
        });
        String cityPinYin = new LocalSharedPreferences().readStringData(getApplicationContext(), "cityPY");
        gainAqiUtil.sendRequestWithHttpClient(cityPinYin);
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        Log.d("mjc", "onStopListening");
    }

    //更新Tile
    public void updateTile(int aqi) {
        try {
            //检测下版本是否高于Android N
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Tile tile = getQsTile();
                tile.setLabel(aqi + "");
                if (aqi < 100) {
                    tile.setIcon(Icon.createWithResource(this, R.drawable.ic_good));
                } else if (aqi < 200) {
                    tile.setIcon(Icon.createWithResource(this, R.drawable.ic_middle));
                } else {
                    tile.setIcon(Icon.createWithResource(this, R.drawable.ic_toxic));
                }
                tile.setState(Tile.STATE_ACTIVE);
                tile.updateTile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @date create time 2017/4/24
     * @author mjc
     * @Description 保存数据到本地
     * @Param
     * @Version v450
     */
    public void saveDataToLocal(int aqi) {
        new LocalSharedPreferences().saveIntegerData(getApplicationContext(), "aqi", aqi);
    }

}
