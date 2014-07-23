package pw.ollie.minimalclans;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import pw.ollie.minimalclans.clan.ClanManager;

public final class MCListener implements Listener {
	private final MinimalClans plugin;

	public MCListener(final MinimalClans plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void entityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player)
				|| !(event.getDamager() instanceof Player)) {
			return;
		}

		UUID id = event.getEntity().getUniqueId();
		UUID id2 = event.getDamager().getUniqueId();
		ClanManager clans = plugin.getClanManager();

		if (clans.getPlayerClan(id) == clans.getPlayerClan(id2)) {
			event.setCancelled(true);
		}
	}
}
