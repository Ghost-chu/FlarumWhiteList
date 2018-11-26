package com.mcsunnyside.whitelist;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.maxgamer.quickshop.Database.Database;
import org.maxgamer.quickshop.Database.DatabaseCore;
import org.maxgamer.quickshop.Database.MySQLCore;

public class Main
  extends JavaPlugin
  implements Listener
{
  private boolean setupDBonEnableding;
  private Database database;
  private String dbPrefix;
  private ArrayList<String> players;
  
  public void onEnable()
  {
	 this.players = new ArrayList<>();
    Bukkit.getPluginManager().registerEvents(this, this);
    saveDefaultConfig();
    reloadConfig();
    setupDatabase();
    new BukkitRunnable()
    {
      public void run()
      {
        Main.this.players.clear();
      }
    }.runTaskTimerAsynchronously(this, 4000L, 4000L);
  }
  
	@EventHandler(priority = EventPriority.LOWEST)
	public void login(AsyncPlayerPreLoginEvent e) throws SQLException {
			String player = e.getName();
			// Search caching first.
			for (String string : this.players) {
				if (string.equals(player)) {
					e.setLoginResult(AsyncPlayerPreLoginEvent.Result.ALLOWED);
					return;
				}
			}
			// Check player exist in users table.
			String query = "SELECT * FROM `" + this.dbPrefix + "users` WHERE `username` = ?";
			PreparedStatement ps = this.database.getConnection().prepareStatement(query);
			ps.setString(1, player);
			ResultSet rs = ps.executeQuery();
			int uid = 0;
			while (rs.next()) {
				uid = rs.getInt("id");
			}
			if (uid == 0) {
				e.setKickMessage(getConfig().getString("messages.noreg"));
				e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
				return;
			}
			// Check player's permission group.
			query = "SELECT * FROM `" + this.dbPrefix + "users_groups` WHERE `user_id` = ?";
			PreparedStatement ps2 = this.database.getConnection().prepareStatement(query);
			ps2.setString(1, String.valueOf(uid));
			ResultSet rs2 = ps2.executeQuery();
			boolean verifyed = false;
			while (rs2.next()) {
				if (rs2.getInt("group_id") == getConfig().getInt("groupid")) {
					verifyed = true;
					break;
				}
			}
			if (!verifyed) {
				e.setKickMessage(getConfig().getString("messages.noverify"));
				e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
				return;
			}
			// All is correct, allow player join in and add into caching.
			e.setLoginResult(AsyncPlayerPreLoginEvent.Result.ALLOWED);
			this.players.add(player);
	}
  
  public boolean setupDatabase()
  {
    try
    {
      ConfigurationSection dbCfg = getConfig().getConfigurationSection("database");
      
      this.dbPrefix = dbCfg.getString("prefix");
      if ((this.dbPrefix == null) || (this.dbPrefix.equals("none"))) {
        this.dbPrefix = "";
      }
      String user = dbCfg.getString("user");
      String pass = dbCfg.getString("password");
      String host = dbCfg.getString("host");
      String port = dbCfg.getString("port");
      String database = dbCfg.getString("database");
      DatabaseCore dbCore = new MySQLCore(host, user, pass, database, port);
      this.database = new Database(dbCore);
    }
    catch (Database.ConnectionException e)
    {
      e.printStackTrace();
      if (this.setupDBonEnableding)
      {
        getLogger().severe("Error connecting to database. Aborting plugin load.");
        getServer().getPluginManager().disablePlugin(this);
      }
      else
      {
        getLogger().severe("Error connecting to database.");
      }
      return false;
    }
    return true;
  }
}
