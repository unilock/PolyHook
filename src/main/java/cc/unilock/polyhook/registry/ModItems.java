package cc.unilock.polyhook.registry;

import cc.unilock.polyhook.PolyHook;
import cc.unilock.polyhook.item.GrapplingHookItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModItems {
	public static final GrapplingHookItem GRAPPLING_HOOK = register("grappling_hook", new GrapplingHookItem(new Item.Settings()));

	public static void init() {}

	private static <T extends Item> T register(String path, T item) {
		return Registry.register(Registries.ITEM, PolyHook.id(path), item);
	}
}
