package modularLibraries;

public class ModularLibrary {

    // Constructor
    public ModularLibrary(){
        new ModularLibrary().helloWorld();
    }

    static{
        System.loadLibrary("native.dll");
    }

    public int executeSkill(String skill){
        if(skill.equals("Skill_A")){
            return 1;
        }
        return 0;
    }

    private native void helloWorld();
}
