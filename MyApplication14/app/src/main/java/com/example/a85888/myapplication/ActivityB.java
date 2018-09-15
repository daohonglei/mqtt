package com.example.a85888.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class ActivityB extends AppCompatActivity {
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_b);


        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(100);

        Notification.Builder localBuilder = new Notification.Builder(this);
        localBuilder.setAutoCancel(true);
    }
}
