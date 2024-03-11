package modularLibraries;

public class ModularLibraryC {

    // Constructor
    public ModularLibraryC(){

    }

    static{
        System.load("C:\\Users\\ddrod\\Documents\\GitHub\\Thesis\\Project\\src\\main\\java\\modularLibraries\\native.dll");
    }

    public void executeLibrary(){
        new ModularLibraryC().helloWorld();
    }

    private native void helloWorld();
}
