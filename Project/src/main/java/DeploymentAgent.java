import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.util.Logger;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import utilities.Constants;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Level;

public class DeploymentAgent extends Agent {

    JPanel rootPanel;
    JButton stopAgentButton;
    JButton startAgentButton;
    JFormattedTextField formattedTextField1;
    JButton openButton;
    JTextField agentNameTextField;
    JList<String> runningAgentsList;
    JList<String> availableLibrariesList;
    JRadioButton resourceAgentRadioButton;
    JRadioButton transportAgentRadioButton;

    String selectedAgentType = "agents.ResourceAgent";
    String marketplaceXMLPath = "C:\\Users\\ddrod\\Documents\\GitHub\\Thesis\\Project\\marketplace.xml";
    File xmlMarktetplace;
    ContainerController agentContainer;

    String xmlConfigPath;

    public DeploymentAgent(){

        xmlMarktetplace = new File(marketplaceXMLPath);

        availableLibrariesList.setListData(getMarketplaceLibraries());

        openButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File("C:\\Users\\ddrod\\Documents\\GitHub\\Thesis\\Project\\configFiles"));
            int result = fileChooser.showOpenDialog(rootPanel);

            if(result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                xmlConfigPath = selectedFile.getPath();
                formattedTextField1.setText(xmlConfigPath);
            }
        });

        resourceAgentRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedAgentType = "agents.ResourceAgent";
            }
        });

        transportAgentRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedAgentType = "agents.TransportAgent";
            }
        });

        startAgentButton.addActionListener(e ->  {
            try{
                // Get agentName from textField
                String agentName = agentNameTextField.getText();

                // Create List with already created agent names
                DefaultListModel<String> list = new DefaultListModel<>();
                for(int i = 0; i < runningAgentsList.getModel().getSize(); i++){
                    list.addElement(runningAgentsList.getModel().getElementAt(i));
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
                        list.addElement(agentName);
                        runningAgentsList.setModel(list);
                        AgentController ag = agentContainer.createNewAgent(agentName, selectedAgentType, new Object[]{Constants.getStationTransportSkills(agentName), Constants.getStationLocation(agentName), availableLibrariesList.getSelectedValue(), xmlConfigPath, marketplaceXMLPath});
                        ag.start();
                }
            } catch(StaleProxyException ex){
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
        });

        stopAgentButton.addActionListener(e -> {
            try {
                if(!runningAgentsList.isSelectionEmpty()){
                    String agentName = (String) runningAgentsList.getSelectedValue();
                    DefaultListModel<String> list = new DefaultListModel<>();
                    for(int i = 0; i < runningAgentsList.getModel().getSize(); i++){
                        list.addElement(runningAgentsList.getModel().getElementAt(i));
                    }
                    System.out.println("Stopping agent: " + agentName);
                    list.removeElement(agentName);
                    runningAgentsList.setModel(list);
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
        } catch(Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    protected void setup() {
        System.out.println("DeploymentAgent has been launched");

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
