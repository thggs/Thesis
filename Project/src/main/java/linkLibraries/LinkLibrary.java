package linkLibraries;

import moduleEngine.ModuleEngine;

public class LinkLibrary {

    ModuleEngine moduleEngine;

    public LinkLibrary(String xmlPath){
        this.moduleEngine = new ModuleEngine(xmlPath);
    }

    public void executeSkill(){
        moduleEngine.executeMethod();
    }
}
