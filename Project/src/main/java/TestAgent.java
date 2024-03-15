import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import moduleEngine.ModuleEngine;

public class TestAgent extends Agent {

    String xmlPath = "C:\\Users\\ddrod\\Documents\\GitHub\\Thesis\\Project\\mqttConfig.xml";
    String xmlPath2 = "C:\\Users\\ddrod\\Documents\\GitHub\\Thesis\\Project\\config2.xml";

    @Override
    protected void setup() {
        System.out.println("Hello World!");
        System.out.println("My name is " +  getLocalName());

        this.addBehaviour(new behaviour(this, 2000));
    }

    class behaviour extends TickerBehaviour {

        ModuleEngine moduleEngine;

        public behaviour(Agent a, long period) {
            super(a, period);
            moduleEngine = new ModuleEngine("MQTT", xmlPath);
        }

        @Override
        protected void onTick() {
            System.out.println("New Tick");
            moduleEngine.executeSkill("Skill_A");
        }
    }
}
