package com.lgz.mq;

/**
 * Created by 85888 on 2018/7/21.
 */

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class PushCallback implements MqttCallback {

    //private NotificationManager notificationManager;
    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("连接失败---");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        //notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        System.out.println(" 有新消息到达时的回调方法");
        System.out.println(" topic = " + topic);
        String msg = new String(message.getPayload());
        System.out.println("msg = " + msg);
        System.out.println("qos = " + message.getQos());

        new MQTTService().sendNotification(msg);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("--deliveryComplete--成功发布某一消息后的回调方法");
    }

    public void sendNotification(){



       /* Intent intent = new Intent(null, MainActivity.class);



        // 创建一个点击通知的处理

        PendingIntent pendingIntent = PendingIntent.getActivities(new MainActivity(), 0, new Intent[]{intent} ,0);



        Notification.Builder builder = new Notification.Builder(new MainActivity());

        // 设置小图标

        builder.setSmallIcon(R.drawable.xiatianlong);

        // 设置通知时状态栏显示的文本

        builder.setTicker("通知状态栏的显示文本");

        // 设置通知的时间（此处去系统的当前时间）

        builder.setWhen(System.currentTimeMillis());

        // 设置通知的标题

        builder.setContentTitle("通知标题");

        // 设置通知的内容

        builder.setContentText("通知的内容");

        // 设置点击跳转

        builder.setContentIntent(pendingIntent);

        // 设置通知音

        builder.setDefaults(Notification.DEFAULT_SOUND);



        Notification notification = builder.build();



        notificationManager.notify(NOTIFICATION_ID ,notification);*/

    }

}
