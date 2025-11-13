package xyz.openexonaut.extension.exolib.enums;

public enum ExoStateEnum {
    // currentMoveState
    IDLE(0),
    CROUCH(1), // 0x1
    RUN_LEFT(2), // 0x2
    RUN_RIGHT(4), // 0x4
    RUN(6), // 0x6, mask for RUN_LEFT and RUN_RIGHT
    JUMP(8), // 0x8
    JETPACK(16), // 0x10
    AIRDASH(32), // 0x20
    CAPTURED(64), // 0x40
    ROLL(128), // 0x80
    ROLL_REC(256), // 0x100
    FALL(512), // 0x200
    RELEASED(1024), // 0x400
    FREED(2048), // 0x800
    AIRDASH_REC(4096), // 0x1000
    JUMP_FALL(8192), // 0x2000
    JUMP_REC(16384), // 0x4000
    RUN_REV(32768), // 0x8000
    POWER_1(65536), // 0x10000
    POWER_2(131072), // 0x20000
    POWER_3(262144), // 0x40000
    SNIPE(524288), // 0x80000

    // currentActionState
    NO_ACTION(0),
    SHOOT(1048576), // 0x100000
    THROW(2097152), // 0x200000
    RELOAD(4194304), // 0x400000
    // 8388608 not used (0x800000)

    // currentArmState (unused?)
    // 16777216 not used (0x1000000)
    // 33554432 not used (0x2000000)
    // 67108864 not used (0x4000000)
    // 134217728 not used (0x8000000)

    // currentContextState
    NO_TIME(0),
    IN_AIR(268435456), // 0x10000000
    DIRECTION(536870912); // 0x20000000
    // 1073741824 not used (0x40000000)

    // 2147483648 not used (0x80000000)

    public final int mask;

    private ExoStateEnum(int mask) {
        this.mask = mask;
    }

    public static final int smallMask = CROUCH.mask | ROLL.mask | ROLL_REC.mask;
}
