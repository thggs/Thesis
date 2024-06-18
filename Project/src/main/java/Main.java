import io.netty.util.internal.StringUtil;
import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedDataItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main {

    static String endpointUrl = "opc.tcp://192.168.250.100:53880";
    static String value;

    public static void main(String[] args) throws UaException, ExecutionException, InterruptedException, URISyntaxException, IOException {

        OPCUA();

//        List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints(endpointUrl).get();
//        //EndpointDescription endpoint = EndpointUtil.updateUrl(endpoints.getFirst(), "192.168.1.207", 53880);
//
//        for(EndpointDescription item : endpoints){
//            System.out.println(item);
//        }
//
//        OpcUaClientConfigBuilder configBuilder = new OpcUaClientConfigBuilder();
//        configBuilder.setEndpoint(endpoints.getFirst());
//
//        OpcUaClient client = OpcUaClient.create(configBuilder.build());
//
//        client.connect().get();

//        try{
//            URL url = new URI("http://192.168.1.207:1880/conveyor").toURL();
//
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Test_key", "Test_value");
//
//            connection.setDoOutput(true);
//            OutputStream os = connection.getOutputStream();
//            os.write("payload=Source#TOKEN#C2".getBytes());
//            os.flush();
//            os.close();
//
//            int responseCode = connection.getResponseCode();
//            System.out.println("POST response Code: " + responseCode);
//
//            if(responseCode == HttpURLConnection.HTTP_OK){
//                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                String inputLine;
//                StringBuilder response = new StringBuilder();
//
//                while((inputLine = in.readLine()) != null){
//                    response.append(inputLine);
//                }
//                in.close();
//                System.out.println(response);
//            }
//        }
//        catch(Exception ex){
//            throw ex;
//        }
    }

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static void OPCUA() throws UaException, ExecutionException, InterruptedException {

        // Client connection START


        OpcUaClient client = OpcUaClient.create(
                endpointUrl,
                endpoints ->
                    endpoints.stream()
                        .filter(e -> e.getSecurityPolicyUri().equals(SecurityPolicy.None.getUri()))
                        .findFirst(),
                configBuilder ->
                    configBuilder.build()
        );

        client.connect().get();

        // Client connection END

        // Client write value START

        NodeId nodeIdReq = new NodeId(1, "Conveyor_Req");

        AddressSpace addressSpace = client.getAddressSpace();

        UaVariableNode testNode = (UaVariableNode) addressSpace.getNode(nodeIdReq);

        StatusCode statusCode = testNode.writeAttribute(
                AttributeId.Value,
                DataValue.valueOnly(new Variant("42"))
        );

        // Client write value END

        // Client subscription START

        NodeId nodeIdRes = new NodeId(1, "Conveyor_Res");

        ManagedSubscription subscription = ManagedSubscription.create(client);

        ManagedDataItem dataItem = subscription.createDataItem(nodeIdRes);

        if(!dataItem.getStatusCode().isGood()){
            System.out.println("Error");
        }

        subscription.addChangeListener(new ManagedSubscription.ChangeListener() {
            @Override
            public void onDataReceived(List<ManagedDataItem> dataItems, List<DataValue> dataValues){
                DataValue dataValue = dataValues.get(0);
                value = ParseResult(dataValue);
                System.out.println(value);
            }
        });

        // Client subscription END

        Thread.sleep(10000);

        client.disconnect();
    }

    private static String ParseResult(DataValue dataValue){
        String value = dataValue.getValue().toString();
        value = StringUtil.substringBefore(value, '}');
        value = StringUtil.substringAfter(value, '=');
        return value;
    }

    private void HTTP() throws URISyntaxException, IOException{
        URL url = new URI("http://192.168.1.207:1880/conveyor").toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Test_key", "Test_value");

        connection.setDoOutput(true);
        OutputStream os = connection.getOutputStream();
        os.write("payload=Source#TOKEN#C2".getBytes());
        os.flush();
        os.close();

        int responseCode = connection.getResponseCode();
        System.out.println("POST response Code: " + responseCode);

        if(responseCode == HttpURLConnection.HTTP_OK){
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while((inputLine = in.readLine()) != null){
                response.append(inputLine);
            }
            in.close();
            System.out.println(response);
        }
    }
}
