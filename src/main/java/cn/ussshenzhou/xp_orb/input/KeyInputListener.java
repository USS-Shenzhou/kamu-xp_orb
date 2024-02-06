package cn.ussshenzhou.xp_orb.input;

import cn.ussshenzhou.t88.network.NetworkHelper;
import cn.ussshenzhou.xp_orb.network.ShootOrbPacket;
import cn.ussshenzhou.xp_orb.network.SwitchHurtPlayerPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class KeyInputListener {
    public static final KeyMapping SHOOT = new KeyMapping(
            "开~炮！", KeyConflictContext.IN_GAME, KeyModifier.NONE,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "经 验 生 存"
    );
    public static final KeyMapping HURT = new KeyMapping(
            "痛！", KeyConflictContext.IN_GAME, KeyModifier.NONE,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "经 验 生 存"
    );

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (SHOOT.consumeClick()) {
            NetworkHelper.sendToServer(new ShootOrbPacket());
        }else if (SHOOT.consumeClick()){
            NetworkHelper.sendToServer(new SwitchHurtPlayerPacket());
        }
    }
}
