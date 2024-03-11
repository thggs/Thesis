import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import linkLibraries.LinkLibrary;

public class TestAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println("Hello World!");
        System.out.println("My name is " +  getLocalName());

        this.addBehaviour(new behaviour(this, 2000));
    }

    class behaviour extends TickerBehaviour {

        String xmlPath = "C:\\Users\\ddrod\\Documents\\GitHub\\Thesis\\Project\\config.xml";
        LinkLibrary linkLibrary;

        public behaviour(Agent a, long period) {
            super(a, period);
            linkLibrary = new LinkLibrary(xmlPath);
        }

        @Override
        protected void onTick() {
            linkLibrary.executeSkill();
        }
    }
}
