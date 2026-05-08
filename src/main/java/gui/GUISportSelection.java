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

    private static final String STYLE_LABEL = 
            "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Arial';";

    private final Consumer<String> onSportSelected;
    private final Runnable onBackToMenu; 

  
    public GUISportSelection(Consumer<String> onSportSelected, Runnable onBackToMenu) {
        this.onSportSelected = onSportSelected;
        this.onBackToMenu = onBackToMenu;
    }

    public void show() {
      
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-darker");

        // --- CENTER SECTION (SCROLLABLE CARDS) ---
        HBox cardBox = new HBox(50);
        cardBox.setAlignment(Pos.CENTER);
        cardBox.setStyle("-fx-background-color: transparent;");
        cardBox.setPadding(new Insets(50, 50, 50, 50));

        VBox footballCard = createSportCard("FOOTBALL", "images/football_player.png");
        VBox volleyballCard = createSportCard("VOLLEYBALL", "images/volleyball_char.png");

        footballCard.setOnMouseClicked(e -> onSportSelected.accept("FOOTBALL"));
        volleyballCard.setOnMouseClicked(e -> onSportSelected.accept("VOLLEYBALL"));

        cardBox.getChildren().addAll(footballCard, volleyballCard);

        // ScrollPane Settings (Horizontal scrolling)
        ScrollPane scrollPane = new ScrollPane(cardBox);
        scrollPane.setFitToHeight(true); 
        scrollPane.setFitToWidth(false);
        scrollPane.getStyleClass().add("scroll-pane-transparent");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); 
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); 

        root.setCenter(scrollPane);

        // --- BOTTOM SECTION (BACK BUTTON) ---
        Button btnBack = new Button("BACK TO MAIN MENU");
        btnBack.setPrefWidth(300);
        btnBack.setPrefHeight(50);
        btnBack.getStyleClass().add("btn-outline");
        btnBack.setOnAction(e -> onBackToMenu.run());

        HBox footer = new HBox(btnBack);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 0, 40, 0)); 
        root.setBottom(footer);

        SceneManager.changeScene(root, "Sports Manager - Select Sport");
    }

    private VBox createSportCard(String sportName, String imagePath) {
        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(300, 450);
        card.getStyleClass().add("sport-card");

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
                System.err.println("Warning: Image not found -> " + imagePath);
            }
        } catch (Exception e) {
            System.err.println("Failed to load image: " + imagePath);
        }

        Label lblName = new Label(sportName);
        lblName.setStyle(STYLE_LABEL);

        card.getChildren().addAll(imageView, lblName);

        card.setOnMouseEntered(e -> {
            imageView.setOpacity(1.0);
            card.setTranslateY(-10);
        });

        card.setOnMouseExited(e -> {
            imageView.setOpacity(0.8);
            card.setTranslateY(0);
        });

        return card;
    }
}