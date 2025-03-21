package cn.lunadeer.dominion.v1_20_1.events.environment.TNTExplode;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.flag.Flags;
import cn.lunadeer.dominion.cache.CacheManager;
import cn.lunadeer.dominion.events.HighestVersion;
import cn.lunadeer.dominion.utils.XVersionManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import static cn.lunadeer.dominion.misc.Others.checkEnvironmentFlag;

@HighestVersion(XVersionManager.ImplementationVersion.v1_20_1)
public class EntityExploded implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void handler(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity harmer = event.getDamager();
        if (harmer.getType() != EntityType.MINECART_TNT && harmer.getType() != EntityType.PRIMED_TNT) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(entity.getLocation());
        checkEnvironmentFlag(dom, Flags.TNT_EXPLODE, event);
    }
}
