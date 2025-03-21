package cn.lunadeer.dominion.v1_20_1.events.environment.Move;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.flag.Flags;
import cn.lunadeer.dominion.cache.CacheManager;
import cn.lunadeer.dominion.events.PaperOnly;
import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import static cn.lunadeer.dominion.misc.Others.checkEnvironmentFlag;

@PaperOnly
public class PaperMonsterMove implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void handle(EntityPathfindEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Monster)) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(event.getLoc());
        checkEnvironmentFlag(dom, Flags.MONSTER_MOVE, event);
    }
}
