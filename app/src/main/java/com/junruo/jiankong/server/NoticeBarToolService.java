package com.junruo.jiankong.server;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.widget.Toast;
import com.junruo.jiankong.MainActivity;

import androidx.annotation.RequiresApi;


import java.lang.reflect.Method;

@RequiresApi(api = Build.VERSION_CODES.N)
public class NoticeBarToolService extends TileService {

    String TAG = "jiankong";

    // 添加磁贴时调用
    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    // 移除磁贴时调用
    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    //点击事件
    @Override
    public void onClick() {
        super.onClick();

        Context mContext = this;
        collapseStatusBar(mContext);

        if (FloatingImageDisplayService.isStarted) {
            Intent intent = new Intent(this, FloatingImageDisplayService.class);
            stopService(intent);
            getQsTile().setState(Tile.STATE_INACTIVE);
            getQsTile().updateTile();
            return;
        }else {
            getQsTile().setState(Tile.STATE_ACTIVE);
            getQsTile().updateTile();
            Intent intent = new Intent(this, FloatingImageDisplayService.class);
            startService(intent);
        }

    }

    // 只有添加后才调用
    // 通知栏下拉
    @Override
    public void onStartListening() {
        super.onStartListening();
    }

    // 通知栏关闭
    @Override
    public void onStopListening() {
        super.onStopListening();
        Log.d(TAG, "onStopListening: ");
    }

    // 收起通知栏
    public static void collapseStatusBar(Context context) {
        try{
            @SuppressLint("WrongConstant") Object statusBarManager = context.getSystemService("statusbar");
            Method collapse;

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN){
                assert statusBarManager != null;
                collapse = statusBarManager.getClass().getMethod("collapse");
            } else {
                assert statusBarManager != null;
                collapse = statusBarManager.getClass().getMethod("collapsePanels");
            }
            collapse.invoke(statusBarManager);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }



    // 设置磁贴颜色
    public void setQuickSettingColor() {
        // 更改成非活跃状态(灰色)
        getQsTile().setState(Tile.STATE_ACTIVE);
        // 更改成活跃状态(白色)
        // getQsTile().setState(Tile.STATE_INACTIVE);
        getQsTile().updateTile();// 更新Tile
    }

}
