package xyz.openexonaut.extension.exolib;

import java.awt.*;
import java.util.*;
import java.util.List;

import com.badlogic.gdx.physics.box2d.*;

import com.smartfoxserver.v2.api.*;
import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;
import com.smartfoxserver.v2.entities.variables.*;

import xyz.openexonaut.extension.exolib.geo.*;
import xyz.openexonaut.extension.exolib.reqhandlers.*;

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

    // obtains a mutex on the player for the whole function
    @SuppressWarnings("rawtypes")
    public void updateVariables(List changedVariables, ISFSApi sfsApi) {
        synchronized (this) {
            for (Object o : changedVariables) {
                UserVariable var = (UserVariable) o;
                if (var.getName().equals("clientState")) {
                    // fixes spawning across games to do this instead of having avatarState captured
                    // from the beginning
                    if (var.getStringValue().equals("playing")) {
                        List<UserVariable> variableUpdate = new ArrayList<>();
                        variableUpdate.add(new SFSUserVariable("avatarState", "captured"));
                        sfsApi.setUserVariables(user, variableUpdate);
                    }
                }
            }
        }
    }

    public void setSuit(ExoSuit suit) {
        this.suit = suit;
    }

    // return value: seconds since last tick
    public float tick(Room room) {
        long nano = System.nanoTime();
        float deltaTime = (nano - lastNano) / 1_000_000_000f;
        lastNano = nano;

        if (getClientState().equals("playing")) {
            if (getAvatarState().equals("captured")) {
                crashTimer -= deltaTime;
                if (crashTimer <= 0f) {
                    invicibilityTimer = 3f - -crashTimer;
                    room.getExtension()
                            .handleInternalMessage(
                                    "setVariable",
                                    new Object[] {this, "avatarState", "invincible"});
                }
            } else if (getAvatarState().equals("invincible")) {
                invicibilityTimer -= deltaTime;
                if (invicibilityTimer <= 0f) {
                    room.getExtension()
                            .handleInternalMessage(
                                    "setVariable", new Object[] {this, "avatarState", "normal"});
                }
            }
        }

        if (getBoost() != 0) {
            boostTimer = Math.max(boostTimer - deltaTime, 0f);
            if (boostTimer == 0f) {
                ISFSObject notification = new SFSObject();
                notification.putInt("playerId", user.getPlayerId());
                notification.putInt("msgType", EvtEnum.EVT_SEND_PICKUP_COMPLETE.code);
                notification.putInt("uPickup", getBoost());

                ISFSArray eventArray = new SFSArray();
                ISFSObject response = new SFSObject();
                eventArray.addSFSObject(notification);
                response.putSFSArray("events", eventArray);
                room.getExtension().send("sendEvents", response, room.getPlayersList());

                room.getExtension()
                        .handleInternalMessage(
                                "setVariable", new Object[] {this, "boost", (Integer) 0});
            }
        }
        if (getTeamBoost() != 0) {
            teamBoostTimer = Math.max(teamBoostTimer - deltaTime, 0f);
            if (teamBoostTimer == 0f) {
                ISFSObject notification = new SFSObject();
                notification.putInt("playerId", user.getPlayerId());
                notification.putInt("msgType", EvtEnum.EVT_SEND_PICKUP_COMPLETE.code);
                notification.putInt("uPickup", getTeamBoost());

                ISFSArray eventArray = new SFSArray();
                ISFSObject response = new SFSObject();
                eventArray.addSFSObject(notification);
                response.putSFSArray("events", eventArray);
                room.getExtension().send("sendEvents", response, room.getPlayersList());

                room.getExtension()
                        .handleInternalMessage(
                                "setVariable", new Object[] {this, "teamBoost", (Integer) 0});
            }
        }

        float health = getHealth();
        if (health < suit.Health) {
            healthRefillTimer -= deltaTime;
            if (healthRefillTimer < 0f) {
                float refillTime = -healthRefillTimer;
                healthRefillTimer = 0f;
                health = Math.min(health + refillTime * suit.Regen_Speed, suit.Health);
                room.getExtension()
                        .handleInternalMessage(
                                "setVariable", new Object[] {this, "health", (Float) health});
            }
        }

        return deltaTime;
    }

    // obtains a brief mutex on the player to cache the position for the function's lifetime
    public void draw(Graphics g, ExoMap map) {
        float cachedX = 0f;
        float cachedY = 0f;

        synchronized (this) {
            cachedX = getX();
            cachedY = getY();
        }

        ExoUtil.fillCapsule(
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

    public void hit(ExoBullet bullet, int where, Room room) {
        boolean headshot = where == 1;

        float damageModifier = 1f;
        if (headshot) {
            // is this right? description of Brad's Princess Bubblegum video:
            // "Her Marksman fires 1 high-damage shot so if you critical hit light exosuits you can
            // hack them in one shot."
            // her marksman does 100 damage, lights have ca. 125 armor, mediums have ca. 150; puts
            // it between 1.25 and 1.5
            damageModifier += 0.25f;
        }
        if (getBoost() == ExoPickupEnum.boost_armor.id) {
            // this value (0.2 multiplier) was taken from essentially dead client code. is this
            // right?
            damageModifier -= 0.2f;
        }
        if (getTeamBoost() == ExoPickupEnum.boost_team_armor.id) {
            // this value (0.2 multiplier) was taken from essentially dead client code. is this
            // right?
            damageModifier -= 0.2f;
        }

        float health = getHealth();
        health -= bullet.damage * damageModifier;

        ISFSObject notification = new SFSObject();
        notification.putInt("bnum", bullet.num);
        notification.putInt("playerId", user.getPlayerId() - 1);
        notification.putInt("uAttackerID", bullet.player.user.getPlayerId() - 1);
        if (health <= 0f) {
            notification.putInt("msgType", EvtEnum.EVT_SEND_CAPTURED.code);
            crashes++;
            bullet.player.addHack(room, bullet);
            health = suit.Health;
            room.getExtension()
                    .handleInternalMessage(
                            "setVariable",
                            new Object[] {this, "capturedMethod", (Integer) bullet.weaponId});
            room.getExtension()
                    .handleInternalMessage(
                            "setVariable",
                            new Object[] {
                                this, "capturedBy", (Integer) bullet.player.user.getPlayerId()
                            });
            room.getExtension()
                    .handleInternalMessage(
                            "setVariable", new Object[] {this, "avatarState", "captured"});
            crashTimer = 8f;
        } else {
            notification.putInt("msgType", EvtEnum.EVT_SEND_DAMAGE.code);
            notification.putFloat("damage", bullet.damage);
            notification.putInt("hs", headshot ? 1 : 0);
            notification.putFloat("health", health);
            healthRefillTimer = suit.Regen_Delay;
        }

        room.getExtension()
                .handleInternalMessage(
                        "setVariable", new Object[] {this, "health", (Float) health});

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
        room.getExtension()
                .handleInternalMessage(
                        "setVariable", new Object[] {this, "hacks", (Integer) (getHacks() + 1)});

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
        room.getExtension()
                .handleInternalMessage("setVariable", new Object[] {this, "boost", (Integer) type});
    }

    public void setTeamBoost(int type, int time, Room room) {
        tick(room); // clock starts now, not when the last tick happened
        teamBoostTimer = (float) time;
        room.getExtension()
                .handleInternalMessage(
                        "setVariable", new Object[] {this, "teamBoost", (Integer) type});
    }
}
