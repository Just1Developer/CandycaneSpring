package net.justonedev.candycane.lobbysession.packet;

public record PacketProcessResult(PacketProcessResultType type, byte flagByte) {
    public PacketProcessResult(PacketProcessResultType type, PacketProcessResultFlag... flags) {
        this(type, concatFlags(flags));
    }

    public boolean isFlagSet(PacketProcessResultFlag flag) {
        return (flagByte & flag.getFlagValue()) != 0;
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

    public static PacketProcessResult swallow(PacketProcessResultFlag... flags) {
        return new PacketProcessResult(PacketProcessResultType.SWALLOW, flags);
    }
}
