package cn.lunadeer.dominion.v1_20_1.events.player.Anchor;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.flag.Flags;
import cn.lunadeer.dominion.cache.CacheManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import static cn.lunadeer.dominion.misc.Others.checkPrivilegeFlag;

public class Respawn implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void handler(PlayerRespawnEvent event) {
        Player bukkitPlayer = event.getPlayer();
        if (!event.isAnchorSpawn()) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(event.getRespawnLocation());
        if (!checkPrivilegeFlag(dom, Flags.ANCHOR, bukkitPlayer, null)) {
            if (bukkitPlayer.getBedSpawnLocation() != null) {
                event.setRespawnLocation(bukkitPlayer.getBedSpawnLocation());
            } else {
                event.setRespawnLocation(bukkitPlayer.getWorld().getSpawnLocation());
            }
        }
    }
}
