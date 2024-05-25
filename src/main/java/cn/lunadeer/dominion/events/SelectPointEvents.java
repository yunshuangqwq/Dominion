package cn.lunadeer.dominion.events;

import cn.lunadeer.dominion.Dominion;
import cn.lunadeer.minecraftpluginutils.ParticleRender;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SelectPointEvents implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void selectPoint(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Dominion.config.getTool()) {
            return;
        }
        Block block = event.getClickedBlock();
        Action action = event.getAction();
        if (block == null) {
            return;
        }

        Map<Integer, Location> points = Dominion.pointsSelect.get(player.getUniqueId());
        if (points == null) {
            points = new HashMap<>();
        }

        if (action == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            Dominion.notification.info(player, "已选择第一个点: %d %d %d", block.getX(), block.getY(), block.getZ());
            points.put(0, block.getLocation());
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            Dominion.notification.info(player, "已选择第二个点: %d %d %d", block.getX(), block.getY(), block.getZ());
            points.put(1, block.getLocation());
        } else {
            return;
        }

        if (points.size() == 2) {
            World world = points.get(0).getWorld();
            if (world == null) {
                return;
            }
            if (!points.get(0).getWorld().equals(points.get(1).getWorld())) {
                Dominion.notification.error(player, "两个点不在同一个世界");
                return;
            }
            Dominion.notification.info(player, "已选择两个点，可以使用 /dominion create <领地名称> 创建领地");
            Location loc1 = points.get(0);
            Location loc2 = points.get(1);
            int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
            int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
            int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
            int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
            int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
            int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
            if (Dominion.config.getLimitVert()) {
                minY = Dominion.config.getLimitMinY();
                maxY = Dominion.config.getLimitMaxY();
            }
            if (Dominion.config.getEconomyEnable()) {
                int count;
                if (Dominion.config.getEconomyOnlyXZ()) {
                    count = (maxX - minX) * (maxZ - minZ);
                } else {
                    count = (maxX - minX) * (maxY - minY) * (maxZ - minZ);
                }
                float price = count * Dominion.config.getEconomyPrice();
                Dominion.notification.info(player, "预计领地创建价格为 %.2f %s", price, Dominion.vault.getEconomy().currencyNamePlural());
            }
            ParticleRender.showBoxFace(Dominion.instance, player, loc1, loc2);
            Dominion.notification.info(player, "尺寸： %d x %d x %d",
                    Math.abs(points.get(1).getBlockX() - points.get(0).getBlockX()),
                    Math.abs(points.get(1).getBlockY() - points.get(0).getBlockY()),
                    Math.abs(points.get(1).getBlockZ() - points.get(0).getBlockZ()));
            Dominion.notification.info(player, "面积： %d",
                    Math.abs(points.get(1).getBlockX() - points.get(0).getBlockX()) *
                            Math.abs(points.get(1).getBlockZ() - points.get(0).getBlockZ()));
            Dominion.notification.info(player, "高度： %d",
                    Math.abs(points.get(1).getBlockY() - points.get(0).getBlockY()));
            Dominion.notification.info(player, "体积： %d",
                    Math.abs(points.get(1).getBlockX() - points.get(0).getBlockX()) *
                            Math.abs(points.get(1).getBlockY() - points.get(0).getBlockY()) *
                            Math.abs(points.get(1).getBlockZ() - points.get(0).getBlockZ()));
        }
        Dominion.pointsSelect.put(player.getUniqueId(), points);
    }
}
