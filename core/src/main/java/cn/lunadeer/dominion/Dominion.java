package cn.lunadeer.dominion;

import cn.lunadeer.dominion.managers.ConfigManager;
import cn.lunadeer.dominion.managers.DatabaseTables;
import cn.lunadeer.dominion.managers.PlaceHolderApi;
import cn.lunadeer.dominion.managers.Translation;
import cn.lunadeer.dominion.utils.Residence.ResCommands;
import cn.lunadeer.dominion.utils.map.DynmapConnect;
import cn.lunadeer.dominion.utils.map.MapRender;
import cn.lunadeer.minecraftpluginutils.*;
import cn.lunadeer.minecraftpluginutils.databse.DatabaseManager;
import cn.lunadeer.minecraftpluginutils.databse.DatabaseType;
import cn.lunadeer.minecraftpluginutils.scui.CuiManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class Dominion extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        new Notification(this);
        new XLogger(this);
        config = new ConfigManager(this);
        new DatabaseManager(this,
                DatabaseType.valueOf(config.getDbType().toUpperCase()),
                config.getDbHost(),
                config.getDbPort(),
                config.getDbName(),
                config.getDbUser(),
                config.getDbPass());
        DatabaseTables.migrate();
        new Scheduler(this);
        new Cache();
        new DominionInterface();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceHolderApi(this);
        }
        if (config.getGroupTitleEnable() && !Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            XLogger.warn(Translation.Messages_PlaceholderAPINotFound);
            config.setGroupTitleEnable(false);
        }

        new EventsRegister(this);
        Objects.requireNonNull(Bukkit.getPluginCommand("dominion")).setExecutor(new Commands());
        Objects.requireNonNull(Bukkit.getPluginCommand("residence")).setExecutor(new ResCommands());

        bStatsMetrics metrics = new bStatsMetrics(this, 21445);
        metrics.addCustomChart(new bStatsMetrics.SimplePie("database", () -> config.getDbType()));
        metrics.addCustomChart(new bStatsMetrics.SingleLineChart("dominion_count", () -> Cache.instance.getDominionCounts()));
        metrics.addCustomChart(new bStatsMetrics.SingleLineChart("group_count", () -> Cache.instance.getGroupCounts()));
        metrics.addCustomChart(new bStatsMetrics.SingleLineChart("member_count", () -> Cache.instance.getMemberCounts()));

        if (config.getCheckUpdate()) {
            giteaReleaseCheck = new GiteaReleaseCheck(this,
                    "https://ssl.lunadeer.cn:14446",
                    "mirror",
                    "Dominion");
        }

        // SCUI 初始化
        Bukkit.getPluginManager().registerEvents(new CuiManager(this), this);

        XLogger.info(Translation.Messages_PluginEnabled);
        XLogger.info(Translation.Messages_PluginVersion, this.getDescription().getVersion());
        // http://patorjk.com/software/taag/#p=display&f=Big&t=Dominion
        XLogger.info("  _____                  _       _");
        XLogger.info(" |  __ \\                (_)     (_)");
        XLogger.info(" | |  | | ___  _ __ ___  _ _ __  _  ___  _ __");
        XLogger.info(" | |  | |/ _ \\| '_ ` _ \\| | '_ \\| |/ _ \\| '_ \\");
        XLogger.info(" | |__| | (_) | | | | | | | | | | | (_) | | | |");
        XLogger.info(" |_____/ \\___/|_| |_| |_|_|_| |_|_|\\___/|_| |_|");
        XLogger.info(" ");

        if (config.getDynmap()) new DynmapConnect();  // 注册 Dynmap API
        Scheduler.runTaskLaterAsync(MapRender::render, 40 * 20);
        AutoClean.run();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        DatabaseManager.instance.close();
    }

    public static Dominion instance;
    public static ConfigManager config;
    public static Map<UUID, Map<Integer, Location>> pointsSelect = new HashMap<>();
    private GiteaReleaseCheck giteaReleaseCheck;
}
