public class test {
    public static void main(String[] args) {
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        System.out.println(systemClassLoader);

        ClassLoader extClassLoader = systemClassLoader.getParent();

        ClassLoader classLoader = test.class.getClassLoader();
        System.out.println(classLoader);


        ClassLoader stringLoader = String.class.getClassLoader();
        System.out.println(stringLoader );

    }
}
