package pw.ollie.minimalclans.clan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bson.BSONDecoder;
import org.bson.BSONEncoder;
import org.bson.BSONObject;
import org.bson.BasicBSONDecoder;
import org.bson.BasicBSONEncoder;
import org.bson.BasicBSONObject;

import pw.ollie.minimalclans.MinimalClans;

import com.google.common.io.Files;

public final class ClanManager {
	private final MinimalClans plugin;
	private final Map<String, Clan> clans;
	private final List<Invitation> invitations;

	public ClanManager(final MinimalClans plugin) {
		this.plugin = plugin;

		clans = new HashMap<>();
		invitations = new ArrayList<>();
	}

	public Clan createClan(final String name, final List<UUID> owners,
			final List<UUID> members) {
		final Clan clan = new Clan(name, owners, members);
		clans.put(name, clan);
		return clan;
	}

	public Clan removeClan(final String name) {
		return clans.remove(name);
	}

	public Clan getClanFromName(final String name) {
		return clans.get(name);
	}

	public boolean isNameTaken(final String name) {
		return clans.containsKey(name);
	}

	public Clan getPlayerClan(final UUID player) {
		for (final Clan clan : clans.values()) {
			if (clan.isMember(player)) {
				return clan;
			}
		}
		return null;
	}

	public boolean hasClan(final UUID player) {
		for (final Clan clan : clans.values()) {
			if (clan.isMember(player)) {
				return true;
			}
		}
		return false;
	}

	public void invite(final Clan clan, final UUID invitee) {
		invitations.add(new Invitation(clan, invitee));
	}

	public boolean hasInvitation(final UUID player, final Clan clan) {
		for (final Invitation invite : invitations) {
			if (invite.getClan() == clan && invite.getInvitee() == player) {
				return true;
			}
		}
		return false;
	}

	public void acceptInvitation(final Clan clan, final UUID player) {
		for (final Invitation invite : invitations) {
			if (invite.getClan() == clan && invite.getInvitee() == player) {
				clan.getMembers().add(player);
				invitations.remove(invite);
				return;
			}
		}
	}

	public void declineInvitation(final Clan clan, final UUID player) {
		for (final Invitation invite : invitations) {
			if (invite.getClan() == clan && invite.getInvitee() == player) {
				invitations.remove(invite);
				return;
			}
		}
	}

	public void loadClans() throws IOException {
		final BSONDecoder decoder = new BasicBSONDecoder();
		final File dir = plugin.getClansDirectory();

		for (final File clanFile : dir.listFiles()) {
			if (!clanFile.getName().endsWith(".ecl")) {
				continue;
			}

			final byte[] data = Files.toByteArray(clanFile);
			final BSONObject obj = decoder.readObject(data);
			if (obj instanceof BasicBSONObject) {
				final BasicBSONObject bson = (BasicBSONObject) obj;

				final String clanName = bson.getString("name");
				final String ownersStr = bson.getString("owners");
				final String membersStr = bson.getString("members");

				final List<UUID> owners = new ArrayList<>();
				for (final String idStr : ownersStr.split(",")) {
					owners.add(UUID.fromString(idStr));
				}

				final List<UUID> members = new ArrayList<>();
				for (final String idStr : membersStr.split(",")) {
					members.add(UUID.fromString(idStr));
				}

				createClan(clanName, owners, members);
			}
		}
	}

	public void saveClans() throws IOException {
		final BSONEncoder encoder = new BasicBSONEncoder();
		final File dir = plugin.getClansDirectory();
		final List<String> done = new ArrayList<>();

		for (final Clan clan : clans.values()) {
			final File clanFile = new File(dir, clan.getName() + ".ecl");
			done.add(clanFile.getAbsolutePath());

			if (clanFile.exists()) {
				clanFile.delete();
			}
			clanFile.createNewFile();

			final BasicBSONObject bson = new BasicBSONObject();

			StringBuilder builder = new StringBuilder();
			for (final UUID owner : clan.getOwners()) {
				builder.append(owner.toString()).append(",");
			}
			builder.setLength(builder.length() - 1);
			final String ownersStr = builder.toString();

			builder = new StringBuilder();
			for (final UUID member : clan.getMembers()) {
				builder.append(member.toString()).append(",");
			}
			builder.setLength(builder.length() - 1);
			final String membersStr = builder.toString();

			bson.put("name", clan.getName());
			bson.put("owners", ownersStr);
			bson.put("members", membersStr);

			final byte[] data = encoder.encode(bson);
			Files.write(data, clanFile);
		}

		for (final File file : dir.listFiles()) {
			if (!done.contains(file.getAbsolutePath())) {
				file.delete();
			}
		}
	}
}
