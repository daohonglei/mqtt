package com.example.a85888.myapplication;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.lgz.mq.MQTTService;

public class Mcbak1 extends AppCompatActivity {

    private boolean mIsBind = false;

    private boolean mIsConnected = false;

    private MQTTService mMainService;

    private boolean mIsForegroundService = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
           // mMainService = ((MQTTService.ServiceHelp)service).getMainService();
            if (mMainService != null){
                Mcbak1.this.mIsConnected = true;
            }else{
                new Throwable("服务绑定失败");
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Mcbak1.this.mIsConnected = false;
            Log.i("zyq","ServiceConnection:onServiceDisconnected");
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*Intent intent = new Intent(this, MQTTService.class);
        mMainService.startForeground(1,getNotification());
        startService(intent);*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(null == startService(new Intent(Mcbak1.this,MQTTService.class))){
            new Throwable("无法启动服务");
        }
        mIsBind=bindService(new Intent(Mcbak1.this,MQTTService.class),mConnection, Context.BIND_AUTO_CREATE);
        if(mIsBind && mMainService != null && !mIsForegroundService){
            mMainService.startForeground(1,getNotification());
            mIsForegroundService = true;
        }
    }


    private Notification getNotification(){
        Notification.Builder mBuilder = new Notification.Builder(Mcbak1.this);
        mBuilder.setShowWhen(false);
        mBuilder.setAutoCancel(false);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setLargeIcon(((BitmapDrawable)getDrawable(R.drawable.xiatianlong)).getBitmap());
        mBuilder.setContentText("this is content");
        mBuilder.setContentTitle("this is title");
        return mBuilder.build();
    }
}
