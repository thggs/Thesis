import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import moduleEngine.ModuleEngine;

import java.io.File;

public class TestAgent extends Agent {

    File xmlConfigFile;
    File xmlMarktetplace;
    String libType;
    @Override
    protected void setup() {
        System.out.println("Agent " +  getLocalName() + " started");
        Object[] params = this.getArguments();
        if(params.length == 3) {
            libType = (String) params[0];
            xmlConfigFile = new File((String) params[1]);
            xmlMarktetplace = new File((String) params[2]);
        }

        this.addBehaviour(new behaviour(this));
    }

    class behaviour extends OneShotBehaviour {

        ModuleEngine moduleEngine;

        public behaviour(Agent a) {
            super(a);
            moduleEngine = new ModuleEngine(xmlMarktetplace, libType, xmlConfigFile);
        }
        @Override
        public void action() {
            System.out.println("New "+ getLocalName() +" Tick");
            String result = moduleEngine.executeSkill("Skill_A");
            System.out.println(result);
        }
    }
}
