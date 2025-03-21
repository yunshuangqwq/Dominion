package cn.lunadeer.dominion.v1_20_1.events.environment;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.flag.Flags;
import cn.lunadeer.dominion.cache.CacheManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import static cn.lunadeer.dominion.misc.Others.checkEnvironmentFlag;

public class VillagerSpawn implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void handler(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.VILLAGER) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(entity.getLocation());
        checkEnvironmentFlag(dom, Flags.VILLAGER_SPAWN, event);
    }
}
