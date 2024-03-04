package moduleEngine;

import modularLibraries.ModularLibrary;

public class ModuleEngine {

    ModularLibrary modularLibrary;

    // Constructor
    public ModuleEngine(){
        this.modularLibrary = new ModularLibrary();
    }

    public int executeSkill(String skill){
        return modularLibrary.executeSkill(skill);
    }
}
