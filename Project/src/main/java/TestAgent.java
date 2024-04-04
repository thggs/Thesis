import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import moduleEngine.ModuleEngine;

import java.io.File;

public class TestAgent extends Agent {

    File xmlConfigFile;
    String libType;
    @Override
    protected void setup() {
        System.out.println("Agent " +  getLocalName() + " started");
        Object[] params = this.getArguments();
        if(params.length == 2) {
            libType = (String) params[0];
            xmlConfigFile = new File((String) params[1]);
        }

        this.addBehaviour(new behaviour(this, 2000));
    }

    class behaviour extends TickerBehaviour {

        ModuleEngine moduleEngine;

        public behaviour(Agent a, long period) {
            super(a, period);
            moduleEngine = new ModuleEngine(libType, xmlConfigFile);
        }

        @Override
        protected void onTick() {
            System.out.println("New "+ getLocalName() +" Tick");
            moduleEngine.executeSkill("Skill_A");
        }
    }
}
