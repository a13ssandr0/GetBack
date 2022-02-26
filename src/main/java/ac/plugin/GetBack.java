package ac.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
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
import org.bukkit.util.NumberConversions;

import javax.annotation.Nonnull;
import java.util.*;

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
            config.set("deaths", new ArrayList<>());
            saveConfig();
        } else {
            // Load deaths 1 tick after the server finished startup
            // Doing it before would fail because world may not have fully loaded
            new BukkitRunnable() {
                @Override
                public void run() {
                    deaths = unpackMap(getConfig().getMapList("deaths"));
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

    /**
     * Serialize deaths map as a list, adding player name as one of the values of the map
     * @param map A String-Location map containing the death location for each player
     * @return A list of Location maps
     */
    public List<Map<String, Object>> packMap(Map<String, Location> map){
        List<Map<String, Object>> out = new ArrayList<>();
        map.forEach((s, location) -> {
            Map<String, Object> temp = location.serialize();
            // add player name to location map and add it to the list
            temp.put("player", s);
            out.add(temp);
        });
        return out;
    }

    /**
     * Deserialize deaths map as a list
     * @param list A list of Location maps
     * @return A String-Location map containing the death location for each player
     */
    public Map<String, Location> unpackMap(List<Map<?, ?>> list){
        Map<String, Location> out = new HashMap<>();
        list.forEach(args -> {
            /*
            Rewrote Bukkit Location deserialize method
            Find world by its name as in original method
             */
            World world = null;
            if (args.containsKey("world")) {
                world = Bukkit.getWorld((String)args.get("world"));
                if (world == null) {
                    throw new IllegalArgumentException("unknown world");
                }
            }
            // Add elements to new output map and prepare Location object
            out.put((String)args.get("player"), new Location(
                    world,
                    NumberConversions.toDouble(args.get("x")),
                    NumberConversions.toDouble(args.get("y")),
                    NumberConversions.toDouble(args.get("z")),
                    NumberConversions.toFloat(args.get("yaw")),
                    NumberConversions.toFloat(args.get("pitch"))
            ));

        });
        return out;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        deaths.put(event.getEntity().getDisplayName(), event.getEntity().getLocation());
        /*
        we need to save locations as maps with player name as one of its values,
        since native saving method uses player name as a key, messing up the configuration
        when players have special characters in their name
         */
        config.set("deaths", packMap(deaths));
        saveConfig(); // saving config here just in case the plugin would crash before onDisable is called
        event.getEntity().sendMessage("" /*removes NPE warning*/ + config.getString("deathmessage"));
        /*
        IDE warns about config.getString() returning null,
        virtually impossible since its value being present
        is checked when loading config in onEnable
         */
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (command == getCommand("back")){
            Player dstPlayer;
            if (args.length>0) dstPlayer = getServer().getPlayer(args[0]);
            else if (sender instanceof Player) dstPlayer = (Player) sender;
            else {
                sender.sendMessage(ChatColor.RED + "Wrong command. Usage:" + ChatColor.RESET);
                return false;
            }
            if (dstPlayer == null)
                sender.sendMessage(ChatColor.RED + "Player not found" + ChatColor.RESET);
            else if (!deaths.containsKey(dstPlayer.getName()))
                sender.sendMessage(ChatColor.RED + getConfig().getString("errormessage") + ChatColor.RESET);
            else {
                String tp_msg = ChatColor.GREEN + "Teleporting " + ChatColor.BLUE + ChatColor.BOLD + dstPlayer.getName() + ChatColor.RESET + ChatColor.GREEN + " back" + ChatColor.RESET;
                sender.sendMessage(tp_msg);
                if (sender!=dstPlayer) dstPlayer.sendMessage(tp_msg);
                dstPlayer.teleport(deaths.get(dstPlayer.getName()));
            }
            return true;
        } else if (command == getCommand("home")){
            Player dstPlayer;
            if (args.length>0) dstPlayer = getServer().getPlayer(args[0]);
            else if (sender instanceof Player) dstPlayer = (Player) sender;
            else {
                sender.sendMessage(ChatColor.RED + "Wrong command. Usage:" + ChatColor.RESET);
                return false;
            }
            if (dstPlayer == null)
                sender.sendMessage(ChatColor.RED + "Player not found" + ChatColor.RESET);
            else if (dstPlayer.getBedSpawnLocation() == null)
                sender.sendMessage(ChatColor.RED + "No bed found for " + dstPlayer.getName() + ChatColor.RESET);
            else {
                String tp_msg = ChatColor.GREEN + "Teleporting " + ChatColor.BLUE + ChatColor.BOLD + dstPlayer.getName() + ChatColor.RESET + ChatColor.GREEN + " home" + ChatColor.RESET;
                sender.sendMessage(tp_msg);
                if (sender!=dstPlayer) dstPlayer.sendMessage(tp_msg);
                dstPlayer.teleport(dstPlayer.getBedSpawnLocation());
            }
            return true;
        }
        return false;
    }
}