package xyz.openexonaut.extension.exolib.resources;

import java.util.*;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.*;

import com.smartfoxserver.bitswarm.sessions.*;
import com.smartfoxserver.v2.*;

import xyz.openexonaut.extension.exolib.data.*;

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

    // returns: dname if valid non-guest login, null otherwise; input password is encrypted by SFS2X
    public static String checkLogin(Session session, String username, String password) {
        Document document =
                userCollection
                        .find(Filters.eq("user.TEGid", username))
                        .projection(Projections.include("user.authid", "user.dname"))
                        .first();
        if (document != null) {
            if (SmartFoxServer.getInstance()
                    .getAPIManager()
                    .getSFSApi()
                    .checkSecurePassword(
                            session,
                            document.getEmbedded(List.of("user", "authid"), String.class),
                            password)) {
                return document.getEmbedded(List.of("user", "dname"), String.class);
            }
        }
        return null;
    }

    public static int getPlayerLevel(String tegid) {
        Document userObject =
                userCollection
                        .find(Filters.eq("user.TEGid", tegid))
                        .projection(Projections.include("player.Level"))
                        .first();
        if (userObject != null) {
            return userObject.getEmbedded(List.of("player", "Level"), Integer.class);
        }
        throw new RuntimeException(
                String.format("null user when fetching level for tegid %s", tegid));
    }

    public static ExoPlayerDBUpdateOutput endOfMatchPlayerUpdate(
            String tegid, ExoPlayerDBUpdateInput input) {
        // TODO: atomicize somehow
        // TODO: achievements
        Document firstPass =
                userCollection.findOneAndUpdate(
                        Filters.eq("user.TEGid", tegid),
                        Updates.combine(
                                Updates.inc("player.XP", input.award),
                                Updates.inc("player.Credits", input.award),
                                Updates.set("player.LastSuit", input.suit)),
                        new FindOneAndUpdateOptions()
                                .projection(
                                        Projections.include(
                                                "player.Level", "player.XP", "player.Credits"))
                                .returnDocument(ReturnDocument.AFTER));
        if (firstPass != null) {
            int level = firstPass.getEmbedded(List.of("player", "Level"), Integer.class);
            int xp = firstPass.getEmbedded(List.of("player", "XP"), Integer.class);
            int credits = firstPass.getEmbedded(List.of("player", "Credits"), Integer.class);

            ExoLevel calculatedLevel = ExoGameData.getLevelFromXP(level, xp);

            if (level != calculatedLevel.ID) {
                level = calculatedLevel.ID;

                userCollection.updateOne(
                        Filters.eq("user.TEGid", tegid),
                        Updates.combine(
                                Updates.set("player.Level", level),
                                Updates.set("player.Rank", calculatedLevel.Rank)));
            }

            return new ExoPlayerDBUpdateOutput(level, xp, credits);
        }
        throw new RuntimeException(String.format("failed player update for tegid %s", tegid));
    }

    public static void destroy() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
        }
    }

    public static class ExoPlayerDBUpdateInput {
        public final int award;
        public final int suit;

        public ExoPlayerDBUpdateInput(int award, int suit) {
            this.award = award;
            this.suit = suit;
        }
    }

    public static class ExoPlayerDBUpdateOutput {
        public final int level;
        public final int xp;
        public final int credits;

        public ExoPlayerDBUpdateOutput(int level, int xp, int credits) {
            this.level = level;
            this.xp = xp;
            this.credits = credits;
        }
    }
}
