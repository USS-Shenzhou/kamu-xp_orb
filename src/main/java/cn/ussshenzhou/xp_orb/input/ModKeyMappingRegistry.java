package cn.ussshenzhou.xp_orb.input;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModKeyMappingRegistry {
    @SubscribeEvent
    public static void onClientSetup(RegisterKeyMappingsEvent event) {
        event.register(KeyInputListener.SHOOT);
    }
}
