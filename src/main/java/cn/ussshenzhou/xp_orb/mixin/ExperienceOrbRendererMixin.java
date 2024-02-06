package cn.ussshenzhou.xp_orb.mixin;


import net.minecraft.client.renderer.entity.ExperienceOrbRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author USS_Shenzhou
 */
@Mixin(ExperienceOrbRenderer.class)
public class ExperienceOrbRendererMixin {

    @ModifyConstant(method = "<init>",constant = @Constant(floatValue = 0.15f))
    private float noShadow(float constant){
        return 0;
    }
}
