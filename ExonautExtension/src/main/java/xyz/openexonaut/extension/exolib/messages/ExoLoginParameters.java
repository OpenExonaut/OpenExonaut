package xyz.openexonaut.extension.exolib.messages;

import com.smartfoxserver.bitswarm.sessions.*;

public class ExoLoginParameters {
    public final Session session;
    public final String username;
    public final String password;

    public ExoLoginParameters(Session session, String username, String password) {
        this.session = session;
        this.username = username;
        this.password = password;
    }
}
