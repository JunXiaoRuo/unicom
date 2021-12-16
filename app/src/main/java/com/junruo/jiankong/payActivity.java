package com.junruo.jiankong;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.junruo.jiankong.databinding.ActivityGetcookieBinding;
import com.junruo.jiankong.databinding.ActivityPayBinding;
import com.junruo.jiankong.server.FloatingImageDisplayService;


public class payActivity extends AppCompatActivity {

    private ActivityPayBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPayBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.aAboutBtnGoback.setOnClickListener(v ->{
            CookieSyncManager.createInstance(this);
            CookieManager.getInstance().removeAllCookie();
            goBack();
        });

    }


    private void goBack() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition();
        } else {
            finish();
        }
    }

    }

