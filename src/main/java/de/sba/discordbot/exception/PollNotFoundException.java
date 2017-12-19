package de.sba.discordbot.exception;

public class PollNotFoundException extends Exception {

	public PollNotFoundException() {
	}

	public PollNotFoundException(String message) {
		super(message);
	}

	public PollNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public PollNotFoundException(Throwable cause) {
		super(cause);
	}

	public PollNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
