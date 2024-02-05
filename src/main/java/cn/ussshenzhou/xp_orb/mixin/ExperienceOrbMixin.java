package cn.ussshenzhou.xp_orb.mixin;

import cn.ussshenzhou.t88.task.TaskHelper;
import cn.ussshenzhou.xp_orb.IShootOrb;
import cn.ussshenzhou.xp_orb.XpOrb;
import cn.ussshenzhou.xp_orb.network.ShootOrbPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
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
public abstract class ExperienceOrbMixin extends Entity implements IShootOrb {

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

    @Unique
    private double friction = 0.988;

    @Unique
    private boolean fireImmune = false;

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
        if (followingPlayer != null && followingPlayer.getTags().contains(ShootOrbPacket.SHOOTING)) {
            this.checkHit();
            this.age++;
            return;
        }

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

        if (this.followingPlayer != null) {
            if (this.followingPlayer.isSpectator()) {
                XpOrb.updateAmount(this, -1);
                this.followingPlayer = null;
            } else if (this.followingPlayer.isDeadOrDying()) {
                if (random.nextFloat() < 0.1f) {
                    fireImmune = true;
                } else {
                    Vec3 vec3 = new Vec3(
                            this.followingPlayer.getX() - this.getX(),
                            this.followingPlayer.getY() + (double) this.followingPlayer.getEyeHeight() / 2.0 - this.getY() + 2,
                            this.followingPlayer.getZ() - this.getZ()
                    );
                    XpOrb.updateAmount(this, -1);
                    this.followingPlayer = null;
                    var d = vec3.length();
                    this.setDeltaMovement(vec3.scale(d / 10));
                    TaskHelper.addServerTask(() -> {
                        float theta = random.nextBoolean() ? -1 : 1 * random.nextFloat() * (float) Math.PI;
                        float phi = random.nextBoolean() ? -1 : 1 * random.nextFloat() * (float) Math.PI;
                        this.setDeltaMovement(0.1 * Mth.sin(theta) * Mth.cos(phi),
                                0.1 * Mth.sin(theta) * Mth.sin(phi),
                                0.1 * Mth.cos(theta)
                        );
                    }, 10);
                }
            }
            if (fireImmune && !this.followingPlayer.isDeadOrDying()) {
                fireImmune = false;
                this.setDeltaMovement(0, 0, 0);
                this.moveTo(followingPlayer.getEyePosition());
            }
        }

        if (this.followingPlayer != null) {
            friction = followingPlayer.isCrouching() ? 0.85 : 0.988;
            Vec3 vec3 = new Vec3(
                    this.followingPlayer.getX() - this.getX(),
                    this.followingPlayer.getY() + (double) this.followingPlayer.getEyeHeight() / 2.0 - this.getY(),
                    this.followingPlayer.getZ() - this.getZ()
            );
            double d1 = 0.055;
            this.setDeltaMovement(this.getDeltaMovement().add(vec3.normalize().scale(d1)));
        }
        //----
        checkHit();
        //----
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(friction));

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
            XpOrb.updateAmount(this, 1);
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

    @Override
    public boolean fireImmune() {
        return fireImmune;
    }

    @Override
    public void remove(RemovalReason pReason) {
        XpOrb.updateAmount(this, -1);
        super.remove(pReason);
    }

    @Override
    public void restoreFrom(Entity pEntity) {
        super.restoreFrom(pEntity);
        if (!level().isClientSide) {
            if (pEntity instanceof ExperienceOrb orb) {
                if (orb.followingPlayer != null) {
                    var player = level().getServer().getPlayerList().getPlayer(orb.followingPlayer.getUUID());
                    this.followingPlayer = player;
                }
            }
        }
    }


    @Override
    public boolean isOnPortalCooldown() {
        return true;
    }
}
