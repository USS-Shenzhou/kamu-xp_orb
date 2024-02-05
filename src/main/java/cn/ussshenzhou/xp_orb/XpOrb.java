package cn.ussshenzhou.xp_orb;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * @author USS_Shenzhou
 */
// The value here should match an entry in the META-INF/mods.toml file
@Mod(XpOrb.MOD_ID)
public class XpOrb {
    public static final String MOD_ID = "xp_orb";

    public XpOrb(IEventBus modEventBus) {

    }

    public static class DamageSource {
        public static final ResourceKey<DamageType> SHOT = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(MOD_ID, "orb"));
    }
}
