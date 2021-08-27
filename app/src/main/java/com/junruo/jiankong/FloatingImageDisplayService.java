package com.junruo.jiankong;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by dongzhong on 2018/5/30.
 */

public class FloatingImageDisplayService extends Service {
    public static boolean isStarted = false;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;


    private View displayView;

    private String dayin = "";//打印流量包
    private Double mianliu=0.00;//总免流
    private Double zong=0.00;//套餐总量
    private Double yong=0.00;//套餐已用
    private Double sheng =0.00;//剩余流量
    private Double ben = 0.00;//本次免流
    private Double tiao = 0.00;//本次消耗


    private String orone = "yes";//是否首次获取

    private Double onem = 0.00;//初始化本次免流
    private Double onet = 0.00;//初始化本次消耗

    private Long time ;//默认3分钟刷新一次180000l

    private String cookie = "";//储存cookie信息



    TextView miantv,zongtv,yongtv,shengtv,bentv,tiaotv,sjtv,zhe,miant,zongt,yongt,shengt,sjt;

    private String gao,kuan,xgao,xkuan;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate() {
        super.onCreate();

        // 创建SharedPreferences对象用于获取Cookie信息,并将其私有化
        SharedPreferences share = getSharedPreferences("Cookie",
                Context.MODE_PRIVATE);
        // 获取编辑器来存储数据到sharedpreferences中
        cookie = share.getString("Cookie","");
        gao = share.getString("gao","320");
        kuan = share.getString("kuan","230");
        xgao = share.getString("xgao","152");
        xkuan = share.getString("xkuan","202");

        time = Long.valueOf(share.getString("time",""))*1000;
        System.out.println("==========>高"+gao+"==========>宽"+kuan+"==========>小高"+xgao+"==========>小宽"+xkuan);

        isStarted = true;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = Integer.parseInt(kuan);//230
        layoutParams.height = Integer.parseInt(gao);//320
        layoutParams.x = 300;
        layoutParams.y = 300;


        LayoutInflater layoutInflater = LayoutInflater.from(this);
        displayView = layoutInflater.inflate(R.layout.xfc, null);
        displayView.setOnTouchListener(new FloatingOnTouchListener());

        miant = displayView.findViewById(R.id.miant);
        zongt = displayView.findViewById(R.id.zongt);
        yongt = displayView.findViewById(R.id.yongt);
        shengt = displayView.findViewById(R.id.shengt);

        miantv = displayView.findViewById(R.id.mian);
        zongtv = displayView.findViewById(R.id.zong);
        yongtv = displayView.findViewById(R.id.yong);
        shengtv = displayView.findViewById(R.id.sheng);
        bentv = displayView.findViewById(R.id.ben);
        tiaotv = displayView.findViewById(R.id.tiao);
        sjtv = displayView.findViewById(R.id.sj);
        sjt = displayView.findViewById(R.id.sjt);



        zhe = displayView.findViewById(R.id.zhe);


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        windowManager.removeView(displayView);
        handler.removeCallbacksAndMessages(null);
        isStarted = false;

        // Service被终止的同时也停止定时器继续运行
        Toast.makeText(getApplicationContext(), "已关闭悬浮窗", Toast.LENGTH_SHORT).show();

    }

