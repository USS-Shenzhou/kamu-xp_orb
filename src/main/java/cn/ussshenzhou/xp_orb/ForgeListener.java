package cn.ussshenzhou.xp_orb;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

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
}
