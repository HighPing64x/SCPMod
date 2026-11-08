package com.scpmod.item;

import com.scpmod.content.ModItems;
import com.scpmod.scp.ScpDefinition;
import com.scpmod.scp.ScpRegistry;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * SCP 文档（图鉴）：右键查看已收录的异常；玩家首次遇到新 SCP 时自动录入。
 * 数据保存在物品的 custom_data 里，可跨世界/多人携带。
 */
public class ScpDocumentItem extends Item {
	private static final String KEY_RECORDED = "recorded";

	public ScpDocumentItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			show(serverPlayer, serverPlayer.getItemInHand(hand));
		}
		return InteractionResult.SUCCESS;
	}

	/** 把某个 SCP 录入玩家文档；没有文档时自动发放一本。 */
	public static void record(ServerPlayer player, String scpId) {
		if (scpId == null || scpId.isEmpty()) {
			return;
		}
		String path = scpId.contains(":") ? scpId.substring(scpId.indexOf(':') + 1) : scpId;
		ItemStack doc = findDocument(player);
		if (doc == null) {
			ItemStack created = new ItemStack(ModItems.SCP_DOCUMENT);
			if (!player.getInventory().add(created)) {
				player.drop(created, false);
			}
			player.sendSystemMessage(Component.translatable("message.scpmod.document.obtained")
					.withStyle(ChatFormatting.GRAY));
			doc = findDocument(player);
			if (doc == null) {
				return;
			}
		}
		List<String> recorded = recorded(doc);
		if (recorded.contains(path)) {
			return;
		}
		recorded.add(path);
		save(doc, recorded);
		player.sendSystemMessage(Component.translatable("message.scpmod.document.recorded",
				ScpRegistry.displayName(path)).withStyle(ChatFormatting.GREEN));
	}

	/** 通过命令收录全部已注册 SCP（没有文档则先发放一本）。 */
	public static void recordAll(ServerPlayer player) {
		if (findDocument(player) == null) {
			ItemStack created = new ItemStack(ModItems.SCP_DOCUMENT);
			if (!player.getInventory().add(created)) {
				player.drop(created, false);
			}
		}
		ItemStack doc = findDocument(player);
		if (doc == null) {
			return;
		}
		List<String> recorded = recorded(doc);
		for (ScpDefinition definition : ScpRegistry.all()) {
			String path = definition.id().getPath();
			if (!recorded.contains(path)) {
				recorded.add(path);
			}
		}
		save(doc, recorded);
		player.sendSystemMessage(Component.translatable("message.scpmod.document.all",
				recorded.size(), ScpRegistry.size()).withStyle(ChatFormatting.GREEN));
	}

	public static void show(ServerPlayer player, ItemStack stack) {
		List<String> recorded = recorded(stack);
		player.sendSystemMessage(Component.literal("===== SCP 文档 =====").withStyle(ChatFormatting.GOLD));
		player.sendSystemMessage(Component.literal("已收录 " + recorded.size() + " / " + ScpRegistry.size())
				.withStyle(ChatFormatting.YELLOW));
		for (ScpDefinition definition : ScpRegistry.all()) {
			String path = definition.id().getPath();
			if (recorded.contains(path)) {
				player.sendSystemMessage(Component.literal("[✓] " + ScpRegistry.displayName(path).getString()
						+ " (" + definition.classification() + ")").withStyle(ChatFormatting.GRAY));
			} else {
				player.sendSystemMessage(Component.literal("[?] 未知异常").withStyle(ChatFormatting.DARK_GRAY));
			}
		}
	}

	public static List<String> recorded(ItemStack stack) {
		List<String> result = new ArrayList<>();
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		ListTag list = tag.getListOrEmpty(KEY_RECORDED);
		for (int i = 0; i < list.size(); i++) {
			list.getString(i).ifPresent(result::add);
		}
		return result;
	}

	private static void save(ItemStack stack, List<String> recorded) {
		ListTag list = new ListTag();
		for (String id : recorded) {
			list.add(StringTag.valueOf(id));
		}
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put(KEY_RECORDED, list));
	}

	private static ItemStack findDocument(ServerPlayer player) {
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (stack.is(ModItems.SCP_DOCUMENT)) {
				return stack;
			}
		}
		return null;
	}
}
