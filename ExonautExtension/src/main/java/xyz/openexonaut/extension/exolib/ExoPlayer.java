package xyz.openexonaut.extension.exolib;

import java.awt.*;
import java.util.*;
import java.util.List;

import com.badlogic.gdx.physics.box2d.*;

import com.smartfoxserver.v2.api.*;
import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.variables.*;

import xyz.openexonaut.extension.exolib.geo.*;

public class ExoPlayer {
    public final User user;
    public final String nickname;
    public final int suitId;

    public int id = 0;
    public String avatarState = "halted";
    public String clientState = "";
    public float x = 0;
    public float y = 0;
    public int health = 0;
    public int moveState = 0;
    public int moveDir = 0;
    public int boost = 0;
    public int teamBoost = 0;
    public int hacks = 0;
    public int weaponId = 0;

    private int crashTimer = 8;

    public Body body = null;

    public ExoPlayer(User user) {
        this.user = user;
        this.nickname = user.getName();
        this.suitId = user.getVariable("suitId").getIntValue();
    }

    // obtains a mutex on the player for the whole function
    @SuppressWarnings("rawtypes")
    public void updateVariables(List changedVariables, ISFSApi sfsApi) {
        synchronized (this) {
            for (Object o : changedVariables) {
                UserVariable var = (UserVariable) o;
                if (var.getName().equals("clientState")) {
                    this.clientState = var.getStringValue();
                    // fixes spawning across games to do this instead of having avatarState captured
                    // from the beginning
                    if (var.getStringValue().equals("playing")) {
                        List<UserVariable> variableUpdate = new ArrayList<>();
                        variableUpdate.add(new SFSUserVariable("avatarState", "captured"));
                        sfsApi.setUserVariables(user, variableUpdate);
                    }
                } else if (var.getName().equals("avatarState")) {
                    this.avatarState = var.getStringValue();
                } else if (var.getName().equals("x")) {
                    this.x = (float) var.getDoubleValue().doubleValue();
                } else if (var.getName().equals("y")) {
                    this.y = (float) var.getDoubleValue().doubleValue();
                } else if (var.getName().equals("health")) {
                    this.health = var.getIntValue();
                } else if (var.getName().equals("moveState")) {
                    this.moveState = var.getIntValue();
                } else if (var.getName().equals("moveDir")) {
                    this.moveDir = var.getIntValue();
                } else if (var.getName().equals("boost")) {
                    this.health = var.getIntValue();
                } else if (var.getName().equals("teamBoost")) {
                    this.teamBoost = var.getIntValue();
                } else if (var.getName().equals("hacks")) {
                    this.hacks = var.getIntValue();
                } else if (var.getName().equals("weaponId")) {
                    this.weaponId = var.getIntValue();
                }
            }
        }
    }

    // obtains a mutex on the player for the whole function
    public void secondlyTick(ISFSApi sfsApi) {
        synchronized (this) {
            if (crashTimer > 0
                    && user.getVariable("clientState").getStringValue().equals("playing")) {
                if (user.getVariable("avatarState").getStringValue().equals("captured")) {
                    if (--crashTimer == 3) {
                        List<UserVariable> avatarStateUpdate = new ArrayList<>();
                        avatarStateUpdate.add(new SFSUserVariable("avatarState", "invincible"));
                        sfsApi.setUserVariables(user, avatarStateUpdate);
                    }
                } else if (user.getVariable("avatarState").getStringValue().equals("invincible")) {
                    if (--crashTimer == 0) {
                        List<UserVariable> avatarStateUpdate = new ArrayList<>();
                        avatarStateUpdate.add(new SFSUserVariable("avatarState", "normal"));
                        sfsApi.setUserVariables(user, avatarStateUpdate);
                    }
                }
            }
        }
    }

    // obtains a brief mutex on the player to cache the position for the function's lifetime
    public void draw(Graphics g, ExoMap map) {
        float cachedX = 0;
        float cachedY = 0;

        synchronized (this) {
            cachedX = x;
            cachedY = y;
        }

        // the center of the character collider in the game is set to y = 6f, despite the total
        // height being 13f. hope that's not too important
        ExoInt2DVector drawCenter =
                new Exo2DVector(cachedX, cachedY + 6.5f).convertNativeToDraw(map.scale);

        ExoInt2DVector drawHead =
                new Exo2DVector(cachedX - 1.5f, cachedY + 6.5f + 5f + 1.5f)
                        .convertNativeToDraw(map.scale);
        ExoInt2DVector drawFeet =
                new Exo2DVector(cachedX - 1.5f, cachedY + 6.5f - 5f + 1.5f)
                        .convertNativeToDraw(map.scale);
        ExoInt2DVector drawBody =
                new Exo2DVector(cachedX - 1.5f, cachedY + 6.5f + 5f).convertNativeToDraw(map.scale);

        int doubleRadius = (int) (3 * map.scale);
        int height = (int) (10 * map.scale);

        // collider top
        // TODO: where actually is the head, if headshots are still in?
        g.setColor(Color.GREEN);
        g.fillOval(drawHead.x, drawHead.y, doubleRadius, doubleRadius);

        g.setColor(Color.BLUE);
        g.fillOval(drawFeet.x, drawFeet.y, doubleRadius, doubleRadius);
        g.fillRect(drawBody.x, drawBody.y, doubleRadius, height);

        g.setColor(Color.RED);
        g.drawLine(drawCenter.x, drawCenter.y, drawCenter.x, drawCenter.y);

        // TODO: disjoint phantoms?
    }

    public void hit(ExoBullet bullet, int where) {
        // TODO: hit handling
        String place = "";
        switch (where) {
            case 1:
                place = "head";
                break;
            case 3:
                place = "feet";
                break;
            default:
                place = "body";
        }
        System.out.println(
                nickname
                        + " was hit in the "
                        + place
                        + " by bullet id "
                        + bullet.num
                        + " (-1 for sniper), shot by "
                        + bullet.player.nickname);
    }
}
