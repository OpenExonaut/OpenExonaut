package xyz.openexonaut.extension.exolib.game;

import java.awt.*;
import java.util.*;
import java.util.List;

import com.badlogic.gdx.math.*;
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
import xyz.openexonaut.extension.exolib.resources.*;
import xyz.openexonaut.extension.exolib.utils.*;

public class ExoPlayer extends ExoTickable {
    public final User user;

    private ExoSuit suit = null;

    private float crashTimer;
    private float invincibilityTimer;
    private float healthRefillTimer;
    private float boostTimer;
    private float teamBoostTimer;

    private int crashes;
    // TODO: achievement/performance metrics
    @SuppressWarnings("unused")
    private int fuelConsumed, hacksInvisible, hacksSpeed, hacksDamageBoost, hacksArmorBoost;

    private Body activeBody = null;
    private Body standingBody = null;
    private Body crouchingBody = null;

    static {
        Box2D.init();
    }

    public ExoPlayer(User user) {
        this.user = user;
        reset();
    }

    public void reset() {
        crashTimer = 5f;
        invincibilityTimer = 0f;
        healthRefillTimer = 0f;
        boostTimer = 0f;
        teamBoostTimer = 0f;

        crashes = 0;
        fuelConsumed = 0;
        hacksInvisible = 0;
        hacksSpeed = 0;
        hacksDamageBoost = 0;
        hacksArmorBoost = 0;
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
        return activeBody;
    }

    public Body getStandingBody() {
        return standingBody;
    }

    public Body getCrouchingBody() {
        return crouchingBody;
    }

    public int getCrashes() {
        return crashes;
    }

    public void setSuit(ExoSuit suit) {
        this.suit = suit;
    }

    public void addFuelConsumed(int fuelConsumed) {
        this.fuelConsumed += fuelConsumed;
    }

    public void setBodies(Body standingBody, Body crouchingBody) {
        this.standingBody = standingBody;
        this.crouchingBody = crouchingBody;
        activeBody = standingBody;
    }

