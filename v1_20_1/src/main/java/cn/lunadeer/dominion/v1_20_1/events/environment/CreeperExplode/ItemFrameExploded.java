package cn.lunadeer.dominion.v1_20_1.events.environment.CreeperExplode;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.flag.Flags;
import cn.lunadeer.dominion.cache.CacheManager;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;

import static cn.lunadeer.dominion.misc.Others.checkEnvironmentFlag;
import static cn.lunadeer.dominion.misc.Others.isExplodeEntity;

public class ItemFrameExploded implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void handle(HangingBreakByEntityEvent event) {
        Entity entity = event.getEntity();
        if (event.getCause() != HangingBreakEvent.RemoveCause.EXPLOSION) {
            return;
        }
        Entity remover = event.getRemover();
        if (!isExplodeEntity(remover)) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(entity.getLocation());
        checkEnvironmentFlag(dom, Flags.CREEPER_EXPLODE, event);
    }
}
