import agents.ProductAgent;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.util.Logger;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import utilities.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.logging.Level;

public class ProductManagerAgent extends Agent {
    JPanel rootPanel;
    ArrayList<String> productTypesList;
    ContainerController agentContainer;
    ArrayList<JButton> productButtonsList = new ArrayList<>();
    //ArrayList<String> products = new ArrayList<>();
    int productID = 0;

    public ProductManagerAgent(){

    }

    @Override
    protected void setup() {
        System.out.println("AgentManager has been launched");

        productTypesList = Constants.PRODUCT_TYPES;

        jade.core.Runtime runtime = jade.core.Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.CONTAINER_NAME, "ProductAgentsContainer");
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        agentContainer = runtime.createAgentContainer(profile);

        JFrame frame = new JFrame("Product Manager Agent");
        frame.setContentPane(rootPanel);
        frame.setLayout(new FlowLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        for(String product : productTypesList){
            JButton productButton = new JButton(product);
            productButton.setHorizontalTextPosition(SwingConstants.CENTER);
            productButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try{
                        JButton button = (JButton) e.getSource();
                        Object[] params = new Object[]{Constants.getProdSkills(button.getText())};
                        AgentController agentController = agentContainer.createNewAgent("Product_" + productID, "agents.ProductAgent", params);
                        agentController.start();
                        productID++;
                        //products.add("Product_" + productID);
                    } catch (StaleProxyException ex) {
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            productButtonsList.add(productButton);
            rootPanel.add(productButton, BorderLayout.CENTER);
        }

        frame.pack();
        frame.setVisible(true);
    }
}
