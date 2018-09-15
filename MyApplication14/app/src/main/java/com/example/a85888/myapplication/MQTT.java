package com.example.a85888.myapplication;

/**
 * Created by 85888 on 2018/7/21.
 */

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
public class MQTT {
    private String host = "tcp://192.168.160.1:1883";
    private String userName = "admin";
    private String passWord = "admin";
    private MqttClient client = null;
    public static void main(String[] args) {
        //发送消息
        MQTT mq = new MQTT();
        mq.passWord = "admin";
        for (int i = 0; i < 1; i++) {
            mq.send("World", "HelloTest" + 200, new PushCallBack() {

                @Override
                public int saveOnDone(boolean isOk) {
                    System.out.println("MQTT.main(...).new PushCallBack() {...}.saveOnDone()");
                    return 0;
                }
            });
        }

    }

    private static MQTT mqtt = null;

    public static MQTT get() {
        if (mqtt == null) {
            mqtt = new MQTT();
        }
        return mqtt;
    }

    private MQTT() {
        init();
    }

    private void init() {
        try {
            String clientid = MqttClient.generateClientId();
            if (clientid.length() > 23) {
                clientid = clientid.substring(clientid.length() - 23);
            }
            client = new MqttClient(host, clientid,
                    new MqttDefaultFilePersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(false);
            options.setUserName(userName);
            options.setPassword(passWord.toCharArray());
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(20);

            client.setCallback(new MqttCallback() {

                @Override
                public void connectionLost(Throwable cause) {
                    System.out.println("MqttCallback connectionLost-----------连接丢失");
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    System.out
                            .println("MqttCallback deliveryComplete---------交付完成" + token.getMessageId() + "|" + token.isComplete());
                }

                @Override
                public void messageArrived(String topic, MqttMessage arg1) throws Exception {
                    System.out.println("MqttCallback messageArrived----------消息到达");
                }

            });

            client.connect(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void close() {
        try {
            if (client != null) {
                client.close();
                client = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(final String topics, final String notify, PushCallBack pushCallBack) {
        send(topics, notify, 1, pushCallBack);
    }

    private void send(final String topics, final String notify, final int trycnt, final PushCallBack pushCallBack) {
        try {
            MqttDeliveryToken token;
            MqttMessage message;
            MqttTopic topic = client.getTopic(topics);
            message = new MqttMessage();
            message.setQos(0);
            message.setRetained(true);
            message.setPayload(notify.getBytes("UTF-8"));
            token = topic.publish(message);
            final int msgid = token.getMessageId();
            System.out.println(message.isRetained() + "------ratained|" + msgid);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onFailure(IMqttToken arg0, Throwable arg1) {
                    if (pushCallBack != null) {
                        pushCallBack.saveOnDone(false);
                    }
                    System.out.println(msgid + "------ActionCallback false");
                }

                @Override
                public void onSuccess(IMqttToken arg0) {
                    if (pushCallBack != null)
                        pushCallBack.saveOnDone(true);
                    System.out.println(msgid + "------ActionCallback true");
                }
            });
            token.waitForCompletion();
        } catch (Exception e) {
            if (trycnt < 2) {// 继续尝试一次
                close();
                init();
                send(topics, notify, trycnt + 1, pushCallBack);
            } else {
                e.printStackTrace();
            }
        }
    }
}
