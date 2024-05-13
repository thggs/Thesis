package utilities;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class DFInteraction {

    public static void RegisterInDF(Agent agent, String name, String type) throws FIPAException{
        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        dfAgentDescription.setName(agent.getAID());
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(type);
        serviceDescription.setName(name);
        dfAgentDescription.addServices(serviceDescription);
        DFService.register(agent, dfAgentDescription);
    }

    public static void RegisterInDF(Agent myAgent, String[] name, String type) throws FIPAException{
        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        dfAgentDescription.setName(myAgent.getAID());
        for(String n : name){
            ServiceDescription serviceDescription = new ServiceDescription();
            serviceDescription.setType(type);
            serviceDescription.setName(n);
            dfAgentDescription.addServices(serviceDescription);
        }
        DFService.register(myAgent, dfAgentDescription);
    }

    public static void RemoveFromDF(Agent agent, String name, String type) throws FIPAException{
        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        dfAgentDescription.setName(agent.getAID());
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(type);
        serviceDescription.setName(name);
        dfAgentDescription.addServices(serviceDescription);
        DFService.deregister(agent, dfAgentDescription);
    }

    public static DFAgentDescription[] SearchInDFByName(String name, Agent agent) throws FIPAException{
        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setName(name);
        dfAgentDescription.addServices(serviceDescription);
        return DFService.search(agent, dfAgentDescription);
    }

    public static DFAgentDescription[] SearchInDFByType(String type, Agent agent) throws FIPAException{
        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(type);
        dfAgentDescription.addServices(serviceDescription);
        return DFService.search(agent, dfAgentDescription);
    }
}
