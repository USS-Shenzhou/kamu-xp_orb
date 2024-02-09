package cn.ussshenzhou.xp_orb.network;

import cn.ussshenzhou.t88.network.NetworkHelper;
import cn.ussshenzhou.t88.network.annotation.*;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * @author USS_Shenzhou
 */
@NetPacket
public class SwitchHurtPlayerPacket {
    boolean hurt;


    public SwitchHurtPlayerPacket(boolean hurt) {
        this.hurt = hurt;
    }

    @Decoder
    public SwitchHurtPlayerPacket(FriendlyByteBuf buf) {
        this.hurt = buf.readBoolean();
    }

    @Encoder
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(hurt);
    }

    @ClientHandler
    public void clientHandler(PlayPayloadContext context) {
        context.player().ifPresent(player -> {
            if (hurt) {
                player.addTag(HURT);
            } else {
                player.removeTag(HURT);
            }
        });
    }

    public static final String HURT = "orb_hurt";

    @ServerHandler
    public void serverHandler(PlayPayloadContext context) {
        NetworkHelper.sendTo(PacketDistributor.ALL.noArg(), this);
        context.player().ifPresent(player -> {
            if (hurt) {
                player.addTag(HURT);
            } else {
                player.removeTag(HURT);
            }
        });
    }
}
