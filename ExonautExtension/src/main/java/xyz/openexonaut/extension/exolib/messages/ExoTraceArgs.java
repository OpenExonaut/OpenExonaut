package xyz.openexonaut.extension.exolib.messages;

import com.smartfoxserver.v2.extensions.*;

public class ExoTraceArgs {
    public final ExtensionLogLevel level;
    public final Object[] args;

    public ExoTraceArgs(ExtensionLogLevel level, Object... args) {
        this.level = level;
        this.args = args;
    }
}
