package com.jaimehernandez.vc.bot.advanced;

import com.jaimehernandez.vc.dto.GameState;

/**
 * Represents a pub (tavern) on the map
 */
public class Pub {
    private final GameState.Position position;

    public Pub(GameState.Position position) {
        this.position = position;
    }

    public GameState.Position getPosition() {
        return position;
    }
}
