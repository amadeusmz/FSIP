package net.gunfs.IPManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.lenis0012.bukkit.ls.LoginEvent;
import com.lenis0012.bukkit.ls.RegisterEvent;

public class LSListener implements Listener {
	private IPManager main;

	public LSListener(IPManager plugin) {
		main = plugin;
	}

	@EventHandler
	public void onLogin(LoginEvent e) {
		Player player = e.getPlayer();
		Bukkit.getScheduler().runTaskAsynchronously(main,
				() -> main.getMySQL().logPlayer(player.getName(), player.getAddress().getHostString()));
	}
	
	@EventHandler
	public void onRegister(RegisterEvent e) {
		Player player = e.getPlayer();
		Bukkit.getScheduler().runTaskAsynchronously(main,
				() -> main.getMySQL().logPlayer(player.getName(), player.getAddress().getHostString()));
	}
}
