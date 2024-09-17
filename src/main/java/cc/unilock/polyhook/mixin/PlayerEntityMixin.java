package cc.unilock.polyhook.mixin;

import cc.unilock.polyhook.entity.GrapplingHookEntity;
import cc.unilock.polyhook.mixinsupport.GrapplingHooker;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements GrapplingHooker {
	@Unique
	@Nullable
	private GrapplingHookEntity grapplingHook;

	@Override
	public GrapplingHookEntity polyhook$getGrapplingHook() {
		return this.grapplingHook;
	}

	@Override
	public void polyhook$setGrapplingHook(GrapplingHookEntity value) {
		this.grapplingHook = value;
	}
}
