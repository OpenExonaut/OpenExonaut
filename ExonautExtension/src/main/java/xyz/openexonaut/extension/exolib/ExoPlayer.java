package xyz.openexonaut.extension.exolib;

import java.awt.*;
import java.util.*;
import java.util.List;

import com.smartfoxserver.v2.api.*;
import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.variables.*;

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

    public ExoPlayer (User user) {
        this.user = user;
        this.nickname = user.getName();
        this.suitId = user.getVariable("suitId").getIntValue();
    }

    // obtains a mutex on the player for the whole function
    @SuppressWarnings("rawtypes")
    public void updateVariables (List changedVariables, ISFSApi sfsApi) {
        synchronized (this) {
            for (Object o : changedVariables) {
                UserVariable var = (UserVariable)o;
                if (var.getName().equals("clientState")) {
                    this.clientState = var.getStringValue();
                    // fixes spawning across games to do this instead of having avatarState captured from the beginning
                    if (var.getStringValue().equals("playing")) {
                        List<UserVariable> variableUpdate = new ArrayList<>();
                        variableUpdate.add(new SFSUserVariable("avatarState", "captured"));
                        sfsApi.setUserVariables(user, variableUpdate);
                    }
                }
                else if (var.getName().equals("avatarState")) {
                    this.avatarState = var.getStringValue();
                }
                else if (var.getName().equals("x")) {
                    this.x = (float)var.getDoubleValue().doubleValue();
                }
                else if (var.getName().equals("y")) {
                    this.y = (float)var.getDoubleValue().doubleValue();
                }
                else if (var.getName().equals("health")) {
                    this.health = var.getIntValue();
                }
                else if (var.getName().equals("moveState")) {
                    this.moveState = var.getIntValue();
                }
                else if (var.getName().equals("moveDir")) {
                    this.moveDir = var.getIntValue();
                }
                else if (var.getName().equals("boost")) {
                    this.health = var.getIntValue();
                }
                else if (var.getName().equals("teamBoost")) {
                    this.teamBoost = var.getIntValue();
                }
                else if (var.getName().equals("hacks")) {
                    this.hacks = var.getIntValue();
                }
                else if (var.getName().equals("weaponId")) {
                    this.weaponId = var.getIntValue();
                }
            }
        }
    }

    // obtains a mutex on the player for the whole function
    public void secondlyTick (ISFSApi sfsApi) {
        synchronized (this) {
            if (crashTimer > 0 && user.getVariable("clientState").getStringValue().equals("playing")) {
                if (user.getVariable("avatarState").getStringValue().equals("captured")) {
                    if (--crashTimer == 3) {
                        List<UserVariable> avatarStateUpdate = new ArrayList<>();
                        avatarStateUpdate.add(new SFSUserVariable("avatarState", "invincible"));
                        sfsApi.setUserVariables(user, avatarStateUpdate);
                    }
                }
                else if (user.getVariable("avatarState").getStringValue().equals("invincible")) {
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
    public void draw (Graphics g, ExoMap map) {
        float cachedX = 0;
        float cachedY = 0;

        synchronized (this) {
            cachedX = x;
            cachedY = y;
        }

        int drawPositionX = (int)cachedX;
        int drawLeftEdgeX = (int)(cachedX - 1.5f);

        int drawPositionY = (int)cachedY;

        // collider top
        // TODO: where actually is the head, if headshots are still in?
        g.setColor(Color.GREEN);
        // + 6: center
        // + 5: height / 2
        // CharacterCollider height includes the hemispheres, so this gets us to the top already
        g.fillOval(drawLeftEdgeX, (int)(cachedY + 6f + 5f), 3, 3);

        g.setColor(Color.BLUE);
        // this on the other hand, the first + 1.5 only gets us to the semicircle's center
        g.fillOval(drawLeftEdgeX, (int)(cachedY + 6f - 5f + 1.5f + 1.5f), 3, 3);
        g.fillRect(drawLeftEdgeX, (int)(cachedY + 6f + 5f - 1.5f), 3, 7);

        // position (bottom-center of model)
        g.setColor(Color.RED);
        g.drawLine(drawPositionX, drawPositionY, drawPositionX, drawPositionY);

        // TODO: disjoint phantoms?
    }

    // obtains a brief mutex on the player to cache the position for the function's lifetime
    public int collision (float checkX, float checkY) {
        float cachedX = 0;
        float cachedY = 0;

        synchronized (this) {
            cachedX = x;
            cachedY = y;
        }

        // TODO: disjoint phantoms?

        // insideness of rectangle
        if (checkX > cachedX - 1.5f && checkX < cachedX + 1.5f && checkY > cachedY + 2.5f && checkY < cachedY + 9.5f) return 1;
        // insideness of bottom semicircle
        if (Math.hypot(checkX - cachedX, checkY - (cachedY + 2.5f)) < 1.5) return 2;
        // insideness of top semicircle
        if (Math.hypot(checkX - cachedX, checkY - (cachedY + 9.5f)) < 1.5) return 3;
        return 0;
    }

    public void hit (ExoBullet bullet) {
        // TODO: hit handling
    }
}
