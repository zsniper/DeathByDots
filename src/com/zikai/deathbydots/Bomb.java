/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zikai.deathbydots;

import javafx.animation.FadeTransition;
import javafx.animation.FadeTransitionBuilder;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CircleBuilder;
import javafx.util.Duration;
import com.zikai.deathbydots.gameengine.GameWorld;
import com.zikai.deathbydots.gameengine.Sprite;

public class Bomb
        extends Sprite {

    private boolean imploding = false;

    public boolean isImploding() {
        return this.imploding;
    }

    public void setImploding(boolean isImploding) {
        this.imploding = isImploding;
    }

    public Bomb(double radius, Color fill) {
        Circle sphere = ((CircleBuilder) CircleBuilder.create().centerX(radius).centerY(radius).radius(radius).cache(true)).build();

        sphere.setFill(fill);

        this.node = sphere;
        this.collisionBounds = sphere;
    }

    public void update() {
        this.node.setTranslateX(this.node.getTranslateX() + this.vX);
        this.node.setTranslateY(this.node.getTranslateY() + this.vY);
    }

    public Circle getAsCircle() {
        return (Circle) this.node;
    }

    public void implode(final GameWorld gameWorld) {
        setImploding(true);
        final Circle explosionBounds = ((CircleBuilder) ((CircleBuilder) ((CircleBuilder) ((CircleBuilder) CircleBuilder.create().radius(100.0D).strokeWidth(5.0D)).stroke(Color.DARKBLUE)).fill(Color.BLUE)).centerX(this.node.getTranslateX()).centerY(this.node.getTranslateY()).opacity(0.6D)).build();

        this.collisionBounds = explosionBounds;
        gameWorld.getSceneNodes().getChildren().remove(this.node);

        gameWorld.getSceneNodes().getChildren().add(1, explosionBounds);

        this.vX = (this.vY = 0.0D);
        
        FadeTransition shieldFade = FadeTransitionBuilder.create()
                .fromValue(0.5)
                .toValue(0.0)
                .duration(Duration.millis(200.0D))
                .node(explosionBounds)
                .onFinished(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent arg0) {
                        Bomb.this.isDead = true;
                        gameWorld.getSceneNodes().getChildren().remove(explosionBounds);
                    }
                }).build();
        shieldFade.playFromStart();
        super.handleDeath(gameWorld);
    }

    public void handleDeath(final GameWorld gameWorld) {
        this.vX = (this.vY = 0.0D);
        FadeTransitionBuilder.create()
                .node(this.node)
                .duration(Duration.millis(300.0D))
                .fromValue(this.node.getOpacity())
                .toValue(0.0D)
                .onFinished(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent arg0) {
                        Bomb.this.isDead = true;
                        gameWorld.getSceneNodes().getChildren().remove(Bomb.this.node);
                    }
                })
                .build()
                .play();
        super.handleDeath(gameWorld);
    }
}
