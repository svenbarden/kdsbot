package de.sba.discordbot.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopicConfiguration implements Serializable {
    private String test;
    private Map<String, List<String>> autoTopicTargets = new HashMap<>();

    public Map<String, List<String>> getAutoTopicTargets() {
        return autoTopicTargets;
    }

    public void setAutoTopicTargets(Map<String, List<String>> autoTopicTargets) {
        this.autoTopicTargets = autoTopicTargets;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }
}
