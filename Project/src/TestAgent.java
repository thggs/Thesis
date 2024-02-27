import jade.core.Agent;

public class TestAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println("Hello World!");
        System.out.println("My name is " +  getLocalName());
    }

}
