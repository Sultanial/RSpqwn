package assequilent.rspawn;

import assequilent.rspawn.Utils.CheckUpdateUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//Ready Code by assequilent
@SuppressWarnings("all")
public final class RSpawn extends JavaPlugin implements Listener {

    private FileConfiguration config;
    private int pluginId = 0; //Get from site

    String noReplaceConfigGetString (String configString) {
        return ChatColor.translateAlternateColorCodes('&',
                config.getString(configString).replace("$new-line", "\n"));
    }
    String configGetString (String configString, String label) {
        return ChatColor.translateAlternateColorCodes('&',
                config.getString(configString)
                        .replace("$label", label)
                        .replace("{Prefix}", config.getString("options.prefix"))
                        .replace("$new-line", "\n"));
    }
    String configWithoutLabelGetString (String configString) {
        return ChatColor.translateAlternateColorCodes('&',
                config.getString(configString)
                        .replace("{Prefix}", config.getString("options.prefix"))
                        .replace("$new-line", "\n"));
    }
    String superConfigGetString (String configString, String version, String playerName) {
        return ChatColor.translateAlternateColorCodes('&',
                config.getString(configString)
                        .replace("{Prefix}", config.getString("options.prefix"))
                        .replace("$new-line", "\n")
                        .replace("$current-version", getDescription().getVersion())
                        .replace("$latest-version", version)
                        .replace("$download-link", CheckUpdateUtil.getDownloadLink(pluginId))
                        .replace("$target", playerName));
    }
    String superConfigWithoutPlayerNameGetString (String configString, String version) {
        return ChatColor.translateAlternateColorCodes('&',
                config.getString(configString)
                        .replace("{Prefix}", config.getString("options.prefix"))
                        .replace("$new-line", "\n")
                        .replace("$current-version", getDescription().getVersion())
                        .replace("$latest-version", version)
                        .replace("$download-link", CheckUpdateUtil.getDownloadLink(pluginId)));
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        // /spawn
        getCommand("rspawn").setExecutor(this);
        getCommand("rspawn").setTabCompleter(this);
        getCommand("rspawn").setAliases(Arrays.asList("spawn"));
        // /setspawn
        getCommand("rsetspawn").setExecutor(this);
        getCommand("rspawn").setTabCompleter(this);
        getCommand("rsetspawn").setAliases(Arrays.asList("setspawn"));

        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info(noReplaceConfigGetString("messages.console.plugin-started"));
        new CheckUpdateUtil(this, pluginId).getVersion(version -> {
            if (this.getDescription().getVersion().equals(version)) {
                getLogger().info(noReplaceConfigGetString("messages.console.plugin-latest-version"));
            } else {
                getLogger().info(noReplaceConfigGetString("messages.console.plugin-outdate"));
            }
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("rpsawn")) {
            List<String> completions = new ArrayList<>();
            return completions;
        }
        if (cmd.getName().equalsIgnoreCase("rsetspawn")) {
            List<String> completions = new ArrayList<>();
            if (args.length == 1) {
                if (sender.hasPermission(config.getString("options.permissions.admin"))) {
                    completions.add("help");
                    completions.add("reload");
                    completions.add("teleport");
                }
            } else if (args.length == 2) {
                if (args[1].equalsIgnoreCase("teleport") || args[1].equalsIgnoreCase("tp")) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        completions.add(player.getName());
                    }
                }
            }
            return completions;
        }
        return null;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Check update
        if (config.getBoolean("options.check-update")) {
            if (player.hasPermission(config.getString("options.permissions.admin"))) {
                new CheckUpdateUtil(this, pluginId).getVersion(version -> {
                    if (this.getDescription().getVersion().equals(version)) {
                        player.sendMessage(superConfigWithoutPlayerNameGetString("messages.admin.plugin-latest-version", version));
                    } else {
                        player.sendMessage(superConfigWithoutPlayerNameGetString("messages.admin.plugin-outdate", version));
                    }
                });
            }
        }

