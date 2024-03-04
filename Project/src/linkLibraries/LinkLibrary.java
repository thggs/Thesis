package linkLibraries;

import moduleEngine.ModuleEngine;

public class LinkLibrary {

    ModuleEngine moduleEngine;

    public LinkLibrary(){
        this.moduleEngine = new ModuleEngine();
    }

    public int executeSkill(String skill){
        return moduleEngine.executeSkill(skill);
    }

}
