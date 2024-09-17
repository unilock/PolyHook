package cc.unilock.polyhook;

import cc.unilock.polyhook.registry.ModEntityTypes;
import cc.unilock.polyhook.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolyHook implements ModInitializer {
	public static final String MOD_ID = "polyhook";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModEntityTypes.init();
		ModItems.init();

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(ModItems.GRAPPLING_HOOK));
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
