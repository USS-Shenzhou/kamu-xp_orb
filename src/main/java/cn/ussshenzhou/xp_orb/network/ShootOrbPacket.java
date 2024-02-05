package cn.ussshenzhou.xp_orb.network;


import cn.ussshenzhou.t88.network.NetworkHelper;
import cn.ussshenzhou.t88.network.annotation.*;
import cn.ussshenzhou.t88.task.Task;
import cn.ussshenzhou.t88.task.TaskHelper;
import cn.ussshenzhou.xp_orb.IShootOrb;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.HashMap;
import java.util.List;
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
    public void clientHandler(PlayPayloadContext context) {
        process(context);
    }

    @ServerHandler
    public void serverHandler(PlayPayloadContext context) {
        context.player().ifPresent(player -> NetworkHelper.sendToPlayer((ServerPlayer) player, this));
        process(context);
    }

    public static final String SHOOTING = "shooting";
    public static final HashMap<Player, Task> TASK_CACHE_CLIENT = new HashMap<>();
    public static final HashMap<Player, Task> TASK_CACHE_SERVER = new HashMap<>();

    public void process(PlayPayloadContext context) {
        context.player().ifPresent(player -> {
            var tags = player.getTags();
            if (tags.contains(SHOOTING)) {
                tags.remove(SHOOTING);
                if (player.level().isClientSide) {
                    TASK_CACHE_CLIENT.get(player).cancel();
                    TASK_CACHE_CLIENT.remove(player);
                } else {
                    TASK_CACHE_SERVER.get(player).cancel();
                    TASK_CACHE_SERVER.remove(player);
                }
            } else {
                player.addTag(SHOOTING);
                if (player.level().isClientSide) {
                    var list = StreamSupport.stream(((ClientLevel) player.level()).entitiesForRendering().spliterator(), true)
                            .filter(entity -> entity instanceof ExperienceOrb orb && orb.followingPlayer == player)
                            .toList();
                    TASK_CACHE_CLIENT.put(player, TaskHelper.addClientRepeatTask(() -> {
                        line(player, list);
                    }, 0, 0));
                } else {
                    var list = StreamSupport.stream(((ServerLevel) player.level()).getAllEntities().spliterator(), true)
                            .filter(entity -> entity instanceof ExperienceOrb orb && orb.followingPlayer == player)
                            .toList();
                    TASK_CACHE_SERVER.put(player, TaskHelper.addServerRepeatTask(() -> {
                        line(player, list);
                    }, 0, 0));
                }

            }
        });
    }

    public void line(Player player, List<Entity> list) {
        var pos = player.getEyePosition().toVector3f();
        var looking = player.getLookAngle().toVector3f().normalize();
        float step = 96f / list.size();
        for (int i = 0, listSize = list.size(); i < listSize; i++) {
            Entity entity = list.get(i);
            entity.setPos(pos.x + looking.x * step * i,
                    pos.y + looking.y * step * i,
                    pos.z + looking.z * step * i
            );
        }
    }
}
