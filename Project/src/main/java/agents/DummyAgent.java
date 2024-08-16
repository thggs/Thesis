package agents;

import jade.core.Agent;
import moduleEngine.ModuleEngine;
import org.eclipse.paho.client.mqttv3.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.UUID;

public class DummyAgent extends Agent {

    ModuleEngine moduleEngine;
    File xmlMarketplaceFile = new File("C:\\Users\\ddrod\\Documents\\GitHub\\Thesis\\Project\\marketplace.xml");
    File xmlConfigFile = new File("C:\\Users\\ddrod\\Documents\\GitHub\\Thesis\\Project\\configFiles\\configDelayTest.xml");

    //HTTP
    URL url;

    //MQTT
    String broker;
    String publisherId;
    IMqttClient client;
    Integer qos;
    String request_topic;
    String response_topic;
    String skill;
    volatile Boolean responseReceived = false;
    String skillResult;


    @Override
    protected void setup() {
        int iter = 100000;


        long averageDiff = 0;
        for(int i = 0; i < iter; i++){
            String libtype = "HTTP";
            moduleEngine = new ModuleEngine(libtype, xmlConfigFile, xmlMarketplaceFile);
            prepare_http();
            long startTimeME = System.nanoTime();
            moduleEngine.executeSkill("Test");
            long endTimeME = System.nanoTime();
            long executionTimeME = (endTimeME - startTimeME);

            long startTimeNormal = System.nanoTime();
            execute_http("Test");
            long endTimeNormal = System.nanoTime();
            long executionTimeNormal = (endTimeNormal - startTimeNormal);

            long diffTime = executionTimeME - executionTimeNormal;
            averageDiff += diffTime;

            System.out.println("Diff: " + diffTime + "ns. Iter: " + i);
        }

        averageDiff = averageDiff/iter;

        System.out.println("HTTP AverageDiff: " + averageDiff + "ns");
        System.out.println("HTTP AverageDiff: " + averageDiff/1000000 + "ms");

        /*averageDiff = 0;
        for(int i = 0; i < iter; i++){
            String libtype = "MQTT";
            moduleEngine = new ModuleEngine(libtype, xmlConfigFile, xmlMarketplaceFile);

            prepare_mqtt();
            long startTimeME = System.nanoTime();
            moduleEngine.executeSkill("Test");
            long endTimeME = System.nanoTime();
            long executionTimeME = (endTimeME - startTimeME);

            long startTimeNormal = System.nanoTime();
            execute_mqtt("Test");
            long endTimeNormal = System.nanoTime();
            long executionTimeNormal = (endTimeNormal - startTimeNormal);

            long diffTime = executionTimeME - executionTimeNormal;
            averageDiff += diffTime;

            System.out.println("Diff: " + diffTime + "ns");
        }

        averageDiff = averageDiff/iter;

        System.out.println("MQTT AverageDiff: " + averageDiff + "ns");
        System.out.println("MQTT AverageDiff: " + averageDiff/1000000 + "ms");

        averageDiff = 0;
        for(int i = 0; i < iter; i++){
            String libtype = "OPC UA";
            moduleEngine = new ModuleEngine(libtype, xmlConfigFile, xmlMarketplaceFile);
        }*/
    }

    @Override
    protected void takeDown(){
        super.takeDown();
    }

    private void prepare_http() {
        Document xml;

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            xml = builder.parse(xmlConfigFile);
            xml.getDocumentElement().normalize();

            Element configElement = xml.getDocumentElement();
            NodeList addressNode = configElement.getElementsByTagName("address");

            url = new URI(addressNode.item(0).getTextContent()).toURL();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String execute_http(String skill) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            String payload = "payload=" + skill;
            os.write(payload.getBytes());
            os.flush();
            os.close();

            int responseCode = connection.getResponseCode();
            System.out.println("HTTPLib: Post response Code " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            }
            return "error";
        }catch (Exception e){
            e.printStackTrace();
            return "error";
        }
    }

    public void prepare_mqtt(){
        Document xml = null;

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            xml = builder.parse(xmlConfigFile);
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
            client = new MqttClient(broker, publisherId, null);
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

    public String execute_mqtt(String skill){
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
}
