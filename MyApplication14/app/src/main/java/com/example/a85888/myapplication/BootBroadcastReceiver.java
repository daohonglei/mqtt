package com.example.a85888.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {
    //重写onReceive方法
    @Override
    public void onReceive(Context context, Intent intent) {
        //后边的XXX.class就是要启动的服务
        intent = new Intent(context,MainActivity.class);
        context.startActivity(intent);
       /* Log.v("TAG", "开机自动服务自动启动.....");
        //启动应用，参数为需要自动启动的应用的包名
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        context.startActivity(intent );*/
    }

}
