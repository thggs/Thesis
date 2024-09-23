package utilities;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class DFInteraction {

    public static void RegisterInDF(Agent agent, String skill, String type) throws FIPAException{
        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        dfAgentDescription.setName(agent.getAID());
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(type);
        serviceDescription.setName(skill);
        dfAgentDescription.addServices(serviceDescription);
        DFService.register(agent, dfAgentDescription);
    }

    public static void RegisterInDF(Agent myAgent, String[] skills, String type) throws FIPAException{
        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        dfAgentDescription.setName(myAgent.getAID());
        for(String s : skills){
            ServiceDescription serviceDescription = new ServiceDescription();
            serviceDescription.setType(type);
            serviceDescription.setName(s);
            dfAgentDescription.addServices(serviceDescription);
        }
        DFService.register(myAgent, dfAgentDescription);
    }

    public static void RemoveFromDF(Agent agent, String skill, String type) throws FIPAException{
        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        dfAgentDescription.setName(agent.getAID());
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(type);
        serviceDescription.setName(skill);
        dfAgentDescription.addServices(serviceDescription);
        DFService.deregister(agent, dfAgentDescription);
    }

    public static DFAgentDescription[] SearchInDFBySkill(String skill, Agent agent) throws FIPAException{
        DFAgentDescription dfAgentDescription = new DFAgentDescription();
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setName(skill);
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
