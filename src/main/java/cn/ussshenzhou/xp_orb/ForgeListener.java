package cn.ussshenzhou.xp_orb;

import cn.ussshenzhou.t88.task.TaskHelper;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeListener {

    @SubscribeEvent
    public static void cancelDamage(LivingDamageEvent event) {
        var source = event.getSource();
        if (source.is(XpOrb.DamageSource.SHOT)) {
            return;
        }
        if (source.getDirectEntity() instanceof Player || source.getEntity() instanceof Player) {
            event.setAmount(0);
        }
    }

    @SubscribeEvent
    public static void noPickUpXp(PlayerXpEvent.PickupXp event) {
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void playerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        var player = event.getEntity();
        tpToPlayer(player);
    }

    public static void tpToPlayer(Player player) {
        if (player.level().isClientSide) {
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        LinkedList<List<Entity>> cache = new LinkedList<>();
        level.getServer().getAllLevels().forEach(l -> {
            cache.add(StreamSupport.stream(l.getAllEntities().spliterator(), true)
                    .filter(e -> e instanceof ExperienceOrb orb && orb.followingPlayer == player)
                    .collect(Collectors.toList()));
        });
        TaskHelper.addServerTask(() -> {
            cache.forEach(list -> list.forEach(orb -> orb.teleportTo((ServerLevel) player.level(), player.getX(), player.getY(), player.getZ(), Set.of(), 0, 0)));
        }, 20 * 10);
    }

    @SubscribeEvent
    public static void playerTp(EntityTeleportEvent event) {
        if (event.getEntity() instanceof Player player) {
            tpToPlayer(player);
        }
    }

    @SubscribeEvent
    public static void regCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands
                .literal("start")
                .executes(context -> {
                    var s = context.getSource();
                    var c = s.getServer().getCommands();
                    c.performPrefixedCommand(s.getServer().createCommandSourceStack(), "/scoreboard objectives add orbs dummy");
                    c.performPrefixedCommand(s.getServer().createCommandSourceStack(), "/scoreboard objectives setdisplay list orbs");
                    return 1;
                }));
        event.getDispatcher().register(Commands
                .literal("recal")
                .executes(context -> {
                    var s = context.getSource().getServer();
                    s.getPlayerList().getPlayers().forEach(player -> {
                        var o = player.getScoreboard().getObjective(XpOrb.ScoreBoard.ORB_AMOUNT);
                        if (o != null) {
                            player.getScoreboard().getOrCreatePlayerScore(player, o).set(
                                    (int) StreamSupport.stream(s.getAllLevels().spliterator(), true)
                                            .flatMap(level -> StreamSupport.stream(level.getAllEntities().spliterator(), true))
                                            .filter(entity -> entity instanceof ExperienceOrb orb && orb.followingPlayer == player)
                                            .count()
                            );
                        }
                    });
                    return 1;
                }));
    }

    @SubscribeEvent
    public static void playerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        var level = (ServerLevel) event.getEntity().level();
        level.getServer().getAllLevels().forEach(l -> {
            StreamSupport.stream(l.getAllEntities().spliterator(), true)
                    .filter(e -> e instanceof ExperienceOrb orb && orb.followingPlayer == event.getEntity())
                    .sequential()
                    .forEach(entity -> {
                        XpOrb.updateAmount(entity, -1);
                        entity.setDeltaMovement(0, 0, 0);
                        //noinspection DataFlowIssue
                        ((ExperienceOrb) entity).followingPlayer = null;
                    });
        });
    }

    @SubscribeEvent
    public static void playerRespawn(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            if (!event.getEntity().level().isClientSide) {
                var s = event.getEntity().level().getServer();
                s.getCommands().performPrefixedCommand(s.createCommandSourceStack(), "/recal");
            }
        }
    }
}
