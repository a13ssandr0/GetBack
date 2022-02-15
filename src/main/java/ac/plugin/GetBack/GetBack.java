package ac.plugin.GetBack;

import org.bukkit.plugin.java.JavaPlugin;

public class GetBack extends JavaPlugin {
    @Override
    public void onEnable() {
        super.onEnable();
        getLogger().info("GetBack enabled");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        getLogger().info("GetBack disabled");
    }
}