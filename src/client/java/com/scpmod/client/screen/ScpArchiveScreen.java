package com.scpmod.client.screen;

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

/** SCP 档案：左侧索引列表 + 右侧详情页。 */
public class ScpArchiveScreen extends Screen {
	private static final int PANEL_W = 360;
	private static final int PANEL_H = 210;
	private static final int ROW_H = 18;

	private final ItemStack document;
	private final List<ScpDefinition> scps = new ArrayList<>(ScpRegistry.all());
	private final List<String> recorded;
	private int selected;

	public ScpArchiveScreen(ItemStack document) {
		super(Component.translatable("screen.scpmod.archive"));
		this.document = document;
		this.recorded = ScpDocumentItem.recorded(document);
	}

	private int panelX() {
		return (this.width - PANEL_W) / 2;
	}

	private int panelY() {
		return (this.height - PANEL_H) / 2;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		int x0 = panelX();
		int y0 = panelY();
		graphics.fill(0, 0, this.width, this.height, 0xC0121418);
		graphics.fill(x0, y0, x0 + PANEL_W, y0 + PANEL_H, 0xFF1B1D22);
		// 边框
		graphics.fill(x0, y0, x0 + PANEL_W, y0 + 1, 0xFF4B4F57);
		graphics.fill(x0, y0 + PANEL_H - 1, x0 + PANEL_W, y0 + PANEL_H, 0xFF4B4F57);
		graphics.fill(x0, y0, x0 + 1, y0 + PANEL_H, 0xFF4B4F57);
		graphics.fill(x0 + PANEL_W - 1, y0, x0 + PANEL_W, y0 + PANEL_H, 0xFF4B4F57);
		graphics.centeredText(this.font, this.title, this.width / 2, y0 + 8, 0xFFE8E8E0);
		graphics.fill(x0 + 8, y0 + 24, x0 + PANEL_W - 8, y0 + 25, 0xFF3A3F47);

		int listX = x0 + 14;
		int listY = y0 + 34;
		for (int i = 0; i < this.scps.size() && i < 10; i++) {
			ScpDefinition definition = this.scps.get(i);
			boolean known = this.recorded.contains(definition.id().getPath());
			int rowY = listY + i * ROW_H;
			if (i == this.selected) {
				graphics.fill(listX - 4, rowY - 2, listX + 164, rowY + ROW_H - 4, 0xFF343941);
			}
			Component label = known ? ScpRegistry.displayName(definition.id().getPath())
					: Component.translatable("screen.scpmod.unknown");
			graphics.text(this.font, label, listX, rowY, known ? 0xFFE8E8E0 : 0xFF6A6A6A);
			if (known) {
				graphics.text(this.font, "[" + definition.classification() + "]", listX + 120, rowY, 0xFF9FB4C8);
			}
		}

		if (this.selected >= 0 && this.selected < this.scps.size()) {
			ScpDefinition definition = this.scps.get(this.selected);
			boolean known = this.recorded.contains(definition.id().getPath());
			int detailX = x0 + 196;
			int detailY = y0 + 36;
			graphics.text(this.font, known ? ScpRegistry.displayName(definition.id().getPath())
					: Component.translatable("screen.scpmod.unknown"), detailX, detailY, 0xFFFFFFFF);
			if (known) {
				graphics.text(this.font, Component.literal("等级: " + definition.classification()),
						detailX, detailY + 16, 0xFF9FB4C8);
				graphics.textWithWordWrap(this.font, Component.translatable(definition.descriptionKey()),
						detailX, detailY + 34, PANEL_W - 210, 0xFFD0D0C8);
			} else {
				graphics.textWithWordWrap(this.font, Component.translatable("screen.scpmod.unknown_hint"),
						detailX, detailY + 16, PANEL_W - 210, 0xFF888888);
			}
		}
		graphics.centeredText(this.font, Component.translatable("screen.scpmod.close_hint"),
				this.width / 2, y0 + PANEL_H - 14, 0xFF6A6A6A);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		if (event.button() == 0) {
			int listX = panelX() + 14;
			int listY = panelY() + 34;
			for (int i = 0; i < this.scps.size() && i < 10; i++) {
				int rowY = listY + i * ROW_H;
				if (event.x() >= listX - 4 && event.x() <= listX + 164
						&& event.y() >= rowY - 2 && event.y() <= rowY + ROW_H - 4) {
					this.selected = i;
					return true;
				}
			}
		}
		return super.mouseClicked(event, doubled);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
