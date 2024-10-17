package cn.lunadeer.dominion.dtos;

import cn.lunadeer.dominion.Cache;
import cn.lunadeer.dominion.api.dtos.Flag;
import cn.lunadeer.minecraftpluginutils.databse.DatabaseManager;
import cn.lunadeer.minecraftpluginutils.databse.Field;
import cn.lunadeer.minecraftpluginutils.databse.FieldType;
import cn.lunadeer.minecraftpluginutils.databse.syntax.InsertRow;
import cn.lunadeer.minecraftpluginutils.databse.syntax.UpdateRow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.util.*;

public class MemberDTO implements cn.lunadeer.dominion.api.dtos.MemberDTO {

    private static List<MemberDTO> query(String sql, Object... params) {
        List<MemberDTO> players = new ArrayList<>();
        try (ResultSet rs = DatabaseManager.instance.query(sql, params)) {
            return getDTOFromRS(rs);
        } catch (Exception e) {
            DatabaseManager.handleDatabaseError("MemberDTO.query ", e, sql);
        }
        return players;
    }

    private static List<MemberDTO> getDTOFromRS(ResultSet rs) {
        List<MemberDTO> players = new ArrayList<>();
        if (rs == null) return players;
        try {
            while (rs.next()) {
                Map<Flag, Boolean> flags = new HashMap<>();
                for (Flag f : cn.lunadeer.dominion.dtos.Flag.getPrivilegeFlagsEnabled()) {
                    flags.put(f, rs.getBoolean(f.getFlagName()));
                }
                MemberDTO player = new MemberDTO(
                        rs.getInt("id"),
                        UUID.fromString(rs.getString("player_uuid")),
                        rs.getBoolean("admin"),
                        rs.getInt("dom_id"),
                        flags,
                        rs.getInt("group_id")
                );
                players.add(player);
            }
        } catch (Exception e) {
            DatabaseManager.handleDatabaseError("MemberDTO.getDTOFromRS ", e, "");
        }
        return players;
    }

    private MemberDTO doUpdate(UpdateRow updateRow) {
        updateRow.returningAll(id)
                .table("dominion_member")
                .where("id = ?", id.value);
        try (ResultSet rs = updateRow.execute()) {
            List<MemberDTO> players = getDTOFromRS(rs);
            if (players.isEmpty()) return null;
            Cache.instance.loadMembers(getPlayerUUID());
            return players.get(0);
        } catch (Exception e) {
            DatabaseManager.handleDatabaseError("MemberDTO.doUpdate ", e, "");
            return null;
        }
    }

    public static MemberDTO insert(MemberDTO player) {
        InsertRow insertRow = new InsertRow().returningAll().onConflictDoNothing(new Field("id", null))
                .table("dominion_member")
                .field(player.playerUUID)
                .field(player.admin)
                .field(player.domID);
        for (Flag f : cn.lunadeer.dominion.dtos.Flag.getPrivilegeFlagsEnabled()) {
            insertRow.field(new Field(f.getFlagName(), player.getFlagValue(f)));
        }
        try (ResultSet rs = insertRow.execute()) {
            Cache.instance.loadMembers(player.getPlayerUUID());
            List<MemberDTO> players = getDTOFromRS(rs);
            if (players.isEmpty()) return null;
            return players.get(0);
        } catch (Exception e) {
            DatabaseManager.handleDatabaseError("MemberDTO.insert ", e, "");
            return null;
        }
    }

    public static MemberDTO select(UUID playerUUID, Integer dom_id) {
        String sql = "SELECT * FROM dominion_member WHERE player_uuid = ? AND dom_id = ?;";
        List<MemberDTO> p = query(sql, playerUUID.toString(), dom_id);
        if (p.isEmpty()) return null;
        return p.get(0);
    }

    public static List<MemberDTO> select(Integer dom_id) {
        String sql = "SELECT * FROM dominion_member WHERE dom_id = ?;";
        return query(sql, dom_id);
    }

