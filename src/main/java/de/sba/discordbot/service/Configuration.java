package de.sba.discordbot.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@org.springframework.context.annotation.Configuration
@ConfigurationProperties(prefix = "de.sba.discordbot")
public class Configuration {
    private List<String> nicNames = new ArrayList<>();
    private Map<String, List<String>> autoTopicTargets = new HashMap<>();

    public List<String> getNicNames() {
        return nicNames;
    }

    public Map<String, List<String>> getAutoTopicTargets() {
        return autoTopicTargets;
    }
}
