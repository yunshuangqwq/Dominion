package cn.lunadeer.dominion.v1_20_1.events;

import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.flag.Flags;
import cn.lunadeer.dominion.cache.CacheManager;
import cn.lunadeer.dominion.utils.XLogger;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static cn.lunadeer.dominion.misc.Others.checkEnvironmentFlag;
import static cn.lunadeer.dominion.misc.Others.isExplodeEntity;
import static org.bukkit.Material.FARMLAND;

public class EnvironmentEvents implements Listener {
    @EventHandler(priority = EventPriority.LOWEST) // creeper_explode
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        XLogger.debug("EntityExplodeEvent: " + entity.getType());
        if (!isExplodeEntity(entity)) {
            return;
        }
        XLogger.debug("blockList" + event.blockList().size());
        event.blockList().removeIf(block -> {
            DominionDTO dom = CacheManager.instance.getDominion(block.getLocation());
            return !checkEnvironmentFlag(dom, Flags.CREEPER_EXPLODE, null);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST) // creeper_explode - bed anchor
    public void onBedAnchorExplosion(BlockExplodeEvent event) {
        BlockState block = event.getExplodedBlockState();
        if (block == null) {
            return;
        }
        event.blockList().removeIf(blockState -> {
            DominionDTO dom = CacheManager.instance.getDominion(blockState.getLocation());
            return !checkEnvironmentFlag(dom, Flags.CREEPER_EXPLODE, null);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST) // creeper_explode - item frame
    public void onItemFrameExploded(HangingBreakByEntityEvent event) {
        Entity entity = event.getEntity();
        if (event.getCause() != HangingBreakEvent.RemoveCause.EXPLOSION) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(entity.getLocation());
        checkEnvironmentFlag(dom, Flags.CREEPER_EXPLODE, event);
    }

    @EventHandler(priority = EventPriority.LOWEST) // creeper_explode - item frame
    public void onItemFrameShot(ProjectileHitEvent event) {
        Entity hit = event.getHitEntity();
        if (hit == null) {
            return;
        }
        if (event.getEntity().getShooter() instanceof Player) {
            return;
        }
        if (!(hit instanceof Hanging)) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(hit.getLocation());
        checkEnvironmentFlag(dom, Flags.CREEPER_EXPLODE, event);
    }

    @EventHandler(priority = EventPriority.LOWEST) // creeper_explode - armor stand
    public void onArmorStandExploded(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.ARMOR_STAND) {
            return;
        }
        if (isExplodeEntity(event.getDamager())) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(entity.getLocation());
        checkEnvironmentFlag(dom, Flags.CREEPER_EXPLODE, event);
    }

    @EventHandler(priority = EventPriority.LOWEST) // item_frame_proj_damage
    public void removeSomeOnItemFrameByArrow(HangingBreakByEntityEvent event) {
        if (event.getCause() != HangingBreakEvent.RemoveCause.ENTITY) {
            return;
        }
        Entity remover = event.getRemover();
        if (!(remover instanceof Projectile projectile)) {
            return;
        }
        if (!(projectile.getShooter() instanceof Player)) {
            // 玩家破坏由 玩家 break 权限控制
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(event.getEntity().getLocation());
        checkEnvironmentFlag(dom, Flags.ITEM_FRAME_PROJ_DAMAGE, event);
    }

    @EventHandler(priority = EventPriority.LOWEST) // dragon_break_block
    public void onDragonBreakBlock(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.ENDER_DRAGON) {
            return;
        }
        event.blockList().removeIf(block -> {
            DominionDTO dom = CacheManager.instance.getDominion(block.getLocation());
            return !checkEnvironmentFlag(dom, Flags.DRAGON_BREAK_BLOCK, null);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST) // fire_spread
    public void onFireSpread(BlockIgniteEvent event) {
        Player player = event.getPlayer();
        if (player != null) {
            // 如果点燃事件没有玩家触发，那么就是火焰蔓延
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(event.getBlock().getLocation());
        checkEnvironmentFlag(dom, Flags.FIRE_SPREAD, event);
    }

    @EventHandler(priority = EventPriority.LOWEST) // flow_in_protection
    public void onLiquidFlowIn(BlockFromToEvent event) {
        Location from = event.getBlock().getLocation();
        Location to = event.getToBlock().getLocation();
        DominionDTO dom_to = CacheManager.instance.getDominion(to);
        if (dom_to == null) {
            return;
        }
        DominionDTO dom_from = CacheManager.instance.getDominion(from);
        if (dom_from != null) {
            if (Objects.equals(dom_from.getId(), dom_to.getId())) {
                return;
            }
        }
        checkEnvironmentFlag(dom_to, Flags.FLOW_IN_PROTECTION, event);
    }

    @EventHandler(priority = EventPriority.LOWEST) // mob_drop_item
    public void onMobDropItem(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(entity.getLocation());
        if (dom == null) {
            return;
        }
        if (!Flags.MOB_DROP_ITEM.getEnable()) {
            return;
        }
        if (dom.getEnvironmentFlagValue().get(Flags.MOB_DROP_ITEM)) {
            return;
        }
        event.getDrops().clear();
    }

    @EventHandler(priority = EventPriority.LOWEST) // tnt_explode
    public void onTntExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.MINECART_TNT && entity.getType() != EntityType.PRIMED_TNT) {
            return;
        }
        event.blockList().removeIf(block -> {
            DominionDTO dom = CacheManager.instance.getDominion(block.getLocation());
            return !checkEnvironmentFlag(dom, Flags.TNT_EXPLODE, null);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST) // tnt_explode - entity
    public void onArmorStandExplodedByTnt(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity harmer = event.getDamager();
        if (harmer.getType() != EntityType.MINECART_TNT && harmer.getType() != EntityType.PRIMED_TNT) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(entity.getLocation());
        checkEnvironmentFlag(dom, Flags.TNT_EXPLODE, event);
    }

    @EventHandler(priority = EventPriority.LOWEST) // trample player
    public void onFarmlandTrample(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        if (block.getType() != FARMLAND) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(block.getLocation());
        checkEnvironmentFlag(dom, Flags.TRAMPLE, event);
    }

    @EventHandler(priority = EventPriority.LOWEST) // trample mob
    public void onFarmlandTrample(EntityInteractEvent event) {
        Block block = event.getBlock();
        if (block.getType() != FARMLAND) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(block.getLocation());
        checkEnvironmentFlag(dom, Flags.TRAMPLE, event);
    }

    /*
    TRIG_PRESSURE_PROJ("trig_pressure_proj", "投掷物触发压力板", "投掷物（箭/风弹/雪球）是否可以触发压力板", false, true, true),
    TRIG_PRESSURE_MOB("trig_pressure_mob", "生物触发压力板", "生物（不包含玩家）是否可以触发压力板", false, true, true),
    TRIG_PRESSURE_DROP("trig_pressure_drop", "掉落物触发压力板", "掉落物是否可以触发压力板", false, true, true),
     */
    @EventHandler(priority = EventPriority.LOWEST) // trig_pressure_proj
    public void onPressurePlateTriggeredByProjectile(EntityInteractEvent event) {
        if (!(event.getEntity() instanceof Projectile)) {
            return;
        }
        Block block = event.getBlock();
        if (!Tag.PRESSURE_PLATES.isTagged(block.getType())) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(block.getLocation());
        checkEnvironmentFlag(dom, Flags.TRIG_PRESSURE_PROJ, event);
    }

    @EventHandler(priority = EventPriority.LOWEST) // trig_pressure_mob
    public void onPressurePlateTriggeredByMob(EntityInteractEvent event) {
        if (!(event.getEntity() instanceof Mob)) {
            return;
        }
        Block block = event.getBlock();
        if (!Tag.PRESSURE_PLATES.isTagged(block.getType())) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(block.getLocation());
        checkEnvironmentFlag(dom, Flags.TRIG_PRESSURE_MOB, event);
    }

    @EventHandler(priority = EventPriority.LOWEST) // trig_pressure_drop
    public void onPressurePlateTriggeredByDrop(EntityInteractEvent event) {
        if (!(event.getEntity() instanceof Item)) {
            return;
        }
        Block block = event.getBlock();
        if (!Tag.PRESSURE_PLATES.isTagged(block.getType())) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(block.getLocation());
        checkEnvironmentFlag(dom, Flags.TRIG_PRESSURE_DROP, event);
    }

    @EventHandler(priority = EventPriority.LOWEST) // wither_spawn
    public void onWitherSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.WITHER) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(entity.getLocation());
        checkEnvironmentFlag(dom, Flags.WITHER_SPAWN, event);
    }

    @EventHandler(priority = EventPriority.LOWEST) // wither_spawn - explode
    public void onWitherSpawnExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.WITHER) {
            return;
        }
        event.blockList().removeIf(block -> {
            DominionDTO dom = CacheManager.instance.getDominion(block.getLocation());
            return !checkEnvironmentFlag(dom, Flags.WITHER_SPAWN, null);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST) // ender_man spawn
    public void onEnderManSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.ENDERMAN) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(entity.getLocation());
        checkEnvironmentFlag(dom, Flags.ENDER_MAN, event);
    }

    @EventHandler(priority = EventPriority.LOWEST) // ender_man escape
    public void onEnderManEscape(EntityTeleportEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.ENDERMAN) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(entity.getLocation());
        checkEnvironmentFlag(dom, Flags.ENDER_MAN, event);
        if (event.getTo() != null) {
            DominionDTO domTo = CacheManager.instance.getDominion(event.getTo());
            checkEnvironmentFlag(domTo, Flags.ENDER_MAN, event);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST) // monster_spawn
    public void onMonsterSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Enemy)) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(entity.getLocation());
        checkEnvironmentFlag(dom, Flags.MONSTER_SPAWN, event);
    }

    @EventHandler(priority = EventPriority.LOWEST) // monster_damage
    public void onMonsterDamageToPlayer(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (!(damager instanceof Enemy)) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(damager.getLocation());
        checkEnvironmentFlag(dom, Flags.MONSTER_DAMAGE, event);
    }

    @EventHandler(priority = EventPriority.LOWEST) // animal_spawn
    public void onAnimalSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Animals)) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(entity.getLocation());
        checkEnvironmentFlag(dom, Flags.ANIMAL_SPAWN, event);
    }

    @EventHandler(priority = EventPriority.LOWEST) // villager_spawn
    public void onVillagerSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.VILLAGER) {
            return;
        }
        DominionDTO dom = CacheManager.instance.getDominion(entity.getLocation());
        checkEnvironmentFlag(dom, Flags.VILLAGER_SPAWN, event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHopper(InventoryMoveItemEvent event) {    // hopper_outside
        Inventory hopper = event.getDestination();
        Inventory inventory = event.getSource();
        if (hopper.getLocation() == null || inventory.getLocation() == null) {
            return;
        }
        DominionDTO hopperDom = CacheManager.instance.getDominion(hopper.getLocation());
        DominionDTO inventoryDom = CacheManager.instance.getDominion(inventory.getLocation());
        if (hopperDom == null && inventoryDom != null) {
            checkEnvironmentFlag(inventoryDom, Flags.HOPPER_OUTSIDE, event);
        }
        if (hopperDom != null && inventoryDom != null) {
            if (!hopperDom.getId().equals(inventoryDom.getId())) {
                checkEnvironmentFlag(inventoryDom, Flags.HOPPER_OUTSIDE, event);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPushedByPiston(BlockPistonExtendEvent event) {   // piston_outside
        Block piston = event.getBlock();
        DominionDTO pistonDom = CacheManager.instance.getDominion(piston.getLocation());
        BlockFace direction = event.getDirection();
        Block endBlockAfterPush = piston.getRelative(direction, event.getBlocks().size() + 1);
        DominionDTO endBlockDom = CacheManager.instance.getDominion(endBlockAfterPush.getLocation());
        if (pistonDom != null && endBlockDom == null) {
            checkEnvironmentFlag(pistonDom, Flags.PISTON_OUTSIDE, event);
        }
        if (pistonDom == null && endBlockDom != null) {
            checkEnvironmentFlag(endBlockDom, Flags.PISTON_OUTSIDE, event);
        }
        if (pistonDom != null && endBlockDom != null) {
            if (!pistonDom.getId().equals(endBlockDom.getId())) {
                if (!endBlockDom.getEnvFlagValue(Flags.PISTON_OUTSIDE) || !pistonDom.getEnvFlagValue(Flags.PISTON_OUTSIDE)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onGravityBlockFalling(EntityChangeBlockEvent event) {   // gravity_block
        Entity entity = event.getEntity();
        if (!(entity instanceof FallingBlock)) {
            return;
        }
        Block block = event.getBlock();
        if (event.getTo().isAir()) {
            fallingBlockMap.put(entity.getUniqueId(), block.getLocation());
        } else {
            Location locStart = fallingBlockMap.get(entity.getUniqueId());
            if (locStart == null) {
                return;
            }
            fallingBlockMap.remove(entity.getUniqueId());
            Location locEnd = block.getLocation();
            DominionDTO domStart = CacheManager.instance.getDominion(locStart);
            DominionDTO domEnd = CacheManager.instance.getDominion(locEnd);
            if (domEnd == null) {
                return;
            }
            if (domStart != null && domStart.getId().equals(domEnd.getId())) {
                return;
            }
            if (!checkEnvironmentFlag(domEnd, Flags.GRAVITY_BLOCK, null)) {
                event.setCancelled(true);
                locEnd.getWorld().dropItemNaturally(locEnd, new ItemStack(((FallingBlock) entity).getBlockData().getMaterial()));
                entity.remove();
            }
        }
    }

    private static final Map<UUID, Location> fallingBlockMap = new java.util.HashMap<>();
}
