package pw.ollie.minimalclans.clan;

import java.util.List;
import java.util.UUID;

public final class Clan {
	private final String name;
	private final List<UUID> owners;
	private final List<UUID> members;

	public Clan(final String name, final List<UUID> owners,
			final List<UUID> members) {
		this.name = name;
		this.owners = owners;
		this.members = members;
	}

	public String getName() {
		return name;
	}

	public List<UUID> getOwners() {
		return owners;
	}

	public List<UUID> getMembers() {
		return members;
	}

	public boolean isMember(UUID uuid) {
		return members.contains(uuid);
	}

	public boolean isOwner(UUID uuid) {
		return owners.contains(uuid);
	}
}
