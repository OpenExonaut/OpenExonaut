package xyz.openatbp.extension;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.smartfoxserver.v2.entities.data.ISFSObject;

public class StressLogger {
    private String command;
    private int count;
    private long lastUsed;
    private Map<String, Integer> userCount;
    private Map<String, Long> userUsage;

    public StressLogger(String command) {
        this.command = command;
        this.count = 1;
        this.lastUsed = System.currentTimeMillis();
        this.userCount = new HashMap<>();
        this.userUsage = new HashMap<>();
    }

    public void update(ISFSObject params) {
        String target = this.getTarget(params);
        if (target != null) {
            Console.debugLog(target + " used " + this.command);
        }
        long timeDiff = System.currentTimeMillis() - this.lastUsed;
        if (this.userCount.size() != 0 || this.getTarget(params) != null) {
            if (target != null) {
                if (this.userCount.containsKey(target) && this.userUsage.containsKey(target)) {
                    timeDiff = System.currentTimeMillis() - this.userUsage.get(target);
                    if (timeDiff >= 50) this.userCount.put(target, 1);
                    else {
                        this.userCount.put(target, this.userCount.get(target) + 1);
                    }
                } else {
                    // Console.debugLog("HELP ME");
                    this.userCount.put(target, 1);
                }
                this.userUsage.put(target, System.currentTimeMillis());
            } else Console.logWarning("NULL");
            Set<String> keySet = new HashSet<>(this.userCount.keySet());
            for (String user : keySet) {
                if (this.userCount.get(user) >= (this.command.contains("actor_data") ? 100 : 10))
                    Console.logWarning(
                            user
                                    + " has used "
                                    + this.command
                                    + " "
                                    + this.userCount.get(user)
                                    + " times! Last used "
                                    + (System.currentTimeMillis() - this.userUsage.get(user))
                                    + " ms ago.");
            }
        } else {
            if (timeDiff >= 50) this.count = 1;
            else this.count++;
            if (this.count >= (this.command.contains("actor_data") ? 200 : 100))
                Console.logWarning(
                        this.command
                                + " has been used "
                                + this.count
                                + " times and was last used "
                                + timeDiff
                                + " ms ago.");
        }
        this.lastUsed = System.currentTimeMillis();
    }

    private String getTarget(ISFSObject params) {
        switch (this.command) {
            case "cmd_move_actor":
                return params.getUtfString("i");
            default:
                if (params.getUtfString("id") != null) return params.getUtfString("id");
        }
        return null;
    }
}
