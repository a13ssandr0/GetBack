package ac.plugin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GetBack extends JavaPlugin implements Listener, CommandExecutor {

    public Map<String, Location> deaths = new HashMap<>();
    public FileConfiguration config = this.getConfig();

    @Override
    public void onEnable() {
        super.onEnable();
        getServer().getPluginManager().registerEvents(this, this);
        if (!config.contains("deathmessage")) {
            config.set("deathmessage", "You died. Pathetic... Use /back to teleport back to your death location.");
            saveConfig();
        }
        if (!config.contains("errormessage")) {
            config.set("errormessage", "You're not dead, please DIE!!");
            saveConfig();
        }
        if (!config.contains("deaths")) {
            config.set("deaths", deaths);
            saveConfig();
        } else {
            // Load deaths 1 tick after the server finished startup
            // Doing it before would fail because world may not have fully loaded
            new BukkitRunnable() {
                @Override
                public void run() {
                    Objects.requireNonNull(getConfig().getConfigurationSection("deaths")).getValues(true)
                            .forEach((key, value) -> deaths.put(key, (Location) value));
                    getServer().broadcastMessage("[GetBack] Loaded " + deaths.size() + " death location(s)");
                }
            }.runTaskLater(this, 1L);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        getLogger().info("GetBack disabled");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        deaths.put(event.getEntity().getDisplayName(), event.getEntity().getLocation());
        config.set("deaths", deaths);
        saveConfig(); // saving config here just in case the plugin would crash before onDisable is called
        event.getEntity().sendMessage("" /*removes NPE warning*/ + config.getString("deathmessage"));
        // IDE warns about config.getString() returning null,
        // virtually impossible since its value being present
        // is checked when loading config in onEnable
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (command == getCommand("back")){
            if (args.length>0) {
                Player dstPlayer = getServer().getPlayer(args[0]);
                if (dstPlayer == null)
                    sender.sendMessage(ChatColor.RED + "Player not found" + ChatColor.RESET);
                else if (!deaths.containsKey(dstPlayer.getName()))
                    sender.sendMessage(ChatColor.RED + getConfig().getString("errormessage") + ChatColor.RESET);
                else {
                    dstPlayer.sendMessage(ChatColor.GREEN + "Teleporting " + ChatColor.BLUE + ChatColor.BOLD + dstPlayer.getName() + ChatColor.RESET + ChatColor.GREEN + " back" + ChatColor.RESET);
                    dstPlayer.teleport(deaths.get(dstPlayer.getName()));
                }
                return true;
            } else if (sender instanceof Player) {
                if (!deaths.containsKey(sender.getName()))
                    sender.sendMessage(ChatColor.RED + getConfig().getString("errormessage") + ChatColor.RESET);
                else {
                    sender.sendMessage(ChatColor.GREEN + "Teleporting " + ChatColor.BLUE + ChatColor.BOLD + sender.getName() + ChatColor.RESET + ChatColor.GREEN + " back" + ChatColor.RESET);
                    ((Player) sender).teleport(deaths.get(sender.getName()));
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Wrong command. Usage:" + ChatColor.RESET);
            }
        }
        return false;
    }
}