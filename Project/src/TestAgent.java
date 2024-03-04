import jade.core.Agent;
import linkLibraries.LinkLibrary;

public class TestAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println("Hello World!");
        System.out.println("My name is " +  getLocalName());

        LinkLibrary link_library = new LinkLibrary();

        int result = link_library.executeSkill("Skill_B");

        System.out.println("Skill result: " + result);

    }
}
