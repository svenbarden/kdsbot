package de.sba.discordbot.util;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;

public final class UserUtils {
	private UserUtils() {}

	public static String getName(Guild guild, String id) {
		Member member = guild.getMemberById(id);
		return member == null ? null : member.getEffectiveName();
	}
}
