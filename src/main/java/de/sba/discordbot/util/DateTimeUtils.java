package de.sba.discordbot.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class DateTimeUtils {
	private DateTimeUtils() {}

	public static String millisToString(long millis) {
		long since = millis;
		long hours = TimeUnit.MILLISECONDS.toHours(since);
		since -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(since);
		since -= TimeUnit.MINUTES.toMillis(minutes);
//        long seconds = TimeUnit.MILLISECONDS.toSeconds(since);
		return String.format("%d:%02d", hours, minutes);
	}

	public static String toString(Timestamp timestamp) {
		String s = null;
		if(timestamp != null) {
			DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.GERMAN);
			s = dateFormat.format(timestamp);
		}
		return s;
	}
}
