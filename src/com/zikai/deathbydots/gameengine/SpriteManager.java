/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zikai.deathbydots.gameengine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpriteManager {

    private static final List<Sprite> GAME_ACTORS = new ArrayList();
    private static final List<Sprite> CHECK_COLLISION_LIST = new ArrayList();
    private static final Set<Sprite> CLEAN_UP_SPRITES = new HashSet();

    public List<Sprite> getAllSprites() {
        return GAME_ACTORS;
    }

    public void addSprites(Sprite... sprites) {
        GAME_ACTORS.addAll(Arrays.asList(sprites));
    }

    public void removeSprites(Sprite... sprites) {
        GAME_ACTORS.removeAll(Arrays.asList(sprites));
    }

    public Set<Sprite> getSpritesToBeRemoved() {
        return CLEAN_UP_SPRITES;
    }

    public void addSpritesToBeRemoved(Sprite... sprites) {
        if (sprites.length > 1) {
            CLEAN_UP_SPRITES.addAll(Arrays.asList((Sprite[]) sprites));
        } else {
            CLEAN_UP_SPRITES.add(sprites[0]);
        }
    }

    public List<Sprite> getCollisionsToCheck() {
        return CHECK_COLLISION_LIST;
    }

    public void resetCollisionsToCheck() {
        CHECK_COLLISION_LIST.clear();
        CHECK_COLLISION_LIST.addAll(GAME_ACTORS);
    }

    public void cleanupSprites() {
        GAME_ACTORS.removeAll(CLEAN_UP_SPRITES);

        CLEAN_UP_SPRITES.clear();
    }
}
