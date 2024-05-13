package modularLibraries;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ModularLibraryHTTP {

    URL url;

    public ModularLibraryHTTP(File xmlFile){
        Document xml;

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            xml = builder.parse(xmlFile);
            xml.getDocumentElement().normalize();

            Element configElement = xml.getDocumentElement();
            NodeList addressNode = configElement.getElementsByTagName("address");

            url = new URI(addressNode.item(0).getTextContent()).toURL();

            //establishConnection(url);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public String ExecuteSkill(String skill) throws IOException {
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

        if(responseCode == HttpURLConnection.HTTP_OK){
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while((inputLine = in.readLine()) != null){
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        }
        return "error";
    }

    public void Stop(){

    }
}