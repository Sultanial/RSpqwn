package assequilent.rspawn.Utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Consumer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class CheckUpdateUtil {
    private final JavaPlugin plugin;
    private final int resourceId;

    //Util taked and remaked from spigotmc.
    //remaked by assequilent
    public CheckUpdateUtil(JavaPlugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream is = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId + "/~").openStream();
                 Scanner scann = new Scanner(is)) {
                if (scann.hasNext()) {
                    consumer.accept(scann.next());
                }
            } catch (IOException e) {
                plugin.getLogger().info("Не удалось проверить версию плагина: " + e.getMessage());
            }
        });
    }

    public static String getDownloadLink(int resourceId) {
        return "https://www.spigotmc.org/resources/" + resourceId;
    }
}