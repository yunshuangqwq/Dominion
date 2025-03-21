package cn.lunadeer.dominion.v1_20_1.events.player.Break;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.flag.Flags;
import cn.lunadeer.dominion.cache.CacheManager;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import static cn.lunadeer.dominion.misc.Others.checkPrivilegeFlag;

public class ArmorStandShot implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void handler(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        if (!(victim instanceof ArmorStand)) {
            return;
        }
        Entity attacker = event.getDamager();
        if (!(attacker instanceof Projectile projectile)) {
            return;
        }
        if (!(projectile.getShooter() instanceof Player player)) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(victim.getLocation());
        checkPrivilegeFlag(dom, Flags.BREAK_BLOCK, player, event);
    }
}
