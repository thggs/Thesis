package modularLibraries;

import io.netty.util.internal.StringUtil;
import jade.util.Logger;
import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedDataItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.List;
import java.util.logging.Level;

public class ModularLibraryOPCUA {

    String skill;
    String result;
    String endpointUrl;
    NodeId reqNodeId;
    NodeId resNodeId;

    volatile Boolean responseReceived = false;
    OpcUaClient client;

    public ModularLibraryOPCUA(File xmlFile){

        Document xml = null;

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            xml = builder.parse(xmlFile);
            xml.getDocumentElement().normalize();
        } catch (Exception ex){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }

        assert xml != null;
        Element configElement = xml.getDocumentElement();
        NodeList endpointUrlNode = configElement.getElementsByTagName("endpoint_url");
        NodeList namespaceIndexNode = configElement.getElementsByTagName("namespace_index");
        NodeList requestsIdentifierNode = configElement.getElementsByTagName("requests_identifier");
        NodeList responsesIdentifierNode = configElement.getElementsByTagName("responses_identifier");

        this.endpointUrl = endpointUrlNode.item(0).getTextContent();
        int namespaceIndex = Integer.parseInt(namespaceIndexNode.item(0).getTextContent());
        String reqIdentifier = requestsIdentifierNode.item(0).getTextContent();
        String resIdentifier = responsesIdentifierNode.item(0).getTextContent();

        System.out.println("OPCUALib: Connecting to server: " + endpointUrl);

        try{
            List<EndpointDescription> endpointsList = DiscoveryClient.getEndpoints(endpointUrl).get();

            OpcUaClientConfigBuilder config = new OpcUaClientConfigBuilder();
            config.setEndpoint(endpointsList.get(0));

            client = OpcUaClient.create(config.build());
            client.connect().get();
            System.out.println("OPCUALib: Connected to server");
            reqNodeId = new NodeId(namespaceIndex, reqIdentifier);
            resNodeId = new NodeId(namespaceIndex, resIdentifier);
        } catch(Exception ex){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }


    }

    public String ExecuteSkill(String skill) {

        this.skill = skill;

        try {
            AddressSpace addressSpace = client.getAddressSpace();

            UaVariableNode node = (UaVariableNode) addressSpace.getNode(reqNodeId);

            while(true){
                StatusCode statusCode = node.writeAttribute(
                        AttributeId.Value,
                        DataValue.valueOnly(new Variant(skill))
                );

                if(statusCode.isGood()){
                    break;
                }
            }
            System.out.println("Wrote request");
            ManagedSubscription subscription = ManagedSubscription.create(client);

            ManagedDataItem dataItem = subscription.createDataItem(resNodeId);

            if(!dataItem.getStatusCode().isGood()){
                System.out.println("OPCUALib: Error in subscription");
            }

            subscription.addChangeListener(new ManagedSubscription.ChangeListener() {
                @Override
                public void onDataReceived(List<ManagedDataItem> dataItems, List<DataValue> dataValues) {
                    DataValue dataValue = dataValues.get(0);
                    String res = ParseResult(dataValue);
                    responseReceived = true;
                    result = res;
                    System.out.println("Received a Value" + res);
                }
            });

            while(!responseReceived){
                Thread.onSpinWait();
            }
            System.out.println("OPCUALib: Message Received: " + result);
            responseReceived = false;

            subscription.delete();

            responseReceived = false;

            return result;

        }
        catch(Exception ex){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }

        return "error";
    }

    private String ParseResult(DataValue dataValue){
        String value = dataValue.getValue().toString();
        value = StringUtil.substringBefore(value, '}');
        value = StringUtil.substringAfter(value, '=');
        return value;
    }

    public void Stop(){
        System.out.println("OPCUALib: Disconnecting...");
        client.disconnect();
    }

}
