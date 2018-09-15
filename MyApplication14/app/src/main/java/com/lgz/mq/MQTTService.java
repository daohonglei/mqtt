package com.lgz.mq;

/**
 * Created by 85888 on 2018/7/21.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.a85888.myapplication.ActivityB;
import com.example.a85888.myapplication.MainActivity;
import com.example.a85888.myapplication.R;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MQTTService extends Service {

    private MqttClient client;
    private MqttConnectOptions options;
    private String mTopic = "World"; // 默认话题为"World"
    private ScheduledExecutorService scheduler;
    private NotificationManager notificationManager;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                sendNotification(msg.toString());
            } else if (msg.what == 2) {
                System.out.println("连接成功");
                Toast.makeText(getApplicationContext(), "连接成功", Toast.LENGTH_SHORT).show();
                try {
                    client.subscribe(mTopic, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (msg.what == 3) {
                Toast.makeText(getApplicationContext(), "连接失败，系统正在重连", Toast.LENGTH_SHORT).show();
                System.out.println("连接失败，系统正在重连");
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        connect();
        startReconnect();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }


    private void init() {
        try {
            // host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttClient(MqttConstants.host, "1", new MemoryPersistence());
            // MQTT的连接设置
            options = new MqttConnectOptions();
            // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(true);
            // 设置连接的用户名
            options.setUserName(MqttConstants.userName);
            // 设置连接的密码
            options.setPassword(MqttConstants.passWord.toCharArray());
            // 设置超时时间,单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            // 设置回调
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    // 连接丢失后，一般在这里面进行重连
                    System.out.println("connectionLost----------");
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //publish后会执行到这里
                    System.out.println("deliveryComplete---------"+ token.isComplete());
                }
                @Override
                public void messageArrived(String topicName, MqttMessage message) throws Exception {
                    // subscribe后得到的消息会执行到这里面
                    System.out.println("messageArrived----------");
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = topicName + ":" + message.toString();
                    handler.sendMessage(msg);
                    Log.e("TAG", "messageArrived" + message.toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.connect(options);
                    client.subscribe(mTopic);
                    Message msg = new Message();
                    msg.what = 2;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = 3;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    private void startReconnect() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!client.isConnected()) {
                    connect();
                }
            }
        }, 1 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
    }

    public void sendNotification(String  string){
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, ActivityB.class);
        // 创建一个点击通知的处理
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[]{intent} ,0);
        Notification.Builder builder = new Notification.Builder(this);
        // 设置小图标
        builder.setSmallIcon(R.drawable.xiatianlong);
        // 设置通知时状态栏显示的文本
        builder.setTicker("通知状态栏的显示文本");
        // 设置通知的时间（此处去系统的当前时间）
        builder.setWhen(System.currentTimeMillis());
        // 设置通知的标题
        builder.setContentTitle("通知标题");
        // 设置通知的内容
        builder.setContentText(string);
        // 设置点击跳转
        builder.setContentIntent(pendingIntent);
        // 设置通知音
        builder.setDefaults(Notification.DEFAULT_SOUND);
        Notification notification = builder.build();
        notificationManager.notify(100,notification);
       /* Notification.Builder localBuilder = new Notification.Builder(this);
        localBuilder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this,ActivityB.class), 0));
        localBuilder.setAutoCancel(true);
        localBuilder.setSmallIcon(R.drawable.xiatianlong);
        localBuilder.setTicker("Foreground Service Start");
        localBuilder.setContentTitle("Socket服务端");
        localBuilder.setContentText(string);
        Notification notification=localBuilder.getNotification();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        //localBuilder.setAutoCancel(true);
        startForeground(1, notification);*/

    }

    @Override
    public void onDestroy() {
        scheduler.shutdown();
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}
