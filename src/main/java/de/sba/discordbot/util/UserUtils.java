package de.sba.discordbot.util;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;

import java.util.List;
import java.util.stream.Collectors;

public final class UserUtils {
	private UserUtils() {}

	public static String getName(Guild guild, String id) {
		Member member = guild.getMemberById(id);
		return member == null ? null : member.getEffectiveName();
	}

	public static List<String> findUsers(Guild guild, String nameSearch) {
		String lower = nameSearch.toLowerCase();
		return guild.getMembers().stream()
				.filter(member -> member.getEffectiveName().toLowerCase().contains(lower))
				.map(member -> member.getUser().getId())
				.collect(Collectors.toList());
	}
}
