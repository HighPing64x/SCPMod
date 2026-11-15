package com.scpmod.item;

import com.scpmod.content.ModItems;
import com.scpmod.scp.ScpArchiveData;
import com.scpmod.scp.ScpDefinition;
import com.scpmod.scp.ScpRegistry;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * SCP 档案（图鉴）：右键查看已收录的异常。
 *
 * <p>收录进度保存在**玩家身上**（{@link ScpArchiveData} 持久化附件），
 * 而不是物品 NBT：书本丢失/放箱子/死亡掉落都不会丢记录，也不会重复发新书。
 */
public class ScpDocumentItem extends Item {
	public ScpDocumentItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		// 客户端界面由 ScpModClient 的 UseItemCallback 打开。
		return InteractionResult.SUCCESS;
	}

	/** 把某个 SCP 录入玩家档案；玩家还没有档案书时自动发放一本。 */
	public static void record(ServerPlayer player, String scpId) {
		if (scpId == null || scpId.isEmpty()) {
			return;
		}
		String path = scpId.contains(":") ? scpId.substring(scpId.indexOf(':') + 1) : scpId;
		List<String> recorded = ScpArchiveData.get(player);
		if (recorded.contains(path)) {
			return;
		}
		recorded.add(path);
		player.sendSystemMessage(Component.translatable("message.scpmod.document.recorded",
				ScpRegistry.displayName(path)).withStyle(ChatFormatting.GREEN));
		// 首次收录时确保玩家身上有一本档案书（物品只是查看入口）。
		if (!hasDocument(player)) {
			giveDocument(player);
		}
	}

	/** 通过命令收录全部已注册 SCP。 */
	public static void recordAll(ServerPlayer player) {
		List<String> recorded = ScpArchiveData.get(player);
		for (ScpDefinition definition : ScpRegistry.all()) {
			String path = definition.id().getPath();
			if (!recorded.contains(path)) {
				recorded.add(path);
			}
		}
		player.sendSystemMessage(Component.translatable("message.scpmod.document.all",
				recorded.size(), ScpRegistry.size()).withStyle(ChatFormatting.GREEN));
		if (!hasDocument(player)) {
			giveDocument(player);
		}
	}

	/** 读取玩家的收录列表（供界面使用）。 */
	public static List<String> recorded(Player player) {
		return ScpArchiveData.get(player);
	}

	private static boolean hasDocument(ServerPlayer player) {
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			if (player.getInventory().getItem(i).is(ModItems.SCP_DOCUMENT)) {
				return true;
			}
		}
		return false;
	}

	private static void giveDocument(ServerPlayer player) {
		ItemStack created = new ItemStack(ModItems.SCP_DOCUMENT);
		if (!player.getInventory().add(created)) {
			player.drop(created, false);
		}
		player.sendSystemMessage(Component.translatable("message.scpmod.document.obtained")
				.withStyle(ChatFormatting.GRAY));
	}
}
