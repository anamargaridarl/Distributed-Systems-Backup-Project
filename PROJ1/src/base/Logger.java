package base;

import java.util.List;

public class Logger {


    public static void error(String message) {
        System.out.println("ERROR: " + message);
    }

    public static void success(String message) {
        System.out.println("SUCCESS: " + message);
    }

    public static void warning(String message) {
        System.out.println("WARNING: " + message);
    }

}
