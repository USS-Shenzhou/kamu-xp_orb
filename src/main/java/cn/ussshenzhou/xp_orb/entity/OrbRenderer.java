package cn.ussshenzhou.xp_orb.entity;

import cn.ussshenzhou.xp_orb.network.SwitchHurtPlayerPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class OrbRenderer extends EntityRenderer<Orb> {
    private static final ResourceLocation EXPERIENCE_ORB_LOCATION = new ResourceLocation("textures/entity/experience_orb.png");
    private static final RenderType RENDER_TYPE = RenderType.itemEntityTranslucentCull(EXPERIENCE_ORB_LOCATION);

    public OrbRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.shadowRadius = 0;
        this.shadowStrength = 0.75F;
    }

    @Override
    protected int getBlockLightLevel(Orb pEntity, BlockPos pPos) {
        return Mth.clamp(super.getBlockLightLevel(pEntity, pPos) + 7, 0, 15);
    }

    @Override
    public void render(Orb pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();
        int i = pEntity.getIcon();
        float uMin = (float) (i % 4 * 16 + 0) / 64.0F;
        float uMax = (float) (i % 4 * 16 + 16) / 64.0F;
        float vMin = (float) (i / 4 * 16 + 0) / 64.0F;
        float vMax = (float) (i / 4 * 16 + 16) / 64.0F;
        float f4 = 1.0F;
        float f5 = 0.5F;
        float f6 = 0.25F;
        float f7 = 255.0F;
        float f8 = ((float) pEntity.tickCount + pEntity.getId() + pPartialTicks) / 2.0F;
        int r;
        int g;
        //if (pEntity.followingPlayer != null && pEntity.followingPlayer.getTags().contains(SwitchHurtPlayerPacket.HURT)) {
        //    g = (int) ((Mth.sin(f8 + 0.0F) + 1.0F) * 0.5F * 255.0F);
        //    r = 255;
        //} else {
            r = (int) ((Mth.sin(f8 + 0.0F) + 1.0F) * 0.5F * 255.0F);
            g = 255;
        //}
        int b = (int) ((Mth.sin(f8 + (float) (Math.PI * 4.0 / 3.0)) + 1.0F) * 0.1F * 255.0F);
        pPoseStack.translate(0.0F, 0.1F, 0.0F);
        pPoseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        float f9 = 0.3F;
        pPoseStack.scale(0.3F, 0.3F, 0.3F);
        VertexConsumer vertexconsumer = pBuffer.getBuffer(RENDER_TYPE);
        PoseStack.Pose posestack$pose = pPoseStack.last();
        Matrix4f matrix4f = posestack$pose.pose();
        Matrix3f matrix3f = posestack$pose.normal();
        vertex(vertexconsumer, matrix4f, matrix3f, -0.5F, -0.25F, r, g, b, uMin, vMax, pPackedLight);
        vertex(vertexconsumer, matrix4f, matrix3f, 0.5F, -0.25F, r, g, b, uMax, vMax, pPackedLight);
        vertex(vertexconsumer, matrix4f, matrix3f, 0.5F, 0.75F, r, g, b, uMax, vMin, pPackedLight);
        vertex(vertexconsumer, matrix4f, matrix3f, -0.5F, 0.75F, r, g, b, uMin, vMin, pPackedLight);
        pPoseStack.popPose();
    }

    private static void vertex(
            VertexConsumer pConsumer,
            Matrix4f pMatrix,
            Matrix3f pMatrixNormal,
            float pX,
            float pY,
            int pRed,
            int pGreen,
            int pBlue,
            float pTexU,
            float pTexV,
            int pPackedLight
    ) {
        pConsumer.vertex(pMatrix, pX, pY, 0.0F)
                .color(pRed, pGreen, pBlue, 128)
                .uv(pTexU, pTexV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(pPackedLight)
                .normal(pMatrixNormal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getTextureLocation(Orb pEntity) {
        return EXPERIENCE_ORB_LOCATION;
    }
}
