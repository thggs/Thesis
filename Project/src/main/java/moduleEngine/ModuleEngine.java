package moduleEngine;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.*;

public class ModuleEngine {

    Object modularLibrary;
    String marketplaceXMLPath    = "C:\\Users\\ddrod\\Documents\\GitHub\\Thesis\\Project\\marketplace.xml";
    HashMap<String, Class<?>> classesToLoad = new HashMap<>();

    // Constructor
    public ModuleEngine(String libType, File configXMLFile){
        parseMarketplaceXML();
        createObject(libType, configXMLFile);
    }

    public void parseMarketplaceXML() {
        try{
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document marketplaceXML = builder.parse(new File(marketplaceXMLPath));
            marketplaceXML.getDocumentElement().normalize();

            // Get first node, in this case "marketplace"
            Element marketplaceElement = marketplaceXML.getDocumentElement();

            // Get first child node of first node, in this case "class"
            NodeList classNodeList = marketplaceElement.getElementsByTagName("class");

            for(int i = 0; i < classNodeList.getLength(); i++){
                Element classElement = (Element) classNodeList.item(i);

                // Get attributes of "class"
                String className = classElement.getAttribute("className");
                String name = classElement.getAttribute("name");

                // Add current class to class list
                Class<?> currentClass;

                currentClass = Class.forName(className);
                classesToLoad.put(name ,currentClass);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void createObject(String libType, File xmlConfigFile){
        try{
            // Find class in hashmap
            Class<?> classToLoad = classesToLoad.get(libType);

           // Create object
            modularLibrary = classToLoad.getConstructor(File.class).newInstance(xmlConfigFile);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void executeSkill(Object skill) {
        try {
            Class<?> objectClass = modularLibrary.getClass();

            objectClass.getMethod("executeSkill", skill.getClass()).invoke(modularLibrary, skill);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
