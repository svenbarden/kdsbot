package de.sba.discordbot.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class GameLogFilter implements Serializable {
	private Set<String> users = new HashSet<>();
	private Set<String> games = new HashSet<>();

	private GameLogFilter() {}

	public static GameLogFilter build(String[] args, int initialIndex, Function<String, Collection<String>> usernameToId) {
		GameLogFilter filter = new GameLogFilter();
		for(int i = initialIndex; i < args.length; i++) {
			String arg = args[i];
			String[] extracted = arg.split(":", 2);
			if(extracted.length == 2) {
				String value = extracted[1];
				switch (extracted[0].trim()) {
					case "u":
					case "user":
						filter.users.addAll(usernameToId.apply(value));
						break;
					case "g":
					case "game":
						filter.games.add(value);
						break;
				}
			}
		}
		return filter;
	}

	public Set<String> getUsers() {
		return Collections.unmodifiableSet(users);
	}

	public Set<String> getGames() {
		return Collections.unmodifiableSet(games);
	}
}
