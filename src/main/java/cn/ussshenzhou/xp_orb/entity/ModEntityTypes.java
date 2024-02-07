package cn.ussshenzhou.xp_orb.entity;

import cn.ussshenzhou.xp_orb.XpOrb;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * @author USS_Shenzhou
 */
public class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPE = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, XpOrb.MOD_ID);

    public static final Supplier<EntityType<Orb>> ORB = ENTITY_TYPE.register("orb",
            () -> EntityType.Builder.of(Orb::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(8)
                    .updateInterval(Integer.MAX_VALUE)
                    .build("fake_orb")
    );
}
