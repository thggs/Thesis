import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.util.Logger;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import utilities.Constants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.logging.Level;

public class ProductManagerAgent extends Agent {
    JPanel rootPanel;
    JPanel buttonPanel;
    private JTable table1;
    private JScrollPane tablePanel;
    ArrayList<String> productTypesList;
    ContainerController agentContainer;
    DefaultTableModel model;
    ArrayList<JButton> productButtonsList = new ArrayList<>();
    ArrayList<AgentController> products = new ArrayList<>();
    int productID = 0;

    String[] columnNames = {"ID", "Type"};

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
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        model = (DefaultTableModel) table1.getModel();

        model.addColumn("ID");
        model.addColumn("Type");
        model.addColumn("Skills");

        for(String product : productTypesList){
            JButton productButton = new JButton(product);
            productButton.setHorizontalTextPosition(SwingConstants.CENTER);
            productButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try{
                        JButton button = (JButton) e.getSource();
                        Object[] params = new Object[]{Constants.getProdSkills(button.getText())};
                        AgentController agentController = agentContainer.createNewAgent(button.getText() + "_" + productID, "agents.ProductAgent", params);
                        agentController.start();
                        model.addRow(new Object[]{productID, button.getText(), Constants.getProdSkills(button.getText()).toString()});
                        productID++;
                        products.add(agentController);
                    } catch (StaleProxyException ex) {
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            buttonPanel.add(productButton);
            productButtonsList.add(productButton);
        }

        frame.pack();
        frame.setVisible(true);
    }

    public static void UpdateTableRow(){

    }
}
