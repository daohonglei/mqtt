package com.lgz.mq;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class ContentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent it=new Intent(context,MQTTService.class);
        context.startService(it);
    }
}
