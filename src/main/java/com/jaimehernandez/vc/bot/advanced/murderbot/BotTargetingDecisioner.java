package com.jaimehernandez.vc.bot.advanced.murderbot;

import com.jaimehernandez.vc.bot.BotMove;
import com.jaimehernandez.vc.bot.BotUtils;
import com.jaimehernandez.vc.bot.advanced.Mine;
import com.jaimehernandez.vc.bot.advanced.Vertex;
import com.jaimehernandez.vc.dto.GameState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * Figures out who to shank
 *
 * This decisioner figures out which bot deserves it most (or is most vulnerable) and goes after them.
 *
 * On
 */
public class BotTargetingDecisioner implements Decision<AdvancedMurderBot.GameContext, BotMove> {

    private static final Logger logger = LogManager.getLogger(BotTargetingDecisioner.class);

    private final Decision<AdvancedMurderBot.GameContext, BotMove> noTargetFoundDecisioner;

    public BotTargetingDecisioner(Decision<AdvancedMurderBot.GameContext, BotMove> noTargetFoundDecisioner) {
        this.noTargetFoundDecisioner = noTargetFoundDecisioner;
    }

    @Override
    public BotMove makeDecision(AdvancedMurderBot.GameContext context) {
        logger.info("Deciding which bot to target");
        GameState.Hero me = context.getGameState().getMe();

        // Are there a crashed bot with mines we can take advantage of?
        for(Mine currentMine : context.getGameState().getMines().values()) {
            if(currentMine.getOwner() != null && currentMine.getOwner().isCrashed()) {

                // We don't want to target other bots of our type
                if(currentMine.getOwner().getName().equals("T-1000")) continue;
                if(currentMine.getOwner().getName().equals("T-800")) continue;
                if(currentMine.getOwner().getName().equals("T-900")) continue;
                if(currentMine.getOwner().getName().equals("T-3000")) continue;

                GameState.Hero target = currentMine.getOwner();
                AdvancedMurderBot.DijkstraResult currentDijkstraResult =
                        context.getDijkstraResultMap().get(target.getPos());
                GameState.Position nextPosition = target.getPos();

                while(null != currentDijkstraResult && currentDijkstraResult.getDistance() > 1) {
                    nextPosition = currentDijkstraResult.getPrevious();
                    currentDijkstraResult = context.getDijkstraResultMap().get(nextPosition);
                }

                logger.info("Going after a crashed bot");
                return BotUtils.directionTowards(currentDijkstraResult.getPrevious(), nextPosition);
            }
        }

        Set<GameState.Hero> heroesWithMines = new HashSet<>();

        // Which heroes have mines?
        for(Mine currentMine : context.getGameState().getMines().values()) {
            if(currentMine.getOwner() != null)
                heroesWithMines.add(currentMine.getOwner());
        }

        // Ok, crashed bots.  How about bots that aren't squatting?
        GameState.Hero closestTarget = null;
        AdvancedMurderBot.DijkstraResult closestTargetDijkstraResult = null;
        for(GameState.Hero currentHero : heroesWithMines) {
            AdvancedMurderBot.DijkstraResult currentDijkstraResult = context
                    .getDijkstraResultMap()
                    .get(currentHero.getPos());

            // We don't want to target bots that we cannot reach
            if(currentDijkstraResult == null)
                continue;

            // We don't want to target ourselves
            if(currentHero.getId() == context.getGameState().getMe().getId())
                continue;

            // We don't want to target other bots of our type
            // TODO We probably shouldn't hardcode this name
            if(currentHero.getName().equals("T-1000")) continue;
            if(currentHero.getName().equals("T-800")) continue;
            if(currentHero.getName().equals("T-900")) continue;
            if(currentHero.getName().equals("T-3000")) continue;

            // Are they on their spawn?
            if(currentHero.getPos().equals(currentHero.getSpawnPos()))
                continue;

            // Does he have more HP than we do?
            if(currentHero.getLife() > 20 && currentHero.getLife() > me.getLife())
                continue;
            // Does he have less than a third of my money, than he is a not a risk?
            if(currentHero.getGold() < (me.getGold() / 3))
                continue;

            // Ok, we got this far...it must not be squatting.  Is it closest?
            if (closestTarget == null) {
                closestTarget = currentHero;
                closestTargetDijkstraResult = context.getDijkstraResultMap().get(closestTarget.getPos());
                continue;
            } else if (closestTargetDijkstraResult.getDistance() >
                    currentDijkstraResult.getDistance()) {

                // Check the adjacent squares to see if a pub exists
                Vertex currentHeroVertext = context.getGameState().getBoardGraph().get(currentHero.getPos());
                for(Vertex currentVertext : currentHeroVertext.getAdjacentVertices()) {
                    if(context.getGameState().getPubs().containsKey(currentVertext.getPosition())) {
                        continue;
                    }
                }

                closestTarget = currentHero;
                closestTargetDijkstraResult = context.getDijkstraResultMap().get(closestTarget.getPos());
            }
        }

        if(closestTarget != null) {

            // Check the adjacent squares to see if a pub exists
            Vertex currentHeroVertext = context.getGameState().getBoardGraph().get(closestTarget.getPos());
            for(Vertex currentVertext : currentHeroVertext.getAdjacentVertices()) {
                if(context.getGameState().getPubs().containsKey(currentVertext.getPosition())) {
                    // Ok, no one worth attacking.
                    logger.info("No bot worth attacking.  Deferring.");
                    return noTargetFoundDecisioner.makeDecision(context);
                }
            }

            if(closestTarget.getName().equals("T-1000")) return noTargetFoundDecisioner.makeDecision(context);
            if(closestTarget.getName().equals("T-800")) return noTargetFoundDecisioner.makeDecision(context);
            if(closestTarget.getName().equals("T-900")) return noTargetFoundDecisioner.makeDecision(context);
            if(closestTarget.getName().equals("T-3000")) return noTargetFoundDecisioner.makeDecision(context);

            GameState.Position nextMove = closestTarget.getPos();
            while (closestTarget != null && closestTargetDijkstraResult.getDistance() > 1) {
                nextMove = closestTargetDijkstraResult.getPrevious();
                closestTargetDijkstraResult = context.getDijkstraResultMap().get(nextMove);
            }

            logger.info("Going after another bot");
            return BotUtils.directionTowards(closestTargetDijkstraResult.getPrevious(), nextMove);
        }

        // Ok, no one worth attacking.
        logger.info("No bot worth attacking.  Deferring.");
        return noTargetFoundDecisioner.makeDecision(context);
    }
}
