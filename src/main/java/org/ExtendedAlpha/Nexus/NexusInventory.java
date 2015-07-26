/**
 * NexusInventory is a multi-world inventory plugin.
 * Copyright (C) 2014 - 2015  Gnat008
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

package org.ExtendedAlpha.Nexus;

import net.milkbowl.vault.economy.Economy;
import org.ExtendedAlpha.Nexus.Commands.NexusCommand;
import org.ExtendedAlpha.Nexus.Config.ConfigManager;
import org.ExtendedAlpha.Nexus.Config.ConfigType;
import org.ExtendedAlpha.Nexus.Config.defaults.ConfigValues;
import org.ExtendedAlpha.Nexus.Data.DataConverter;
import org.ExtendedAlpha.Nexus.Data.DataSerializer;
import org.ExtendedAlpha.Nexus.Groups.GroupManager;
import org.ExtendedAlpha.Nexus.Listeners.PlayerChangedWorldListener;
import org.ExtendedAlpha.Nexus.Listeners.PlayerGameModeChangeListener;
import org.ExtendedAlpha.Nexus.Listeners.PlayerQuitListener;
import org.ExtendedAlpha.Nexus.Logger.NexusLogger;
import org.ExtendedAlpha.Nexus.Metrics.Metrics;
import org.ExtendedAlpha.Nexus.Updater.SpigotUpdater;
import org.ExtendedAlpha.Nexus.Utils.ChatColor;
import org.ExtendedAlpha.Nexus.Utils.PlayerMessenger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import com.tux2mc.debugreport.DebugReport;

import java.io.*;

public class NexusInventory extends JavaPlugin {

    private Economy economy;
    // Initialize logger (auto implements enable/disable messages to console)
    public static NexusLogger log;
    // Initialize updater
    private SpigotUpdater updater;
    // Initialize instance
    private static NexusInventory instance = null;
    // Initialize debugreport
    public DebugReport dreport = null;

    @Override
    public void onEnable() {
        // Initialize Logger
        log = new NexusLogger();
        instance = this;

        if (!(new File(getDataFolder() + File.separator + "data" + File.separator + "simpleplayerdata").exists())) {
            new File(getDataFolder() + File.separator + "data" + File.separator + "simpleplayerdata").mkdirs();
        }

        if (!(new File(getDataFolder() + File.separator + "simpleplayerdata.json").exists())) {
            saveResource("simpleplayerdata.json", false);
            File dFile = new File(getDataFolder() + File.separator + "simpleplayerdata.json");
            dFile.renameTo(new File(getDefaultFilesDirectory() + File.separator + "simpleplayerdata.json"));
        }

        getConfigManager().addConfig(ConfigType.CONFIG, new File(getDataFolder() + File.separator + "config.yml"));
        getConfigManager().addConfig(ConfigType.GROUPS, new File(getDataFolder() + File.separator + "groups.yml"));

        getGroupManager().loadGroupsToMemory();

        log.info("Initializing commands...");
        getCommand("nexusinv").setExecutor(new NexusCommand(this));
        log.info("Commands Initializing!");
        log.info("Initializing listeners...");
        getServer().getPluginManager().registerEvents(new PlayerChangedWorldListener(this), this);
        log.info("Initializing PlayerChangedWorldListener.");
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        log.info("Initializing PlayerQuitListener.");

        if (ConfigValues.SEPARATE_GAMEMODE_INVENTORIES.getBoolean()) {
            getServer().getPluginManager().registerEvents(new PlayerGameModeChangeListener(this), this);
            log.info("Initializing PlayerGameModeChangeListener.");
        }

        if (getServer().getPluginManager().getPlugin("DebugReport") != null) {
            log.info("DebugReport found! Hooking into it...");
            dreport = DebugReport.getInstance();
        } else {
            log.warning("Unable to hook into DebugReport!");
        }

        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            log.info("Vault found! Hooking into it...");
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
                log.info("Hooked into Vault!");
            } else {
                log.warning("Unable to hook into Vault!");
            }
        }
        if (getConfig().getBoolean("CHECK_UPDATES"));
        {
        log.info("Initializing updater...");
        this.updater = new SpigotUpdater(this);
        getUpdater().checkUpdates();
        if (SpigotUpdater.updateAvailable())
        {
                Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY + "---------------------------------");
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "           NexusInventory Updater");
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + " ");
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "An update for NexusInventory has been found!");
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "NexusInventory " + SpigotUpdater.getHighest());
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "You are running " + getDescription().getVersion());
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + " ");
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Download at:");
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "SpigotMC: https://goo.gl/W7b4yK");
                Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY + "---------------------------------");
            }
        }
        log.info("Initializing Metrics..");
        setupMetrics();
        log.info("Enabled!");
    }

    @Override
    public void onDisable() {
        PlayerMessenger.disable();
        DataSerializer.disable();
        DataConverter.disable();
        getConfigManager().disable();
        getGroupManager().disable();
        getServer().getScheduler().cancelTasks(this);
    }

    private void setupMetrics() {
            if (ConfigValues.ENABLE_METRICS.getBoolean()) {
                getLogger().info("Starting metrics...");
                try {
                    Metrics metrics = new Metrics(this);
                    metrics.start();
                } catch (IOException e) {
                    getLogger().info("Failed to start metrics!");
                }
            }
        }

    public static NexusInventory getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Cannot get instance before onEnable() has been called!");
        }

        return instance;
    }

    public ConfigManager getConfigManager() {
        return ConfigManager.getInstance();
    }

    public DataConverter getDataConverter() {
        return DataConverter.getInstance(this);
    }

    public DataSerializer getSerializer() {
        return DataSerializer.getInstance(this);
    }

    public Economy getEconomy() {
        return this.economy;
    }

    public File getDefaultFilesDirectory() {
        return new File(getDataFolder() + File.separator + "data" + File.separator + "simpleplayerdata");
    }

    public GroupManager getGroupManager() {
        return GroupManager.getInstance(this);
    }

    public PlayerMessenger getPlayerMessenger() {
        return PlayerMessenger.getInstance(this);
    }

    public SpigotUpdater getUpdater()
    {
        return this.updater;
    }

    public void copyFile(File from, File to) {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = new FileInputStream(from);
            out = new FileOutputStream(to);

            byte[] buff = new byte[1024];
            int len;
            while ((len = in.read(buff)) > 0) {
                out.write(buff, 0, len);
            }
        } catch (IOException ex) {
            log.warning("An error occurred copying file '" + from.getName() + "' to '" + to.getName() + "': " + ex.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
