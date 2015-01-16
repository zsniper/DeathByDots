/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zikai.deathbydots;

import javafx.application.Application;
import javafx.stage.Stage;
import com.zikai.deathbydots.gameengine.GameWorld;

public class GameLoop
        extends Application {

    DeathByDots gameWorld = new DeathByDots(60, "Death By Dots");

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        this.gameWorld.initialize(primaryStage);

        primaryStage.show();
    }
}