    @Override
    public float tick(ISFSArray eventQueue) {
        float deltaTime = super.tick(eventQueue);

        if (isSmall()) {
            crouchingBody.setActive(true);
            standingBody.setActive(false);
            activeBody = crouchingBody;
        } else {
            standingBody.setActive(true);
            crouchingBody.setActive(false);
            activeBody = standingBody;
        }

        activeBody.setTransform(new Vector2(getX(), getY()), 0f);

        List<UserVariable> variableChanges = new ArrayList<>();

        if (getClientState().equals("playing")) {
            if (getAvatarState().equals("captured") || getAvatarState().equals("halted")) {
                crashTimer -= deltaTime;
                if (crashTimer <= 0f) {
                    invincibilityTimer = 3f + crashTimer;
                    if (invincibilityTimer <= 0f) {
                        variableChanges.add(new SFSUserVariable("avatarState", "normal"));
                    } else {
                        variableChanges.add(new SFSUserVariable("avatarState", "invincible"));
                    }
                } else if (getAvatarState().equals("halted")) {
                    // fixes spawning across games to do this instead of having avatarState captured
                    // from the beginning
                    variableChanges.add(new SFSUserVariable("avatarState", "captured"));
                }
            }
            if (getAvatarState().equals("invincible")) {
                invincibilityTimer -= deltaTime;
                if (invincibilityTimer <= 0f) {
                    variableChanges.add(new SFSUserVariable("avatarState", "normal"));
                }
            }
        }

        if (getBoost() >= 0) {
            boostTimer = Math.max(boostTimer - deltaTime, 0f);
            if (boostTimer == 0f) {
                eventQueue.addSFSObject(
                        ExoParamUtils.serialize(
                                new SendPickupComplete(getBoost()), user.getPlayerId()));

                variableChanges.add(new SFSUserVariable("boost", (Integer) (-1)));
            }
        }
        if (getTeamBoost() >= 0) {
            teamBoostTimer = Math.max(teamBoostTimer - deltaTime, 0f);
            if (teamBoostTimer == 0f) {
                eventQueue.addSFSObject(
                        ExoParamUtils.serialize(
                                new SendPickupComplete(getTeamBoost()), user.getPlayerId()));

                variableChanges.add(new SFSUserVariable("teamBoost", (Integer) (-1)));
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
        float centerX = getX();
        float centerY =
                getY()
                        + ExoDefs.radius
                        + (isSmall() ? ExoDefs.crouchRollHalfHeight : ExoDefs.standingHalfHeight);

        ExoDrawUtils.fillCapsule(
                g,
                Color.GREEN,
                Color.BLUE,
                Color.BLUE,
                centerX,
                centerY,
                ExoDefs.radius,
                (isSmall() ? ExoDefs.crouchRollHalfHeight : ExoDefs.standingHalfHeight) * 2f,
                map.scale);

        // the center of the character collider in the game is set to y = 6f, despite the total
        // height being 13f. hope that's not too important
        ExoInt2DVector drawCenter =
                new Exo2DVector(centerX, centerY).convertNativeToDraw(map.scale);
        int scaleInt = (int) map.scale;
        int halfScale = (int) (map.scale / 2f);
        g.setColor(Color.RED);
        g.fillRect(drawCenter.x - halfScale, drawCenter.y - halfScale, scaleInt, scaleInt);
    }

    public void bulletHit(ExoBullet bullet, int where, Room room, ISFSArray eventQueue) {
        hit(
                bullet.player,
                bullet.num,
                bullet.weaponId,
                bullet.damage,
                bullet.damageModifier,
                where == 1,
                room,
                eventQueue);
    }

    public void blastHit(
            ExoPlayer sender,
            int weaponId,
            float damage,
            float damageModifier,
            boolean headshot,
            Room room,
            ISFSArray eventQueue) {
        hit(sender, -1, weaponId, damage, damageModifier, headshot, room, eventQueue);
    }

    private void hit(
            ExoPlayer sender,
            int bnum,
            int weaponId,
            float damage,
            float damageModifierAttackOnly,
            boolean headshot,
            Room room,
            ISFSArray eventQueue) {
        if (!getAvatarState().equals("normal")) {
            return;
        }

        float damageModifier = damageModifierAttackOnly;
        if (headshot) {
            damageModifier += ExoProps.getHeadshotMod();
        }
        if (getBoost() == ExoPickupEnum.boost_armor.id) {
            damageModifier -= ExoProps.getBoostArmorMod();
        }
        if (getTeamBoost() == ExoPickupEnum.boost_team_armor.id) {
            damageModifier -= ExoProps.getBoostTeamArmorMod();
        }

        float health = getHealth();
        health -= damage * damageModifier;

        if (health <= 0f) {
            eventQueue.addSFSObject(
                    ExoParamUtils.serialize(
                            new SendCaptured(bnum, sender.user.getPlayerId() - 1),
                            user.getPlayerId() - 1));

            crashes++;
            sender.addHack(damageModifierAttackOnly, room);
            health = suit.Health;
            crashTimer = 8f;

            setVariables(
                    List.of(
                            new SFSUserVariable("capturedMethod", (Integer) weaponId),
                            new SFSUserVariable("capturedBy", (Integer) sender.user.getPlayerId()),
                            new SFSUserVariable("avatarState", "captured"),
                            new SFSUserVariable("health", (Float) health)));
        } else {
            eventQueue.addSFSObject(
                    ExoParamUtils.serialize(
                            new SendDamage(
                                    bnum,
                                    sender.user.getPlayerId() - 1,
                                    damage,
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

    public void addHack(float damageModifier, Room room) {
        setVariables(List.of(new SFSUserVariable("hacks", (Integer) (getHacks() + 1))));
        room.getExtension()
                .handleInternalMessage(
                        "addTeamHack",
                        user.getVariable("faction").getStringValue().equals("banzai"));

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
        if (damageModifier > 1f) {
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

    public void setVariables(List<UserVariable> variables) {
        SmartFoxServer.getInstance().getAPIManager().getSFSApi().setUserVariables(user, variables);
    }

    public boolean isSmall() {
        return (getMoveState() & ExoStateEnum.smallMask) != 0;
    }
}
