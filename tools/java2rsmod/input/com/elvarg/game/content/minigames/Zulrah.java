package com.elvarg.game.content.minigames.impl;

import com.elvarg.game.World;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.impl.ZulrahArea;
import com.elvarg.game.model.teleportation.TeleportHandler;
import com.elvarg.game.model.teleportation.TeleportType;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;

public class Zulrah {

    public static final Location ENTRANCE = new Location(3075, 3248);
    public static final Location EXIT = new Location(3075, 3248);
    private static final Location ZULRAH_SPAWN_POS = new Location(3075, 3248);

    public static void start(Player player) {
        final ZulrahArea area = new ZulrahArea();
        TeleportHandler.teleport(player, ENTRANCE, TeleportType.NORMAL, false);
        area.add(player);
        TaskManager.submit(new Task(14, player, false) {
            @Override
            protected void execute() {
                stop();
                if (area.isDestroyed()) {
                    return;
                }
                World.getAddNPCQueue().add(new com.elvarg.game.entity.impl.npc.impl.Zulrah(2042, ZULRAH_SPAWN_POS.clone()));
            }
        });
    }
}