package moduleEngine;

import modularLibraries.ModularLibraryC;
import modularLibraries.ModularLibraryHTTP;
import modularLibraries.ModularLibraryMQTT;


import javax.naming.Binding;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.objectweb.asm.tree.analysis.Interpreter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ModuleEngine {

    Document xml;
    Class<?> classToLoad;
    Object obj;
    List<String> methodnames = new ArrayList<>();

    // Constructor
    public ModuleEngine(String xmlPath){
        parseXML(xmlPath);
//        this.modularLibraryC = new ModularLibraryC();
//        this.modularLibraryHTTP = new ModularLibraryHTTP();
//        this.modularLibraryMQTT = new ModularLibraryMQTT("tcp://broker.hivemq.com:1883");
    }

    public void parseXML(String xmlPath) {
        try{
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            xml = builder.parse(new File(xmlPath));
            xml.getDocumentElement().normalize();

            getClassCharacteristics(xml);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void getClassCharacteristics(Document xml){
        // Get first node, in this case "config"
        Element configElement = xml.getDocumentElement();

        // Get first child node of first node, in this case "class"
        NodeList classList = configElement.getElementsByTagName("class");
        Element classElement = (Element) classList.item(0);

        // Get attributes of "class"
        String packageName = classElement.getAttribute("package");
        String className = classElement.getAttribute("name");

        // Get "constructor" of "class"
        NodeList constructorList = classElement.getElementsByTagName("constructor");
        Element constructorElement = (Element) constructorList.item(0);

        // Get all "argument_type" of "constructor"
        NodeList param_typeList = constructorElement.getElementsByTagName("param_type");
        List<Class<?>> param_type = new ArrayList<>();

        for(int i = 0; i < param_typeList.getLength(); i++){
            Element argument_typeElement = (Element) param_typeList.item(i);
            String argType = argument_typeElement.getTextContent();
            switch (argType){
                case "Byte":
                    param_type.add(Byte.TYPE);
                    break;
                case "Short":
                    param_type.add(Short.TYPE);
                    break;
                case "Int":
                    param_type.add(Integer.TYPE);
                    break;
                case "Long":
                    param_type.add(Long.TYPE);
                    break;
                case "Float":
                    param_type.add(Float.TYPE);
                    break;
                case "Double":
                    param_type.add(Double.TYPE);
                    break;
                case "Boolean":
                    param_type.add(Boolean.TYPE);
                    break;
                case "Char":
                    param_type.add(Character.TYPE);
                    break;
                default:
                    try{
                        Class<?> cls = Class.forName(argType);
                        param_type.add(cls);
                    }
                    catch(ClassNotFoundException ex) {
                        ex.printStackTrace();
                    }
                    break;
            }
        }

        Class<?>[] argument_typeArray = (Class<?>[]) param_type.toArray();

        // Create object in "packageName", of class "className" with constructor parameter types as "argument_typeArray"
        createObject(packageName, className, argument_typeArray);

        // Get all "methods" of "class"
        NodeList methodList = classElement.getElementsByTagName("method");
        for(int i = 0; i < methodList.getLength(); i++){
            Element methodElement = (Element) methodList.item(i);
            methodnames.add(methodElement.getAttribute("name"));

            // TODO: Implement an hashMap with key as name and value as an array of all types of input parameters
        }

    }

    public void createObject(String packageName, String className, Class<?>[] constructor_param_types){
        try{
            classToLoad = Class.forName(packageName + className);
            // TODO: Make this generic for all parameter values and quantities
            obj = classToLoad.getConstructor(constructor_param_types).newInstance("tcp://broker.hivemq.com:1883");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // TODO: Update this
    public void executeMethod() {

        Class<?>[] param_types = new Class<?>[3];

        try {
            param_types[0] = String.class;
            param_types[1] = Integer.TYPE;
            param_types[2] = String.class;

            this.classToLoad.getMethod("executeLibrary", param_types).invoke(this.obj, "message", 2, "topic");
        } catch (Exception e){
            e.printStackTrace();
        }


//        switch (protocol){
//            case "C":
//                modularLibraryC.executeLibrary();
//                break;
//            case "HTTP":
//                modularLibraryHTTP.executeLibrary();
//                break;
//            case "MQTT":
//                modularLibraryMQTT.executeLibrary("Hello", 2, "random_topic");
//                break;
//        }
    }
}
