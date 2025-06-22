package xyz.openexonaut.extension.exolib.resources;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.*;

import com.smartfoxserver.bitswarm.sessions.*;
import com.smartfoxserver.v2.*;

public final class ExoDB {
    private static MongoClient mongoClient = null;
    private static MongoDatabase database = null;
    private static MongoCollection<Document> userCollection = null;

    private ExoDB() {}

    public static void init(String mongoURI) {
        destroy();

        mongoClient = MongoClients.create(mongoURI);
        database = mongoClient.getDatabase("openexonaut");
        userCollection = database.getCollection("users");
    }

    private static Document getFullUserObject(String tegid) {
        return userCollection.find(Filters.eq("user.TEGid", tegid)).first();
    }

    // returns: dname if valid non-guest login, null otherwise; input password is encrypted by SFS2X
    public static String checkLogin(Session session, String username, String password) {
        Document userObject = getFullUserObject(username);
        if (userObject != null) {
            if (SmartFoxServer.getInstance()
                    .getAPIManager()
                    .getSFSApi()
                    .checkSecurePassword(
                            session,
                            userObject.get("user", Document.class).getString("authid"),
                            password)) {
                return userObject.get("user", Document.class).getString("dname");
            }
        }
        return null;
    }

    public static Document getPlayerObject(String tegid) {
        Document userObject = getFullUserObject(tegid);
        if (userObject != null) {
            return userObject.get("player", Document.class);
        }
        return null;
    }

    public static void destroy() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
