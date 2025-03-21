package cn.lunadeer.dominion.v1_20_1.events.player.Container;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.flag.Flags;
import cn.lunadeer.dominion.cache.CacheManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

import static cn.lunadeer.dominion.misc.Others.checkPrivilegeFlag;

public class ArmorStand implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void handler(PlayerArmorStandManipulateEvent event) {
        DominionDTO dominion = CacheManager.instance.getDominion(event.getRightClicked().getLocation());
        checkPrivilegeFlag(dominion, Flags.CONTAINER, event.getPlayer(), event);
    }
}
