package com.scpmod.scp;

import com.scpmod.ScpMod;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

/**
 * 玩家身上的「SCP 档案」进度（持久化 + 同步附件）。
 *
 * <p>把收录进度挂在玩家本身，而不是物品上：档案书丢在地上、存进箱子、或玩家死亡后
 * 掉落都不会丢记录，也不会重复发放新书。同步到客户端后，档案界面可直接读取。
 */
public final class ScpArchiveData {
	private ScpArchiveData() {
	}

	private static final StreamCodec<io.netty.buffer.ByteBuf, List<String>> LIST_CODEC =
			ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8);

	public static final AttachmentType<List<String>> RECORDED =
			AttachmentRegistry.<List<String>>builder()
					.persistent(com.mojang.serialization.Codec.STRING.listOf())
					.initializer(ArrayList::new)
					.copyOnDeath()
					.syncWith(LIST_CODEC, AttachmentSyncPredicate.targetOnly())
					.buildAndRegister(ScpMod.id("scp_archive"));

	/** 已收录的 SCP path 列表（可变，直接修改即可）。 */
	public static List<String> get(net.minecraft.world.entity.player.Player player) {
		List<String> list = player.getAttachedOrCreate(RECORDED, ArrayList::new);
		if (!(list instanceof ArrayList)) {
			list = new ArrayList<>(list);
			player.setAttached(RECORDED, list);
		}
		return list;
	}

	/** 客户端只读访问（可能还没同步到）。 */
	public static List<String> getOrEmpty(net.minecraft.world.entity.player.Player player) {
		List<String> list = player.getAttached(RECORDED);
		return list == null ? List.of() : list;
	}
}
