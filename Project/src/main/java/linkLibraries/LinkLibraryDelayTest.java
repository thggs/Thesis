package linkLibraries;

import java.io.File;

public class LinkLibraryDelayTest extends LinkLibrary{

    public LinkLibraryDelayTest(File xmlFile){

    }

    @Override
    public String ExecuteSkill(String skill) {
        return "Skill received: ".concat(skill);
    }

    @Override
    public void Stop() {
        System.out.println("Stopping...");
    }
}
