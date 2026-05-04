package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class GUISportSelection {

    private static final String STYLE_ROOT_BG = "-fx-background-color: #050505;";
    private static final String STYLE_CARD_DEFAULT = 
            "-fx-border-color: rgba(255, 255, 255, 0.2); " +
            "-fx-border-radius: 25; " +
            "-fx-border-width: 2; " +
            "-fx-background-color: rgba(255, 255, 255, 0.03); " +
            "-fx-background-radius: 25;";
    private static final String STYLE_CARD_HOVER = 
            "-fx-border-color: #ffffff; " +
            "-fx-border-radius: 25; " +
            "-fx-border-width: 3; " +
            "-fx-background-color: rgba(255, 255, 255, 0.1); " +
            "-fx-background-radius: 25;";
    private static final String STYLE_LABEL = 
            "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Arial';";

    private final Consumer<String> onSportSelected;
    private final Runnable onBackToMenu; 

  
    public GUISportSelection(Consumer<String> onSportSelected, Runnable onBackToMenu) {
        this.onSportSelected = onSportSelected;
        this.onBackToMenu = onBackToMenu;
    }

    public void show(Stage primaryStage) {
      
        BorderPane root = new BorderPane();
        root.setStyle(STYLE_ROOT_BG);

        // --- ORTA KISIM (KAYDIRILABİLİR KARTLAR) ---
        HBox cardBox = new HBox(50);
        cardBox.setAlignment(Pos.CENTER);
        cardBox.setStyle("-fx-background-color: transparent;");
        cardBox.setPadding(new Insets(50, 50, 50, 50));

        VBox footballCard = createSportCard("FOOTBALL", "images/football_player.png");
        VBox volleyballCard = createSportCard("VOLLEYBALL", "images/volleyball_char.png");

        footballCard.setOnMouseClicked(e -> onSportSelected.accept("FOOTBALL"));
        volleyballCard.setOnMouseClicked(e -> onSportSelected.accept("VOLLEYBALL"));

        cardBox.getChildren().addAll(footballCard, volleyballCard);

        // ScrollPane Ayarları (Yatay kaydırma)
        ScrollPane scrollPane = new ScrollPane(cardBox);
        scrollPane.setFitToHeight(true); 
        scrollPane.setFitToWidth(false);
        scrollPane.setStyle("-fx-background: #050505; -fx-background-color: transparent; -fx-control-inner-background: #050505;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); 
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); 

        root.setCenter(scrollPane);

        // --- ALT KISIM (GERİ BUTONU) ---
        Button btnBack = new Button("BACK TO MAIN MENU");
        styleButton(btnBack);
        btnBack.setOnAction(e -> onBackToMenu.run());

        HBox footer = new HBox(btnBack);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 0, 40, 0)); 
        root.setBottom(footer);

        primaryStage.setTitle("Sports Manager - Select Sport");
        
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(root, 1280, 720));
        } else {
            primaryStage.getScene().setRoot(root);
        }
        
        primaryStage.show();
    }

    private VBox createSportCard(String sportName, String imagePath) {
        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(300, 450);
        card.setStyle(STYLE_CARD_DEFAULT);

        ImageView imageView = new ImageView();
        try {
            var resource = getClass().getResourceAsStream("/" + imagePath);
            if (resource != null) {
                Image image = new Image(resource);
                imageView.setImage(image);
                imageView.setFitWidth(220);
                imageView.setPreserveRatio(true);
                imageView.setOpacity(0.8);
            } else {
                System.out.println("Uyarı: Görsel bulunamadı -> " + imagePath);
            }
        } catch (Exception e) {
            System.err.println("Görsel yüklenemedi: " + imagePath);
        }

        Label lblName = new Label(sportName);
        lblName.setStyle(STYLE_LABEL);

        card.getChildren().addAll(imageView, lblName);

        card.setOnMouseEntered(e -> {
            card.setStyle(STYLE_CARD_HOVER);
            imageView.setOpacity(1.0);
            card.setTranslateY(-10);
        });

        card.setOnMouseExited(e -> {
            card.setStyle(STYLE_CARD_DEFAULT);
            imageView.setOpacity(0.8);
            card.setTranslateY(0);
        });

        return card;
    }

    // Geri butonu için stil metodu
    private void styleButton(Button button) {
        button.setPrefWidth(300);
        button.setPrefHeight(50);
        String defaultStyle = "-fx-background-color: transparent; -fx-border-color: #ffffff; -fx-border-radius: 30; -fx-border-width: 2; -fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Arial';";
        String hoverStyle = "-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 30; -fx-border-color: #ffffff; -fx-border-radius: 30; -fx-border-width: 2; -fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Arial';";
        
        button.setStyle(defaultStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(defaultStyle));
    }
}