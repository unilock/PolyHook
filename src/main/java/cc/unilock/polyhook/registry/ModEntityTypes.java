package cc.unilock.polyhook.registry;

import cc.unilock.polyhook.PolyHook;
import cc.unilock.polyhook.entity.GrapplingHookEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntityTypes {
	public static final EntityType<GrapplingHookEntity> GRAPPLING_HOOK = register("grappling_hook", EntityType.Builder.<GrapplingHookEntity>create(GrapplingHookEntity::new, SpawnGroup.MISC).disableSaving().disableSummon().dimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(5));

	public static void init() {}

	private static <T extends Entity> EntityType<T> register(String path, EntityType.Builder<T> builder) {
		Identifier id = PolyHook.id(path);
		return Registry.register(Registries.ENTITY_TYPE, id, builder.build(id.toString()));
	}
}
