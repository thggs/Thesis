public class Test {
    static {
        System.loadLibrary("native");
    }

    public static void main(String[] args) {
        new Test().sayHello();
    }

    // Declare a native method sayHello() that receives no arguments and returns void
    private native void sayHello();

}
