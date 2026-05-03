package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;
import java.util.function.Consumer;

public class GUIPopup {

    public static void showMessage(Stage ownerStage, String titleText, String headerText, String contentText) {
        Stage popupStage = createBaseStage(ownerStage);
        VBox menuBox = createBaseMenuBox();

        Label title = new Label(titleText);
        title.setTextFill(Color.web("#e43f5a"));
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

        Label header = new Label(headerText != null ? headerText : "");
        header.setTextFill(Color.WHITE);
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        header.setWrapText(true);
        header.setAlignment(Pos.CENTER);

        Label content = new Label(contentText);
        content.setTextFill(Color.web("#a5a5b0"));
        content.setFont(Font.font("Segoe UI", 14));
        content.setWrapText(true);
        content.setAlignment(Pos.CENTER);

        Button btnClose = createMenuButton("OK", "#4CAF50");
        btnClose.setOnAction(e -> popupStage.close());

        menuBox.getChildren().addAll(title, header, content, new Region(), btnClose);
        VBox.setVgrow(menuBox.getChildren().get(menuBox.getChildren().size() - 2), Priority.ALWAYS);

        finalizeAndShow(popupStage, ownerStage, menuBox);
    }

    public static void showConfirmation(Stage ownerStage, String titleText, String headerText, String contentText, Runnable onConfirm, Runnable onCancel) {
        Stage popupStage = createBaseStage(ownerStage);
        VBox menuBox = createBaseMenuBox();

        Label title = new Label(titleText);
        title.setTextFill(Color.web("#f0a500"));
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

        Label header = new Label(headerText != null ? headerText : "");
        header.setTextFill(Color.WHITE);
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        header.setWrapText(true);
        header.setAlignment(Pos.CENTER);

        Label content = new Label(contentText);
        content.setTextFill(Color.web("#a5a5b0"));
        content.setFont(Font.font("Segoe UI", 14));
        content.setWrapText(true);
        content.setAlignment(Pos.CENTER);

        Button btnYes = createMenuButton("Yes", "#4CAF50");
        Button btnNo = createMenuButton("No", "#e43f5a");

        btnYes.setOnAction(e -> { popupStage.close(); if (onConfirm != null) onConfirm.run(); });
        btnNo.setOnAction(e -> { popupStage.close(); if (onCancel != null) onCancel.run(); });

        HBox btnBox = new HBox(10, btnYes, btnNo);
        btnBox.setAlignment(Pos.CENTER);

        menuBox.getChildren().addAll(title, header, content, new Region(), btnBox);
        VBox.setVgrow(menuBox.getChildren().get(menuBox.getChildren().size() - 2), Priority.ALWAYS);

        finalizeAndShow(popupStage, ownerStage, menuBox);
    }

    public static void showChoiceDialog(Stage ownerStage, String titleText, String headerText, String contentText, List<String> choices, Consumer<String> onSelect) {
        Stage popupStage = createBaseStage(ownerStage);
        VBox menuBox = createBaseMenuBox();
        menuBox.setMaxWidth(450);

        Label title = new Label(titleText);
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

        Label header = new Label(headerText != null ? headerText : "");
        header.setTextFill(Color.web("#a5a5b0"));
        header.setFont(Font.font("Segoe UI", 14));
        header.setWrapText(true);
        header.setAlignment(Pos.CENTER);

        VBox choicesBox = new VBox(10);
        choicesBox.setAlignment(Pos.CENTER);
        for (String choice : choices) {
            Button btnChoice = createMenuButton(choice, "#1f4068");
            btnChoice.setOnAction(e -> { popupStage.close(); if (onSelect != null) onSelect.accept(choice); });
            choicesBox.getChildren().add(btnChoice);
        }

        ScrollPane scrollPane = new ScrollPane(choicesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-control-inner-background: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Button btnCancel = createMenuButton("Cancel", "#e43f5a");
        btnCancel.setOnAction(e -> popupStage.close());

        menuBox.getChildren().addAll(title, header, scrollPane, new Region(), btnCancel);
        VBox.setVgrow(menuBox.getChildren().get(menuBox.getChildren().size() - 2), Priority.ALWAYS);

        finalizeAndShow(popupStage, ownerStage, menuBox);
    }

    private static Stage createBaseStage(Stage ownerStage) {
        Stage popupStage = new Stage();
        popupStage.initOwner(ownerStage);
        popupStage.initStyle(StageStyle.TRANSPARENT);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        return popupStage;
    }

    private static VBox createBaseMenuBox() {
        VBox menuBox = new VBox(15);
        menuBox.setPadding(new Insets(40, 60, 40, 60));
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setMaxWidth(350);
        menuBox.setStyle("-fx-background-color: #162447; -fx-background-radius: 15; -fx-border-color: #e43f5a; -fx-border-width: 2; -fx-border-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 20, 0, 0, 0);");
        return menuBox;
    }

    private static void finalizeAndShow(Stage popupStage, Stage ownerStage, VBox menuBox) {
        StackPane root = new StackPane(menuBox);
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        root.setPadding(new Insets(50));

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        popupStage.setScene(scene);

        if (ownerStage != null) {
            popupStage.setWidth(ownerStage.getWidth());
            popupStage.setHeight(ownerStage.getHeight());
            popupStage.setX(ownerStage.getX());
            popupStage.setY(ownerStage.getY());
        }

        popupStage.showAndWait();
    }

    private static Button createMenuButton(String text, String baseColor) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(45);
        btn.setStyle("-fx-background-color: " + baseColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 5;");
        
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: derive(" + baseColor + ", 30%); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 5; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + baseColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 5;"));
        
        return btn;
    }
}