        // Teleport to spawn
        if (config.getBoolean("options.teleport-when-joined")) {
            if (!config.getString("options.spawn.world").equalsIgnoreCase("unset") &&
                    !config.getString("options.spawn.x").equalsIgnoreCase("unset") &&
                    !config.getString("options.spawn.y").equalsIgnoreCase("unset") &&
                    !config.getString("options.spawn.z").equalsIgnoreCase("unset") &&
                    !config.getString("options.spawn.yaw").equalsIgnoreCase("unset") &&
                    !config.getString("options.spawn.pitch").equalsIgnoreCase("unset")) {
                Location Location = new Location(Bukkit.getWorld(
                        config.getString("options.spawn.world")),
                        config.getDouble("options.spawn.x"),
                        config.getDouble("options.spawn.y"),
                        config.getDouble("options.spawn.z"));
                Location.setYaw((float) config.getDouble("options.spawn.yaw"));
                Location.setPitch((float) config.getDouble("options.spawn.pitch"));
                player.teleport(Location);
                player.sendMessage(configWithoutLabelGetString("messages.player.teleported"));
            } else {
                if (player.hasPermission(config.getString("options.permissions.admin")) || player.isOp()) {
                    player.sendMessage(configWithoutLabelGetString("messages.admin.spawn-not-set"));
                } else {
                    player.sendMessage(configWithoutLabelGetString("messages.player.spawn-not-set"));
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("rspawn")) {
            if (sender instanceof Player) {
                Player senderPlayer = (Player) sender;
                if (args.length == 0) {
                    if (!config.getString("options.spawn.world").equalsIgnoreCase("unset") &&
                            !config.getString("options.spawn.x").equalsIgnoreCase("unset") &&
                            !config.getString("options.spawn.y").equalsIgnoreCase("unset") &&
                            !config.getString("options.spawn.z").equalsIgnoreCase("unset") &&
                            !config.getString("options.spawn.yaw").equalsIgnoreCase("unset") &&
                            !config.getString("options.spawn.pitch").equalsIgnoreCase("unset")) {
                        Location Location = new Location(Bukkit.getWorld(
                                config.getString("options.spawn.world")),
                                config.getDouble("options.spawn.x"),
                                config.getDouble("options.spawn.y"),
                                config.getDouble("options.spawn.z"));
                        Location.setYaw((float) config.getDouble("options.spawn.yaw"));
                        Location.setPitch((float) config.getDouble("options.spawn.pitch"));
                        senderPlayer.teleport(Location);
                        senderPlayer.sendMessage(configGetString("messages.player.teleported", label));
                    } else {
                        if (senderPlayer.hasPermission(config.getString("options.permissions.admin")) || sender.isOp()) {
                            senderPlayer.sendMessage(configGetString("messages.admin.spawn-not-set", label));
                        } else {
                            senderPlayer.sendMessage(configGetString("messages.player.spawn-not-set", label));
                        }
                    }
                } else if (args.length >= 1) {
                    if (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("teleport")) {
                        if (senderPlayer.hasPermission(config.getString("options.permissions.admin")) || sender.isOp()) {
                            if (args[1] != null) {
                                Player target = Bukkit.getPlayer(args[1]);
                                if (target != null) {
                                    if (!target.isOnline()) {
                                        senderPlayer.sendMessage(configGetString("messages.admin.target-is-offline", label));
                                    } else {
                                        if (!config.getString("options.spawn.world").equalsIgnoreCase("unset") &&
                                                !config.getString("options.spawn.x").equalsIgnoreCase("unset") &&
                                                !config.getString("options.spawn.y").equalsIgnoreCase("unset") &&
                                                !config.getString("options.spawn.z").equalsIgnoreCase("unset") &&
                                                !config.getString("options.spawn.yaw").equalsIgnoreCase("unset") &&
                                                !config.getString("options.spawn.pitch").equalsIgnoreCase("unset")) {
                                            Location Location = new Location(Bukkit.getWorld(
                                                    config.getString("options.spawn.world")),
                                                    config.getDouble("options.spawn.x"),
                                                    config.getDouble("options.spawn.y"),
                                                    config.getDouble("options.spawn.z"));
                                            Location.setYaw((float) config.getDouble("options.spawn.yaw"));
                                            Location.setPitch((float) config.getDouble("options.spawn.pitch"));
                                            target.teleport(Location);
                                            target.sendMessage(configGetString("messages.player.teleported-by-admin", label));
                                            senderPlayer.sendMessage(superConfigGetString("messages.admin.teleported-player", label, senderPlayer.getName()));
                                        } else {
                                            if (senderPlayer.hasPermission(config.getString("options.permissions.admin")) || sender.isOp()) {
                                                senderPlayer.sendMessage(configGetString("messages.admin.spawn-not-set", label));
                                            } else {
                                                senderPlayer.sendMessage(configGetString("messages.player.spawn-not-set", label));
                                            }
                                        }
                                    }
                                } else {
                                    senderPlayer.sendMessage(configGetString("messages.admin.target-not-found", label));
                                }
                            } else {
                                senderPlayer.sendMessage(configGetString("messages.admin.use-commands.spawn-teleport", label));
                            }
                        } else {
                            senderPlayer.sendMessage(configGetString("messages.player.no-permission", label));
                        }
                    } else if (args[0].equalsIgnoreCase("reload")) {
                        if (senderPlayer.hasPermission(config.getString("options.permissions.admin")) || sender.isOp()) {
                            reloadConfig();
                            config = getConfig();
                            senderPlayer.sendMessage(configGetString("messages.other.plugin-restarted", label));
                        } else {
                            senderPlayer.sendMessage(configGetString("messages.player.no-permission", label));
                        }
                    } else if (args[0].equalsIgnoreCase("help")) {
                        if (senderPlayer.hasPermission(config.getString("options.permissions.admin")) || sender.isOp()) {
                            senderPlayer.sendMessage(configGetString("messages.admin.use-commands.help", label));
                        } else {
                            senderPlayer.sendMessage(configGetString("messages.other.use-commands.spawn", label));
                        }
                    } else {
                        if (senderPlayer.hasPermission(config.getString("options.permissions.admin")) || sender.isOp()) {
                            senderPlayer.sendMessage(configGetString("messages.admin.use-commands.help", label));
                        } else {
                            senderPlayer.sendMessage(configGetString("messages.other.use-commands.spawn", label));
                        }
                    }
                } else {
                    if (senderPlayer.hasPermission(config.getString("options.permissions.admin")) || sender.isOp()) {
                        senderPlayer.sendMessage(configGetString("messages.admin.use-commands.help", label));
                    } else {
                        senderPlayer.sendMessage(configGetString("messages.other.use-commands.spawn", label));
                    }
                }
            } else {
                sender.sendMessage(configGetString("messages.console.cannot-run-as-console", label));
            }
        } else if (cmd.getName().equalsIgnoreCase("rsetspawn")) {
            if (sender instanceof Player) {
                Player senderPlayer = (Player) sender;
                if (args.length == 0) {
                    config.set("options.spawn.world", senderPlayer.getWorld().getName());
                    config.set("options.spawn.x", senderPlayer.getLocation().getX());
                    config.set("options.spawn.y", senderPlayer.getLocation().getY());
                    config.set("options.spawn.z", senderPlayer.getLocation().getZ());
                    config.set("options.spawn.yaw", senderPlayer.getLocation().getYaw());
                    config.set("options.spawn.pitch", senderPlayer.getLocation().getPitch());
                    saveConfig();
                    senderPlayer.sendMessage(configGetString("messages.admin.spawn-point-created", label));
                } else {
                    senderPlayer.sendMessage(configGetString("messages.admin.use-commands.set-spawn", label));
                }
            } else {
                sender.sendMessage(configGetString("messages.console.cannot-run-as-console", label));
            }
        }
        return true;
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.translateAlternateColorCodes('&', config.getString("messages.console.plugin-disabled")));
    }
}