    //1，首先创建一个Handler对象
    Handler handler=new Handler();
    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {

            update();
            //2，然后创建一个Runnable对像
            Runnable runnable=new Runnable(){
                @Override
                public void run() {
                    update();
                    windowManager.updateViewLayout(displayView, layoutParams);
                    // TODO Auto-generated method stub
                    //要做的事情，这里再次调用此Runnable对象，以实现每两秒实现一次的定时器操作
                    handler.postDelayed(this, time);
                }
            };
            //3，使用PostDelayed方法，调用此Runnable对象
            handler.postDelayed(runnable, time);
            //4，关闭此定时器，可以这样操作
            //  handler.removeCallbacks(runnable);
            //移除所有的消息
            //handler.removeCallbacksAndMessages(null);


            zhe.setOnClickListener(v -> {
                if (zhe.getText().toString().equals("折叠")){
                    layoutParams.width = Integer.parseInt(xkuan);//200
                    layoutParams.height = Integer.parseInt(xgao);//150


                    sjt.setText("更：");
                    miantv.setVisibility(8);
                    zongtv.setVisibility(8);
                    yongtv.setVisibility(8);
                    shengtv.setVisibility(8);
                    miant.setVisibility(8);
                    zongt.setVisibility(8);
                    yongt.setVisibility(8);
                    shengt.setVisibility(8);
                    zhe.setText("展开");

                    windowManager.updateViewLayout(displayView, layoutParams);

                }else if (zhe.getText().toString().equals("展开")){
                    layoutParams.width = Integer.parseInt(kuan);//230
                    layoutParams.height = Integer.parseInt(gao);//320
                    sjt.setText("时间：");
                    miantv.setVisibility(0);
                    zongtv.setVisibility(0);
                    yongtv.setVisibility(0);
                    shengtv.setVisibility(0);
                    miant.setVisibility(0);
                    zongt.setVisibility(0);
                    yongt.setVisibility(0);
                    shengt.setVisibility(0);
                    zhe.setText("折叠");

                    windowManager.updateViewLayout(displayView, layoutParams);
                }


            });

            windowManager.addView(displayView, layoutParams);


        }
    }

    private void update(){
        mianliu=0.00;//总免流
        zong=0.00;//套餐总量
        yong=0.00;//套餐已用
        sheng =0.00;//剩余流量

        DecimalFormat df = new DecimalFormat("0.000");
        Date day=new Date();
        SimpleDateFormat sj = new SimpleDateFormat("HH:mm:ss");
        sjtv.setText(sj.format(day));

        try {

        OkHttpUtils.post("https://m.client.10010.com/mobileservicequery/operationservice/queryOcsPackageFlowLeftContent")
                .headers("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .headers("Cookie", cookie)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        if (s.equals("999999")||s.equals("")||s == null){
                            Toast.makeText(FloatingImageDisplayService.this,"解析cookie失败，请重新获取。",Toast.LENGTH_LONG).show();
                            return;
                        }
                        JSONObject json = JSONObject.parseObject(s);
                        System.out.println("=========================>成功");
                        //binding.packageName.setText(json.get("packageName").toString());


                        JSONArray jsonArray = json.getJSONArray("resources");

                        JSONObject job = jsonArray.getJSONObject(0);

                        JSONArray details = job.getJSONArray("details");


                        for (int i = 0; i < details.size(); i++) {
                            JSONObject liuliang = details.getJSONObject(i);
                            if (liuliang.getString("limited").equals("0")) {//套内流量包
                                if (liuliang.getString("addupItemCode") != null){

                                    if (!liuliang.getString("addupItemCode").equals("40008")){//通用流量包
                                        String total = liuliang.getString("total");//流量包总量
                                        String use = liuliang.getString("use");//流量包使用
                                        String remain = liuliang.getString("remain");//流量包剩余

                                        zong = zong + Double.parseDouble(total);
                                        yong = yong + Double.parseDouble(use);
                                        sheng = sheng + Double.parseDouble(remain);
                                    }else {//定向流量包

                                        String use = liuliang.getString("use");//流量包使用

                                        mianliu = mianliu + Double.parseDouble(use);

                                    }

                            }else {
                                    String total = liuliang.getString("total");//流量包总量
                                    String use = liuliang.getString("use");//流量包使用
                                    String remain = liuliang.getString("remain");//流量包剩余

                                    zong = zong + Double.parseDouble(total);
                                    yong = yong + Double.parseDouble(use);
                                    sheng = sheng + Double.parseDouble(remain);
                                }


                               // dayin = dayin + "\n流量包名称：" + feePolicyName + "总量：" + total + "M，已使用：" + use + "M，剩余" + remain + "M\n";
                            }else if (liuliang.getString("addUpItemName")==null||liuliang.getString("addupItemCode").equals("40008")){
                                String feePolicyName = liuliang.getString("feePolicyName");//免流包名称
                                String use = liuliang.getString("use");//已免流


                                mianliu = mianliu + Double.parseDouble(use);
                            }

                        }

                        if (orone.equals("yes")){
                            onet = yong;
                            onem = mianliu;
                            orone="no";
                            // 创建SharedPreferences对象用于存储Cookie信息,并将其私有化
                            SharedPreferences share = getSharedPreferences("Cookie",
                                    Context.MODE_PRIVATE);
                            // 获取编辑器来存储数据到sharedpreferences中
                            SharedPreferences.Editor editor = share.edit();
                            editor.putString("Cookie",cookie);
                            editor.commit();
                        }else {
                        }

                        ben = mianliu - onem;//本次免流
                        if (ben >= 1024.00){//流量大于1024m将使用G来表示
                            ben = ben / 1024.00;
                            bentv.setText(df.format(ben)+"G");
                        }else {
                            bentv.setText(df.format(ben)+"M");
                        }

                        tiao = yong - onet;//本次消耗
                        if (tiao >= 1024.00){//流量大于1024m将使用G来表示
                            tiao = tiao / 1024.00;

                            tiaotv.setText(df.format(tiao)+"G");
                        }else {
                            tiaotv.setText(df.format(tiao)+"M");
                        }


                        if (mianliu >= 1024.00){//流量大于1024m将使用G来表示
                            mianliu = mianliu / 1024.00;

                            miantv.setText(df.format(mianliu)+"G");
                        }else {
                            miantv.setText(df.format(mianliu)+"M");
                        }


                        if (zong >= 1024.00){//流量大于1024m将使用G来表示
                            zong = zong / 1024.00;

                            zongtv.setText(df.format(zong)+"G");
                        }else {
                            zongtv.setText(df.format(zong)+"M");
                        }


                        if (yong >= 1024.00){//流量大于1024m将使用G来表示
                            yong = yong / 1024.00;

                            yongtv.setText(df.format(yong)+"G");
                        }else {
                            yongtv.setText(df.format(yong)+"M");
                        }


                        if (sheng >= 1024.00){//流量大于1024m将使用G来表示
                            sheng = sheng / 1024.00;

                            shengtv.setText(df.format(sheng)+"G");
                        }else {
                            shengtv.setText(df.format(sheng)+"M");
                        }

                    }
                });

        }catch (Exception e){
            e.printStackTrace();
        }


    }




    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    windowManager.updateViewLayout(view, layoutParams);
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}
