/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zikai.deathbydots;

import java.io.PrintStream;
import java.net.URL;
import javafx.animation.FadeTransition;
import javafx.animation.FadeTransitionBuilder;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CircleBuilder;
import javafx.util.Duration;
import com.zikai.deathbydots.gameengine.GameWorld;
import com.zikai.deathbydots.gameengine.Sprite;

public class Character
        extends Sprite {

    private final Group flipBook = new Group();
    private KeyCode keyCode;
    private boolean shieldOn;
    private Circle shield;
    FadeTransition shieldFade;
    private Circle hitBounds;
    private ImageView imageView;

    public Character() {
        Image shipImage = new Image(getClass().getClassLoader().getResource("resources/ship.png").toExternalForm(), true);

        this.imageView = new ImageView(shipImage);
        this.imageView.setCache(true);
        this.imageView.setCacheHint(CacheHint.SPEED);
        this.imageView.setManaged(false);

        this.flipBook.getChildren().add(this.imageView);

        this.node = this.flipBook;
        this.flipBook.setTranslateX(200.0D);
        this.flipBook.setTranslateY(300.0D);
        this.flipBook.setCache(true);
        this.flipBook.setCacheHint(CacheHint.SPEED);
        this.flipBook.setManaged(false);
        this.flipBook.setAutoSizeChildren(false);
        initHitZone();
    }

    private void initHitZone() {
        if (this.hitBounds == null) {
            double hZoneCenterX = 10.0D;
            double hZoneCenterY = 10.0D;
            this.hitBounds = ((CircleBuilder) ((CircleBuilder) ((CircleBuilder) CircleBuilder.create().centerX(hZoneCenterX).centerY(hZoneCenterY).stroke(Color.PINK)).fill(Color.RED)).radius(9.0D).opacity(0.0D)).build();

            System.out.println("");
            this.flipBook.getChildren().add(this.hitBounds);
            this.collisionBounds = this.hitBounds;
        }
    }

    public void update() {
        this.flipBook.setTranslateX(this.flipBook.getTranslateX() + this.vX);
        this.flipBook.setTranslateY(this.flipBook.getTranslateY() + this.vY);
    }

    private ImageView getImageView() {
        return this.imageView;
    }

    public double getCenterX() {
        ImageView shipImage = getImageView();
        return this.node.getTranslateX() + shipImage.getBoundsInLocal().getWidth() / 2.0D;
    }

    public double getCenterY() {
        ImageView shipImage = getImageView();
        return this.node.getTranslateY() + shipImage.getBoundsInLocal().getHeight() / 2.0D;
    }

    public void setCoordinates(double screenX, double screenY) {
        this.flipBook.setTranslateX(screenX - getImageView().getBoundsInLocal().getWidth() / 2.0D);
        this.flipBook.setTranslateY(screenY - getImageView().getBoundsInLocal().getHeight() / 2.0D);
    }

    public void changeWeapon(KeyCode keyCode) {
        this.keyCode = keyCode;
    }

    public void shieldToggle() {
        if (this.shield == null) {
            ImageView shipImage = getImageView();
            double x = shipImage.getBoundsInLocal().getWidth() / 2.0D;
            double y = shipImage.getBoundsInLocal().getHeight() / 2.0D;

            this.shield = ((CircleBuilder) ((CircleBuilder) ((CircleBuilder) ((CircleBuilder) CircleBuilder.create().radius(getWidth() * 1.5D).strokeWidth(5.0D)).stroke(Color.GREEN)).fill(Color.LIMEGREEN)).centerX(x).centerY(y).opacity(0.6D)).build();

            this.collisionBounds = this.shield;
            this.shieldFade = FadeTransitionBuilder.create()
                    .node(this.shield)
                    .duration(Duration.millis(500))
                    .fromValue(0.8)
                    .toValue(0.4)
                    .cycleCount(4)
                    .autoReverse(true)
                    .onFinished(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            Character.this.shieldOn = false;
                            Character.this.flipBook.getChildren().remove(Character.this.shield);
                            Character.this.shieldFade.stop();
                            Character.this.collisionBounds = Character.this.hitBounds;
                        }
                    }).build();
            this.shieldFade.playFromStart();
        }
        this.shieldOn = (!this.shieldOn);
        if (this.shieldOn) {
            this.collisionBounds = this.shield;
            this.flipBook.getChildren().add(0, this.shield);
            this.shieldFade.playFromStart();
        } else {
            this.flipBook.getChildren().remove(this.shield);
            this.shieldFade.stop();
            this.collisionBounds = this.hitBounds;
        }
    }

    public void implode(final GameWorld gameWorld) {
        this.vX = (this.vY = 0.0D);
        FadeTransitionBuilder.create()
                .node(this.node)
                .duration(Duration.millis(300))
                .fromValue(this.node.getOpacity())
                .toValue(0)
                .onFinished(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent arg0) {
                        Character.this.isDead = true;
                        gameWorld.getSceneNodes().getChildren().remove(Character.this.node);
                    }
                })
                .build()
                .play();
    }

    public void handleDeath(GameWorld gameWorld) {
        implode(gameWorld);
        super.handleDeath(gameWorld);
    }

    public boolean isImmune() {
        return this.shieldOn;
    }

    public double getWidth() {
        return getImageView().getImage().getWidth();
    }

    public double getHeight() {
        return getImageView().getImage().getHeight();
    }
}
