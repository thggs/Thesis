package modularLibraries;

import org.eclipse.paho.client.mqttv3.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.UUID;

public class ModularLibraryMQTT {

    String broker;
    String publisherId;
    IMqttClient client;
    Integer qos;
    String request_topic;
    String response_topic;
    String skill;
    volatile Boolean responseReceived = false;
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
        NodeList qosNode = configElement.getElementsByTagName("quality_of_service");
        NodeList topicReqNode = configElement.getElementsByTagName("request_topic");
        NodeList topicResNode = configElement.getElementsByTagName("response_topic");
        NodeList publisherIdNode = configElement.getElementsByTagName("publisher_id");

        this.broker = brokerNode.item(0).getTextContent();
        this.qos = Integer.valueOf(qosNode.item(0).getTextContent());
        this.request_topic = topicReqNode.item(0).getTextContent();
        this.response_topic = topicResNode.item(0).getTextContent();
        if(publisherIdNode.getLength() == 0){
            this.publisherId = UUID.randomUUID().toString();
        }else{
            this.publisherId = publisherIdNode.item(0).getTextContent();
        }

        ConnectToBroker();
    }

    private void ConnectToBroker(){
        try {
            client = new MqttClient(broker, publisherId);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setConnectionTimeout(10);
            System.out.println("MQTTLib: Connecting to broker: " + broker);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {

                }

                @Override
                public void messageArrived(String msgTopic, MqttMessage mqttMessage) {
                    String msg = new String(mqttMessage.getPayload());
                    System.out.println("MQTT Library message received: " + msg);
                    responseReceived = true;
                    skillResult = msg;
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                }
            });

            client.connect(options);
            client.subscribe(response_topic);
            System.out.println("MQTTLib: Connected");
        } catch(MqttException ex) {
            System.out.println("reason "+ex.getReasonCode());
            System.out.println("msg "+ex.getMessage());
            System.out.println("loc "+ex.getLocalizedMessage());
            System.out.println("cause "+ex.getCause());
            System.out.println("excep "+ex);
            ex.printStackTrace();
        }
    }

    public String ExecuteSkill(String skill){
        try {
            if(!client.isConnected()){
                ConnectToBroker();
            }
            this.skill = skill;
            System.out.println("MQTTLib: Publishing message: " + skill + " in Topic: " + request_topic);
            MqttMessage message = new MqttMessage(skill.getBytes());
            message.setQos(qos);
            message.setId(0);
            client.publish(request_topic, message);
            System.out.println("MQTTLib: Message Published");

            while (!responseReceived) {
                Thread.onSpinWait();
            }
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
        return "error";
    }

    public void Stop() throws MqttException {
        System.out.println("MQTTLib: Disconnecting...");
        client.disconnect();
    }
}
