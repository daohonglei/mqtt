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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import com.example.a85888.myapplication.ActivityB;
import com.example.a85888.myapplication.R;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTService_bak extends Service {

    //消息服务器的URL
    public static final String BROKER_URL = "tcp://192.168.1.81:61613";
    //客户端ID，用来标识一个客户，可以根据不同的策略来生成
    public static final String clientId = "admin1";
    //订阅的主题名
    public static final String TOPIC = "World";
    //mqtt客户端类
    private MqttClient mqttClient;
    //mqtt连接配置类
    private MqttConnectOptions options;

    private String userName = "admin";
    private String passWord = "password";
    private String msg;
    private Runnable runnable;


    private NotificationManager notificationManager;


    /*private ServiceHelp mHelper = new ServiceHelp();

    public class ServiceHelp extends Binder {

        public MQTTService getMainService(){

            return MQTTService.this;

        }

    }*/

    @Override
    public void onCreate() {
        super.onCreate();
        connect();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }
    private void connect(){
        try {
            //在服务开始时new一个mqttClient实例，客户端ID为clientId，第三个参数说明是持久化客户端，如果是null则是非持久化
            mqttClient = new MqttClient(BROKER_URL, clientId, new MemoryPersistence());
            // MQTT的连接设置
            options = new MqttConnectOptions();
            // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            //换而言之，设置为false时可以客户端可以接受离线消息
            options.setCleanSession(true);
            // 设置连接的用户名
            options.setUserName(userName);
            // 设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            options.setAutomaticReconnect(true);
            // 设置回调  回调类的说明看后面
            mqttClient.setCallback(new MqttCallback(){
                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("连接失败---");
                }
                @Override
                public String toString() {
                    return super.toString();
                }
                @Override
                public void messageArrived(String topic,MqttMessage message) throws Exception {
                    System.out.println(" 有新消息到达时的回调方法");
                    System.out.println(" topic = " + topic);
                    msg = new String(message.getPayload());
                    System.out.println("msg = " + msg);
                    System.out.println("qos = " + message.getQos());
                    if (!"close".equals(msg) &&msg!=""){
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "msg is coming!" , Toast.LENGTH_LONG).show();
                                sendNotification(msg);
                            }
                        };
                        new Thread(){
                            public void run() {
                                new Handler(Looper.getMainLooper()).post(runnable);
                            }
                        }.start();
                    }
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    System.out.println("--deliveryComplete--成功发布某一消息后的回调方法");
                }
            });
            //MqttTopic topic = mqttClient.getTopic(TOPIC);
            //setWill方法，如果项目中需要知道客户端是否掉线可以调用该方法。设置最终端口的通知消息
            //options.setWill(topic, "close".getBytes(), 2, false);
            //mqtt客户端连接服务器
            mqttClient.connect(options);

            //mqtt客户端订阅主题
            //在mqtt中用QoS来标识服务质量
            //QoS=0时，报文最多发送一次，有可能丢失
            //QoS=1时，报文至少发送一次，有可能重复
            //QoS=2时，报文只发送一次，并且确保消息只到达一次。
            int[] Qos = {1};
            String[] topic1 = {TOPIC};
            mqttClient.subscribe(topic1,Qos);
        } catch (MqttException e) {
            Toast.makeText(getApplicationContext(), "Something went wrong!" + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
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
        try {
            mqttClient.disconnect(0);
        } catch (MqttException e) {
            Toast.makeText(getApplicationContext(), "Something went wrong!" + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

}
