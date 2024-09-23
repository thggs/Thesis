package agents;

import jade.core.Agent;
import moduleEngine.ModuleEngine;

import java.io.*;

public class DummyAgentDeployed extends Agent {

    ModuleEngine moduleEngine;

    File xmlMarketplaceFile;
    File xmlConfigFile;
    String libType;

    boolean startModuleEngine;
    File statsFile;

    @Override
    protected void setup() {

        try {

            Object[] params = this.getArguments();
            FileWriter writer = new FileWriter((String)params[4], true);

            startModuleEngine = (boolean) params[3];

            long start = System.nanoTime();
            long end;

            if(startModuleEngine) {
                libType = (String) params[0];
                xmlConfigFile = new File((String) params[1]);
                xmlMarketplaceFile = new File((String) params[2]);
                moduleEngine = new ModuleEngine(libType, xmlConfigFile, xmlMarketplaceFile);
                end = System.nanoTime();
                System.out.println("With Module Engine time: " + (end - start) + "ns");
            }else{
                end = System.nanoTime();
                System.out.println("Without Module Engine time: " + (end - start) + "ns");
            }

            writer.write((end - start) + "\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
