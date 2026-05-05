package Classes;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {

    private static final List<String> errorLog = new ArrayList<>();

    // Add a simple error message to the log
    public static void logError(String errorMessage) {
        errorLog.add(errorMessage);
        System.err.println("[ERROR] " + errorMessage);
    }

    // Add an exception to the log
    public static void logError(Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        errorLog.add(message);
        System.err.println("[EXCEPTION] " + message);
        e.printStackTrace();
    }

    // Retrieve all stored errors
    public static List<String> getErrors() {
        return new ArrayList<>(errorLog);
    }

    // Check if there are any unresolved errors
    public static boolean hasErrors() {
        return !errorLog.isEmpty();
    }

    // Clear the error log once they have been handled/displayed
    public static void clearErrors() {
        errorLog.clear();
    }
    
    // Get all errors as a single formatted string (useful for displaying in GUIPopup)
    public static String getErrorsAsString() {
        if (errorLog.isEmpty()) {
            return "No errors.";
        }
        return String.join("\n", errorLog);
    }
}
