package cn.lunadeer.dominion.v1_20_1.events.player;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.flag.Flags;
import cn.lunadeer.dominion.cache.CacheManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;

import static cn.lunadeer.dominion.misc.Others.checkPrivilegeFlag;

public class Shear implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void handler(PlayerShearEntityEvent event) {
        Player player = event.getPlayer();
        DominionDTO dom = CacheManager.instance.getDominion(event.getEntity().getLocation());
        checkPrivilegeFlag(dom, Flags.SHEAR, player, event);
    }
}
