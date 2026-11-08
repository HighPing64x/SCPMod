package com.scpmod.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * 公开 API：SCP-005（万能钥匙）尝试开锁事件。
 * 其他模组可以监听该事件来解锁自己的“锁”机制；返回 true 表示已处理。
 */
public final class Scp005Events {
	public interface TryUnlock {
		boolean onTryUnlock(ServerPlayer player, Level level, BlockPos pos);
	}

	public static final Event<TryUnlock> TRY_UNLOCK = EventFactory.createArrayBacked(TryUnlock.class,
			callbacks -> (player, level, pos) -> {
				for (TryUnlock callback : callbacks) {
					if (callback.onTryUnlock(player, level, pos)) {
						return true;
					}
				}
				return false;
			});

	private Scp005Events() {
	}
}
