package gui;

import Classes.ErrorHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

public class GUIError  {
    
    public static void show(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1b1b2f;");

        // --- Integration with standard Sidebar and TopBar ---
        root.setTop(GUILeftandTopBarHelper.createTopBar(primaryStage, null));
        root.setLeft(GUILeftandTopBarHelper.createSidebar(primaryStage, "Error Log"));

        // Fetch errors and calculate the total count
        List<String> errors = ErrorHandler.getErrors();
        int errorCount = errors.size();

        // --- Main Content Area ---
        VBox content = new VBox(20);
        content.setPadding(new Insets(25, 40, 20, 40));

        // --- Header Section ---
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("ERROR LOG");
        titleLabel.setTextFill(Color.web("#e43f5a"));
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));

        Label countLabel = new Label("Total Errors Occurred: " + errorCount);
        countLabel.setTextFill(Color.web("#f0a500"));
        countLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        header.getChildren().addAll(titleLabel, countLabel);

        // --- Split Section: Left (List) and Right (Details) ---
        HBox centerBox = new HBox(20);

        // Left side: Error List
        VBox leftBox = new VBox(10);
        leftBox.setPrefWidth(300);
        Label listLabel = new Label("Error List");
        listLabel.setTextFill(Color.WHITE);
        listLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        ListView<String> errorListView = new ListView<>();
        errorListView.setStyle("-fx-control-inner-background: #162447; -fx-background-color: #162447; -fx-text-fill: white; -fx-font-family: 'Segoe UI';");
        for (int i = 0; i < errors.size(); i++) {
            String fullError = errors.get(i);
            String shortError = fullError.length() > 40 ? fullError.substring(0, 40) + "..." : fullError;
            errorListView.getItems().add("Error #" + (i + 1) + ": " + shortError);
        }
        VBox.setVgrow(errorListView, Priority.ALWAYS);
        leftBox.getChildren().addAll(listLabel, errorListView);

        // Right side: Error Details / Why it happened
        VBox rightBox = new VBox(10);
        Label detailLabel = new Label("Error Details & Causes");
        detailLabel.setTextFill(Color.WHITE);
        detailLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        TextArea detailArea = new TextArea();
        detailArea.setEditable(false);
        detailArea.setWrapText(true);
        detailArea.setStyle("-fx-control-inner-background: #1a294c; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Consolas'; -fx-border-color: #1f4068; -fx-border-width: 2;");
        detailArea.setText("Select an error from the list on the left to view why it happened and detailed traces.");
        VBox.setVgrow(detailArea, Priority.ALWAYS);
        rightBox.getChildren().addAll(detailLabel, detailArea);
        HBox.setHgrow(rightBox, Priority.ALWAYS);

        // Selection Listener to update the right side text area
        errorListView.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.intValue() >= 0) {
                detailArea.setText("DETAILED ERROR REPORT:\n\n" + errors.get(newVal.intValue()) + "\n\n[End of error trace]");
            }
        });

        centerBox.getChildren().addAll(leftBox, rightBox);
        VBox.setVgrow(centerBox, Priority.ALWAYS);

        // --- Footer Section: Buttons ---
        HBox footer = new HBox(20);
        footer.setAlignment(Pos.CENTER_LEFT);

        Button btnClear = new Button("Clear All Errors");
        btnClear.setStyle("-fx-background-color: #e43f5a; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 30; -fx-background-radius: 5; -fx-cursor: hand;");
        btnClear.setOnMouseEntered(e -> btnClear.setStyle("-fx-background-color: derive(#e43f5a, 20%); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 30; -fx-background-radius: 5; -fx-cursor: hand;"));
        btnClear.setOnMouseExited(e -> btnClear.setStyle("-fx-background-color: #e43f5a; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 30; -fx-background-radius: 5; -fx-cursor: hand;"));
        btnClear.setOnAction(e -> { ErrorHandler.clearErrors(); show(primaryStage); });

        footer.getChildren().addAll(btnClear);

        content.getChildren().addAll(header, centerBox, footer);
        root.setCenter(content);

        primaryStage.setTitle("Sports Manager - Error Log");
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(root, 1280, 720));
        } else {
            primaryStage.getScene().setRoot(root);
        }
        primaryStage.show();
    }
}
