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

    public static void showMessage(String titleText, String headerText, String contentText) {
        Stage ownerStage = SceneManager.getPrimaryStage();
        Stage popupStage = createBaseStage(ownerStage);
        VBox menuBox = createBaseMenuBox();

        Label title = new Label(titleText);
        title.setTextFill(Color.web("#e43f5a"));
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setWrapText(true);
        title.setAlignment(Pos.CENTER);

        Label header = new Label(headerText != null ? headerText : "");
        header.setTextFill(Color.WHITE);
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        header.setWrapText(true);
        header.setAlignment(Pos.CENTER);

        Label content = new Label(contentText);
        content.setTextFill(Color.web("#a5a5b0"));
        content.setFont(Font.font("Segoe UI", 12));
        content.setWrapText(true);
        content.setAlignment(Pos.CENTER);

        Button btnClose = createMenuButton("OK", "btn-success");
        btnClose.setOnAction(e -> popupStage.close());

        menuBox.getChildren().addAll(title, header, content, new Region(), btnClose);
        VBox.setVgrow(menuBox.getChildren().get(menuBox.getChildren().size() - 2), Priority.ALWAYS);

        finalizeAndShow(popupStage, ownerStage, menuBox);
    }

    public static void showConfirmation(String titleText, String headerText, String contentText, Runnable onConfirm, Runnable onCancel) {
        Stage ownerStage = SceneManager.getPrimaryStage();
        Stage popupStage = createBaseStage(ownerStage);
        VBox menuBox = createBaseMenuBox();

        Label title = new Label(titleText);
        title.setTextFill(Color.web("#f0a500"));
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setWrapText(true);
        title.setAlignment(Pos.CENTER);

        Label header = new Label(headerText != null ? headerText : "");
        header.setTextFill(Color.WHITE);
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        header.setWrapText(true);
        header.setAlignment(Pos.CENTER);

        Label content = new Label(contentText);
        content.setTextFill(Color.web("#a5a5b0"));
        content.setFont(Font.font("Segoe UI", 12));
        content.setWrapText(true);
        content.setAlignment(Pos.CENTER);

        Button btnYes = createMenuButton("Yes", "btn-success");
        Button btnNo = createMenuButton("No", "btn-primary");

        btnYes.setOnAction(e -> { popupStage.close(); if (onConfirm != null) onConfirm.run(); });
        btnNo.setOnAction(e -> { popupStage.close(); if (onCancel != null) onCancel.run(); });

        HBox btnBox = new HBox(10, btnYes, btnNo);
        btnBox.setAlignment(Pos.CENTER);

        menuBox.getChildren().addAll(title, header, content, new Region(), btnBox);
        VBox.setVgrow(menuBox.getChildren().get(menuBox.getChildren().size() - 2), Priority.ALWAYS);

        finalizeAndShow(popupStage, ownerStage, menuBox);
    }

    public static void showChoiceDialog(String titleText, String headerText, String contentText, List<String> choices, Consumer<String> onSelect) {
        Stage ownerStage = SceneManager.getPrimaryStage();
        Stage popupStage = createBaseStage(ownerStage);
        VBox menuBox = createBaseMenuBox();
        menuBox.setMaxWidth(675);

        Label title = new Label(titleText);
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setWrapText(true);
        title.setAlignment(Pos.CENTER);

        Label header = new Label(headerText != null ? headerText : "");
        header.setTextFill(Color.web("#a5a5b0"));
        header.setFont(Font.font("Segoe UI", 12));
        header.setWrapText(true);
        header.setAlignment(Pos.CENTER);

        VBox choicesBox = new VBox(10);
        choicesBox.setAlignment(Pos.CENTER);
        for (String choice : choices) {
            Button btnChoice = createMenuButton(choice, "btn-info");
            btnChoice.setOnAction(e -> { popupStage.close(); if (onSelect != null) onSelect.accept(choice); });
            choicesBox.getChildren().add(btnChoice);
        }

        ScrollPane scrollPane = new ScrollPane(choicesBox);
        scrollPane.setFitToWidth(false);
        scrollPane.setPrefHeight(450);
        scrollPane.getStyleClass().add("scroll-pane-transparent");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Button btnCancel = createMenuButton("Cancel", "btn-primary");
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
        menuBox.getStyleClass().add("menu-box");
        return menuBox;
    }

    private static void finalizeAndShow(Stage popupStage, Stage ownerStage, VBox menuBox) {
        StackPane root = new StackPane(menuBox);
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        root.setPadding(new Insets(50));

        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(SceneManager.class.getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Warning: styles.css not found in resources folder.");
        }
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

    private static Button createMenuButton(String text, String cssClass) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(45);
        btn.getStyleClass().addAll("btn", cssClass);
        
        return btn;
    }
}