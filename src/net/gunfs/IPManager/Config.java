package net.gunfs.IPManager;

import org.bukkit.configuration.file.FileConfiguration;

public class Config {

	public static String MYSQL_HOST;
	public static int MYSQL_PORT;
	public static String MYSQL_USER;
	public static String MYSQL_PASS;
	public static String MYSQL_DATABASE;
	public static boolean LOBBY = false;

	public static void load() {
		IPManager ins = IPManager.getInstance();
		ins.reloadConfig();
		FileConfiguration config = ins.getConfig();
		MYSQL_HOST = config.getString("mysql.host");
		MYSQL_PORT = config.getInt("mysql.port");
		MYSQL_USER = config.getString("mysql.user");
		MYSQL_PASS = config.getString("mysql.pass");
		MYSQL_DATABASE = config.getString("mysql.database");
		LOBBY = config.getBoolean("lobby");
	}
}
