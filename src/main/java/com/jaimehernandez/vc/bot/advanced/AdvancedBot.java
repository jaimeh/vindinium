package com.jaimehernandez.vc.bot.advanced;

import com.jaimehernandez.vc.bot.BotMove;

public interface AdvancedBot {

    public BotMove move(AdvancedGameState gameState);

    /**
     * Called before the game is started
     */
    public void setup();

    /**
     * Called after the game
     */
    public void shutdown();

}
