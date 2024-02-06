package cn.ussshenzhou.xp_orb.mixin;

import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author USS_Shenzhou
 */
@Mixin(EntityType.class)
public class EntityTypeMixin {

    @ModifyConstant(method = "<clinit>",constant = @Constant(intValue = 20,ordinal = 1))
    private static int alwaysTrackXpOrb(int constant){
        return 1;
    }

}
