package testJNI;

public class Test {
    static {
        System.load("C:\\Users\\ddrod\\Documents\\GitHub\\Thesis\\Project\\src\\modularLibraries\\native.dll");
    }

    public static void main(String[] args) {
        new Test().sayHello();
    }

    // Declare a native method sayHello() that receives no arguments and returns void
    private native void sayHello();

}