    public static void delete(UUID player, Integer domID) {
        String sql = "DELETE FROM dominion_member WHERE player_uuid = ? AND dom_id = ?;";
        query(sql, player.toString(), domID);
        Cache.instance.loadMembers(player);
    }

    public static List<MemberDTO> selectAll() {
        String sql = "SELECT * FROM dominion_member;";
        return query(sql);
    }

    public static List<MemberDTO> selectAll(UUID player) {
        String sql = "SELECT * FROM dominion_member WHERE player_uuid = ?;";
        return query(sql, player.toString());
    }

    public static List<MemberDTO> selectByGroupId(Integer groupId) {
        String sql = "SELECT * FROM dominion_member WHERE group_id = ?;";
        return query(sql, groupId);
    }

    public static List<MemberDTO> selectByDomGroupId(Integer domId, Integer groupId) {
        String sql = "SELECT * FROM dominion_member WHERE group_id = ? AND dom_id = ?;";
        return query(sql, groupId, domId);
    }

    Field id = new Field("id", FieldType.INT);
    Field playerUUID = new Field("player_uuid", FieldType.STRING);
    Field admin = new Field("admin", FieldType.BOOLEAN);
    Field domID = new Field("dom_id", FieldType.INT);
    Field groupId = new Field("group_id", FieldType.INT);

    @Override
    public Integer getId() {
        return (Integer) id.value;
    }

    @Override
    public UUID getPlayerUUID() {
        return UUID.fromString((String) playerUUID.value);
    }

    @Override
    public Boolean getAdmin() {
        return (Boolean) admin.value;
    }

    @Override
    public Integer getDomID() {
        return (Integer) domID.value;
    }

    @Override
    public Integer getGroupId() {
        return (Integer) groupId.value;
    }

    private final Map<Flag, Boolean> flags = new HashMap<>();

    @Override
    public @NotNull Boolean getFlagValue(Flag flag) {
        if (!flags.containsKey(flag)) return flag.getDefaultValue();
        return flags.get(flag);
    }

    @Override
    public @NotNull Map<Flag, Boolean> getFlagsValue() {
        return flags;
    }

    @Override
    public MemberDTO setFlagValue(@NotNull Flag flag, @NotNull Boolean value) {
        if (flag.isEnvironmentFlag()) {
            return null;
        }
        flags.put(flag, value);
        Field f = new Field(flag.getFlagName(), value);
        UpdateRow updateRow = new UpdateRow().field(f);
        return doUpdate(updateRow);
    }

    @Override
    public @Nullable MemberDTO setAdmin(@NotNull Boolean admin) {
        this.admin.value = admin;
        UpdateRow updateRow = new UpdateRow().field(this.admin);
        return doUpdate(updateRow);
    }

    public MemberDTO setGroupId(Integer groupId) {
        this.groupId.value = groupId;
        UpdateRow updateRow = new UpdateRow().field(this.groupId);
        return doUpdate(updateRow);
    }

    public MemberDTO applyTemplate(PrivilegeTemplateDTO template) {
        this.admin.value = template.getAdmin();
        UpdateRow updateRow = new UpdateRow().field(admin);
        for (Flag f : cn.lunadeer.dominion.dtos.Flag.getPrivilegeFlagsEnabled()) {
            this.flags.put(f, template.getFlagValue(f));
            updateRow.field(new Field(f.getFlagName(), template.getFlagValue(f)));
        }
        return doUpdate(updateRow);
    }

    private MemberDTO(Integer id, UUID playerUUID, Boolean admin, Integer domID, Map<Flag, Boolean> flags, Integer groupId) {
        this.id.value = id;
        this.playerUUID.value = playerUUID.toString();
        this.admin.value = admin;
        this.domID.value = domID;
        this.groupId.value = groupId;
        this.flags.putAll(flags);
    }

    public MemberDTO(UUID playerUUID, DominionDTO dom) {
        this.id.value = null;
        this.playerUUID.value = playerUUID.toString();
        this.admin.value = false;
        this.domID.value = dom.getId();
        this.flags.putAll(dom.getGuestPrivilegeFlagValue());
    }

}
