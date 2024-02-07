package cn.ussshenzhou.xp_orb.network;

import cn.ussshenzhou.t88.network.NetworkHelper;
import cn.ussshenzhou.t88.network.annotation.*;
import cn.ussshenzhou.t88.task.Task;
import cn.ussshenzhou.t88.task.TaskHelper;
import cn.ussshenzhou.xp_orb.entity.Orb;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

/**
 * @author USS_Shenzhou
 */
@NetPacket
public class RecommendOrbCheckPacket {
    int id;
    Vec3 pos;
    Vec3 motion;
    UUID follow;
    int tickCount;

    public RecommendOrbCheckPacket(Orb orb) {
        this.id = orb.getId();
        this.pos = orb.position();
        this.motion = orb.getDeltaMovement();
        this.follow = orb.followingPlayer == null ? new UUID(0, 0) : orb.followingPlayer.getUUID();
        this.tickCount = orb.tickCount;
    }

    @Decoder
    public RecommendOrbCheckPacket(FriendlyByteBuf buf) {
        id = buf.readInt();
        pos = buf.readVec3();
        motion = buf.readVec3();
        follow = buf.readUUID();
        tickCount = buf.readInt();
    }

    @Encoder
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(id);
        buf.writeVec3(pos);
        buf.writeVec3(motion);
        buf.writeUUID(follow);
        buf.writeInt(tickCount);
    }

    @ClientHandler
    public void clientHandler(PlayPayloadContext context) {
        context.level().ifPresent(level -> {
            var e = level.getEntity(id);
            if (e instanceof Orb orb) {
                if ((!follow.equals(orb.followingPlayer == null ? new UUID(0, 0) : orb.followingPlayer.getUUID()))
                        || pos.distanceToSqr(orb.position()) > 2 * 2
                ) {
                    orb.tickCount = tickCount;
                    orb.moveTo(pos);
                    orb.setDeltaMovement(motion);
                    if (follow.equals(new UUID(0, 0))) {
                        orb.followingPlayer = null;
                    } else {
                        orb.followingPlayer = orb.level().getPlayerByUUID(follow);
                    }
                }
            }
        });
    }

    @ServerHandler
    public void serverHandler(PlayPayloadContext context) {
    }
}
