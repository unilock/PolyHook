package cc.unilock.polyhook.item;

import cc.unilock.polyhook.entity.GrapplingHookEntity;
import cc.unilock.polyhook.mixinsupport.GrapplingHooker;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class GrapplingHookItem extends Item implements PolymerItem {
	public GrapplingHookItem(Item.Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack itemStack = user.getStackInHand(hand);
		GrapplingHooker hooker = (GrapplingHooker) user;
		if (hooker.polyhook$getGrapplingHook() != null) {
			if (!world.isClient) {
				hooker.polyhook$getGrapplingHook().use();
			}

			world.playSound(
				null,
				user.getX(),
				user.getY(),
				user.getZ(),
				SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE,
				SoundCategory.NEUTRAL,
				1.0F,
				0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F)
			);
			user.emitGameEvent(GameEvent.ITEM_INTERACT_FINISH);
		} else {
			world.playSound(
				null,
				user.getX(),
				user.getY(),
				user.getZ(),
				SoundEvents.ENTITY_FISHING_BOBBER_THROW,
				SoundCategory.NEUTRAL,
				0.5F,
				0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F)
			);
			if (!world.isClient) {
				world.spawnEntity(new GrapplingHookEntity(user, world));
			}

			user.incrementStat(Stats.USED.getOrCreateStat(this));
			user.emitGameEvent(GameEvent.ITEM_INTERACT_START);
		}

		return TypedActionResult.success(itemStack, world.isClient());
	}

	@Override
	public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
		return Items.FISHING_ROD;
	}

	@Override
	public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player) {
		return PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
	}
}
