package moduleEngine;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import jade.util.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Level;

public class ModuleEngine {

    Object linkLibrary;
    HashMap<String, Class<?>> classesToLoad = new HashMap<>();

    // Constructor
    public ModuleEngine(String libType, File configXMLFile, File marketplaceXML){
        parseMarketplaceXML(marketplaceXML);
        createObject(libType, configXMLFile);
    }

    public void parseMarketplaceXML(File marketplaceXMLFile) {
        try{

            File file = new File("c:\\other_classes\\");

            URL url = file.toURI().toURL();
            URL[] urls = new URL[]{url};

            ClassLoader cl = new URLClassLoader(urls);


            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document marketplaceXML = builder.parse(marketplaceXMLFile);
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

                currentClass = cl.loadClass(className);
                classesToLoad.put(name, currentClass);
            }
        } catch(Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void createObject(String libType, File xmlConfigFile){
        try{
            // Find class in hashmap
            Class<?> classToLoad = classesToLoad.get(libType);

           // Create object
            linkLibrary = classToLoad.getConstructor(File.class).newInstance(xmlConfigFile);
        }catch(Exception ex){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String executeSkill(Object skill) {
        Class<?> objectClass = linkLibrary.getClass();

        try {
            Method method = objectClass.getMethod("ExecuteSkill", skill.getClass());

            return (String) method.invoke(linkLibrary, skill);
        } catch (Exception ex){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            return "error";
        }
    }

    public void shutdown(){
        Class<?> objectClass = linkLibrary.getClass();

        try {
            Method method = objectClass.getMethod("Stop");

            method.invoke(linkLibrary);
        } catch (Exception ex){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
}
