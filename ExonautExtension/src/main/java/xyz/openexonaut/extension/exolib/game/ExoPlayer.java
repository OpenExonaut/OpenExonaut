package xyz.openexonaut.extension.exolib.game;

import java.awt.*;
import java.util.*;
import java.util.List;

import com.badlogic.gdx.physics.box2d.*;

import com.smartfoxserver.v2.*;
import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.data.*;
import com.smartfoxserver.v2.entities.variables.*;

import xyz.openexonaut.extension.exolib.data.*;
import xyz.openexonaut.extension.exolib.enums.*;
import xyz.openexonaut.extension.exolib.evthandlers.*;
import xyz.openexonaut.extension.exolib.geo.*;
import xyz.openexonaut.extension.exolib.map.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class ExoPlayer extends ExoTickable {
    public final User user;

    private ExoSuit suit = null;

    private float crashTimer = 5f;
    private float invicibilityTimer = 0f;
    private float healthRefillTimer = 0f;
    private float boostTimer = 0f;
    private float teamBoostTimer = 0f;

    // TODO: achievement/performance metrics
    @SuppressWarnings("unused")
    private int crashes = 0,
            fuelConsumed = 0,
            hacksInvisible = 0,
            hacksSpeed = 0,
            hacksDamageBoost = 0,
            hacksArmorBoost = 0;

    private Body body = null;

    static {
        Box2D.init();
    }

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

    public ExoSuit getSuit() {
        return suit;
    }

    public Body getBody() {
        return body;
    }

    @SuppressWarnings("rawtypes") // SFS2X gives a raw list
    public void updateVariables(List changedVariables) {
        for (Object o : changedVariables) {
            UserVariable var = (UserVariable) o;
            if (var.getName().equals("clientState")) {
                // fixes spawning across games to do this instead of having avatarState captured
                // from the beginning
                if (var.getStringValue().equals("playing")) {
                    setVariables(List.of(new SFSUserVariable("avatarState", "captured")));
                }
            }
        }
    }

    public void setSuit(ExoSuit suit) {
        this.suit = suit;
    }

    public void addFuelConsumed(int fuelConsumed) {
        this.fuelConsumed += fuelConsumed;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    @Override
    public float tick(ISFSArray eventQueue) {
        float deltaTime = super.tick(eventQueue);

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
                eventQueue.addSFSObject(
                        ExoParamUtils.serialize(
                                new SendPickupComplete(getBoost()), user.getPlayerId()));

                variableChanges.add(new SFSUserVariable("boost", (Integer) 0));
            }
        }
        if (getTeamBoost() != 0) {
            teamBoostTimer = Math.max(teamBoostTimer - deltaTime, 0f);
            if (teamBoostTimer == 0f) {
                eventQueue.addSFSObject(
                        ExoParamUtils.serialize(
                                new SendPickupComplete(getTeamBoost()), user.getPlayerId()));

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
            setVariables(variableChanges);
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

    public void hit(ExoBullet bullet, int where, ExoProps exoProps, ISFSArray eventQueue) {
        String avatarState = getAvatarState();
        if (avatarState.equals("captured") || avatarState.equals("invincible")) {
            return;
        }

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

        if (health <= 0f) {
            eventQueue.addSFSObject(
                    ExoParamUtils.serialize(
                            new SendCaptured(bullet.num, bullet.player.user.getPlayerId() - 1),
                            user.getPlayerId() - 1));

            crashes++;
            bullet.player.addHack(bullet);
            health = suit.Health;
            crashTimer = 8f;

            setVariables(
                    List.of(
                            new SFSUserVariable("capturedMethod", (Integer) bullet.weaponId),
                            new SFSUserVariable(
                                    "capturedBy", (Integer) bullet.player.user.getPlayerId()),
                            new SFSUserVariable("avatarState", "captured"),
                            new SFSUserVariable("health", (Float) health)));
        } else {
            eventQueue.addSFSObject(
                    ExoParamUtils.serialize(
                            new SendDamage(
                                    bullet.num,
                                    bullet.player.user.getPlayerId() - 1,
                                    bullet.damage,
                                    headshot ? 1 : 0,
                                    health),
                            user.getPlayerId() - 1));

            healthRefillTimer = suit.Regen_Delay;
            setVariables(List.of(new SFSUserVariable("health", (Float) health)));
        }
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

    public void addHack(ExoBullet bullet) {
        setVariables(List.of(new SFSUserVariable("hacks", (Integer) (getHacks() + 1))));

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

    public void setBoost(int type, int time, ISFSArray eventQueue) {
        tick(eventQueue); // clock starts now, not when the last tick happened
        boostTimer = (float) time;
        setVariables(List.of(new SFSUserVariable("boost", (Integer) type)));
    }

    public void setTeamBoost(int type, int time, ISFSArray eventQueue) {
        tick(eventQueue); // clock starts now, not when the last tick happened
        teamBoostTimer = (float) time;
        setVariables(List.of(new SFSUserVariable("teamBoost", (Integer) type)));
    }

    private void setVariables(List<UserVariable> variables) {
        SmartFoxServer.getInstance().getAPIManager().getSFSApi().setUserVariables(user, variables);
    }
}
