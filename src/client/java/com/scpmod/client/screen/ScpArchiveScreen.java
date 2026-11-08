package com.scpmod.client.screen;

import com.scpmod.content.ModItems;
import com.scpmod.item.ScpDocumentItem;
import com.scpmod.scp.ScpDefinition;
import com.scpmod.scp.ScpRegistry;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** SCP 档案：左侧索引列表 + 右侧详情（含图标预览）。 */
public class ScpArchiveScreen extends Screen {
	private static final int ROW_H = 22;
	private static final int LIST_PAD = 8;

	private final List<ScpDefinition> scps = new ArrayList<>(ScpRegistry.all());
	private final List<String> recorded;
	private int selected;

	public ScpArchiveScreen(ItemStack document) {
		super(Component.translatable("screen.scpmod.archive"));
		this.recorded = ScpDocumentItem.recorded(document);
	}

	private int panelW() {
		return Math.min(460, this.width - 60);
	}

	private int panelH() {
		return Math.min(300, this.height - 60);
	}

	private int panelX() {
		return (this.width - panelW()) / 2;
	}

	private int panelY() {
		return (this.height - panelH()) / 2;
	}

	private int listW() {
		return Math.min(190, (int) (panelW() * 0.42D));
	}

	private int listX() {
		return panelX() + LIST_PAD;
	}

	private int listY() {
		return panelY() + 34;
	}

	private int detailX() {
		return panelX() + listW() + LIST_PAD + 6;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		int x0 = panelX();
		int y0 = panelY();
		int w = panelW();
		int h = panelH();
		graphics.fill(0, 0, this.width, this.height, 0xC0121418);
		graphics.fill(x0, y0, x0 + w, y0 + h, 0xFF1B1D22);
		graphics.fill(x0, y0, x0 + w, y0 + 1, 0xFF4B4F57);
		graphics.fill(x0, y0 + h - 1, x0 + w, y0 + h, 0xFF4B4F57);
		graphics.fill(x0, y0, x0 + 1, y0 + h, 0xFF4B4F57);
		graphics.fill(x0 + w - 1, y0, x0 + w, y0 + h, 0xFF4B4F57);
		graphics.centeredText(this.font, this.title, this.width / 2, y0 + 8, 0xFFE8E8E0);
		graphics.fill(x0 + LIST_PAD, y0 + 24, x0 + w - LIST_PAD, y0 + 25, 0xFF3A3F47);

		int rowY = listY();
		for (int i = 0; i < this.scps.size(); i++) {
			ScpDefinition definition = this.scps.get(i);
			boolean known = this.recorded.contains(definition.id().getPath());
			if (i == this.selected) {
				graphics.fill(listX() - 4, rowY - 2, listX() + listW() - 4, rowY + ROW_H - 4, 0xFF343941);
			}
			Component label = ScpRegistry.displayName(definition.id().getPath());
			graphics.text(this.font, label, listX(), rowY, known ? 0xFFE8E8E0 : 0xFF7A7A7A);
			if (!known) {
				graphics.text(this.font, Component.translatable("screen.scpmod.not_recorded"),
						listX() + listW() - 50, rowY, 0xFF5E5E5E);
			}
			rowY += ROW_H;
		}

		int detailX = detailX();
		int detailY = listY() - 2;
		if (this.selected >= 0 && this.selected < this.scps.size()) {
			ScpDefinition definition = this.scps.get(this.selected);
			boolean known = this.recorded.contains(definition.id().getPath());
			// 图标预览
			graphics.item(preview(definition.id().getPath()), detailX, detailY);
			graphics.text(this.font, ScpRegistry.displayName(definition.id().getPath()),
					detailX + 24, detailY + 4, 0xFFFFFFFF);
			if (known) {
				graphics.text(this.font, Component.literal("等级: " + definition.classification()),
						detailX + 24, detailY + 20, 0xFF9FB4C8);
				graphics.textWithWordWrap(this.font, Component.translatable(definition.descriptionKey()),
						detailX, detailY + 42, panelW() - listW() - LIST_PAD * 2 - 12, 0xFFD0D0C8);
			} else {
				graphics.textWithWordWrap(this.font, Component.translatable("screen.scpmod.unknown_hint"),
						detailX, detailY + 42, panelW() - listW() - LIST_PAD * 2 - 12, 0xFF888888);
			}
		}
		graphics.centeredText(this.font, Component.translatable("screen.scpmod.close_hint"),
				this.width / 2, y0 + h - 14, 0xFF6A6A6A);
	}

	private ItemStack preview(String path) {
		return switch (path) {
			case "scp_173" -> new ItemStack(ModItems.SCP_173_SPAWNER);
			case "scp_500" -> new ItemStack(ModItems.SCP_500);
			case "scp_005" -> new ItemStack(ModItems.SCP_005);
			default -> new ItemStack(ModItems.SCP_DOCUMENT);
		};
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		if (event.button() == 0) {
			int rowY = listY();
			for (int i = 0; i < this.scps.size(); i++) {
				if (event.x() >= listX() - 4 && event.x() <= listX() + listW() - 4
						&& event.y() >= rowY - 2 && event.y() <= rowY + ROW_H - 4) {
					this.selected = i;
					return true;
				}
				rowY += ROW_H;
			}
		}
		return super.mouseClicked(event, doubled);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
