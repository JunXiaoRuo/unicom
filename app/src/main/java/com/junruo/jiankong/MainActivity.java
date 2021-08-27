package com.junruo.jiankong;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.junruo.jiankong.databinding.ActivityMainBinding;
import com.lzy.okhttputils.OkHttpUtils;
import com.lzy.okhttputils.callback.StringCallback;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private String dayin = "";//打印流量包
    private Double mianliu=0.00;//总免流
    private Double zong=0.00;//套餐总量
    private Double yong=0.00;//套餐已用
    private Double sheng =0.00;//剩余流量
    private Double ben = 0.00;//本次免流
    private Double tiao = 0.00;//本次消耗

    private Double dingz = 0.00;//定向总量
    private Double dingy = 0.00;//定向已用
    private Double dings = 0.00;//定向剩余

    private String orone = "yes";//是否首次获取

    private Double onem = 0.00;//初始化本次免流
    private Double onet = 0.00;//初始化本次消耗

    private String time ;

    private String cookie = "";//储存cookie信息

    private String versionName = "";
    private int versioncode;
    private String oldVersion ;

    private String NewVersion,versionmsg;

    private String gao,kuan,xgao,xkuan;
    //定义读写权限
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashHandler.getInstance().init(getApplicationContext());
        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        //获取当前版本
        oldVersion = getAppVersionName(this);
        binding.banben.setText("V"+oldVersion);

        //初始化
        initView();

        //获取读写外部存储权限
        verifyStoragePermissions(this);

    }

    //申请读写权限
    public void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }else {
                File file = new File(getExternalFilesDir(null).toString()+"/crash/");
                //判断文件夹是否存在,如果不存在则创建文件夹
                if (!file.exists()) {
                    boolean mkdir = file.mkdir();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取当前版本
    public String getAppVersionName(Context context) {
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            versioncode = pi.versionCode;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }



    private void initView() {
        //检查更新
        upupup();



        // 创建SharedPreferences对象用于获取Cookie信息,并将其私有化
        SharedPreferences share = getSharedPreferences("Cookie",
                Context.MODE_PRIVATE);
        // 获取编辑器来存储数据到sha redpreferences中
        cookie = share.getString("Cookie","");
        time = share.getString("time","");
        gao = share.getString("gao","320");
        xgao = share.getString("xgao","150");
        xkuan = share.getString("xkuan","200");
        kuan = share.getString("kuan","230");


        if (time.equals("")){

        }else {
            binding.shuaxin.setText(time);
        }

        binding.cookie.setText(cookie);

        binding.kuan.setText(kuan);
        binding.gao.setText(gao);
        binding.xkuan.setText(xkuan);
        binding.xgao.setText(xgao);


        OnClick();


    }

    private void OnClick() {

        binding.stop.setOnClickListener(v -> {//点击开始
            if (binding.cookie.getText().toString().equals("")){
                toast("请填写cookie后再登录！");
            }else {
                cookie = binding.cookie.getText().toString();
                //开始获取数据
                update();
            }

        });


        binding.join.setOnClickListener(v -> {
            //加群代码

            String key = "QE-4bRJCa2w2Tu4lnWNuRBx4NqlIL8Op";
            Intent intent = new Intent();
            intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D" + key));
            // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                startActivity(intent);
                toast("正在加群.....");
                return ;
            } catch (Exception e) {
                // 未安装手Q或安装的版本不支持
                toast("未安装手Q或安装的版本不支持.");
                return ;
            }

        });


    }

    //版本更新检查
    private void upupup() {

        try {

            OkHttpUtils.get("http://tool.chaoxing.zmorg.cn/api/appConfig.php?id=5&action=useConfig")
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(String s, Call call, Response response) {

                            JSONObject json = JSONObject.parseObject(s);

                            NewVersion = json.getString("version");
                            versionmsg = json.getString("msg");

                            if (!oldVersion.equals(NewVersion)) {
                                AlertDialog alertDialog2 = new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("有更新啦！")
                                        .setMessage(versionmsg)
                                        .setIcon(R.mipmap.ic_launcher)
                                        .setPositiveButton("更新", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                String key = "QE-4bRJCa2w2Tu4lnWNuRBx4NqlIL8Op";
                                                Intent intent = new Intent();
                                                intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D" + key));
                                                // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                try {
                                                    startActivity(intent);
                                                    toast("正在加群.....");
                                                    return ;
                                                } catch (Exception e) {
                                                    // 未安装手Q或安装的版本不支持
                                                    toast("未安装手Q或安装的版本不支持.");
                                                    return ;
                                                }


                                            }
                                        })

                                        .setNegativeButton("待会更新", new DialogInterface.OnClickListener() {//添加取消
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                            }
                                        })
                                        .create();
                                alertDialog2.show();

                            }
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startFloatingButtonService(View view) {
        if (binding.xfc.getText().toString().equals("显示悬浮窗")){

            if (binding.cookie.getText().toString().equals("")){
                toast("请填写cookie后再开启悬浮窗！");
            }else {
                if (FloatingImageDisplayService.isStarted) {
                    binding.xfc.setText("关闭悬浮窗");
                    return;
                }
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "当前无权限，请授权", Toast.LENGTH_SHORT);
                    startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 1);
                } else {
                    if (binding.shuaxin.getText().toString().equals("")){
                        toast("请输入刷新时间后开启悬浮窗。");
                    }else {
                        // 创建SharedPreferences对象用于存储Cookie信息,并将其私有化
                        SharedPreferences share = getSharedPreferences("Cookie",
                                Context.MODE_PRIVATE);
                        // 获取编辑器来存储数据到sharedpreferences中
                        SharedPreferences.Editor editor = share.edit();
                        editor.putString("time",binding.shuaxin.getText().toString());
                        editor.putString("Cookie",binding.cookie.getText().toString());
                        editor.putString("gao",binding.gao.getText().toString());
                        editor.putString("kuan",binding.kuan.getText().toString());
                        editor.putString("xgao",binding.xgao.getText().toString());
                        editor.putString("xkuan",binding.xkuan.getText().toString());
                        editor.commit();
                        binding.xfc.setText("关闭悬浮窗");

                        Intent intent = new Intent(MainActivity.this, FloatingImageDisplayService.class);
                        startService(intent);


                    }
                }
            }

        }  else {

            Intent intent = new Intent(MainActivity.this, FloatingImageDisplayService.class);
            stopService(intent);

            binding.xfc.setText("显示悬浮窗");

        }

    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (!Settings.canDrawOverlays(this)) {
            } else {
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                //startService(new Intent(MainActivity.this, FloatingImageDisplayService.class));
            }
        }
    }



    //更新数据
    private void update(){

        //初始化数据防止累加
        mianliu=0.00;//总免流
        zong=0.00;//套餐总量
        yong=0.00;//套餐已用
        sheng =0.00;//剩余流量
        dingz = 0.00;//定向总量
        dingy = 0.00;//定向已用
        dings = 0.00;//定向剩余

        dayin = "";
        binding.dayin.setText("");
        //格式化double值
        DecimalFormat df = new DecimalFormat("0.000");
        Date day=new Date();
        SimpleDateFormat sj = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        binding.sj.setText(sj.format(day));

        try {
        OkHttpUtils.post("https://m.client.10010.com/mobileservicequery/operationservice/queryOcsPackageFlowLeftContent")
                .headers("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .headers("Cookie", cookie)
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                        Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }

                    @SuppressLint("LongLogTag")
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        toast("正在解析");
                        System.out.println(s);
                        if (s.equals("999999")||s.equals("")||s == null){
                            toast("失败，请重新获取cookie");
                            return;
                        }
                        JSONObject json = JSONObject.parseObject(s);

                        binding.packageName.setText(json.get("packageName").toString());


                        JSONArray jsonArray = json.getJSONArray("resources");

                        JSONObject job = jsonArray.getJSONObject(0);

                        JSONArray details = job.getJSONArray("details");


                        for (int i = 0; i < details.size(); i++) {
                            JSONObject liuliang = details.getJSONObject(i);
                            if (liuliang.getString("limited").equals("0")) {//套内流量包
                                if (liuliang.getString("addupItemCode") != null){

                                    if (!liuliang.getString("addupItemCode").equals("40008")){//通用流量包
                                        String feePolicyName = liuliang.getString("feePolicyName");//流量包名称
                                        String total = liuliang.getString("total");//流量包总量
                                        String use = liuliang.getString("use");//流量包使用
                                        String remain = liuliang.getString("remain");//流量包剩余

                                        zong = zong + Double.parseDouble(total);
                                        yong = yong + Double.parseDouble(use);
                                        sheng = sheng + Double.parseDouble(remain);

                                        dayin = dayin + "\n通用包名称：" + feePolicyName + "总量：" + total + "M，已使用：" + use + "M，剩余" + remain + "M\n";
                                    }else {//定向流量包
                                        String feePolicyName = liuliang.getString("feePolicyName");//流量包名称
                                        String total = liuliang.getString("total");//流量包总量
                                        String use = liuliang.getString("use");//流量包使用
                                        String remain = liuliang.getString("remain");//流量包剩余

                                        dingz = dingz + Double.parseDouble(total);
                                        dingy = dingy + Double.parseDouble(use);
                                        dings = dings + Double.parseDouble(remain);


                                        mianliu = mianliu + Double.parseDouble(use);

                                        dayin = dayin + "\n定向包名称：" + feePolicyName + "总量：" + total + "M，已使用：" + use + "M，剩余" + remain + "M\n";
                                    }

                                }else {

                                    String feePolicyName = liuliang.getString("feePolicyName");//流量包名称
                                    String total = liuliang.getString("total");//流量包总量
                                    String use = liuliang.getString("use");//流量包使用
                                    String remain = liuliang.getString("remain");//流量包剩余

                                    zong = zong + Double.parseDouble(total);
                                    yong = yong + Double.parseDouble(use);
                                    sheng = sheng + Double.parseDouble(remain);

                                    dayin = dayin + "\n通用包名称：" + feePolicyName + "总量：" + total + "M，已使用：" + use + "M，剩余" + remain + "M\n";

                                }

                            }else if (liuliang.getString("addUpItemName")==null||liuliang.getString("addupItemCode").equals("40008")){
                                String feePolicyName = liuliang.getString("feePolicyName");//免流包名称
                                String use = liuliang.getString("use");//已免流


                                mianliu = mianliu + Double.parseDouble(use);
                                dayin = dayin + "\n免流包名称：" + feePolicyName +"已使用：" + use + "M\n";
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
                            //toast("不是第一次");
                        }

                        ben = mianliu - onem;//本次免流
                        if (ben >= 1024.00){//流量大于1024m将使用G来表示
                            ben = ben / 1024.00;

                            binding.ben.setText(df.format(ben)+"G");
                        }else {
                            binding.ben.setText(df.format(ben)+"M");
                        }

                        tiao = yong - onet;//本次消耗
                        if (tiao >= 1024.00){//流量大于1024m将使用G来表示
                            tiao = tiao / 1024.00;

                            binding.tiao.setText(df.format(tiao)+"G");
                        }else {
                            binding.tiao.setText(df.format(tiao)+"M");
                        }


                        if (mianliu >= 1024.00){//流量大于1024m将使用G来表示
                            mianliu = mianliu / 1024.00;

                            binding.mian.setText(df.format(mianliu)+"G");
                        }else {
                            binding.mian.setText(df.format(mianliu)+"M");
                        }

                        //套餐
                        if (zong >= 1024.00){//流量大于1024m将使用G来表示
                            zong = zong / 1024.00;

                            binding.zong.setText(df.format(zong)+"G");
                        }else {
                            binding.zong.setText(df.format(zong)+"M");
                        }


                        if (yong >= 1024.00){//流量大于1024m将使用G来表示
                            yong = yong / 1024.00;

                            binding.yong.setText(df.format(yong)+"G");
                        }else {
                            binding.yong.setText(df.format(yong)+"M");
                        }


                        if (sheng >= 1024.00){//流量大于1024m将使用G来表示
                            sheng = sheng / 1024.00;

                            binding.sheng.setText(df.format(sheng)+"G");
                        }else {
                            binding.sheng.setText(df.format(sheng)+"M");
                        }

                        //定向
                        if (dingz >= 1024.00){//流量大于1024m将使用G来表示
                            dingz = dingz / 1024.00;

                            binding.dingz.setText(df.format(dingz)+"G");
                        }else {
                            binding.dingz.setText(df.format(dingz)+"M");
                        }


                        if (dingy >= 1024.00){//流量大于1024m将使用G来表示
                            dingy = dingy / 1024.00;

                            binding.dingy.setText(df.format(dingy)+"G");
                        }else {
                            binding.dingy.setText(df.format(dingy)+"M");
                        }


                        if (dings >= 1024.00){//流量大于1024m将使用G来表示
                            dings = dings / 1024.00;

                            binding.dings.setText(df.format(dings)+"G");
                        }else {
                            binding.dings.setText(df.format(dings)+"M");
                        }

                        binding.dayin.setText(dayin);

                        toast("解析成功");

                      /*  if (code.equals("200")){
                            //toast("获取用户信息成功！");
                            JSONObject data = JSONArray.parseObject(json.get("data").toString());

                        }else {
                            toast("未知错误");
                        }*/

                    }
                });
        }catch (Exception e){
            e.printStackTrace();
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