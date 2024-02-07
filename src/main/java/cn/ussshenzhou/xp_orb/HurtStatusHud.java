package cn.ussshenzhou.xp_orb;

import cn.ussshenzhou.t88.gui.util.ImageFit;
import cn.ussshenzhou.t88.gui.widegt.TComponent;
import cn.ussshenzhou.t88.gui.widegt.TImage;
import cn.ussshenzhou.xp_orb.network.SwitchHurtPlayerPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * @author USS_Shenzhou
 */
public class HurtStatusHud extends TImage {
    public static final ResourceLocation GREEN = new ResourceLocation(XpOrb.MOD_ID, "gui/orb_green.png");
    public static final ResourceLocation RED = new ResourceLocation(XpOrb.MOD_ID, "gui/orb_red.png");

    public HurtStatusHud() {
        super(GREEN);
    }

    @Override
    public void resizeAsHud(int screenWidth, int screenHeight) {
        this.setAbsBounds(0, screenHeight - 4, 4, 4);
        super.resizeAsHud(screenWidth, screenHeight);
        this.setImageFit(ImageFit.STRETCH);
    }

    @Override
    public void tickT() {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getTags().contains(SwitchHurtPlayerPacket.HURT)) {
            if (!this.getImageLocation().equals(RED)) {
                this.setImageLocation(RED);
            }
        } else {
            if (!this.getImageLocation().equals(GREEN)) {
                this.setImageLocation(GREEN);
            }
        }
        super.tickT();
    }
}
