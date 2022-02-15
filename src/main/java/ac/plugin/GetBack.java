package ac.plugin;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GetBack extends JavaPlugin implements Listener {

    public Map<String, int[]> deaths = new HashMap<>();
    public FileConfiguration config = this.getConfig();

    @Override
    public void onEnable() {
        super.onEnable();
        getLogger().info("GetBack enabled");
        getServer().getPluginManager().registerEvents(this, this);
        config.addDefault("deathmessage", "You died. Pathetic... Use /back to teleport back to your death location.");
        config.addDefault("errormessage", "You're not dead, please DIE!!");
        config.addDefault("deaths", "");
        config.options().copyDefaults(true);
        saveConfig();
        String[] dt = Objects.requireNonNull(config.getString("deaths")).split(";");
        for (String item : dt){
            String[] pack = item.split(":");
            if (pack.length>=3)
                deaths.put(pack[0], new int[]{Integer.parseInt(pack[1]), Integer.parseInt(pack[2]), Integer.parseInt(pack[3])});
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        getLogger().info("GetBack disabled");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        event.setDeathMessage(config.getString("deathmessage"));
        deaths.put(event.getEntity().getDisplayName(), new int[]{
                (int) Math.round(event.getEntity().getLocation().getX()),
                (int) Math.round(event.getEntity().getLocation().getY()),
                (int) Math.round(event.getEntity().getLocation().getZ())
        });
        config.set("deaths", packMap(deaths));
        saveConfig();
    }

    public String packMap(Map<String, int[]> map){
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, int[]> entry : map.entrySet()){
            if (out.length()>0) out.append(";");
            out.append(entry.getKey())
                    .append(":").append(entry.getValue()[0])
                    .append(":").append(entry.getValue()[1])
                    .append(":").append(entry.getValue()[2]);
        }
        return out.toString();
    }

/*    class LocationHolder {
        int x, y, z;
        public LocationHolder(int x, int y, int z) {this.x = x; this.y = y; this.z = z; getLogger().info(toString());}
        public LocationHolder(double x, double y, double z) {
            new LocationHolder((int) Math.round(x), (int) Math.round(y), (int) Math.round(z));}
        public LocationHolder(Location location) {
            new LocationHolder(location.getX(), location.getY(), location.getZ());}
//        public LocationHolder(String data){
//            String[] pack = data.split(":");
//            if (pack.length>=2)
//                new LocationHolder(Integer.parseInt(pack[0]), Integer.parseInt(pack[1]), Integer.parseInt(pack[2]));
//        }
        public String toString(){
            return x + ":" + y + ":" + z;
        }
    }*/
}