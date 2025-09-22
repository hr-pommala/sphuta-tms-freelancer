package net.sphuta.tms.freelancer.config;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * <h2>TmsUserMessagesConfig</h2>
 *
 * Configuration class for managing user-related messages in the TMS application.
 * <p>
 * This class provides a centralized way to define and retrieve messages associated
 * with user operations such as creation, retrieval, updating, and deletion.
 * </p>
 */
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
