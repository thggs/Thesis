package agents;

import jade.core.Agent;
import moduleEngine.ModuleEngine;

import java.io.*;
import java.util.*;

public class DummyAgentDelay extends Agent {

    ModuleEngine moduleEngine;
    File xmlMarketplaceFile = new File("C:\\Users\\ddrod\\Documents\\GitHub\\Thesis\\Project\\marketplace.xml");
    File xmlConfigFile = new File("C:\\Users\\ddrod\\Documents\\GitHub\\Thesis\\Project\\configFiles\\configDelayTest.xml");

    @Override
    protected void setup() {
        int iter = 60;
        long[] timesME = new long[iter];
        long[] timesNormal = new long[iter];


        for(int i = 0; i < iter; i++){

            String libtype = "DelayTest";
            moduleEngine = new ModuleEngine(libtype, xmlConfigFile, xmlMarketplaceFile);

            long startTimeME = System.nanoTime();
            System.out.print(moduleEngine.executeSkill("Test"));
            long endTimeME = System.nanoTime();
            timesME[i] = (endTimeME - startTimeME);

            long startTimeNormal = System.nanoTime();
            System.out.println("Skill Received: Test");
            long endTimeNormal = System.nanoTime();
            timesNormal[i] = (endTimeNormal - startTimeNormal);

            System.out.println("Iteration: " + (i+1));
        }

        Arrays.sort(timesME);
        Arrays.sort(timesNormal);


        System.out.println("Min: " + timesME[0] + " Max: " + timesME[timesME.length-1]);
        System.out.println("Min: " + timesNormal[0] + " Max: " + timesNormal[timesNormal.length-1]);

        long sumME = 0;
        long sumNormal = 0;

        for(int i = 5; i < 55; i++){
            sumME += timesME[i];
            sumNormal += timesNormal[i];
        }

        sumME /= 50;
        sumNormal /= 50;

        System.out.println("Average Module Engine: " + sumME + "ns");
        System.out.println("Average No Module Engine: " + sumNormal + "ns");
        System.out.println("Average Module Engine: " + (sumME/1000000) + "ms");
        System.out.println("Average No Module Engine: " + (sumNormal/1000000) + "ms");
        System.out.println("Difference of the averages: " + (sumME - sumNormal) + "ns");
    }
}
