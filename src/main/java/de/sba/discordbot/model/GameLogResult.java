package de.sba.discordbot.model;

import org.apache.commons.lang3.mutable.MutableLong;

import java.io.Serializable;
import java.util.Map;

public class GameLogResult implements Serializable {
    private String from;
    private String to;
    private Map<String, Map<String, MutableLong>> data;

    public String getFrom() {
        return from;
    }

    public GameLogResult setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getTo() {
        return to;
    }

    public GameLogResult setTo(String to) {
        this.to = to;
        return this;
    }

    public Map<String, Map<String, MutableLong>> getData() {
        return data;
    }

    public GameLogResult setData(Map<String, Map<String, MutableLong>> data) {
        this.data = data;
        return this;
    }
}
