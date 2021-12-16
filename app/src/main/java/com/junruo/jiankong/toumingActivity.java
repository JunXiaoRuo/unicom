package com.junruo.jiankong;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.service.quicksettings.Tile;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.junruo.jiankong.databinding.ActivityGetcookieBinding;
import com.junruo.jiankong.server.FloatingImageDisplayService;

import static android.view.KeyEvent.KEYCODE_BACK;


public class toumingActivity extends AppCompatActivity {



    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FloatingImageDisplayService.isStarted) {
            Intent intent = new Intent(this, FloatingImageDisplayService.class);
            stopService(intent);
            finish();
        }else {
            toast("已开启");
            Intent intent = new Intent(this, FloatingImageDisplayService.class);
            startService(intent);
            finish();
        }

    }


    //自定义吐司
    void toast(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    }

