/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zikai.deathbydots;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javafx.animation.FadeTransition;
import javafx.animation.FadeTransitionBuilder;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.zikai.deathbydots.gameengine.GameWorld;
import com.zikai.deathbydots.gameengine.Sprite;
import com.zikai.deathbydots.gameengine.SpriteManager;
import javafx.event.Event;

public class DeathByDots
        extends GameWorld {

    private final int SCREEN_WIDTH = 640;
    private final int SCREEN_HEIGHT = 480;
    private final int OFFSET_LEFT = 10;
    private final int OFFSET_TOP = 50;
    private final int OFFSET_RIGHT = 10;
    private final int OFFSET_BOTTOM = 10;
    private final int SCORE_MOD_LOOP = 5;
    private final int SPAWN_MOD_LOOP = 1000;
    private final int BOMB_MOD_LOOP = 15000;
    private final int MAX_ENEMIES = 250;
    private final int MAX_BOMBS = 3;
    private final int INITIAL_TELEPORTS = 3;
    private final int INITIAL_SHIELDS = 1;
    private final int NUMBER_OF_HIGH_SCORES_TO_SHOW = 5;
    private final Paint BORDER_COLOR = Color.DARKTURQUOISE;
    private final String HIGH_SCORE_FILE = "./highscores";
    private static final Integer STARTTIME = Integer.valueOf(3);
    private static final Label NUM_SPRITES_FIELD = new Label();
    private Label mousePtLabel = new Label();
    private Label mousePressPtLabel = new Label();
    private Label scoreLabel = new Label();
    private int score = 0;
    private int scoreMod = 0;
    private int spawnMod = 0;
    private int bombMod = 0;
    private int numberOfBombs = 0;
    private int numberOfTeleports = 3;
    private int numberOfShields = 1;
    private boolean firstBombCollision = true;
    private Label timerLabel = new Label();
    private Timeline timeline;
    private IntegerProperty timeSeconds = new SimpleIntegerProperty(STARTTIME.intValue());
    private Label shieldLabel = new Label();
    private Label teleportLabel = new Label();
    private boolean isGameOn = false;
    Character character = new Character();
    ArrayList<Integer> highScoresArray = new ArrayList();

    public DeathByDots(int fps, String title) {
        super(fps, title);
    }

    public void initialize(Stage primaryStage) {
        primaryStage.setTitle(getWindowTitle());
        primaryStage.setResizable(false);

        setSceneNodes(new Group());
        setGameSurface(new Scene(getSceneNodes(), SCREEN_WIDTH, SCREEN_HEIGHT));
        primaryStage.setScene(getGameSurface());
        getGameSurface().setFill(this.BORDER_COLOR);
        getGameSurface().getStylesheets().add("resources/styles.css");
        setupMouseInput(primaryStage);

        beginGameLoop();

        Rectangle gamePane = new Rectangle();
        gamePane.setWidth(630.0D);
        gamePane.setHeight(430.0D);
        gamePane.setLayoutX(10.0D);
        gamePane.setLayoutY(50.0D);
        gamePane.setFill(Color.WHITE);

        getSceneNodes().getChildren().add(0, gamePane);

        this.timerLabel.textProperty().bind(this.timeSeconds.asString());
        this.timerLabel.setId("timerLabel");
        this.timerLabel.setVisible(false);

        VBox stats = ((VBoxBuilder) ((VBoxBuilder) VBoxBuilder.create().spacing(5.0D).translateX(10.0D)).translateY(7.0D)).build();

        AnchorPane hudBox = new AnchorPane();
        hudBox.setPrefWidth(620.0D);

        this.scoreLabel.setTextFill(Color.WHITE);
        this.scoreLabel.setLayoutY(3.0D);
        this.scoreLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 22.0D));
        this.scoreLabel.setText("Version: 1.0");

        TilePane powerUps = new TilePane();
        powerUps.setPrefColumns(2);
        powerUps.setLayoutX(520.0D);

        Label shieldText = new Label("Shields:");
        shieldText.setId("powerUpLabel");

        Label teleportText = new Label("Teleports:");
        teleportText.setId("powerUpLabel");

        this.shieldLabel.setId("powerUpLabel");
        this.teleportLabel.setId("powerUpLabel");

        powerUps.getChildren().add(shieldText);
        powerUps.getChildren().add(this.shieldLabel);
        powerUps.getChildren().add(teleportText);
        powerUps.getChildren().add(this.teleportLabel);

        hudBox.getChildren().add(this.scoreLabel);
        hudBox.getChildren().add(powerUps);
        stats.getChildren().add(hudBox);

        getSceneNodes().getChildren().add(stats);

        VBox vb = new VBox(20.0D);
        vb.setAlignment(Pos.CENTER);
        vb.setPrefWidth(getGameSurface().getWidth());
        vb.setLayoutY(getGameSurface().getHeight() / 2.0D - 50.0D);
        vb.getChildren().add(this.timerLabel);
        getSceneNodes().getChildren().add(vb);

        loadHighScoresArray();

        generateManySpheres(70);

        showTitleScreen();
    }

    private void setupMouseInput(Stage primaryStage) {
        EventHandler fireOrMove = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                DeathByDots.this.mousePressPtLabel.setText("Mouse Press PT = (" + event.getX() + ", " + event.getY() + ")");
                if (event.getButton() == MouseButton.PRIMARY) {
                    DeathByDots.this.shield();
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    DeathByDots.this.teleport();
                }
            }
        };
        EventHandler changeWeapons = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (KeyCode.Z == event.getCode()) {
                    DeathByDots.this.shield();
                } else if (KeyCode.X == event.getCode()) {
                    DeathByDots.this.teleport();
                }
            }
        };
        primaryStage.getScene().setOnKeyPressed(changeWeapons);
        primaryStage.getScene().setOnMousePressed(fireOrMove);

        EventHandler showMouseMove = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                DeathByDots.this.mousePtLabel.setText("Mouse PT = (" + event.getX() + ", " + event.getY() + ")");

                double x = event.getX();
                double y = event.getY();
                if (event.getX() > DeathByDots.this.getGameSurface().getWidth() - DeathByDots.this.character.getWidth() / 2.0D - 10.0D) {
                    x = DeathByDots.this.getGameSurface().getWidth() - DeathByDots.this.character.getWidth() / 2.0D - 10.0D;
                } else if (event.getX() < 10.0D + DeathByDots.this.character.getWidth() / 2.0D) {
                    x = 10.0D + DeathByDots.this.character.getWidth() / 2.0D;
                }
                if (event.getY() > DeathByDots.this.getGameSurface().getHeight() - DeathByDots.this.character.getHeight() / 2.0D - 10.0D) {
                    y = DeathByDots.this.getGameSurface().getHeight() - DeathByDots.this.character.getHeight() / 2.0D - 10.0D;
                } else if (event.getY() < 50.0D + DeathByDots.this.character.getHeight() / 2.0D) {
                    y = 50.0D + DeathByDots.this.character.getHeight() / 2.0D;
                }
                DeathByDots.this.character.setCoordinates(x, y);
            }
        };
        primaryStage.getScene().setOnMouseMoved(showMouseMove);
    }

    private void shield() {
        if (this.numberOfShields > 0) {
            this.character.shieldToggle();
            this.numberOfShields -= 1;
            this.shieldLabel.setText(this.numberOfShields + "");
        }
    }

    private void teleport() {
        if (this.numberOfTeleports > 0) {
            Timeline gameLoop = getGameLoop();
            switch (gameLoop.statusProperty().get().ordinal()) {
                case 1:
                    gameLoop.stop();
                    break;
                case 2:
                    gameLoop.play();
                    this.numberOfTeleports -= 1;
                    this.teleportLabel.setText(this.numberOfTeleports + "");
            }
        }
    }

    private void createCharacter() {
        if (this.character.isDead == true) {
            this.character = new Character();
        }
        getSpriteManager().addSprites(new Sprite[]{this.character});
        getSceneNodes().getChildren().add(this.character.node);
    }

    private void enterSphereFromEdge() {
        Random rnd = new Random();

        Scene gameSurface = getGameSurface();

        Atom b = new Atom(5.0D, Color.ORANGERED);
        Circle circle = b.getAsCircle();

        b.vX = ((rnd.nextInt(1) + rnd.nextDouble()) * (rnd.nextBoolean() ? 1 : -1) + 0.1D);
        b.vY = ((rnd.nextInt(1) + rnd.nextDouble()) * (rnd.nextBoolean() ? 1 : -1) + 0.1D);

        int side = rnd.nextInt(4);

        double newX = 20.0D;
        double newY = 60.0D;
        if (side == 0) {
            newX = rnd.nextInt((int) gameSurface.getWidth() - (int) circle.getRadius() * 2 - 10 - 10) + 10;
            newY = 50.0D;
            b.vY = ((rnd.nextInt(1) + rnd.nextDouble()) * 1.0D + 0.3D);
        } else if (side == 1) {
            newY = rnd.nextInt((int) gameSurface.getHeight() - (int) circle.getRadius() * 2 - 50 - 10) + 50;
            newX = gameSurface.getWidth() - circle.getRadius() * 2.0D - 10.0D - 1.0D;
            b.vX = ((rnd.nextInt(1) + rnd.nextDouble()) * -1.0D - 0.3D);
        } else if (side == 2) {
            newX = rnd.nextInt((int) gameSurface.getWidth() - (int) circle.getRadius() * 2 - 10 - 10) + 10;
            newY = gameSurface.getHeight() - circle.getRadius() * 2.0D - 10.0D - 1.0D;
            b.vY = ((rnd.nextInt(1) + rnd.nextDouble()) * -1.0D - 0.3D);
        } else if (side == 3) {
            newY = rnd.nextInt((int) gameSurface.getHeight() - (int) circle.getRadius() * 2 - 50 - 10) + 50;
            newX = 10.0D;
            b.vX = ((rnd.nextInt(1) + rnd.nextDouble()) * 1.0D + 0.3D);
        }
        circle.setTranslateX(newX);
        circle.setTranslateY(newY);
        circle.setVisible(true);
        circle.setOpacity(0.0D);
        circle.setId(b.toString());

        getSpriteManager().addSprites(new Sprite[]{b});

        getSceneNodes().getChildren().add(b.node);

        FadeTransitionBuilder.create().node(b.node).duration(Duration.millis(300.0D)).fromValue(0.0D).toValue(1.0D).build().play();
    }

    private void enterBomb() {
        Random rnd = new Random();

        Scene gameSurface = getGameSurface();

        Bomb b = new Bomb(10.0D, Color.BLUE);
        Circle circle = b.getAsCircle();

        b.vX = (rnd.nextDouble() * (rnd.nextBoolean() ? 1 : -1));
        b.vY = (rnd.nextDouble() * (rnd.nextBoolean() ? 1 : -1));

        int newX = rnd.nextInt((int) gameSurface.getWidth() - (int) circle.getRadius() * 2 - 10 - 10) + 10;
        int newY = rnd.nextInt((int) gameSurface.getHeight() - (int) circle.getRadius() * 2 - 50 - 10) + 50;

        circle.setTranslateX(newX);
        circle.setTranslateY(newY);
        circle.setVisible(true);
        circle.setOpacity(0.0D);
        circle.setId(b.toString());

        getSpriteManager().addSprites(new Sprite[]{b});

        getSceneNodes().getChildren().add(1, b.node);

        FadeTransitionBuilder.create().node(b.node).duration(Duration.millis(300.0D)).fromValue(0.0D).toValue(1.0D).build().play();

        this.numberOfBombs += 1;
    }

    private void generateManySpheres(int numSpheres) {
        for (int i = 0; i < numSpheres; i++) {
            enterSphereFromEdge();
        }
    }

    protected void handleUpdate(Sprite sprite) {
        if (this.isGameOn) {
            this.scoreMod += 1;
            if (this.scoreMod == 5) {
                this.scoreMod = 0;
                this.score += 1;
            }
            this.scoreLabel.setText(this.score + "");

            this.spawnMod += 1;
            if (this.spawnMod == 1000) {
                this.spawnMod = 0;
                if (getSpriteManager().getAllSprites().size() < 250) {
                    enterSphereFromEdge();
                }
            }
            this.bombMod += 1;
            if (this.bombMod == 15000) {
                this.bombMod = 0;
                if (this.numberOfBombs < 3) {
                    enterBomb();
                }
            }
        }
        sprite.update();

        bounceOffWalls(sprite);
    }

    private void bounceOffWalls(Sprite sprite) {
        Node displayNode = sprite.node;
        if ((sprite.node.getTranslateX() > getGameSurface().getWidth() - displayNode.getBoundsInParent().getWidth() - 10.0D) || (displayNode.getTranslateX() < 10.0D)) {
            sprite.vX *= -1.0D;
        }
        if ((sprite.node.getTranslateY() > getGameSurface().getHeight() - displayNode.getBoundsInParent().getHeight() - 10.0D) || (sprite.node.getTranslateY() < 50.0D)) {
            sprite.vY *= -1.0D;
        }
    }

    protected boolean handleCollision(Sprite spriteA, Sprite spriteB) {
        if ((spriteA != spriteB) && (this.isGameOn)
                && (spriteA.collide(spriteB))) {
            if (((spriteA instanceof Atom)) && ((spriteB instanceof Atom))) {
                return false;
            }
            if (((spriteA instanceof Bomb)) && ((spriteB instanceof Bomb))) {
                return false;
            }
            if ((((spriteA instanceof Bomb)) && (spriteB == this.character)) || ((spriteA == this.character) && ((spriteB instanceof Bomb)))) {
                if (spriteA != this.character) {
                    ((Bomb) spriteA).implode(this);
                } else if (spriteB != this.character) {
                    ((Bomb) spriteB).implode(this);
                }
                if (this.firstBombCollision) {
                    this.numberOfBombs -= 1;
                    this.firstBombCollision = false;
                } else {
                    this.firstBombCollision = true;
                }
                return true;
            }
            if (((spriteA instanceof Bomb)) && ((spriteB instanceof Atom))) {
                Bomb b = (Bomb) spriteA;
                if (b.isImploding()) {
                    spriteB.handleDeath(this);
                }
                return true;
            }
            if (((spriteA instanceof Atom)) && ((spriteB instanceof Bomb))) {
                Bomb b = (Bomb) spriteB;
                if (b.isImploding()) {
                    spriteA.handleDeath(this);
                }
                return true;
            }
            if ((!this.character.isImmune()) && (((spriteA instanceof Atom)) || ((spriteB instanceof Atom))) && ((spriteA == this.character) || (spriteB == this.character))) {
                spriteA.handleDeath(this);
                spriteB.handleDeath(this);
                this.isGameOn = false;
                gameOver();
            } else if ((this.character.isImmune()) && (((spriteA instanceof Atom)) || ((spriteB instanceof Atom))) && ((spriteA == this.character) || (spriteB == this.character))) {
                if (spriteA != this.character) {
                    spriteA.handleDeath(this);
                }
                if (spriteB != this.character) {
                    spriteB.handleDeath(this);
                }
            }
        }
        return false;
    }

    protected void cleanupSprites() {
        super.cleanupSprites();

        NUM_SPRITES_FIELD.setText(String.valueOf(getSpriteManager().getAllSprites().size()));
    }

    private void newGame(int difficulty) {
        getGameSurface().setCursor(Cursor.NONE);
        for (Sprite sprite : getSpriteManager().getAllSprites()) {
            sprite.handleDeath(this);
        }
        this.score = 0;
        this.scoreMod = 0;
        this.spawnMod = 0;
        this.numberOfBombs = 0;
        this.bombMod = 0;
        this.numberOfTeleports = 3;
        this.numberOfShields = 1;

        this.shieldLabel.setText(this.numberOfShields + "");
        this.teleportLabel.setText(this.numberOfTeleports + "");

        this.timerLabel.setVisible(true);
        if (this.timeline != null) {
            this.timeline.stop();
        }
        this.timeSeconds.set(STARTTIME.intValue());
        this.timeline = new Timeline();
        this.timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(STARTTIME.intValue()), new KeyValue[]{new KeyValue(this.timeSeconds, Integer.valueOf(1))}));

        this.timeline.playFromStart();
        this.timeline.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                DeathByDots.this.isGameOn = true;
                DeathByDots.this.timerLabel.setVisible(false);

                DeathByDots.this.generateManySpheres(30);
            }
        });
        createCharacter();
    }

    private void showTitleScreen() {
        getGameSurface().setCursor(Cursor.DEFAULT);

        final AnchorPane pane = new AnchorPane();
        VBox vbox = new VBox(15.0D);
        vbox.setPadding(new Insets(40.0D, 0.0D, 40.0D, 0.0D));
        Image img = new Image("resources/panel.png", false);
        ImageView bgView = new ImageView(img);

        Label titleLabel = new Label("Death By Dots");
        titleLabel.setId("titleLabel");
        titleLabel.setFont(Font.font("Bebas Neue", FontWeight.BOLD, 72.0D));
        titleLabel.setPrefWidth(img.getWidth());

        Label nameLabel = new Label("By: Zi Kai Chen");
        nameLabel.setId("nameLabel");
        nameLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 18.0D));
        nameLabel.setPrefWidth(img.getWidth());

        VBox buttonBox = new VBox();
        buttonBox.spacingProperty().set(20.0D);

        Button newGameButton = new Button("Let's Play!");
        newGameButton.setPrefHeight(45.0D);
        newGameButton.setPrefWidth(180.0D);
        newGameButton.setAlignment(Pos.CENTER);
        newGameButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                DeathByDots.this.newGame(0);
                DeathByDots.this.getSceneNodes().getChildren().remove(pane);
            }
        });
        Button highScoresButton = new Button("High Scores");
        newGameButton.setId("secondaryButton");
        highScoresButton.setPrefWidth(180.0D);
        highScoresButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                DeathByDots.this.showHighScores();
            }
        });
        Button howToPlayButton = new Button("How To Play");
        newGameButton.setId("secondaryButton");
        howToPlayButton.setPrefWidth(180.0D);
        howToPlayButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                DeathByDots.this.showHowToPlay();
            }
        });
        buttonBox.getChildren().add(newGameButton);
        buttonBox.getChildren().add(highScoresButton);
        buttonBox.getChildren().add(howToPlayButton);
        buttonBox.setAlignment(Pos.CENTER);

        vbox.getChildren().add(titleLabel);
        vbox.getChildren().add(nameLabel);
        vbox.getChildren().add(buttonBox);
        pane.getChildren().add(bgView);
        pane.getChildren().add(vbox);

        pane.setPrefWidth(img.getWidth());
        pane.setPrefHeight(img.getHeight());
        pane.setLayoutX((getGameSurface().getWidth() - img.getWidth()) / 2.0D);
        pane.setLayoutY((getGameSurface().getHeight() - img.getHeight()) / 2.0D);

        pane.setOpacity(0.0D);
        getSceneNodes().getChildren().add(pane);
        FadeTransitionBuilder.create().node(pane).duration(Duration.millis(1500.0D)).fromValue(0.0D).toValue(1.0D).build().play();
    }

    private void showHowToPlay() {
        final AnchorPane pane = new AnchorPane();
        VBox vbox = new VBox(10.0D);
        vbox.setPadding(new Insets(20.0D, 40.0D, 40.0D, 40.0D));
        Image img = new Image("resources/panel2.png", false);
        ImageView bgView = new ImageView(img);

        Label highScoresLabel = new Label("How To Play");
        highScoresLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 24.0D));
        highScoresLabel.setPrefWidth(img.getWidth() - 80.0D);
        highScoresLabel.setId("nameLabel");

        vbox.getChildren().add(highScoresLabel);

        Label message = new Label();
        message.setPrefWidth(img.getWidth() - 80.0D);
        message.setPrefHeight(200.0D);
        message.setWrapText(true);
        message.setTextFill(Color.WHITE);
        message.setFont(Font.font("Verdana", FontWeight.NORMAL, 14.0D));
        message.setText("1. Control the smiley face with your mouse. \n2. Dodge the red dots \n3. Hit the blue dots to use a bomb \n4. Stay alive and earn points!! \n\nPower Ups:\n\n1x Left Click for shield.\n3x Right Click, move mouse and right click again to teleport. \n");

        vbox.getChildren().add(message);

        HBox hbox = new HBox();
        hbox.setPadding(new Insets(0.0D, 0.0D, 40.0D, 0.0D));
        hbox.setSpacing(40.0D);
        hbox.setPrefWidth(img.getWidth());

        Button okButton = new Button("Ok, Go Back");
        okButton.setPrefHeight(40.0D);
        okButton.setPrefWidth(180.0D);
        okButton.setAlignment(Pos.CENTER);
        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                FadeTransitionBuilder.create()
                        .node(pane)
                        .duration(Duration.millis(1500.0D))
                        .fromValue(1.0D)
                        .toValue(0.0D)
                        .onFinished(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent arg0) {
                                DeathByDots.this.getSceneNodes().getChildren().remove(pane);
                            }
                        })
                        .build().play();
}
});
hbox.getChildren().add(okButton);
        vbox.getChildren().add(hbox);
        pane.getChildren().add(bgView);
        pane.getChildren().add(vbox);

        pane.setPrefWidth(img.getWidth());
        pane.setPrefHeight(img.getHeight());
        pane.setLayoutX((getGameSurface().getWidth() - img.getWidth()) / 2.0D);
        pane.setLayoutY((getGameSurface().getHeight() - img.getHeight()) / 2.0D);

        pane.setOpacity(0.0D);
        getSceneNodes().getChildren().add(pane);
        FadeTransitionBuilder.create().node(pane).duration(Duration.millis(1500.0D)).fromValue(0.0D).toValue(1.0D).build().play();
    }

    private void gameOver() {
        this.highScoresArray.add(Integer.valueOf(this.score));
        Collections.sort(this.highScoresArray, Collections.reverseOrder());
        writeHighscoresArray();

        getGameSurface().setCursor(Cursor.DEFAULT);

        final AnchorPane pane = new AnchorPane();
        VBox vbox = new VBox(10.0D);
        vbox.setPadding(new Insets(40.0D, 0.0D, 40.0D, 0.0D));
        Image img = new Image("resources/panel.png", false);
        ImageView bgView = new ImageView(img);

        Label gameOverLabel = new Label("GAME OVER");
        gameOverLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 42.0D));
        gameOverLabel.setId("nameLabel");
        gameOverLabel.setPrefWidth(img.getWidth());
        if (this.highScoresArray.indexOf(Integer.valueOf(this.score)) < 5) {
            gameOverLabel.setText("NEW HIGH SCORE");
        }
        Label finalScoreLabel = new Label(this.score + "");
        finalScoreLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 78.0D));
        finalScoreLabel.setTextFill(Color.WHITE);
        finalScoreLabel.setPrefWidth(img.getWidth());
        finalScoreLabel.setAlignment(Pos.CENTER);

        HBox hbox = new HBox();
        hbox.setPadding(new Insets(40.0D));
        hbox.setSpacing(40.0D);
        Button newGameButton = new Button("Play Again");
        newGameButton.setPrefHeight(40.0D);
        newGameButton.setPrefWidth(180.0D);
        newGameButton.setId("button");
        newGameButton.setAlignment(Pos.CENTER);
        newGameButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                DeathByDots.this.newGame(0);
                DeathByDots.this.getSceneNodes().getChildren().remove(pane);
            }
        });
        Button highScoresButton = new Button("High Scores");
        highScoresButton.setPrefHeight(40.0D);
        highScoresButton.setPrefWidth(180.0D);
        highScoresButton.setAlignment(Pos.CENTER);
        highScoresButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                DeathByDots.this.showHighScores();
            }
        });
        hbox.getChildren().add(newGameButton);
        hbox.getChildren().add(highScoresButton);
        vbox.getChildren().add(gameOverLabel);
        vbox.getChildren().add(finalScoreLabel);
        vbox.getChildren().add(hbox);
        pane.getChildren().add(bgView);
        pane.getChildren().add(vbox);

        pane.setPrefWidth(img.getWidth());
        pane.setPrefHeight(img.getHeight());
        pane.setLayoutX((getGameSurface().getWidth() - img.getWidth()) / 2.0D);
        pane.setLayoutY((getGameSurface().getHeight() - img.getHeight()) / 2.0D);

        pane.setOpacity(0.0D);
        getSceneNodes().getChildren().add(pane);
        FadeTransitionBuilder.create().node(pane).duration(Duration.millis(1500.0D)).fromValue(0.0D).toValue(1.0D).build().play();
    }

    private void showHighScores() {
        final AnchorPane pane = new AnchorPane();
        VBox vbox = new VBox(10.0D);
        vbox.setPadding(new Insets(20.0D, 40.0D, 40.0D, 40.0D));
        Image img = new Image("resources/panel2.png", false);
        ImageView bgView = new ImageView(img);

        Label highScoresLabel = new Label("High Scores");
        highScoresLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 24.0D));
        highScoresLabel.setId("nameLabel");
        highScoresLabel.setPrefWidth(img.getWidth() - 80.0D);

        vbox.getChildren().add(highScoresLabel);
        for (int i = 0; i < 5; i++) {
            AnchorPane nameScorePane = new AnchorPane();

            Label hName = new Label("" + (i + 1));
            hName.setFont(Font.font("Verdana", FontWeight.BOLD, 18.0D));
            hName.setTextFill(Color.WHITE);
            hName.setPrefWidth(img.getWidth());
            hName.setAlignment(Pos.CENTER_LEFT);

            Label hScore = new Label("-----");
            hScore.setFont(Font.font("Verdana", FontWeight.BOLD, 18.0D));
            hScore.setTextFill(Color.WHITE);
            hScore.setPrefWidth(img.getWidth() - 80.0D);
            hScore.setAlignment(Pos.CENTER_RIGHT);
            if (i < this.highScoresArray.size()) {
                hScore.setText(this.highScoresArray.get(i) + "");
                if (this.score == ((Integer) this.highScoresArray.get(i)).intValue()) {
                    hName.setTextFill(Color.YELLOW);
                    hScore.setTextFill(Color.YELLOW);
                }
            }
            nameScorePane.getChildren().add(hName);
            nameScorePane.getChildren().add(hScore);
            vbox.getChildren().add(nameScorePane);
        }
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(40.0D, 0.0D, 40.0D, 0.0D));
        hbox.setSpacing(40.0D);
        hbox.setPrefWidth(img.getWidth());
        Button okButton = new Button("Ok, Go Back");
        okButton.setPrefHeight(40.0D);
        okButton.setPrefWidth(180.0D);
        okButton.setAlignment(Pos.CENTER);
        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                FadeTransitionBuilder.create()
                        .node(pane)
                        .duration(Duration.millis(1500.0D))
                        .fromValue(1.0D)
                        .toValue(0.0D)
                        .onFinished(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent arg0) {
                                DeathByDots.this.getSceneNodes().getChildren().remove(pane);
                            }
                        })
                        .build()
                        .play();
}
});
hbox.getChildren().add(okButton);
        vbox.getChildren().add(hbox);
        pane.getChildren().add(bgView);
        pane.getChildren().add(vbox);

        pane.setPrefWidth(img.getWidth());
        pane.setPrefHeight(img.getHeight());
        pane.setLayoutX((getGameSurface().getWidth() - img.getWidth()) / 2.0D);
        pane.setLayoutY((getGameSurface().getHeight() - img.getHeight()) / 2.0D);

        pane.setOpacity(0.0D);
        getSceneNodes().getChildren().add(pane);
        FadeTransitionBuilder.create().node(pane).duration(Duration.millis(1500.0D)).fromValue(0.0D).toValue(1.0D).build().play();
    }

    private void loadHighScoresArray() {
        try {
            FileReader reader = new FileReader(new File("./highscores"));
            BufferedReader in = new BufferedReader(reader);
            boolean stillCanRead = true;
            while (stillCanRead) {
                String s = in.readLine();
                if (s == null) {
                    stillCanRead = false;
                } else {
                    s = s.trim();
                    int x = Integer.parseInt(s);
                    if (x != -1) {
                        this.highScoresArray.add(Integer.valueOf(x));
                    }
                }
            }
        } catch (IOException ie) {
        }
    }

    private void writeHighscoresArray() {
        try {
            FileWriter writer = new FileWriter(new File("./highscores"));
            BufferedWriter out = new BufferedWriter(writer);
            for (Integer i : this.highScoresArray) {
                out.write(i + "\n");
            }
            out.flush();
            out.close();
        } catch (IOException ie) {
        }
    }
}
