package de.sba.discordbot.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MessageBuilder {
	private static final int MAX_CHARS = 1800;
	private MessageType messageType = MessageType.PLAIN;
	private final List<CharSequence> messageParts = new ArrayList<>();
	private final StringBuilder currentPart = new StringBuilder();

	public static MessageBuilder build() {
		return new MessageBuilder();
	}

	public static MessageBuilder build(CharSequence msg) {
		return new MessageBuilder().add(msg);
	}

	public static MessageBuilder build(MessageType messageType) {
		return new MessageBuilder().messageType(messageType);
	}

	public static MessageBuilder build(MessageType messageType, CharSequence msg) {
		return new MessageBuilder().messageType(messageType).add(msg);
	}

	public MessageBuilder nextPart() {
		messageParts.add(currentPart.toString());
		currentPart.setLength(0);
		return this;
	}

	public MessageBuilder append(int msg) {
		currentPart.append(msg);
		return this;
	}

	public MessageBuilder append(long msg) {
		currentPart.append(msg);
		return this;
	}

	public MessageBuilder append(CharSequence msg) {
		currentPart.append(msg);
		return this;
	}

	public MessageBuilder add(CharSequence msg) {
		currentPart.append(msg);
		return this;
	}

	public MessageBuilder messageType(MessageType messageType) {
		this.messageType = messageType;
		return this;
	}

	public List<String> toLimitedString() {
		List<String> output = new ArrayList<>();
		StringBuilder content = new StringBuilder();
		messageParts.forEach(messagePart -> {
			int messageLength = messagePart.length();
			if(content.length() + messageLength > MAX_CHARS) {
				output.add(messageType.format(content.toString()));
				content.setLength(0);
			}
			content.append(messagePart);
		});
		if(content.length() > 0) {
			output.add(messageType.format(content.toString()));
		}
		return Collections.unmodifiableList(output);
	}

	@Override
	public String toString() {
		String collected = messageParts.stream().collect(Collectors.joining());
		if(currentPart.length() > 0) {
			collected += currentPart;
		}
		return messageType.format(collected);
	}
}
