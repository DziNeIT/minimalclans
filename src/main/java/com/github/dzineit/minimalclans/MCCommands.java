package com.github.dzineit.minimalclans;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.dzineit.minimalclans.clan.Clan;
import com.github.dzineit.minimalclans.clan.ClanManager;

public final class MCCommands implements CommandExecutor {
	private final MinimalClans plugin;

	public MCCommands(final MinimalClans plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd,
			final String label, final String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED
					+ "Only players can perform clan-related commands");
			return true;
		}

		final Player player = (Player) sender;

		if (!player.hasPermission("minimalclans.use")) {
			sender.sendMessage(ChatColor.RED
					+ "You don't have permission for that!");
			return true;
		}

		final UUID playerId = player.getUniqueId();
		final ClanManager clans = plugin.getClanManager();
		final Server server = plugin.getServer();

		if (args.length == 0) {
			sender.sendMessage(ChatColor.GRAY + "MinimalClans - Help");
			sender.sendMessage(ChatColor.GRAY
					+ "/clan create [name] - Creates a clan");
			sender.sendMessage(ChatColor.GRAY
					+ "/clan disband - Disbands your current clan");
			sender.sendMessage(ChatColor.GRAY
					+ "/clan promote [player] - Promotes the given player");
			sender.sendMessage(ChatColor.GRAY
					+ "/clan invite [player] - Invites the given player to your clan");
			sender.sendMessage(ChatColor.GRAY
					+ "/clan warp [player] - Request to teleport to the given player");
			return true;
		}

		final String command = args[0].toLowerCase();

		if (command.equals("create")) {
			if (args.length == 1) {
				sender.sendMessage(ChatColor.RED + "Usage: /clan create [name]");
			} else {
				final String name = args[1];

				if (clans.isNameTaken(name)) {
					sender.sendMessage(ChatColor.RED
							+ "That name is already taken!");
					return true;
				}

				final List<UUID> owners = new ArrayList<>();
				final List<UUID> members = new ArrayList<>();

				owners.add(playerId);
				members.add(playerId);

				clans.createClan(name, owners, members);
				sender.sendMessage(ChatColor.GREEN
						+ "Successfully created clan " + name + "!");
			}
		} else if (command.equals("disband")) {
			clans.removeClan(clans.getPlayerClan(playerId).getName());
			sender.sendMessage(ChatColor.GREEN
					+ "Your clan has been disbanded!");
		} else if (command.equals("invite")) {
			if (args.length == 1) {
				sender.sendMessage(ChatColor.RED + "Usage: /clan invite [name]");
			} else {
				final Clan clan = clans.getPlayerClan(playerId);
				if (clan == null) {
					sender.sendMessage(ChatColor.RED + "You aren't in a clan!");
				} else if (!clan.isOwner(playerId)) {
					sender.sendMessage(ChatColor.RED
							+ "You don't have the required level of privilege to invite somebody!");
				} else {
					final String name = args[1];
					@SuppressWarnings("deprecation")
					final Player target = server.getPlayer(name);

					if (player != null) {
						clans.invite(clan, target.getUniqueId());

						// I hate eclipse's string formatting always
						target.sendMessage(ChatColor.GRAY
								+ "You've been invited to " + clan.getName()
								+ "!");
						target.sendMessage(ChatColor.GRAY + "Use /clan accept "
								+ clan.getName() + " to join");
						target.sendMessage(ChatColor.GRAY
								+ "Alternatively, type /clan decline "
								+ clan.getName() + " to snub the invitation");

						sender.sendMessage(ChatColor.GREEN + "Invited "
								+ target.getName());
					} else {
						sender.sendMessage(ChatColor.RED
								+ "That player is not online!");
					}
				}
			}
		} else if (command.equals("promote")) {
			if (args.length == 1) {
				sender.sendMessage(ChatColor.RED
						+ "Usage: /clan promote [player]");
			} else {
				final Clan clan = clans.getPlayerClan(playerId);
				final String promotee = args[1];
				@SuppressWarnings("deprecation")
				final Player promoteeP = server.getPlayer(promotee);

				if (promoteeP == null) {
					sender.sendMessage(ChatColor.RED
							+ "That player isn't online!");
				} else if (!clan.isMember(promoteeP.getUniqueId())) {
					sender.sendMessage(ChatColor.RED
							+ "That person isn't in your clan!");
				} else if (!clan.isOwner(player.getUniqueId())) {
					sender.sendMessage(ChatColor.RED
							+ "You don't have the required level of permission to promote somebody!");
				} else if (clan.isOwner(promoteeP.getUniqueId())) {
					sender.sendMessage(ChatColor.RED
							+ "That player can't be promoted any higher!");
				} else {
					clan.getOwners().add(promoteeP.getUniqueId());
					sender.sendMessage(ChatColor.GREEN + "Promoted " + promotee
							+ "!");
					promoteeP.sendMessage(ChatColor.GREEN
							+ "You have been promoted in your clan!");
				}
			}
		} else if (command.equals("warp")) {
			if (args.length == 1) {
				sender.sendMessage(ChatColor.RED + "Usage: /clan warp [player]");
			} else {
				final String target = args[1];
				@SuppressWarnings("deprecation")
				final Player tgt = server.getPlayer(target);

				if (tgt == null) {
					sender.sendMessage(ChatColor.RED
							+ "That player isn't online!");
				} else if (clans.getPlayerClan(tgt.getUniqueId()) != clans
						.getPlayerClan(playerId)) {
					sender.sendMessage(ChatColor.RED
							+ "That player is in a different clan to you!");
				} else {
					player.teleport(tgt);
					sender.sendMessage(ChatColor.GREEN + "Warped to " + target);
					tgt.sendMessage(ChatColor.GRAY + player.getName()
							+ " warped to you!");
				}
			}
		} else if (command.equals("accept") || command.equals("decline")) {
			if (args.length == 1) {
				sender.sendMessage(ChatColor.RED + "Usage: /clan " + command
						+ " [clan]");
			} else {
				final String clanName = args[1];
				final Clan clan = clans.getClanFromName(clanName);
				if (clans.hasInvitation(playerId, clan)) {
					if (command.equals("accept")) {
						clans.acceptInvitation(clan, playerId);
						sender.sendMessage(ChatColor.GREEN + "You have joined "
								+ clan.getName());

						for (final UUID member : clan.getMembers()) {
							server.getPlayer(member).sendMessage(
									ChatColor.GREEN + "Player "
											+ sender.getName()
											+ " has joined the clan!");
						}
					} else if (command.equals("decline")) {
						clans.declineInvitation(clan, playerId);
						sender.sendMessage(ChatColor.GRAY
								+ "You snubbed the invitation to join "
								+ clan.getName());
					}
				} else {
					sender.sendMessage(ChatColor.RED
							+ "You don't have an invitation from that clan!");
				}
			}
		}

		return true;
	}
}
