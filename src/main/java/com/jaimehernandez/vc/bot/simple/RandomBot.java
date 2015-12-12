package com.jaimehernandez.vc.bot.simple;

import com.jaimehernandez.vc.bot.BotMove;
import com.jaimehernandez.vc.dto.GameState;

/**
 * Example bot
 */
public class RandomBot implements SimpleBot {
    @Override
    public BotMove move(GameState gameState) {
        int randomNumber = (int)(Math.random() * 4);

        switch(randomNumber) {
            case 1:
                return BotMove.NORTH;
            case 2:
                return BotMove.SOUTH;
            case 3:
                return BotMove.EAST;
            case 4:
                return BotMove.WEST;
            default:
                return BotMove.STAY;
        }
    }

    @Override
    public void setup() {
        // No-op
    }

    @Override
    public void shutdown() {
        // No-op
    }
}
