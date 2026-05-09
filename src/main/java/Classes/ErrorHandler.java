package Classes;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Date;

public class ErrorHandler {

    private static final List<String> errorLog = new ArrayList<>();
    private static boolean isRedirectingToError = false;

    // Intercept all fatal crashes globally
    static {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Exception e = (throwable instanceof Exception) ? (Exception) throwable : new Exception(throwable);
            logError("FATAL CRASH on thread " + thread.getName() + ": " + e.getMessage());
            logError(e);
            exportCrashLog();
            
            if (!isRedirectingToError) {
                isRedirectingToError = true;
                try {
                    javafx.application.Platform.runLater(() -> {
                        gui.GUIError.show();
                        isRedirectingToError = false;
                    });
                } catch (Exception ex) {
                    System.err.println("Could not redirect to GUIError: " + ex.getMessage());
                    isRedirectingToError = false;
                }
            }
        });
    }

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

    // Export crash log to the GlobalManagerSaves directory
    private static void exportCrashLog() {
        try {
            File dir = new File(io.SaveManager.getSaveDirectory());
            if (!dir.exists()) dir.mkdirs();
            File logFile = new File(dir, "crash_log.txt");
            
            String timeStamp = "\n\n--- CRASH LOG: " + new Date().toString() + " ---\n";
            Files.writeString(logFile.toPath(), timeStamp, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            Files.writeString(logFile.toPath(), getErrorsAsString(), StandardOpenOption.APPEND);
        } catch (Exception ex) {
            System.err.println("Failed to write crash log to disk: " + ex.getMessage());
        }
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
