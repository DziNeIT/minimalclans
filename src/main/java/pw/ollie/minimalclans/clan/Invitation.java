package pw.ollie.minimalclans.clan;

import java.util.UUID;

public final class Invitation {
	private final Clan clan;
	private final UUID invitee;

	public Invitation(final Clan clan, final UUID invitee) {
		this.clan = clan;
		this.invitee = invitee;
	}

	public Clan getClan() {
		return clan;
	}

	public UUID getInvitee() {
		return invitee;
	}
}
