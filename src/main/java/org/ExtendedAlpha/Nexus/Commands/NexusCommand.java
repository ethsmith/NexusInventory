/**
 * NexusInventory is a multi-world inventory plugin.
 * Copyright (C) 2014 - 2015 Gnat008
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ExtendedAlpha.Nexus.Commands;

import org.ExtendedAlpha.Nexus.Groups.Group;
import org.ExtendedAlpha.Nexus.NexusInventory;
import org.ExtendedAlpha.Nexus.TacoSerialization.PlayerSerialization;
import org.ExtendedAlpha.Nexus.TacoSerialization.Serializer;
import org.ExtendedAlpha.Nexus.Utils.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.LinkedList;

public class NexusCommand implements CommandExecutor {

    private NexusInventory plugin;

    private final String NO_PERMISSION = "You do not have permission to do that.";
    private final String PERMISSION_NODE = "nexusinventory.";

    public NexusCommand(NexusInventory plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean isPlayer = false;
        Player player = null;
        if (sender instanceof Player) {
            isPlayer = true;
            player = (Player) sender;
        }

        NICommands command;
        try {
            command = NICommands.valueOf(args[0].toUpperCase());
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException ex) {
            if (isPlayer) {
                plugin.getPlayerMessenger().sendMessage((Player) sender, "Not a valid command. Please type /nexusinv help for help.");
            } else {
                displayConsoleHelp();
            }

            return true;
        }

        switch (command) {
            case CONVERT:
                if (isPlayer) {
                    if (player.hasPermission(PERMISSION_NODE + "convert")) {
                        if (args.length == 2) {
                            switch (args[1].toUpperCase()) {
                                case "MULTIVERSE":
                                    plugin.getPlayerMessenger().sendMessage(player, "Converting from MultiVerse-Inventories! All messages are sent to console!");
                                    mvConvert();
                                    break;
                                case "MULTIINV":
                                    plugin.getPlayerMessenger().sendMessage(player, "Converting from MultiInv! All messages are sent to console!");
                                    miConvert();
                                    break;
                                default:
                                    plugin.getPlayerMessenger().sendMessage(player, "Valid arguments are: MULTIVERSE | MULTIINV");
                                    break;
                            }
                        } else {
                            plugin.getPlayerMessenger().sendMessage(player, "You must specify the plugin to convert from: MULTIVERSE | MULTIINV");
                        }
                    } else {
                        plugin.getPlayerMessenger().sendMessage(player, NO_PERMISSION);
                    }
                } else {
                    if (args.length == 2) {
                        switch (args[1].toUpperCase()) {
                            case "MULTIVERSE":
                                NexusInventory.log.info("Converting from MultiVerse-Inventories!");
                                mvConvert();
                                break;
                            case "MULTIINV":
                                NexusInventory.log.info("Converting from MultiInv!");
                                miConvert();
                                break;
                            default:
                                NexusInventory.log.info("Valid arguments are: MULTIVERSE | MULTIINV");
                                break;
                        }
                    } else {
                        NexusInventory.log.warning("You must specify the plugin to convert from: MULTIVERSE | MULTIINV");
                    }
                }

                return true;

            case DEBUG:
                if (isPlayer) {
                    if (player.hasPermission(PERMISSION_NODE + "debug")) {

                    }
                    if (plugin.dreport != null) {
                        LinkedList<String> customdata = new LinkedList<String>();
                        customdata.add("NexusInventory Custom Data");
                        customdata.add("================================");
                        customdata.add("-----------config.yml-----------");
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(new FileReader(plugin.getDataFolder() + File.separator + "config.yml"));
                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                if (line.startsWith("  password:")) {
                                    line = "  password: NOTSHOWN";
                                }
                                customdata.add(line);
                            }
                        } catch (FileNotFoundException e) {
                        } catch (IOException e) {
                        } finally {
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e) {
                                }
                                reader = null;
                            }
                        }
                        customdata.add("-----------groups.yml-----------");
                        try {
                            reader = new BufferedReader(new FileReader(plugin.getDataFolder() + File.separator + "groups.yml"));
                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                customdata.add(line);
                            }
                        } catch (FileNotFoundException e) {
                        } catch (IOException e) {
                        } finally {
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e) {
                                }
                            }
                        }
                        plugin.dreport.createReport(sender, customdata);
                    }
                    sender.sendMessage(ChatColor.RED + "In order to generate a debug report you need the plugin DebugReport!");
                }

                return true;

            case HELP:
                if (isPlayer) {
                    if (player.hasPermission(PERMISSION_NODE + "help")) {
                        displayPlayerHelp(player);
                    } else {
                        plugin.getPlayerMessenger().sendMessage(player, NO_PERMISSION);
                    }
                }

                return true;

            case RELOAD:
                if (isPlayer) {
                    if (player.hasPermission(PERMISSION_NODE + "reload")) {
                        reload(player);
                    } else {
                        plugin.getPlayerMessenger().sendMessage(player, NO_PERMISSION);
                    }
                } else {
                    reload();
                }

                return true;

            case SETWORLDDEFAULT:
                if (isPlayer) {
                    if (player.hasPermission(PERMISSION_NODE + "setdefaults")) {
                        Group group;

                        if (args.length == 2) {
                            group = args[1].equalsIgnoreCase("simpleplayerdata") ? new Group("simpleplayerdata", null, null) : plugin.getGroupManager().getGroup(args[1]);
                            setWorldDefault(player, group);
                        } else {
                            try {
                                group = plugin.getGroupManager().getGroupFromWorld(player.getWorld().getName());
                                setWorldDefault(player, group);
                            } catch (IllegalArgumentException ex) {
                                plugin.getPlayerMessenger().sendMessage(player, "You are not standing in a valid world!");
                            }
                        }
                    } else {
                        plugin.getPlayerMessenger().sendMessage(player, NO_PERMISSION);
                    }
                } else {
                    NexusInventory.log.warning("This command can only be run from ingame.");
                }

                return true;
        }

        return false;
    }

    private void mvConvert() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getDataConverter().convertMultiVerseData();
            }
        });
    }

    private void miConvert() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getDataConverter().convertMultiInvData();
            }
        });
    }

    private void displayConsoleHelp() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "-------------------------------------------------------");
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "" + "Available Commands:");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "/nexusinv convert " + ChatColor.AQUA + "- Convert Multiverse-Inventories or Multiinv data into NexusInventory");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "/nexusinv help " + ChatColor.AQUA + "- Displays this help");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "/nexusinv reload " + ChatColor.AQUA + "- Reload Config and world files");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "-------------------------------------------------------");
    }

    private void displayPlayerHelp(Player player) {
        String version = plugin.getDescription().getVersion();

        player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------------------------------------------");
        player.sendMessage(ChatColor.DARK_GRAY + "  [" + ChatColor.AQUA + ChatColor.ITALIC + ChatColor.BOLD + "NexusInventory Help:" + ChatColor.DARK_GRAY + "]");
        player.sendMessage("");
        player.sendMessage(ChatColor.RED + "" + ChatColor.ITALIC + " Version: " + ChatColor.GREEN + version);
        player.sendMessage(ChatColor.RED + "" + ChatColor.ITALIC + " Authors: " + ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "ExileDev, ExtendedAlpha" + ChatColor.DARK_GRAY + "]");
        player.sendMessage(ChatColor.RED + "" + ChatColor.ITALIC + " Original Authors: " + ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "Gnat008" + ChatColor.DARK_GRAY + "]");
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + " /nexusinv convert" + ChatColor.RED + "" + ChatColor.ITALIC + " - Convert data from Multiverse-Inventories or Multiinv into NexusInventory.");
        player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + " /nexusinv help" + ChatColor.RED + "" + ChatColor.ITALIC + " - Displays this help page.");
        player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + " /nexusinv reload" + ChatColor.RED + "" + ChatColor.ITALIC + " - Reloads all configuration files.");
        player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + " /nexusinv setworlddefault [group]" + ChatColor.RED + "" + ChatColor.ITALIC + " - Set the default inventory of the world you are standing in.");
        player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + " /nexusinv debug" + ChatColor.RED + "" + ChatColor.ITALIC + " - A simple debug command for making a error / bug reports.");
        player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------------------------------------------");
    }

    private void reload() {
        reloadConfigFiles();

        NexusInventory.log.info("Configuration files reloaded.");
    }

    private void reload(Player player) {
        reloadConfigFiles();

        plugin.getPlayerMessenger().sendMessage(player, "Configuration files reloaded.");
    }

    private void reloadConfigFiles() {
        plugin.getConfigManager().reloadConfigs();
        plugin.getGroupManager().loadGroupsToMemory();
    }

    private void setWorldDefault(Player player, Group group) {
        File file = new File(plugin.getDefaultFilesDirectory() + File.separator + group.getName() + ".json");
        if (!file.exists()) {
            plugin.getPlayerMessenger().sendMessage(player, "Default file for this group not found!");
            return;
        }

        File tmp = new File(plugin.getDataFolder() + File.separator + "data" + File.separator + player.getUniqueId() + File.separator + "tmp.json");
        try {
            tmp.getParentFile().mkdirs();
            tmp.createNewFile();
        } catch (IOException ex) {
            plugin.getPlayerMessenger().sendMessage(player, "Could not create temporary file! Aborting!");
            return;
        }
        plugin.getSerializer().writePlayerDataToFile(player, PlayerSerialization.serializePlayer(player, plugin), new Group("tmp", null, null), GameMode.SURVIVAL);

        player.setFoodLevel(20);
        player.setHealth(20);
        player.setSaturation(20);
        player.setTotalExperience(0);

        plugin.getSerializer().writeData(file, Serializer.toString(PlayerSerialization.serializePlayer(player, plugin)));

        plugin.getSerializer().getPlayerDataFromFile(player, new Group("tmp", null, null), GameMode.SURVIVAL);
        tmp.delete();
        plugin.getPlayerMessenger().sendMessage(player, "Defaults for '" + group.getName() + "' set!");
    }
}
