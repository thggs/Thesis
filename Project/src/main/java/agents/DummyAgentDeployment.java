package agents;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class DummyAgentDeployment extends Agent {

    String xmlMarketplaceFile = "C:\\Users\\ddrod\\Documents\\GitHub\\Thesis\\Project\\marketplace.xml";
    String xmlConfigFile = "C:\\Users\\ddrod\\Documents\\GitHub\\Thesis\\Project\\configFiles\\configDelayTest.xml";

    ContainerController agentContainer;

    int iterations = 100;
    @Override
    protected void setup() {

        System.out.println("DummyAgentDeployment has been launched");

        ArrayList<Long> timesMEList = new ArrayList<>();
        ArrayList<Long> timesNormalList = new ArrayList<>();

        jade.core.Runtime runtime = jade.core.Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.CONTAINER_NAME, "DummyAgentsContainer");
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        agentContainer = runtime.createAgentContainer(profile);

        File me = new File("statsMEFile.txt");
        File normal = new File("statsNormalFile.txt");
        me.delete();
        normal.delete();
        try {
            me.createNewFile();
            normal.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int ID = 0;


        for (int i = 0; i < iterations; i++) {
            try {
                AgentController ag = agentContainer.createNewAgent("DummyAgentDeployed" + ID, "agents.DummyAgentDeployed", new Object[]{"DelayTest", xmlConfigFile, xmlMarketplaceFile, true, "statsMEFile.txt"});
                ag.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ID++;
            try {
                AgentController ag = agentContainer.createNewAgent("DummyAgentDeployed" + ID, "agents.DummyAgentDeployed", new Object[]{"DelayTest", xmlConfigFile, xmlMarketplaceFile, false, "statsNormalFile.txt"});
                ag.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            ID++;
        }

        BufferedReader readerME;
        BufferedReader readerNormal;

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            readerME = new BufferedReader(new FileReader("statsMEFile.txt"));
            readerNormal = new BufferedReader(new FileReader("statsNormalFile.txt"));

            String lineME = readerME.readLine();

            while(lineME != null){
                timesMEList.add(Long.parseLong(lineME));
                lineME = readerME.readLine();
            }

            String lineNormal = readerNormal.readLine();

            while(lineNormal != null){
                timesNormalList.add(Long.parseLong(lineNormal));
                lineNormal = readerNormal.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Long[] timesME = timesMEList.toArray(new Long[timesMEList.size()]);
        Long[] timesNormal = timesNormalList.toArray(new Long[timesNormalList.size()]);

        Arrays.sort(timesME);
        Arrays.sort(timesNormal);

        System.out.println(Arrays.toString(timesME));
        System.out.println(Arrays.toString(timesNormal));

        long sumME = 0;
        long sumNormal = 0;

        for(int i = 0; i < iterations; i++){
            sumME += timesME[i];
            sumNormal += timesNormal[i];
        }

        System.out.println("Total time Module Engine: " + sumME + "ns");
        System.out.println("Total time No Module Engine: " + sumNormal + "ns");
        //System.out.println("Total time Module Engine: " + (sumME/1000000) + "ms");
        //System.out.println("Total time No Module Engine: " + (sumNormal/1000000) + "ms");

        sumME /= iterations;
        sumNormal /= iterations;

        System.out.println("Average Module Engine: " + sumME + "ns");
        System.out.println("Average No Module Engine: " + sumNormal + "ns");
        //System.out.println("Average Module Engine: " + (sumME/1000000) + "ms");
        //System.out.println("Average No Module Engine: " + (sumNormal/1000000) + "ms");
        System.out.println("Difference of the averages: " + (sumME - sumNormal) + "ns");

    }
}
