package cn.ussshenzhou.xp_orb.network;

import cn.ussshenzhou.t88.network.annotation.*;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * @author USS_Shenzhou
 */
@NetPacket
public class SwitchHurtPlayerPacket {

    public SwitchHurtPlayerPacket() {
    }

    @Decoder
    public SwitchHurtPlayerPacket(FriendlyByteBuf buf) {
    }

    @Encoder
    public void write(FriendlyByteBuf buf) {

    }

    @ClientHandler
    public void clientHandler(PlayPayloadContext context) {
    }

    public static final String HURT = "orb_hurt";

    @ServerHandler
    public void serverHandler(PlayPayloadContext context) {
        context.player().ifPresent(player -> {
            if (player.getTags().contains(HURT)) {
                player.removeTag(HURT);
            } else {
                player.addTag(HURT);
            }
        });
    }
}
