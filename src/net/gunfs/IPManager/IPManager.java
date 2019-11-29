package net.gunfs.IPManager;

import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class IPManager extends JavaPlugin {

	private MySQL mysql = null;
	private static IPManager _instance;

	public static IPManager getInstance() {
		return _instance;
	}

	public IPManager() {
		_instance = this;
	}

	@Override
	public void onEnable() {
		saveDefaultConfig();
		try {
			init();
		} catch (Exception ignored) {
		}
		PluginManager pm = getServer().getPluginManager();
		if (pm.getPlugin("LoginSecurity") != null)
			pm.registerEvents(new LSListener(this), this);
		else if (Config.LOBBY)
			pm.registerEvents(new LoginListener(), this);
	}

	private void init() throws Exception {
		Config.load();
		if (mysql != null && mysql.isEnabled())
			mysql.closeConnection();
		mysql = new MySQL(this);
	}

	@Override
	public void onDisable() {
		try {
			mysql.closeConnection();
		} catch (SQLException e) {
			getLogger().log(Level.SEVERE, "Khong the dong ket noi MySQL", e);
		}
	}

	public MySQL getMySQL() {
		return mysql;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		new Thread(() -> {
			switch (args[0]) {
			case "r":
			case "lienquan":
			case "related":
				if (mysql == null) {
					sender.sendMessage("§cMySQL đang tắt do config không hợp lệ, config lại rồi reload.");
					break;
				}
				if (args.length <= 1) {
					sendHelp(sender, cmd.getName());
					break;
				}
				int a = 0;
				if (args.length > 2) {
					try {
						a = Integer.parseInt(args[2]);
						if (a <= 0) {
							sender.sendMessage("§cSố ngày phải lớn hơn 0.");
							break;
						}
					} catch (NumberFormatException e) {
						sender.sendMessage("§cSố ngày không hợp lệ.");
						break;
					}
				}
				mysql.sendRelatedPlayers(sender, args[1], a);
				break;
			case "c":
			case "f":
			case "check":
			case "find":
			case "tim":
				if (mysql == null) {
					sender.sendMessage("§cMySQL đang tắt do config không hợp lệ, config lại rồi reload.");
					break;
				}
				if (args.length <= 1) {
					sendHelp(sender, cmd.getName());
					break;
				}
				mysql.sendPlayersFromIP(sender, args[1]);
				break;
			case "reload":
				sender.sendMessage("§eReloading....");
				try {
					init();
					sender.sendMessage("§eReload done.");
				} catch (Exception e) {
					sender.sendMessage("Reload fail. Check console.");
				}
				break;
			default:
				sendHelp(sender, cmd.getName());
				break;
			}
		}).start();
		return true;
	}

	public void sendHelp(CommandSender p, String cmd) {
		p.sendMessage("§e" + cmd + " <r/f> <ip/name> or " + cmd + " reload");
	}
}
