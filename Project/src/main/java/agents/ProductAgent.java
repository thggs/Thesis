package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.proto.ContractNetInitiator;
import utilities.Constants;
import utilities.DFInteraction;

import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductAgent extends Agent {

    ArrayList<String> executionPlan = new ArrayList<>();
    String location;
    int step;

    @Override
    protected void setup() {
        Object[] args = this.getArguments();
        step = 0;
        executionPlan = (ArrayList<String>) args[0];
        location = Constants.LOCATION_SOURCE;
        System.out.println("Product launched at: " + location + "\nWith exectution plan: " + executionPlan.toString());
        System.out.println("Executing first skill");
        this.addBehaviour(new executeNextSkill(this));
    }

    @Override
    protected void takeDown(){
        super.takeDown();
    }

    private class executeNextSkill extends OneShotBehaviour{

        public executeNextSkill(Agent a){
            super(a);
        }

        // Looks up what agents perform desired skill and creates a message to be sent to those agents
        @Override
        public void action(){
            DFAgentDescription[] result = new DFAgentDescription[0];
            String skill = executionPlan.get(step);
            try{
                while(true) {
                    result = DFInteraction.SearchInDFBySkill(skill, this.getAgent());
                    if(result.length > 0){
                        break;
                    }
                    Thread.onSpinWait();
                }
            }catch(FIPAException ex){
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
            ACLMessage msg = new ACLMessage(ACLMessage.CFP);
            msg.setContent(skill);

            for(DFAgentDescription dfAgentDescription : result){
                System.out.println(dfAgentDescription.getName());
                msg.addReceiver(dfAgentDescription.getName());
            }
            myAgent.addBehaviour(new contractNetInitiator(myAgent, msg, skill));
        }
    }

    private class contractNetInitiator extends ContractNetInitiator{
        String skill;

        public contractNetInitiator(Agent a, ACLMessage msg, String skill){
            super(a, msg);
            this.skill = skill;
        }

        @Override
        protected void handleInform(ACLMessage inform){
            DFAgentDescription[] result = new DFAgentDescription[0];
            System.out.println(myAgent.getLocalName() + ": INFORM message received from " + inform.getSender().getLocalName() + " with contents: " + inform.getContent());
            try{
                while(true) {
                    result = DFInteraction.SearchInDFByType("TransportAgent", this.getAgent());
                    if(result.length > 0){
                        break;
                    }
                    Thread.onSpinWait();
                }
            }catch(FIPAException ex){
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(result[0].getName());
            msg.setContent(location + "#TOKEN#" + inform.getContent());
            location = inform.getContent();
            myAgent.addBehaviour(new requestTransportMove(myAgent, msg, skill));
        }

        @Override
        protected void handleAllResponses(Vector responses, Vector acceptances){
            boolean acceptance = false;

            for (Object aclMessage : responses) {
                if (((ACLMessage)aclMessage).getPerformative() == ACLMessage.PROPOSE) {
                    acceptance = true;
                }
            }
            if(responses.isEmpty() || !acceptance){
                myAgent.addBehaviour(new executeNextSkill(myAgent));
                return;
            }

            ACLMessage acceptMsg = (ACLMessage) responses.get(0);
            System.out.println(myAgent.getLocalName() + ": ALL PROPOSALS received");
            ACLMessage replyAccept = acceptMsg.createReply();
            replyAccept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            acceptances.add(replyAccept);
            System.out.println(myAgent.getLocalName() + " Sends accept_proposal to: " + acceptances);
        }
    }

    private class requestTransportMove extends AchieveREInitiator{

        String skill;

        public requestTransportMove(Agent a, ACLMessage msg, String skill){
            super(a, msg);
            this.skill = skill;
        }

        @Override
        protected void handleAgree(ACLMessage agree){
            System.out.println(myAgent.getLocalName() + " : AGREE message received");
        }

        @Override
        protected void handleInform(ACLMessage inform){
            System.out.println(myAgent.getLocalName() + ": INFORM message received");
            if(skill != null){
                System.out.println("Executing skill: " + skill);
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(new AID(Constants.getLocationStation(location), false));
                msg.setContent(skill);
                myAgent.addBehaviour(new requestStationSkill(myAgent, msg));
            }
            else{
                System.out.println(myAgent.getLocalName() + ": Product is done. Terminating...");
                takeDown();
            }
        }
    }

    private class requestStationSkill extends AchieveREInitiator{

        public requestStationSkill(Agent a, ACLMessage msg){
            super(a, msg);
        }

        @Override
        protected void handleAgree(ACLMessage agree){
            System.out.println(myAgent.getLocalName() + " : AGREE message received");
        }

        @Override
        protected void handleInform(ACLMessage inform){
            DFAgentDescription[] result = new DFAgentDescription[0];
            System.out.println(myAgent.getLocalName() + ": INFORM message received");
            step++;
            // If there are more skills to be executed, restart
            if(executionPlan.size() > step)
            {
                System.out.println("Executing next skill");
                myAgent.addBehaviour(new executeNextSkill(myAgent));
            }
            // If not, then ask for move to storage
            else
            {
                try{
                    result = DFInteraction.SearchInDFByType("TransportAgent", this.getAgent());
                }catch(FIPAException ex){
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                }
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(result[0].getName());
                msg.setContent(location + "#TOKEN#" + Constants.LOCATION_STORAGE);
                location = inform.getContent();
                myAgent.addBehaviour(new requestTransportMove(myAgent, msg, null));
            }
        }
    }
}
