package modularLibraries;

import org.eclipse.paho.client.mqttv3.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.UUID;

public class ModularLibrary {

    public ModularLibrary(File xmlFile){

    }

    public String ExecuteSkill(String skill){
        System.out.println("executeSkill method no implemented");
        return null;
    }

    public void Stop(){
        System.out.println("Stop method not implemented");
    }
}
