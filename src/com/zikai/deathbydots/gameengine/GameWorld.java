/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zikai.deathbydots.gameengine;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TimelineBuilder;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public abstract class GameWorld {

    private Scene gameSurface;
    private Group sceneNodes;
    private static Timeline gameLoop;
    private final int framesPerSecond;
    private final String windowTitle;
    private final SpriteManager spriteManager = new SpriteManager();

    public GameWorld(int fps, String title) {
        this.framesPerSecond = fps;
        this.windowTitle = title;

        buildAndSetGameLoop();
    }

    protected final void buildAndSetGameLoop() {
        final Duration oneFrameAmt = Duration.millis(1000 / getFramesPerSecond());
        final KeyFrame oneFrame = new KeyFrame(oneFrameAmt, new EventHandler() {
            @Override
            public void handle(Event t) {
                try {
                    GameWorld.this.updateSprites();

                    GameWorld.this.checkCollisions();

                    GameWorld.this.cleanupSprites();
                } catch (Exception e) {
                }
            }
        });

        setGameLoop(TimelineBuilder.create()
                .cycleCount(Animation.INDEFINITE)
                .keyFrames(oneFrame)
                .build());
    }

    public abstract void initialize(Stage paramStage);

    public void beginGameLoop() {
        getGameLoop().play();
    }

    protected void updateSprites() {
        for (Sprite sprite : this.spriteManager.getAllSprites()) {
            handleUpdate(sprite);
        }
    }

    protected void handleUpdate(Sprite sprite) {
    }

    protected void checkCollisions() {
        this.spriteManager.resetCollisionsToCheck();
        for (Sprite spriteA : spriteManager.getCollisionsToCheck()) {
            for (Sprite spriteB : this.spriteManager.getAllSprites()) {
                if (handleCollision(spriteA, spriteB)) {
                    break;
                }
            }
        }
    }

    protected boolean handleCollision(Sprite spriteA, Sprite spriteB) {
        return false;
    }

    protected void cleanupSprites() {
        this.spriteManager.cleanupSprites();
    }

    protected int getFramesPerSecond() {
        return this.framesPerSecond;
    }

    public String getWindowTitle() {
        return this.windowTitle;
    }

    protected static Timeline getGameLoop() {
        return gameLoop;
    }

    protected static void setGameLoop(Timeline gameLoop) {
        GameWorld.gameLoop = gameLoop;
    }

    protected SpriteManager getSpriteManager() {
        return this.spriteManager;
    }

    public Scene getGameSurface() {
        return this.gameSurface;
    }

    protected void setGameSurface(Scene gameSurface) {
        this.gameSurface = gameSurface;
    }

    public Group getSceneNodes() {
        return this.sceneNodes;
    }

    protected void setSceneNodes(Group sceneNodes) {
        this.sceneNodes = sceneNodes;
    }
}
