package modularLibraries;

import org.eclipse.paho.client.mqttv3.*;

/*import org.eclipse.paho.mqttv5.client.IMqttClient;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;*/
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Arrays;

public class ModularLibraryMQTT {

    String broker;
    String publisherId;
    IMqttClient client;
    Integer qos;
    String topic;
    Boolean requestSent = false;
    Boolean responseReceived = false;
    String skillResult;

    public ModularLibraryMQTT(File xmlFile){

        Document xml = null;

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            xml = builder.parse(xmlFile);
            xml.getDocumentElement().normalize();
        } catch (Exception e){
            e.printStackTrace();
        }

        assert xml != null;
        Element configElement = xml.getDocumentElement();
        NodeList brokerNode = configElement.getElementsByTagName("broker");
        NodeList publisherIdNode = configElement.getElementsByTagName("publisher_id");
        NodeList qosNode = configElement.getElementsByTagName("quality_of_service");
        NodeList topicNode = configElement.getElementsByTagName("topic");

        this.broker = brokerNode.item(0).getTextContent();
        this.publisherId = publisherIdNode.item(0).getTextContent();
        this.qos = Integer.valueOf(qosNode.item(0).getTextContent());
        this.topic = topicNode.item(0).getTextContent();

        connectToBroker();
    }

    private void connectToBroker(){
        try {
            client = new MqttClient(broker, publisherId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setConnectionTimeout(10);
            System.out.println("Connecting to broker: " + broker);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                    responseReceived = true;

                    skillResult = Arrays.toString(mqttMessage.getPayload());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                }
            });

            client.connect(options);
            client.subscribe(topic);
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

    public String executeSkill(String skill){
        try {
            if(!client.isConnected()){
                connectToBroker();
            }
            System.out.println("Publishing message: " + skill + " in Topic: " + topic);
            MqttMessage message = new MqttMessage(skill.getBytes());
            message.setQos(qos);
            client.publish(topic, message);
            requestSent = true;
            System.out.println("Message Published");

            while(!responseReceived){}
            responseReceived = false;
            return skillResult;

        }catch(MqttException ex){
            System.out.println("reason "+ex.getReasonCode());
            System.out.println("msg "+ex.getMessage());
            System.out.println("loc "+ex.getLocalizedMessage());
            System.out.println("cause "+ex.getCause());
            System.out.println("excep "+ex);
            ex.printStackTrace();
        }
        return "Error";
    }
}
