package cn.ussshenzhou.xp_orb;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.scores.Objective;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.spongepowered.asm.mixin.Unique;

/**
 * @author USS_Shenzhou
 */
// The value here should match an entry in the META-INF/mods.toml file
@Mod(XpOrb.MOD_ID)
public class XpOrb {
    public static final String MOD_ID = "xp_orb";

    public XpOrb(IEventBus modEventBus) {

    }

    public static void updateAmount(ExperienceOrb orb, int getOrLost) {
        //noinspection ConstantValue
        if (orb.followingPlayer == null || orb.level().isClientSide) {
            return;
        }
        var score = orb.level().getScoreboard();
        var obj = score.getObjective(ScoreBoard.ORB_AMOUNT);
        if (obj != null) {
            score.getOrCreatePlayerScore(orb.followingPlayer, obj).add(getOrLost);
        }
    }

    public static void updateAmount(Object orb, int getOrLost) {
        updateAmount((ExperienceOrb) orb, getOrLost);
    }

    public static class DamageSource {
        public static final ResourceKey<DamageType> SHOT = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(MOD_ID, "orb"));
    }

    public static class ScoreBoard {
        public static final String ORB_AMOUNT = "orbs";
    }
}
