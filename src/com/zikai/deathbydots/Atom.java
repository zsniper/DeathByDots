/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zikai.deathbydots;

import com.zikai.deathbydots.gameengine.GameWorld;
import com.zikai.deathbydots.gameengine.Sprite;
import javafx.animation.FadeTransitionBuilder;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CircleBuilder;
import javafx.util.Duration;

public class Atom
        extends Sprite {

    public Atom(double radius, Color fill) {
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
        this.vX = (this.vY = 0.0D);
        FadeTransitionBuilder.create()
                .node(this.node)
                .duration(Duration.millis(300.0D))
                .fromValue(this.node.getOpacity())
                .toValue(0.0D)
                .onFinished(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent arg0) {
                        Atom.this.isDead = true;
                        gameWorld.getSceneNodes().getChildren().remove(Atom.this.node);
                    }
                }).build().play();
    }

    public void handleDeath(GameWorld gameWorld) {
        implode(gameWorld);
        super.handleDeath(gameWorld);
    }
}
