package org.mcnative.loader;

public class Test {


    public static String TEST = "resources\n" +
            "key:value\n" +
            "--:variables\n" +
            "key:value";

    public static void main(String[] args) {
        String[] split = TEST.split("--:");
        System.out.println();
    }
}
