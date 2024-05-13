package modularLibraries;

import io.netty.util.internal.StringUtil;
import jade.util.Logger;
import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedDataItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.List;
import java.util.logging.Level;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

public class ModularLibraryOPCUA {

    String skill;
    String result;
    String endpointUrl;
    String identifier;
    NodeId nodeId;
    int namespaceIndex;

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
        NodeList identifierNode = configElement.getElementsByTagName("identifier");

        this.endpointUrl = endpointUrlNode.item(0).getTextContent();
        this.namespaceIndex = Integer.parseInt(namespaceIndexNode.item(0).getTextContent());
        this.identifier = identifierNode.item(0).getTextContent();

        try{
            List<EndpointDescription> endpointsList = DiscoveryClient.getEndpoints(endpointUrl).get();
            EndpointDescription endpoint = EndpointUtil.updateUrl(endpointsList.getFirst(), "192.168.1.207", 54840);

            OpcUaClientConfigBuilder config = new OpcUaClientConfigBuilder();
            config.setEndpoint(endpoint);

            client = OpcUaClient.create(config.build());
            client.connect().get();
            nodeId = new NodeId(namespaceIndex, identifier);
        } catch(Exception ex){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }


    }

    public String ExecuteSkill(String skill) {

        this.skill = skill;

        try {
            AddressSpace addressSpace = client.getAddressSpace();

            UaVariableNode node = (UaVariableNode) addressSpace.getNode(nodeId);

            while(true){
                StatusCode statusCode = node.writeAttribute(
                        AttributeId.Value,
                        DataValue.valueOnly(new Variant(skill))
                );

                if(statusCode.isGood()){
                    break;
                }
            }

            ManagedSubscription subscription = ManagedSubscription.create(client);

            ManagedDataItem dataItem = subscription.createDataItem(nodeId);

            if(!dataItem.getStatusCode().isGood()){
                System.out.println("OPCUALib: Error in subscription");
            }

            subscription.addChangeListener(new ManagedSubscription.ChangeListener() {
                @Override
                public void onDataReceived(List<ManagedDataItem> dataItems, List<DataValue> dataValues) {
                    //System.out.println("AAAAAAAAAAAAAAAAA" + dataItems);
                    //System.out.println("AAAAAAAAAAAAAAAAA" + dataValues);
                    DataValue dataValue = dataValues.getFirst();
                    String res = ParseResult(dataValue);
                    if(!res.equals(skill)){
                        responseReceived = true;
                        result = res;
                    }
                }
            });

            while(!responseReceived){
                Thread.onSpinWait();
            }

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
