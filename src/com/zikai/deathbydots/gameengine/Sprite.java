/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zikai.deathbydots.gameengine;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.shape.Circle;

public abstract class Sprite {

    public Node node;
    public double vX = 0.0D;
    public double vY = 0.0D;
    public boolean isDead = false;
    public Circle collisionBounds;

    public abstract void update();

    public boolean collide(Sprite other) {
        if ((this.collisionBounds == null) || (other.collisionBounds == null)) {
            return false;
        }
        Circle otherSphere = other.collisionBounds;
        Circle thisSphere = this.collisionBounds;
        Point2D otherCenter = otherSphere.localToScene(otherSphere.getCenterX(), otherSphere.getCenterY());
        Point2D thisCenter = thisSphere.localToScene(thisSphere.getCenterX(), thisSphere.getCenterY());
        double dx = otherCenter.getX() - thisCenter.getX();
        double dy = otherCenter.getY() - thisCenter.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        double minDist = otherSphere.getRadius() + thisSphere.getRadius();

        return distance < minDist;
    }

    public void handleDeath(GameWorld gameWorld) {
        gameWorld.getSpriteManager().addSpritesToBeRemoved(new Sprite[]{this});
    }
}
