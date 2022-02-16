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

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class GetBack extends JavaPlugin implements Listener, CommandExecutor {

    public Map<String, Location> deaths = new HashMap<>();
    public FileConfiguration config = this.getConfig();

    @Override
    public void onEnable() {
        super.onEnable();
        getServer().getPluginManager().registerEvents(this, this);
        config.addDefault("deathmessage", "You died. Pathetic... Use /back to teleport back to your death location.");
        config.addDefault("errormessage", "You're not dead, please DIE!!");
        config.addDefault("deaths", "");
        config.options().copyDefaults(true);
        saveConfig();
        String[] dt = Objects.requireNonNull(config.getString("deaths")).split(";");
        for (String item : dt){
            String[] pack = item.split(":");
            if (pack.length==5)
                deaths.put(pack[0], new Location(getServer().getWorld(UUID.fromString(pack[1])),  Double.parseDouble(pack[2]), Double.parseDouble(pack[3]), Double.parseDouble(pack[4])));
        }
        Objects.requireNonNull(getCommand("back")).setExecutor(this);
        getLogger().info("Loaded " + deaths.size() + " death location(s)");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        getLogger().info("GetBack disabled");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        event.setDeathMessage(config.getString("deathmessage"));
        deaths.put(event.getEntity().getDisplayName(), event.getEntity().getLocation());
        config.set("deaths", packMap(deaths));
        saveConfig();
    }

    public String packMap(Map<String, Location> map){
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, Location> entry : map.entrySet()){
            if (out.length()>0) out.append(";");
            out.append(entry.getKey())
                    .append(":").append(Objects.requireNonNull(entry.getValue().getWorld()).getUID())
                    .append(":").append(entry.getValue().getX())
                    .append(":").append(entry.getValue().getY())
                    .append(":").append(entry.getValue().getZ());
        }
        return out.toString();
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (command == getCommand("back")){
            if (args.length>0) {
                Player dstPlayer = Objects.requireNonNull(getServer().getPlayer(args[0]));
                dstPlayer.sendMessage(String.format("Teleporting %s back", dstPlayer.getName()));
                dstPlayer.teleport(deaths.get(args[0]));
                return true;
            } else if (sender instanceof Player) {
                sender.sendMessage(String.format("Teleporting %s back", sender.getName()));
                ((Player) sender).teleport(deaths.get(sender.getName()));
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Wrong command. Usage:" + ChatColor.RESET);
            }
        }
        return false;
    }
}