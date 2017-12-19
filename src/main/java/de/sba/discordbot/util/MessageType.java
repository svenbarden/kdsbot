package de.sba.discordbot.util;

public enum MessageType {
	PLAIN("", ""), FORMATTED("```\n", "\n```"), MARKDOWN("```markdown\n", "\n```");

	private final String prefix;
	private final String suffix;

	MessageType(String prefix, String suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
	}

	public String format(String msg) {
		return prefix + msg + suffix;
	}
}
