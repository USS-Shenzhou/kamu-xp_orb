package cn.ussshenzhou.xp_orb.network;


import cn.ussshenzhou.t88.network.NetworkHelper;
import cn.ussshenzhou.t88.network.annotation.*;
import cn.ussshenzhou.t88.task.Task;
import cn.ussshenzhou.t88.task.TaskHelper;
import cn.ussshenzhou.xp_orb.entity.Orb;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

/**
 * @author USS_Shenzhou
 */
@NetPacket
public class ShootOrbPacket {
    UUID follow;
    boolean shoot;

    public ShootOrbPacket(UUID uuid, boolean shoot) {
        follow = uuid;
        this.shoot = shoot;
    }

    @Decoder
    public ShootOrbPacket(FriendlyByteBuf buf) {
        follow = buf.readUUID();
        shoot = buf.readBoolean();
    }

    @Encoder
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(follow);
        buf.writeBoolean(shoot);
    }

    @ClientHandler
    public void clientHandler(PlayPayloadContext context) {
        process(context);
    }

    @ServerHandler
    public void serverHandler(PlayPayloadContext context) {
        context.player().ifPresent(player -> NetworkHelper.sendTo(PacketDistributor.ALL.noArg(), this));
        process(context);
    }

    public static final String SHOOTING = "shooting";
    public static final HashMap<Player, Task> TASK_CACHE_CLIENT = new HashMap<>();
    public static final HashMap<Player, Task> TASK_CACHE_SERVER = new HashMap<>();

    public void process(PlayPayloadContext context) {
        if (context.level().isEmpty()) {
            return;
        }
        var player = context.level().get().getPlayerByUUID(follow);
        if (player == null) {
            return;
        }
        var tags = player.getTags();
        tags.remove(SHOOTING);
        if (player.level().isClientSide) {
            var t = TASK_CACHE_CLIENT.get(player);
            if (t != null) {
                t.cancel();
                TASK_CACHE_CLIENT.remove(player);
            }
        } else {
            var t = TASK_CACHE_SERVER.get(player);
            if (t != null) {
                t.cancel();
                TASK_CACHE_SERVER.remove(player);
            }
        }
        if (shoot) {
            player.addTag(SHOOTING);
            if (player.level().isClientSide) {
                var list = StreamSupport.stream(((ClientLevel) player.level()).entitiesForRendering().spliterator(), true)
                        .filter(entity -> entity instanceof Orb orb && orb.followingPlayer == player)
                        .toList();
                TASK_CACHE_CLIENT.put(player, TaskHelper.addClientRepeatTask(() -> {
                    line(player, list);
                }, 0, 0));
            } else {
                var list = StreamSupport.stream(((ServerLevel) player.level()).getAllEntities().spliterator(), true)
                        .filter(entity -> entity instanceof Orb orb && orb.followingPlayer == player)
                        .toList();
                TASK_CACHE_SERVER.put(player, TaskHelper.addServerRepeatTask(() -> {
                    line(player, list);
                }, 0, 0));
            }

        }
    }

    public void line(Player player, List<Entity> list) {
        var pos = player.getEyePosition().toVector3f();
        var looking = player.getLookAngle().toVector3f().normalize();
        var size = list.size();
        float step = size > 320 ? 96f / size : 0.3f;
        for (int i = 0; i < size; i++) {
            Entity entity = list.get(i);
            entity.setPos(pos.x + looking.x * step * i,
                    pos.y + looking.y * step * i,
                    pos.z + looking.z * step * i
            );
        }
    }
}
