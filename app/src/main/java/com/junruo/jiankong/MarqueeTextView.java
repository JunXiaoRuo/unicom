package com.junruo.jiankong;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * Copyright:meixiansong-driver-android
 * Author: liyang <br>
 * Date:2018/4/2 下午2:50<br>
 * Desc: <br>
 */

public class MarqueeTextView extends AppCompatTextView {

    public MarqueeTextView(Context context) {
        super(context);
        initView();
    }

    private void initView() {
        setSingleLine();
        setEllipsize(TruncateAt.MARQUEE);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }


    @Override
    public boolean isFocused() {
        return true;
    }
}
