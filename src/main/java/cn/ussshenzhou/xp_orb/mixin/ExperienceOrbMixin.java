package cn.ussshenzhou.xp_orb.mixin;

import cn.ussshenzhou.xp_orb.XpOrb;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author USS_Shenzhou
 */
@Mixin(ExperienceOrb.class)
@ParametersAreNonnullByDefault
public abstract class ExperienceOrbMixin extends Entity {

    @Shadow
    private int age;

    @Shadow
    protected abstract void setUnderwaterMovement();

    @Shadow
    @Nullable
    private Player followingPlayer;

    public ExperienceOrbMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    @Override
    public void tick() {
        super.tick();
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        if (this.isEyeInFluid(FluidTags.WATER)) {
            this.setUnderwaterMovement();
        }

        if (this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA)) {
            this.setDeltaMovement(
                    (this.random.nextFloat() - this.random.nextFloat()) * 0.2F,
                    0.2F,
                    (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
            );
        }

        if (!this.level().noCollision(this.getBoundingBox())) {
            this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
        }

        if (this.tickCount % 20 == 1) {
            this.scanForEntities();
        }

        if (this.followingPlayer != null && (this.followingPlayer.isSpectator() || this.followingPlayer.isDeadOrDying())) {
            this.followingPlayer = null;
        }

        if (this.followingPlayer != null) {
            Vec3 vec3 = new Vec3(
                    this.followingPlayer.getX() - this.getX(),
                    this.followingPlayer.getY() + (double) this.followingPlayer.getEyeHeight() / 2.0 - this.getY(),
                    this.followingPlayer.getZ() - this.getZ()
            );
            double d1 = 0.05 + random.nextFloat() * 0.02;
            this.setDeltaMovement(this.getDeltaMovement().add(vec3.normalize().scale(d1)));
        }
        //----
        checkHit();
        //----
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.988));

        ++this.age;
    }

    @Unique
    private void checkHit() {
        if (followingPlayer == null) {
            return;
        }
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, e -> true);
        HitResult.Type type = hitresult.getType();
        if (type == HitResult.Type.ENTITY) {
            var hit = (EntityHitResult) hitresult;
            var e = hit.getEntity();
            if (e instanceof ExperienceOrb
                    || e instanceof ItemEntity
                    || e.getUUID().equals(followingPlayer.getUUID())
            ) {
                return;
            }
            e.hurt(this.damageSources().source(XpOrb.DamageSource.SHOT, followingPlayer), 1);
        }
    }

    /**
     * @author
     * @reason
     */
    @Override
    @Overwrite
    public void playerTouch(Player pEntity) {
        if (!this.level().isClientSide) {

        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void scanForEntities() {
        if (this.followingPlayer == null) {
            this.followingPlayer = this.level().getNearestPlayer(this, 16);
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private static boolean tryMergeToExisting(ServerLevel pLevel, Vec3 pPos, int pAmount) {
        return false;

    }

}
