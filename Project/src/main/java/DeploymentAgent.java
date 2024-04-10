import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DeploymentAgent extends Agent {

    public JPanel rootPanel;
    private JButton stopAgentButton;
    private JButton startAgentButton;
    private JFormattedTextField formattedTextField1;
    private JButton openButton;
    private JTextField agentNameTextField;
    private JList runningAgentsList;
    private JList availableLibrariesList;


    String marketplaceXMLPath = "C:\\Users\\David\\Documents\\FCT\\Thesis\\Thesis\\Project\\marketplace.xml";
    File xmlMarktetplace;
    ContainerController agentContainer;

    String xmlConfigPath;

    public DeploymentAgent(){

        xmlMarktetplace = new File(marketplaceXMLPath);

        availableLibrariesList.setListData(getMarketplaceLibraries());

        openButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File("C:\\Users\\David\\Documents\\FCT\\Thesis\\Thesis\\Project"));
            int result = fileChooser.showOpenDialog(rootPanel);

            if(result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                xmlConfigPath = selectedFile.getPath();
                formattedTextField1.setText(xmlConfigPath);
            }
        });

        startAgentButton.addActionListener(e ->  {
            try{
                // Get agentName from textField
                String agentName = agentNameTextField.getText();

                // Create List with already created agent names
                List<String> list = new ArrayList<>();
                for(int i = 0; i < runningAgentsList.getModel().getSize(); i++){
                    list.add(runningAgentsList.getModel().getElementAt(i).toString());
                }


                if(xmlConfigPath == null){ // XML config not selected
                    JOptionPane.showMessageDialog(rootPanel, "No config file selected!", "Error", JOptionPane.WARNING_MESSAGE);
                } else if(list.contains(agentName)) { // Duplicate agent name
                    JOptionPane.showMessageDialog(rootPanel, "Agent name already in use!", "Error", JOptionPane.WARNING_MESSAGE);
                } else if(agentName.isEmpty()) { // Missing agent name
                    JOptionPane.showMessageDialog(rootPanel, "No agent name given!", "Error", JOptionPane.WARNING_MESSAGE);
                } else if(availableLibrariesList.isSelectionEmpty()) { // Modular Library not selected
                    JOptionPane.showMessageDialog(rootPanel, "No library selected!", "Error", JOptionPane.WARNING_MESSAGE);
                }
                else {
                        list.add(agentName);
                        runningAgentsList.setListData(list.toArray());
                        AgentController ag = agentContainer.createNewAgent(agentName, "TestAgent", new Object[]{availableLibrariesList.getSelectedValue(), xmlConfigPath, marketplaceXMLPath});
                        ag.start();
                }
            } catch(StaleProxyException ex){
                ex.printStackTrace();
            }
        });

        stopAgentButton.addActionListener(e -> {
            try {
                if(!runningAgentsList.isSelectionEmpty()){
                    String agentName = (String) runningAgentsList.getSelectedValue();
                    List<String> list = new ArrayList<>();
                    for(int i = 0; i < runningAgentsList.getModel().getSize(); i++){
                        list.add(runningAgentsList.getModel().getElementAt(i).toString());
                    }
                    System.out.println("Stopping agent: " + agentName);
                    list.remove(agentName);
                    runningAgentsList.setListData(list.toArray());
                    agentContainer.getAgent(agentName).kill();
                }
            } catch (ControllerException ex) {
                throw new RuntimeException(ex);
            }

        });
    }

    public String[] getMarketplaceLibraries() {
        try{
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document marketplaceXML = builder.parse(xmlMarktetplace);
            marketplaceXML.getDocumentElement().normalize();

            // Get first node, in this case "marketplace"
            Element marketplaceElement = marketplaceXML.getDocumentElement();

            // Get first child node of first node, in this case "class"
            NodeList classNodeList = marketplaceElement.getElementsByTagName("class");

            String[] libList = new String[classNodeList.getLength()];

            for(int i = 0; i < classNodeList.getLength(); i++){
                Element classElement = (Element) classNodeList.item(i);

                // Get attributes of "class"
                libList[i]  = classElement.getAttribute("name");
            }
            return libList;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void setup() {
        System.out.println("AgentManager has been launched");

        jade.core.Runtime runtime = jade.core.Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.CONTAINER_NAME, "DeployedAgentsContainer");
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        agentContainer = runtime.createAgentContainer(profile);

        JFrame frame = new JFrame("Deployment Agent");
        frame.setContentPane(rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
