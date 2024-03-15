package modularLibraries;

import org.eclipse.paho.mqttv5.client.IMqttClient;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class ModularLibraryMQTT {

    String broker;
    String publisherId;
    IMqttClient publisher;
    Integer qos;
    String topic;

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

    public void executeSkill(String skill){
        try {
            System.out.println("Publishing message: " + skill + " in Topic: " + topic);
            MqttMessage message = new MqttMessage(skill.getBytes());
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
