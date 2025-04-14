package net.justonedev.candycane.lobbysession.packet;

import lombok.Getter;

@Getter
public enum PacketProcessResultFlag {
    SEND_POWER_UPDATE((byte) 0x01),
    SEND_WORLD_UPDATE((byte) 0x02),
    ;

    private final byte flagValue;

    PacketProcessResultFlag(byte flagValue) {
        this.flagValue = flagValue;
    }
}
