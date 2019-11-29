package net.gunfs.IPManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class LoginListener implements Listener {
	@EventHandler
	public void onLogin(PlayerLoginEvent e) {
		Player player = e.getPlayer();
		IPManager main = IPManager.getInstance();
		Bukkit.getScheduler().runTaskAsynchronously(main,
				() -> main.getMySQL().logPlayer(player.getName(), player.getAddress().getHostString()));
	}
}
