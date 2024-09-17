package cc.unilock.polyhook.entity;

import cc.unilock.polyhook.PolyHook;
import cc.unilock.polyhook.mixin.EntityAccessor;
import cc.unilock.polyhook.mixinsupport.GrapplingHooker;
import cc.unilock.polyhook.registry.ModEntityTypes;
import cc.unilock.polyhook.registry.ModItems;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GrapplingHookEntity extends ProjectileEntity implements PolymerEntity {
	private int removalTimer;
	private State state = State.FLYING;

	public GrapplingHookEntity(EntityType<? extends ProjectileEntity> entityType, World world) {
		super(entityType, world);
	}

	public GrapplingHookEntity(PlayerEntity thrower, World world) {
		this(ModEntityTypes.GRAPPLING_HOOK, world);
		this.setOwner(thrower);
		float throwerPitch = thrower.getPitch();
		Vec3d throwerVelocity = thrower.getVelocity().normalize();
		float throwerYaw = thrower.getYaw();
		float i = MathHelper.cos(-throwerYaw * (float) (Math.PI / 180.0) - (float) Math.PI);
		float j = MathHelper.sin(-throwerYaw * (float) (Math.PI / 180.0) - (float) Math.PI);
		float k = -MathHelper.cos(-throwerPitch * (float) (Math.PI / 180.0));
		float l = MathHelper.sin(-throwerPitch * (float) (Math.PI / 180.0));
		double x = thrower.getX() - (double)j * 0.3;
		double y = thrower.getEyeY();
		double z = thrower.getZ() - (double)i * 0.3;
		this.refreshPositionAndAngles(x, y, z, throwerYaw, throwerPitch);
		Vec3d vec3d = new Vec3d(-j, MathHelper.clamp(-(l / k), -5.0F, 5.0F), -i);
		double m = vec3d.length();
		vec3d = vec3d.multiply(
			0.6 / m + this.random.nextTriangular(0.5, 0.0103365),
			0.6 / m + this.random.nextTriangular(0.5, 0.0103365),
			0.6 / m + this.random.nextTriangular(0.5, 0.0103365)
		);
		vec3d = vec3d.add(throwerVelocity.x, 0.0, throwerVelocity.z);
		vec3d = vec3d.multiply(3.0);
		this.setVelocity(vec3d);
		this.setYaw((float)(MathHelper.atan2(vec3d.x, vec3d.z) * 180.0F / (float)Math.PI));
		this.setPitch((float)(MathHelper.atan2(vec3d.y, vec3d.horizontalLength()) * 180.0F / (float)Math.PI));
		this.prevYaw = this.getYaw();
		this.prevPitch = this.getPitch();
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		// NO-OP
	}

	@Override
	public boolean shouldRender(double distance) {
		return distance < 4096.0;
	}

	@Override
	public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps) {
		// NO-OP
	}

	@Override
	public void tick() {
		super.tick();
		PlayerEntity playerEntity = this.getPlayerOwner();
		if (playerEntity == null) {
			this.discard();
		} else if (this.getWorld().isClient || !this.removeIfInvalid(playerEntity)) {
			if (this.isHooked()) {
				this.removalTimer++;
				if (this.removalTimer >= 1200) {
					this.discard();
					return;
				}
			} else {
				this.removalTimer = 0;
			}

			if (!this.isHooked()) {
				this.checkForCollision();

				this.move(MovementType.SELF, this.getVelocity());
				this.updateRotation();
				if (this.isOnGround() || this.horizontalCollision) {
					this.state = State.HOOKED;
					this.setVelocity(Vec3d.ZERO);
				} else {
					this.setVelocity(this.getVelocity().multiply(0.92));
				}

				this.refreshPosition();
			}
		}
	}

	private boolean removeIfInvalid(PlayerEntity player) {
		ItemStack itemStack = player.getMainHandStack();
		ItemStack itemStack2 = player.getOffHandStack();
		boolean bl = itemStack.isOf(ModItems.GRAPPLING_HOOK);
		boolean bl2 = itemStack2.isOf(ModItems.GRAPPLING_HOOK);
		if (!player.isRemoved() && player.isAlive() && (bl || bl2) && !(this.squaredDistanceTo(player) > 1024.0)) {
			return false;
		} else {
			this.discard();
			return true;
		}
	}

	private void checkForCollision() {
		HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
		this.hitOrDeflect(hitResult);
	}

	@Override
	protected boolean canHit(Entity entity) {
		return false;
	}

	@Override
	protected void onBlockHit(BlockHitResult blockHitResult) {
		super.onBlockHit(blockHitResult);
		this.setVelocity(this.getVelocity().normalize().multiply(blockHitResult.squaredDistanceTo(this)));
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		// NO-OP
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		// NO-OP
	}

	public void use() {
		PlayerEntity playerEntity = this.getPlayerOwner();
		if (!this.getWorld().isClient && playerEntity != null && !this.removeIfInvalid(playerEntity) && this.isHooked()) {
			playerEntity.addVelocity(this.getPos().subtract(playerEntity.getPos()).normalize().multiply(3.0));
			((EntityAccessor) playerEntity).callScheduleVelocityUpdate();

			this.discard();
		}
	}

	@Override
	protected Entity.MoveEffect getMoveEffect() {
		return Entity.MoveEffect.NONE;
	}

	@Override
	public void remove(Entity.RemovalReason reason) {
		this.setPlayerGrapplingHook(null);
		super.remove(reason);
	}

	@Override
	public void onRemoved() {
		this.setPlayerGrapplingHook(null);
	}

	@Override
	public void setOwner(@Nullable Entity entity) {
		super.setOwner(entity);
		this.setPlayerGrapplingHook(this);
	}

	private void setPlayerGrapplingHook(@Nullable GrapplingHookEntity grapplingHook) {
		PlayerEntity playerEntity = this.getPlayerOwner();
		if (playerEntity != null) {
			((GrapplingHooker) playerEntity).polyhook$setGrapplingHook(grapplingHook);
		}
	}

	@Nullable
	public PlayerEntity getPlayerOwner() {
		Entity entity = this.getOwner();
		return entity instanceof PlayerEntity player ? player : null;
	}

	@Override
	public boolean canUsePortals(boolean allowVehicles) {
		return false;
	}

	@Override
	public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
		Entity entity = this.getOwner();
		return new EntitySpawnS2CPacket(this, entityTrackerEntry, entity == null ? this.getId() : entity.getId());
	}

	@Override
	public void onSpawnPacket(EntitySpawnS2CPacket packet) {
		super.onSpawnPacket(packet);
		if (this.getPlayerOwner() == null) {
			int i = packet.getEntityData();
			PolyHook.LOGGER.error("Failed to recreate grappling hook on client. {} (id: {}) is not a valid owner.", this.getWorld().getEntityById(i), i);
			this.kill();
		}
	}

	public boolean isHooked() {
		return this.state == State.HOOKED;
	}

	@Override
	public EntityType<?> getPolymerEntityType(ServerPlayerEntity player) {
		return EntityType.FISHING_BOBBER;
	}

	private enum State {
		FLYING,
		HOOKED
	}
}
