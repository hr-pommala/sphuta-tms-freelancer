package net.sphuta.tms.freelancer.config;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class TmsUserMessagesConfig {
    private final Map<String, String> userMessages;

    public TmsUserMessagesConfig() {
        userMessages = new HashMap<>();
        userMessages.put("create", "User created successfully");
        userMessages.put("retrieve", "User retrieved successfully");
        userMessages.put("retrieve-all", "All users retrieved successfully");
        userMessages.put("update", "User updated successfully");
        userMessages.put("partial-update", "User partially updated successfully");
        userMessages.put("delete", "User deleted successfully");
    }

    public Map<String, String> getUser() {
        return userMessages;
    }
}
