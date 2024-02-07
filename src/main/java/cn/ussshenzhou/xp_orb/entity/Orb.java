package cn.ussshenzhou.xp_orb.entity;

import cn.ussshenzhou.t88.network.NetworkHelper;
import cn.ussshenzhou.t88.task.TaskHelper;
import cn.ussshenzhou.xp_orb.XpOrb;
import cn.ussshenzhou.xp_orb.network.RecommendOrbCheckPacket;
import cn.ussshenzhou.xp_orb.network.ShootOrbPacket;
import cn.ussshenzhou.xp_orb.network.SwitchHurtPlayerPacket;
import cn.ussshenzhou.xp_orb.util.MovementHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * @author USS_Shenzhou
 */
@ParametersAreNonnullByDefault
public class Orb extends Entity {
    public int value = 1;
    private int age = 0;
    private int health = 5;
    public @Nullable Player followingPlayer;
    private double friction = 0.988;
    private boolean fireImmune = false;
    private int findPlayerCd = 0;

    public Orb(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static Orb convert(ExperienceOrb xpOrb) {
        var orb = new Orb(ModEntityTypes.ORB.get(), xpOrb.level());
        orb.value = xpOrb.value;
        orb.health = xpOrb.health;
        orb.followingPlayer = xpOrb.followingPlayer;
        orb.setPos(xpOrb.position());
        return orb;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (this.isInvulnerableTo(pSource)) {
            return false;
        } else if (this.level().isClientSide) {
            return true;
        } else {
            this.markHurt();
            this.health = (int) ((float) this.health - pAmount);
            if (this.health <= 0) {
                this.discard();
            }

            return true;
        }
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putShort("Health", (short) this.health);
        pCompound.putShort("Age", (short) this.age);
        pCompound.putShort("Value", (short) this.value);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        this.health = pCompound.getShort("Health");
        this.age = pCompound.getShort("Age");
        this.value = pCompound.getShort("Value");
    }

    public int getIcon() {
        if (this.value >= 2477) {
            return 10;
        } else if (this.value >= 1237) {
            return 9;
        } else if (this.value >= 617) {
            return 8;
        } else if (this.value >= 307) {
            return 7;
        } else if (this.value >= 149) {
            return 6;
        } else if (this.value >= 73) {
            return 5;
        } else if (this.value >= 37) {
            return 4;
        } else if (this.value >= 17) {
            return 3;
        } else if (this.value >= 7) {
            return 2;
        } else {
            return this.value >= 3 ? 1 : 0;
        }
    }

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

        if (followingPlayer == null && findPlayerCd <= 0) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, -0.02, 0));
        }

        if (this.isEyeInFluid(FluidTags.WATER)) {
            this.setUnderwaterMovement();
        }

        if (findPlayerCd <= 0 && this.tickCount % 20 == 1) {
            this.scanForEntities();
        }

        if (this.tickCount % 40 == 1 && !this.level().isClientSide && followingPlayer != null) {
            NetworkHelper.sendTo(PacketDistributor.ALL.noArg(), new RecommendOrbCheckPacket(this));
        }

        if (this.followingPlayer != null) {
            if (this.followingPlayer.isSpectator()) {
                XpOrb.updateAmount(this, -1);
                this.followingPlayer = null;
            } else if (this.followingPlayer.isDeadOrDying()) {
                random.setSeed(this.getId());
                if (random.nextFloat() < 0.1f) {
                    fireImmune = true;
                } else {
                    Vec3 vec3 = new Vec3(
                            this.followingPlayer.getX() - this.getX(),
                            this.followingPlayer.getY() + (double) this.followingPlayer.getEyeHeight() - this.getY() + 2,
                            this.followingPlayer.getZ() - this.getZ()
                    );
                    XpOrb.updateAmount(this, -1);
                    this.followingPlayer = null;
                    this.findPlayerCd = 8 * 20;
                    var d = vec3.length();
                    this.setDeltaMovement(vec3.scale(d / 10));
                    if (level().isClientSide) {
                        TaskHelper.addClientTask(() -> {
                            random.setSeed(this.getId());
                            float r = random.nextFloat() * 0.2f;
                            float theta = (random.nextBoolean() ? -1 : 1) * random.nextFloat() * (float) Math.PI;
                            float phi = (random.nextBoolean() ? -1 : 1) * random.nextFloat() * (float) Math.PI;
                            this.setDeltaMovement(r * Mth.sin(theta) * Mth.cos(phi),
                                    r * Mth.cos(theta),
                                    r * Mth.sin(theta) * Mth.sin(phi)
                            );
                            this.friction = 0.98;
                        }, 10);
                    } else {
                        TaskHelper.addServerTask(() -> {
                            random.setSeed(this.getId());
                            float r = random.nextFloat() * 0.2f;
                            float theta = (random.nextBoolean() ? -1 : 1) * random.nextFloat() * (float) Math.PI;
                            float phi = (random.nextBoolean() ? -1 : 1) * random.nextFloat() * (float) Math.PI;
                            this.setDeltaMovement(r * Mth.sin(theta) * Mth.cos(phi),
                                    r * Mth.cos(theta),
                                    r * Mth.sin(theta) * Mth.sin(phi)
                            );
                            this.friction = 0.98;
                        }, 10);
                    }
                }
            }
            if (fireImmune && this.followingPlayer != null && !this.followingPlayer.isDeadOrDying()) {
                fireImmune = false;
                this.setDeltaMovement(0, 0, 0);
                if (!level().isClientSide) {
                    var p = followingPlayer.position().add(-0.5, 0, -0.5);
                    random.setSeed(this.getId());
                    this.teleportTo(p.x + random.nextDouble(), p.y + random.nextDouble(), p.z + random.nextDouble());
                }
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
        findPlayerCd--;
    }

    private void setUnderwaterMovement() {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x * 0.99F, Math.min(vec3.y + 5.0E-4F, 0.06F), vec3.z * 0.99F);
    }

    private void checkHit() {
        if (followingPlayer == null || level().isClientSide) {
            return;
        }
        StreamSupport.stream(((ServerLevel) level()).getAllEntities().spliterator(), true)
                .filter(e -> e != null && (!(e instanceof Orb)) && (!(e instanceof ItemEntity)) && !e.getUUID().equals(followingPlayer.getUUID()))
                .filter(e -> this.getBoundingBox().intersects(e.getBoundingBox()))
                .sequential()
                .forEach(e -> {
                    if ((!this.followingPlayer.getTags().contains(SwitchHurtPlayerPacket.HURT)) && e instanceof Player) {
                        return;
                    }
                    e.hurt(this.damageSources().source(XpOrb.DamageSource.ORB, followingPlayer), 1);
                });
    }

    private void scanForEntities() {
        if (this.followingPlayer == null) {
            this.followingPlayer = this.level().getNearestPlayer(this, 8);
            XpOrb.updateAmount(this, 1);
            this.friction = 0.988;
        }
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
            if (pEntity instanceof Orb orb) {
                if (orb.followingPlayer != null) {
                    this.followingPlayer = level().getServer().getPlayerList().getPlayer(orb.followingPlayer.getUUID());
                }
            }
        }
    }

    @Override
    public boolean isOnPortalCooldown() {
        return true;
    }

    @Override
    public Vec3 collide(Vec3 pVec) {
        var r = MovementHelper.collideBoundingBox(null, pVec.x, pVec.y, pVec.z, this.getBoundingBox(), this.level(), List.of());
        return r == null ? pVec : r;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.AMBIENT;
    }

    @Override
    protected BlockPos getBlockPosBelowThatAffectsMyMovement() {
        return this.getOnPos(0.999999F);
    }
}
