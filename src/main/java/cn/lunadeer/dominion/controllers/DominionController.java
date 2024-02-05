package cn.lunadeer.dominion.controllers;

import cn.lunadeer.dominion.Dominion;
import cn.lunadeer.dominion.dtos.DominionDTO;
import cn.lunadeer.dominion.utils.Database;
import cn.lunadeer.dominion.utils.Notification;
import cn.lunadeer.dominion.utils.Time;
import cn.lunadeer.dominion.utils.XLogger;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class DominionController {

    /**
     * 创建领地
     *
     * @param owner 拥有者
     * @param name  领地名称
     * @param loc1  位置1
     * @param loc2  位置2
     * @return 创建的领地
     */
    public static DominionDTO create(Player owner, String name, Location loc1, Location loc2) {
        return create(owner, name, loc1, loc2, "");
    }

    /**
     * 创建子领地
     *
     * @param owner                拥有者
     * @param name                 领地名称
     * @param loc1                 位置1
     * @param loc2                 位置2
     * @param parent_dominion_name 父领地名称
     * @return 创建的领地
     */
    public static DominionDTO create(Player owner, String name,
                                     Location loc1, Location loc2,
                                     String parent_dominion_name) {
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            Notification.error(owner, "禁止跨世界操作");
            return null;
        }
        if (!owner.getWorld().equals(loc1.getWorld())) {
            Notification.error(owner, "禁止跨世界操作");
            return null;
        }
        DominionDTO dominion = new DominionDTO(owner.getUniqueId(), name, owner.getWorld().getName(),
                (int) Math.min(loc1.getX(), loc2.getX()), (int) Math.min(loc1.getY(), loc2.getY()),
                (int) Math.min(loc1.getZ(), loc2.getZ()), (int) Math.max(loc1.getX(), loc2.getX()),
                (int) Math.max(loc1.getY(), loc2.getY()), (int) Math.max(loc1.getZ(), loc2.getZ()));
        DominionDTO parent_dominion;
        if (parent_dominion_name.isEmpty()) {
            parent_dominion = DominionDTO.select(-1);
        } else {
            parent_dominion = DominionDTO.select(parent_dominion_name);
        }
        if (parent_dominion == null) {
            Notification.error(owner, "父领地 " + parent_dominion_name + " 不存在");
            if (parent_dominion_name.isEmpty()) {
                XLogger.err("根领地丢失！");
            }
            return null;
        }
        // 是否是父领地的拥有者
        if (!isOwner(owner, parent_dominion) && parent_dominion.getId() != -1) {
            return null;
        }
        // 如果parent_dominion不为-1 检查是否在同一世界
        if (parent_dominion.getId() != -1 && !parent_dominion.getWorld().equals(dominion.getWorld())) {
            Notification.error(owner, "禁止跨世界操作");
            return null;
        }
        // 检查是否超出父领地范围
        if (!isContained(dominion, parent_dominion)) {
            Notification.error(owner, "超出父领地 " + parent_dominion_name + " 范围");
            return null;
        }
        // 获取此父领地的所有子领地
        List<DominionDTO> sub_dominions = DominionDTO.selectByParentId(dominion.getWorld(), parent_dominion.getId());
        // 检查是否与其他子领地冲突
        for (DominionDTO sub_dominion : sub_dominions) {
            if (isIntersect(sub_dominion, dominion)) {
                Notification.error(owner, "与领地 " + sub_dominion.getName() + " 冲突");
                return null;
            }
        }
        dominion = DominionDTO.insert(dominion);
        if (dominion == null) {
            Notification.error(owner, "创建失败，详细错误请联系管理员查询日志（当前时间：" + Time.nowStr() + "）");
            return null;
        }
        return dominion.setParentDomId(parent_dominion.getId());
    }

    /**
     * 向一个方向扩展领地
     * 会尝试对操作者当前所在的领地进行操作，当操作者不在一个领地内或者在子领地内时
     * 需要手动指定要操作的领地名称
     *
     * @param operator 操作者
     * @param size     扩展的大小
     * @return 扩展后的领地
     */
    public static DominionDTO expand(Player operator, Integer size) {
        Location location = operator.getLocation();
        List<DominionDTO> dominions = DominionDTO.selectByLocation(location.getWorld().getName(),
                (int) location.getX(), (int) location.getY(), (int) location.getZ());
        if (dominions.size() != 1) {
            Notification.error(operator, "你不在一个领地内或在一个子领地内，无法确定你要操作的领地，请手动指定要操作的领地名称");
            return null;
        }
        return expand(operator, size, dominions.get(0).getName());
    }

    /**
     * 向一个方向扩展领地
     *
     * @param operator      操作者
     * @param size          扩展的大小
     * @param dominion_name 领地名称
     * @return 扩展后的领地
     */
    public static DominionDTO expand(Player operator, Integer size, String dominion_name) {
        Location location = operator.getLocation();
        BlockFace face = operator.getFacing();
        DominionDTO dominion = DominionDTO.select(dominion_name);
        if (dominion == null) {
            Notification.error(operator, "领地 " + dominion_name + " 不存在");
            return null;
        }
        if (!isOwner(operator, dominion)) {
            return null;
        }
        if (!location.getWorld().getName().equals(dominion.getWorld())) {
            Notification.error(operator, "禁止跨世界操作");
            return null;
        }
        Integer x1 = dominion.getX1();
        Integer y1 = dominion.getY1();
        Integer z1 = dominion.getZ1();
        Integer x2 = dominion.getX2();
        Integer y2 = dominion.getY2();
        Integer z2 = dominion.getZ2();
        switch (face) {
            case NORTH:
                z1 -= size;
                break;
            case SOUTH:
                z2 += size;
                break;
            case WEST:
                x1 -= size;
                break;
            case EAST:
                x2 += size;
                break;
            case UP:
                y2 += size;
                break;
            case DOWN:
                y1 -= size;
                break;
            default:
                Notification.error(operator, "无效的方向");
                return null;
        }
        // 校验是否超出父领地范围
        DominionDTO parent_dominion = DominionDTO.select(dominion.getParentDomId());
        if (parent_dominion == null) {
            Notification.error(operator, "父领地丢失");
            return null;
        }
        if (!isContained(x1, y1, z1, x2, y2, z2, parent_dominion)) {
            Notification.error(operator, "超出父领地 " + parent_dominion.getName() + " 范围");
            return null;
        }
        // 获取同世界下的所有同级领地
        List<DominionDTO> exist_dominions = DominionDTO.selectByParentId(dominion.getWorld(), dominion.getParentDomId());
        for (DominionDTO exist_dominion : exist_dominions) {
            if (isIntersect(exist_dominion, x1, y1, z1, x2, y2, z2)) {
                // 如果是自己，跳过
                if (exist_dominion.getId().equals(dominion.getId())) continue;
                Notification.error(operator, "与 " + exist_dominion.getName() + " 冲突");
                return null;
            }
        }
        return dominion.setXYZ(x1, y1, z1, x2, y2, z2);
    }

    /**
     * 缩小领地
     * 会尝试对操作者当前所在的领地进行操作，当操作者不在一个领地内或者在子领地内时
     * 需要手动指定要操作的领地名称
     *
     * @param operator 操作者
     * @param size     缩小的大小
     * @return 缩小后的领地
     */
    public static DominionDTO contract(Player operator, Integer size) {
        Location location = operator.getLocation();
        List<DominionDTO> dominions = DominionDTO.selectByLocation(location.getWorld().getName(),
                (int) location.getX(), (int) location.getY(), (int) location.getZ());
        if (dominions.size() != 1) {
            Notification.error(operator, "你不在一个领地内或在子领地内，无法确定你要操作的领地，请手动指定要操作的领地名称");
            return null;
        }
        return contract(operator, size, dominions.get(0).getName());
    }

    /**
     * 缩小领地
     *
     * @param operator      操作者
     * @param size          缩小的大小
     * @param dominion_name 领地名称
     * @return 缩小后的领地
     */
    public static DominionDTO contract(Player operator, Integer size, String dominion_name) {
        Location location = operator.getLocation();
        BlockFace face = operator.getFacing();
        DominionDTO dominion = DominionDTO.select(dominion_name);
        if (dominion == null) {
            Notification.error(operator, "领地 " + dominion_name + " 不存在");
            return null;
        }
        if (!isOwner(operator, dominion)) {
            return null;
        }
        if (!location.getWorld().getName().equals(dominion.getWorld())) {
            Notification.error(operator, "禁止跨世界操作");
            return null;
        }
        Integer x1 = dominion.getX1();
        Integer y1 = dominion.getY1();
        Integer z1 = dominion.getZ1();
        Integer x2 = dominion.getX2();
        Integer y2 = dominion.getY2();
        Integer z2 = dominion.getZ2();
        switch (face) {
            case NORTH:
                z2 -= size;
                break;
            case SOUTH:
                z1 += size;
                break;
            case WEST:
                x2 -= size;
                break;
            case EAST:
                x1 += size;
                break;
            case UP:
                y2 -= size;
                break;
            case DOWN:
                y1 += size;
                break;
            default:
                Notification.error(operator, "无效的方向");
                return null;
        }
        // 校验第二组坐标是否小于第一组坐标
        if (x1 >= x2 || y1 >= y2 || z1 >= z2) {
            Notification.error(operator, "缩小后的领地无效");
            return null;
        }
        // 获取所有的子领地
        List<DominionDTO> sub_dominions = DominionDTO.selectByParentId(dominion.getWorld(), dominion.getId());
        for (DominionDTO sub_dominion : sub_dominions) {
            if (!isContained(sub_dominion, x1, y1, z1, x2, y2, z2)) {
                Notification.error(operator, "缩小后的领地 " + dominion_name + " 无法包含子领地 " + sub_dominion.getName());
                return null;
            }
        }
        return dominion.setXYZ(x1, y1, z1, x2, y2, z2);
    }


    /**
     * 判断两个领地是否相交
     */
    private static boolean isIntersect(DominionDTO a, DominionDTO b) {
        return a.getX1() < b.getX2() && a.getX2() > b.getX1() &&
                a.getY1() < b.getY2() && a.getY2() > b.getY1() &&
                a.getZ1() < b.getZ2() && a.getZ2() > b.getZ1();
    }

    private static boolean isIntersect(DominionDTO a, Integer x1, Integer y1, Integer z1, Integer x2, Integer y2, Integer z2) {
        return a.getX1() < x2 && a.getX2() > x1 &&
                a.getY1() < y2 && a.getY2() > y1 &&
                a.getZ1() < z2 && a.getZ2() > z1;
    }

    /**
     * 判断 sub 是否完全被 parent 包裹
     */
    private static boolean isContained(DominionDTO sub, DominionDTO parent) {
        return sub.getX1() >= parent.getX1() && sub.getX2() <= parent.getX2() &&
                sub.getY1() >= parent.getY1() && sub.getY2() <= parent.getY2() &&
                sub.getZ1() >= parent.getZ1() && sub.getZ2() <= parent.getZ2();
    }

    private static boolean isContained(Integer x1, Integer y1, Integer z1, Integer x2, Integer y2, Integer z2, DominionDTO parent) {
        return x1 >= parent.getX1() && x2 <= parent.getX2() &&
                y1 >= parent.getY1() && y2 <= parent.getY2() &&
                z1 >= parent.getZ1() && z2 <= parent.getZ2();
    }

    private static boolean isContained(DominionDTO sub, Integer x1, Integer y1, Integer z1, Integer x2, Integer y2, Integer z2) {
        return sub.getX1() >= x1 && sub.getX2() <= x2 &&
                sub.getY1() >= y1 && sub.getY2() <= y2 &&
                sub.getZ1() >= z1 && sub.getZ2() <= z2;
    }

    public static boolean isOwner(Player player, DominionDTO dominion) {
        if (dominion.getOwner().equals(player.getUniqueId())) return true;
        Notification.error(player, "你不是领地 " + dominion.getName() + " 的拥有者，无法执行此操作");
        return false;
    }


}
