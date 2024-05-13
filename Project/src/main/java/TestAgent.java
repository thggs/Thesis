import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAException;
import jade.util.Logger;
import moduleEngine.ModuleEngine;
import utilities.DFInteraction;

import java.io.File;
import java.util.logging.Level;

public class TestAgent extends Agent {

    String skill = "Skill_A";
    File xmlConfigFile;
    File xmlMarktetplace;
    String libType;
    ModuleEngine moduleEngine;
    @Override
    protected void setup() {
        System.out.println("Agent " +  getLocalName() + " started");
        Object[] params = this.getArguments();
        libType = (String) params[0];
        xmlConfigFile = new File((String) params[2]);
        xmlMarktetplace = new File((String) params[3]);

        moduleEngine = new ModuleEngine(libType, xmlConfigFile, xmlMarktetplace);

        // Add Agent to DF
        try{
            DFInteraction.RegisterInDF(this, skill, "TestAgent");
        }catch(FIPAException ex){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }

        this.addBehaviour(new behaviour(this, 10, moduleEngine));
    }

    @Override
    public void takeDown(){
        try{
            DFInteraction.RemoveFromDF(this, skill, "TestAgent");
        }catch(FIPAException ex){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        super.takeDown();
    }

    class behaviour extends TickerBehaviour {

        ModuleEngine moduleEngine;

        public behaviour(Agent a, int period, ModuleEngine moduleEngine) {
            super(a, period);
            this.moduleEngine = moduleEngine;
        }
        @Override
        public void onTick() {
            System.out.println("New "+ getLocalName() +" Tick");
            String result = moduleEngine.executeSkill(skill);
            System.out.println(result);
        }
    }
}
