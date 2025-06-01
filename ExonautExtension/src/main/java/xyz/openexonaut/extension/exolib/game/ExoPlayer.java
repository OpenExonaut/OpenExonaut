package xyz.openexonaut.extension.exolib.game;

import java.awt.*;
import java.util.*;
import java.util.List;

import com.badlogic.gdx.physics.box2d.*;

import com.smartfoxserver.v2.api.*;
import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;
import com.smartfoxserver.v2.entities.variables.*;

import xyz.openexonaut.extension.exolib.data.*;
import xyz.openexonaut.extension.exolib.enums.*;
import xyz.openexonaut.extension.exolib.geo.*;
import xyz.openexonaut.extension.exolib.map.*;
import xyz.openexonaut.extension.exolib.messages.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class ExoPlayer {
    public final User user;

    public ExoSuit suit = null;

    public float crashTimer = 5f;
    public float invicibilityTimer = 0f;
    public float healthRefillTimer = 0f;
    public float boostTimer = 0f;
    public float teamBoostTimer = 0f;

    public int crashes = 0;
    public int fuelConsumed = 0;
    public int hacksInvisible = 0;
    public int hacksSpeed = 0;
    public int hacksDamageBoost = 0;
    public int hacksArmorBoost = 0;

    public long lastNano = System.nanoTime();

    public Body body = null;

    public ExoPlayer(User user) {
        this.user = user;
    }

    public String getClientState() {
        return user.getVariable("clientState").getStringValue();
    }

    public String getAvatarState() {
        return user.getVariable("avatarState").getStringValue();
    }

    public float getX() {
        return (float) user.getVariable("x").getDoubleValue().doubleValue();
    }

    public float getY() {
        return (float) user.getVariable("y").getDoubleValue().doubleValue();
    }

    public float getHealth() {
        return (float) user.getVariable("health").getDoubleValue().doubleValue();
    }

    public int getMoveState() {
        return user.getVariable("moveState").getIntValue();
    }

    public int getMoveDir() {
        return user.getVariable("moveDir").getIntValue();
    }

    public int getBoost() {
        return user.getVariable("boost").getIntValue();
    }

    public int getTeamBoost() {
        return user.getVariable("teamBoost").getIntValue();
    }

    public int getHacks() {
        return user.getVariable("hacks").getIntValue();
    }

    public int getWeaponId() {
        return user.getVariable("weaponId").getIntValue();
    }

    @SuppressWarnings("rawtypes")
    public void updateVariables(List changedVariables, ISFSApi sfsApi) {
        for (Object o : changedVariables) {
            UserVariable var = (UserVariable) o;
            if (var.getName().equals("clientState")) {
                // fixes spawning across games to do this instead of having avatarState captured
                // from the beginning
                if (var.getStringValue().equals("playing")) {
                    sfsApi.setUserVariables(
                            user, List.of(new SFSUserVariable("avatarState", "captured")));
                }
            }
        }
    }

    public void setVariables(List<UserVariable> variables, Room room) {
        room.getExtension()
                .handleInternalMessage("setVariables", new ExoVariableUpdate(user, variables));
    }

    public void setSuit(ExoSuit suit) {
        this.suit = suit;
    }

    // return value: seconds since last tick
    public float tick(Room room) {
        long nano = System.nanoTime();
        float deltaTime = (nano - lastNano) / 1_000_000_000f;
        lastNano = nano;

        List<UserVariable> variableChanges = new ArrayList<>();

        if (getClientState().equals("playing")) {
            if (getAvatarState().equals("captured")) {
                crashTimer -= deltaTime;
                if (crashTimer <= 0f) {
                    invicibilityTimer = 3f + crashTimer;
                    variableChanges.add(new SFSUserVariable("avatarState", "invincible"));
                }
            } else if (getAvatarState().equals("invincible")) {
                invicibilityTimer -= deltaTime;
                if (invicibilityTimer <= 0f) {
                    variableChanges.add(new SFSUserVariable("avatarState", "normal"));
                }
            }
        }

        if (getBoost() != 0) {
            boostTimer = Math.max(boostTimer - deltaTime, 0f);
            if (boostTimer == 0f) {
                ISFSObject notification = new SFSObject();
                notification.putInt("playerId", user.getPlayerId());
                notification.putInt("msgType", ExoEvtEnum.EVT_SEND_PICKUP_COMPLETE.code);
                notification.putInt("uPickup", getBoost());

                ISFSArray eventArray = new SFSArray();
                ISFSObject response = new SFSObject();
                eventArray.addSFSObject(notification);
                response.putSFSArray("events", eventArray);
                room.getExtension().send("sendEvents", response, room.getPlayersList());

                variableChanges.add(new SFSUserVariable("boost", (Integer) 0));
            }
        }
        if (getTeamBoost() != 0) {
            teamBoostTimer = Math.max(teamBoostTimer - deltaTime, 0f);
            if (teamBoostTimer == 0f) {
                ISFSObject notification = new SFSObject();
                notification.putInt("playerId", user.getPlayerId());
                notification.putInt("msgType", ExoEvtEnum.EVT_SEND_PICKUP_COMPLETE.code);
                notification.putInt("uPickup", getTeamBoost());

                ISFSArray eventArray = new SFSArray();
                ISFSObject response = new SFSObject();
                eventArray.addSFSObject(notification);
                response.putSFSArray("events", eventArray);
                room.getExtension().send("sendEvents", response, room.getPlayersList());

                variableChanges.add(new SFSUserVariable("teamBoost", (Integer) 0));
            }
        }

        float health = getHealth();
        if (health < suit.Health) {
            healthRefillTimer -= deltaTime;
            if (healthRefillTimer < 0f) {
                float refillTime = -healthRefillTimer;
                healthRefillTimer = 0f;
                health = Math.min(health + refillTime * suit.Regen_Speed, suit.Health);
                variableChanges.add(new SFSUserVariable("health", (Float) health));
            }
        }

        if (variableChanges.size() > 0) {
            setVariables(variableChanges, room);
        }

        return deltaTime;
    }

    public void draw(Graphics g, ExoMap map) {
        float cachedX = getX();
        float cachedY = getY();

        ExoDrawUtils.fillCapsule(
                g,
                Color.GREEN,
                Color.BLUE,
                Color.BLUE,
                cachedX,
                cachedY + 6.5f,
                1.5f,
                10f,
                map.scale);

        // the center of the character collider in the game is set to y = 6f, despite the total
        // height being 13f. hope that's not too important
        ExoInt2DVector drawCenter =
                new Exo2DVector(cachedX, cachedY + 6.5f).convertNativeToDraw(map.scale);
        g.setColor(Color.RED);
        g.drawLine(drawCenter.x, drawCenter.y, drawCenter.x, drawCenter.y);

        // TODO: disjoint phantoms?
    }

    public void hit(ExoBullet bullet, int where, Room room, ExoProps exoProps) {
        boolean headshot = where == 1;

        float damageModifier = 1f;
        if (headshot) {
            damageModifier += exoProps.headshotMod;
        }
        if (getBoost() == ExoPickupEnum.boost_armor.id) {
            damageModifier -= exoProps.boostArmorMod;
        }
        if (getTeamBoost() == ExoPickupEnum.boost_team_armor.id) {
            damageModifier -= exoProps.boostTeamArmorMod;
        }

        float health = getHealth();
        health -= bullet.damage * damageModifier;

        List<UserVariable> variableChanges = new ArrayList<>();

        ISFSObject notification = new SFSObject();
        notification.putInt("bnum", bullet.num);
        notification.putInt("playerId", user.getPlayerId() - 1);
        notification.putInt("uAttackerID", bullet.player.user.getPlayerId() - 1);

        if (health <= 0f) {
            notification.putInt("msgType", ExoEvtEnum.EVT_SEND_CAPTURED.code);

            crashes++;
            bullet.player.addHack(room, bullet);
            health = suit.Health;
            crashTimer = 8f;

            variableChanges.add(new SFSUserVariable("capturedMethod", (Integer) bullet.weaponId));
            variableChanges.add(
                    new SFSUserVariable("capturedBy", (Integer) bullet.player.user.getPlayerId()));
            variableChanges.add(new SFSUserVariable("avatarState", "captured"));
        } else {
            notification.putInt("msgType", ExoEvtEnum.EVT_SEND_DAMAGE.code);
            notification.putFloat("damage", bullet.damage);
            notification.putInt("hs", headshot ? 1 : 0);
            notification.putFloat("health", health);

            healthRefillTimer = suit.Regen_Delay;
        }

        variableChanges.add(new SFSUserVariable("health", (Float) health));

        setVariables(variableChanges, room);

        ISFSArray eventArray = new SFSArray();
        ISFSObject response = new SFSObject();
        eventArray.addSFSObject(notification);
        response.putSFSArray("events", eventArray);
        room.getExtension().send("sendEvents", response, room.getPlayersList());
    }

    public int getBoostResponse() {
        int responseVal = 0;
        int boost = getBoost();
        int teamBoost = getTeamBoost();

        responseVal |= boost;
        responseVal |= teamBoost << 16;

        if (boost != 0) responseVal |= 0x8000;
        // client bug: the returned integer is treated as an i32 and if <= 0 ignored, but the top
        // bit is also used to signal an active boost
        if (teamBoost != 0) responseVal |= 0x8000_0000;

        return responseVal;
    }

    public void addHack(Room room, ExoBullet bullet) {
        setVariables(List.of(new SFSUserVariable("hacks", (Integer) (getHacks() + 1))), room);

        int boost = getBoost();
        int teamBoost = getTeamBoost();
        if (boost == ExoPickupEnum.boost_invis.id
                || teamBoost == ExoPickupEnum.boost_team_invis.id) {
            hacksInvisible++;
        }
        if (boost == ExoPickupEnum.boost_speed.id
                || teamBoost == ExoPickupEnum.boost_team_speed.id) {
            hacksSpeed++;
        }
        if (bullet.boosted) {
            hacksDamageBoost++;
        }
        if (boost == ExoPickupEnum.boost_armor.id
                || teamBoost == ExoPickupEnum.boost_team_armor.id) {
            hacksArmorBoost++;
        }
    }

    public void setBoost(int type, int time, Room room) {
        tick(room); // clock starts now, not when the last tick happened
        boostTimer = (float) time;
        setVariables(List.of(new SFSUserVariable("boost", (Integer) type)), room);
    }

    public void setTeamBoost(int type, int time, Room room) {
        tick(room); // clock starts now, not when the last tick happened
        teamBoostTimer = (float) time;
        setVariables(List.of(new SFSUserVariable("teamBoost", (Integer) type)), room);
    }
}
