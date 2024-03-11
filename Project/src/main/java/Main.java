import java.lang.reflect.InvocationTargetException;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> classToLoad = Class.forName("Test");
        Object obj = classToLoad.getConstructor().newInstance();
        classToLoad.getMethod("printStuff").invoke(obj);
    }

}
