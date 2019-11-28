package net.gunfs.IPManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class MySQL {

	private Connection sql;
	private Plugin plugin;
	private boolean enabled = false;

	public MySQL(Plugin plugin) throws Exception {
		this.plugin = plugin;
		connect();
	}

	public void connect() throws Exception {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			sql = DriverManager.getConnection(
					"jdbc:mysql://" + Config.MYSQL_HOST + ":" + Config.MYSQL_PORT + "/" + Config.MYSQL_DATABASE,
					Config.MYSQL_USER, Config.MYSQL_PASS);
			enabled = true;
			createTable();
		} catch (Exception e) {
			this.plugin.getLogger().log(Level.SEVERE, "Khong the mo ket noi MySQL", e);
			throw e;
		}
	}

	private String INSERT_IP = "insert into user_ip(name, ip, time) values(?, ?, @time:=?)"
			+ " on duplicate key update time=@time, count=count+1;",
			// RELATED_PLAYER = "select name from user_ip where name!=@name:=? and ip in
			// (select ip from user_ip where name=@name)",
			RELATED_PLAYER_BY_IP = "select ip, GROUP_CONCAT(name SEPARATOR '|') as names from user_ip where ip in (select ip from user_ip"
					+ " where name=?) group by ip order by time desc",
			RELATED_PLAYER_BY_IP_LAST_X_DAYS = "select ip, GROUP_CONCAT(name SEPARATOR '|') as names from user_ip where ip in (select ip from user_ip"
					+ " where name=?) and time > ? group by ip order by time desc",
			PLAYER_FROM_IP = "select name from user_ip where ip = ?",
			TABLE = "CREATE TABLE IF NOT EXISTS `user_ip` (\r\n"
					+ "  `name` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\r\n"
					+ "  `ip` varchar(32) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\r\n"
					+ "  `time` bigint(20) NOT NULL DEFAULT 0,\r\n" + "  `count` int(5) NOT NULL DEFAULT 0,\r\n"
					+ "  UNIQUE KEY `name` (`name`,`ip`)\r\n" + ") ENGINE=InnoDB DEFAULT CHARSET=latin1;";

	public void createTable() {
		PreparedStatement ps = null;
		try {
			ps = sql.prepareStatement(TABLE);
			ps.executeUpdate();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "Can't create table", e);
			enabled = false;
		} finally {
			cleanup(null, ps);
		}
	}

	public void logPlayer(String player, String ip) {
		if (!enabled)
			return;
		PreparedStatement ps = null;
		try {
			ps = sql.prepareStatement(INSERT_IP);
			ps.setString(1, player);
			ps.setString(2, ip);
			ps.setLong(3, System.currentTimeMillis());
			ps.executeUpdate();
		} catch (SQLException e) {
			plugin.getLogger().log(Level.SEVERE, "Can't log player IP: " + player + ", " + ip, e);
		} finally {
			cleanup(null, ps);
		}
	}

	public void sendRelatedPlayers(CommandSender sender, String player, long days) {
		if (!enabled) {
			sender.sendMessage("§cMySQL đang tắt do config không hợp lệ, config lại rồi reload.");
			return;
		}
		PreparedStatement ps = null;
		ResultSet s = null;
		try {
			if (days > 0) {
				ps = sql.prepareStatement(RELATED_PLAYER_BY_IP_LAST_X_DAYS);
				ps.setLong(2, System.currentTimeMillis() - days * 86400000);
			} else
				ps = sql.prepareStatement(RELATED_PLAYER_BY_IP);
			ps.setString(1, player);
			s = ps.executeQuery();
			sender.sendMessage("§eNhững IP và tài khoản có liên quan đến §a" + player + "§e"
					+ ((days > 0) ? " trong §a" + days + " §engày trước" : "") + ":");
			while (s.next()) {
				String ip = s.getString("ip");
				String names = s.getString("names");
				String a = "§e" + ip + " (" + names.split("\\|").length + "): "
						+ Stream.of(names.split("\\|")).map(x -> "§a" + x).collect(Collectors.joining("§e, "));
				sender.sendMessage(a);
			}
		} catch (SQLException e) {
			sender.sendMessage("Có lỗi xảy ra, hãy thử lại.");
			plugin.getLogger().log(Level.SEVERE, "Can't get related players", e);
		} finally {
			cleanup(s, ps);
		}
	}

	public void sendPlayersFromIP(CommandSender sender, String ip) {
		if (!enabled) {
			sender.sendMessage("§cMySQL đang tắt do config không hợp lệ, config lại rồi reload.");
			return;
		}
		PreparedStatement ps = null;
		ResultSet s = null;
		try {
			ps = sql.prepareStatement(PLAYER_FROM_IP);
			ps.setString(1, ip);
			s = ps.executeQuery();
			List<String> names = new ArrayList<String>();
			while (s.next())
				names.add(s.getString("name"));
			if (names.size() > 0) {
				sender.sendMessage("§eCó §c§l" + names.size() + " §etài khoản liên quan đến IP §a" + ip + "§e:");
				String a = names.stream().map(x -> "§a" + x).collect(Collectors.joining("§e, "));
				sender.sendMessage(a);
			} else
				sender.sendMessage("§cKhông tìm thấy tài khoản nào liên quan đến IP §a" + ip);
		} catch (SQLException e) {
			sender.sendMessage("Có lỗi xảy ra, hãy thử lại.");
			plugin.getLogger().log(Level.SEVERE, "Can't get players from IP", e);
		} finally {
			cleanup(s, ps);
		}
	}

	protected void cleanup(ResultSet result, PreparedStatement statement) {
		if (result != null) {
			try {
				result.close();
			} catch (SQLException e) {
				plugin.getLogger().log(Level.SEVERE, "SQLException on cleanup", e);
			}
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				plugin.getLogger().log(Level.SEVERE, "SQLException on cleanup", e);
			}
		}
	}

	public void closeConnection() throws SQLException {
		sql.close();
		enabled = false;
	}

	public boolean isEnabled() {
		return enabled;
	}
}
