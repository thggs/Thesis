package modularLibraries;

import org.eclipse.paho.mqttv5.client.IMqttClient;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;

import java.util.UUID;

public class ModularLibraryMQTT {

    String broker;
    String publisherId;
    IMqttClient publisher;

    public ModularLibraryMQTT(String broker){
        this.publisherId = UUID.randomUUID().toString();
        this.broker = broker;
        connectToBroker();
    }

    public void setPublisherId(String publisherId){
        this.publisherId = publisherId;
        connectToBroker();
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setBroker(String broker){
        this.broker = broker;
        connectToBroker();
    }

    public String getBroker(){
        return broker;
    }

    private void connectToBroker(){
        try {
            publisher = new MqttClient(broker, publisherId);
            MqttConnectionOptions options = new MqttConnectionOptions();
            options.setAutomaticReconnect(true);
            options.setCleanStart(true);
            options.setConnectionTimeout(10);
            System.out.println("Connecting to broker: " + broker);
            publisher.connect(options);
            System.out.println("Connected");
        } catch(MqttException ex) {
            System.out.println("reason "+ex.getReasonCode());
            System.out.println("msg "+ex.getMessage());
            System.out.println("loc "+ex.getLocalizedMessage());
            System.out.println("cause "+ex.getCause());
            System.out.println("excep "+ex);
            ex.printStackTrace();
        }
    }

    public void sendMessage(String content, int qos, String topic){
        try {
            System.out.println("Publishing message: " + content + " in Topic: " + topic);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            publisher.publish(topic, message);
            System.out.println("Message Published");

        }catch(MqttException ex){
            System.out.println("reason "+ex.getReasonCode());
            System.out.println("msg "+ex.getMessage());
            System.out.println("loc "+ex.getLocalizedMessage());
            System.out.println("cause "+ex.getCause());
            System.out.println("excep "+ex);
            ex.printStackTrace();
        }
    }
}
