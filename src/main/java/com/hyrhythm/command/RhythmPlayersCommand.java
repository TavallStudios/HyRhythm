package com.hyrhythm.command;

import com.hyrhythm.player.interfaces.RhythmPlayerTargetAccess;
import com.hyrhythm.player.model.RhythmPlayerTarget;
import com.hypixel.hytale.server.core.command.system.CommandContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class RhythmPlayersCommand extends AbstractRhythmCommand implements RhythmPlayerTargetAccess {
    public RhythmPlayersCommand() {
        super("players", "List online players available for targeted rhythm commands.", false);
    }

    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext context) {
        List<RhythmPlayerTarget> players = listRhythmOnlinePlayers();
        if (players.isEmpty()) {
            sendInfo(context, "No online players are available for rhythm targeting.");
            logCommandUsage(context, "players_listed", new LinkedHashMap<>() {{
                put("onlineCount", 0);
            }});
            return completed();
        }

        sendInfo(context, "Online players: " + players.stream().map(RhythmPlayerTarget::summary).toList());
        logCommandUsage(context, "players_listed", new LinkedHashMap<>() {{
            put("onlineCount", players.size());
        }});
        return completed();
    }
}
