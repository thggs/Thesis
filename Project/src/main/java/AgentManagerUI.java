import javax.swing.*;
import java.io.File;

public class AgentManagerUI {

    public JPanel rootPanel;
    private JButton stopAgentButton;
    private JButton startAgentButton;
    private JFormattedTextField formattedTextField1;
    private JButton openButton;

    public AgentManagerUI() {
        openButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            int result = fileChooser.showOpenDialog(rootPanel);

            if(result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                formattedTextField1.setText(selectedFile.getPath());
            }
        });
    }
}
