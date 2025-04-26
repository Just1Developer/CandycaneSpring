package net.justonedev.candycane.lobbysession.packet;

import lombok.Getter;
import net.justonedev.candycane.lobbysession.world.WorldBuildingResponse;

import java.util.Optional;

@Getter
public class PacketProcessResult extends Packet {
    private final PacketProcessResultType type;
    private final byte flagByte;
    private final WorldBuildingResponse response;

    private PacketProcessResult(PacketProcessResultType type, WorldBuildingResponse response, byte flagByte) {
        this.type = type;
        this.flagByte = flagByte;
        this.response = response;
    }

    private PacketProcessResult(PacketProcessResultType type, byte flagByte) {
        this(type, null, flagByte);
    }

    public PacketProcessResult(PacketProcessResultType type, PacketProcessResultFlag... flags) {
        this(type, concatFlags(flags));
    }

    public PacketProcessResult(PacketProcessResultType type, WorldBuildingResponse response, PacketProcessResultFlag... flags) {
        this(type, response, concatFlags(flags));
    }

    public boolean isFlagSet(PacketProcessResultFlag flag) {
        return (flagByte & flag.getFlagValue()) != 0;
    }

    public PacketProcessResult withAttribute(String key, String value) {
        addAttribute(key, value);
        return this;
    }

    private static byte concatFlags(PacketProcessResultFlag... flags) {
        byte flagByte = 0;
        for (PacketProcessResultFlag flag : flags) {
            flagByte |= flag.getFlagValue();
        }
        return flagByte;
    }

    public static PacketProcessResult relay(PacketProcessResultFlag... flags) {
        return new PacketProcessResult(PacketProcessResultType.RELAY, flags);
    }

    public static PacketProcessResult relay(WorldBuildingResponse response, PacketProcessResultFlag... flags) {
        return new PacketProcessResult(PacketProcessResultType.RELAY, response, flags);
    }

    public static PacketProcessResult swallow(PacketProcessResultFlag... flags) {
        return new PacketProcessResult(PacketProcessResultType.SWALLOW, flags);
    }

    @Override
    public String toString() {
        return "[RESULT: %s, FLAGS: %8s]".formatted(type, Integer.toBinaryString(flagByte));
    }

    public Optional<WorldBuildingResponse> getWorldBuildingResponse() {
        return Optional.ofNullable(response);
    }

    public boolean noFlags() {
        return flagByte == 0;
    }
}
