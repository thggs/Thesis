package agents;

import jade.core.Agent;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetResponder;
import jade.util.Logger;
import jade.lang.acl.MessageTemplate;
import moduleEngine.ModuleEngine;
import utilities.DFInteraction;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

public class ResourceAgent extends Agent {

    File xmlConfigFile;
    File xmlMarketplaceFile;
    String libType;
    String location;
    ModuleEngine moduleEngine;
    ArrayList<String> associatedSkills = new ArrayList<>();

    @Override
    protected void setup(){
        System.out.println(getLocalName() + ": Agent started as " + getClass());
        Object[] params = this.getArguments();

        associatedSkills = (ArrayList<String>) params[0];
        location = (String) params[1];

        libType = (String) params[2];
        xmlConfigFile = new File((String) params[3]);
        xmlMarketplaceFile = new File((String) params[4]);


        moduleEngine = new ModuleEngine(libType, xmlConfigFile, xmlMarketplaceFile);

        // Register in DF
        try{
            DFInteraction.RegisterInDF(this, associatedSkills.toArray(String[]::new),this.getClass().getName());
        }catch(FIPAException ex){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }

        this.addBehaviour(new requestResponder(this, MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
        this.addBehaviour(new contractNetResponder(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)));
    }

    @Override
    protected void takeDown(){
        try{
            DFInteraction.RemoveFromDF(this, Arrays.toString(associatedSkills.toArray()), this.getClass().getName());
        }catch(FIPAException ex){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        };
        moduleEngine.shutdown();
        super.takeDown();
    }

    private class requestResponder extends AchieveREResponder{
        public requestResponder(Agent a, MessageTemplate messageTemplate){
            super(a, messageTemplate);
        }

        @Override
        protected ACLMessage handleRequest(ACLMessage request){
            System.out.println(myAgent.getLocalName() + ": Processing REQUEST message");
            ACLMessage msg = request.createReply();
            msg.setPerformative(ACLMessage.AGREE);
            return msg;
        }

        @Override
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response){
            System.out.println(myAgent.getLocalName() + ": Preparing result of REQUEST");
            String result = moduleEngine.executeSkill(request.getContent());
            while(!result.equals("done")){
                result = moduleEngine.executeSkill(request.getContent());
            }
            ACLMessage msg = request.createReply();
            msg.setPerformative(ACLMessage.INFORM);
            System.out.println(myAgent.getLocalName() + ": Skill " + request.getContent() + " executed");
            return msg;
        }
    }

    private class contractNetResponder extends ContractNetResponder{

        public contractNetResponder(Agent a, MessageTemplate messageTemplate){
            super(a, messageTemplate);
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp){
            System.out.println(myAgent.getLocalName() + ": Processing CFP message");
            ACLMessage msg = cfp.createReply();
            msg.setPerformative(ACLMessage.PROPOSE);
            return msg;
        }

        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept){
            System.out.println(myAgent.getLocalName() + ": Preparing result of CFP");
            ACLMessage msg = cfp.createReply();
            msg.setPerformative(ACLMessage.INFORM);
            msg.setContent(location);
            return msg;
        }

    }
}
