package cn.ussshenzhou.xp_orb.network;


import cn.ussshenzhou.t88.network.annotation.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author USS_Shenzhou
 */
@NetPacket
public class ShootOrbPacket {

    public ShootOrbPacket() {
    }

    @Decoder
    public ShootOrbPacket(FriendlyByteBuf buf) {
    }

    @Encoder
    public void write(FriendlyByteBuf buf) {

    }

    @ClientHandler
    public void clientHandler(PlayPayloadContext context){

    }

    @ServerHandler
    public void serverHandler(PlayPayloadContext context){
        context.player().ifPresent(player -> {
            StreamSupport.stream(((ServerLevel)player.level()).getAllEntities().spliterator(),true)
                    .filter(entity -> entity instanceof ExperienceOrb orb && orb.followingPlayer == player)
                    .sequential()
                    .forEach(entity -> {

                    });
        });
    }
}
