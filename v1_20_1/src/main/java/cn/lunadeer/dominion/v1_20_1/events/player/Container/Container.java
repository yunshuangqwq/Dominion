package cn.lunadeer.dominion.v1_20_1.events.player.Container;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.flag.Flags;
import cn.lunadeer.dominion.cache.CacheManager;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import static cn.lunadeer.dominion.misc.Others.checkPrivilegeFlag;

public class Container implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void handler(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        if (event.getClickedBlock().getType() != Material.CHEST &&
                event.getClickedBlock().getType() != Material.BARREL &&
                !Tag.SHULKER_BOXES.isTagged(event.getClickedBlock().getType())) {
            return;
        }
        DominionDTO dominion = CacheManager.instance.getDominion(event.getClickedBlock().getLocation());
        checkPrivilegeFlag(dominion, Flags.CONTAINER, event.getPlayer(), event);
    }
}
