package modularLibraries;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class ModularLibraryC {

    static String libraryPath;
    File xmlFile;

    // Constructor
    public ModularLibraryC(File xmlFile){

        Document xml = null;
        this.xmlFile = xmlFile;

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            xml = builder.parse(xmlFile);
            xml.getDocumentElement().normalize();
        } catch (Exception e){
            e.printStackTrace();
        }

        assert xml != null;
        Element configElement = xml.getDocumentElement();
        NodeList dllPathNode = configElement.getElementsByTagName("dll_path");

        libraryPath = dllPathNode.item(0).getTextContent();
    }

//    static{
//        System.load(libraryPath);
//    }

    public void executeSkill(String skill){
        new ModularLibraryC(xmlFile).helloWorld();
    }

    private native void helloWorld();
}
