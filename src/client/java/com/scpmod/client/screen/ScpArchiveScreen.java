package com.scpmod.client.screen;

import com.scpmod.content.ModEntities;
import com.scpmod.content.ModItems;
import com.scpmod.entity.Scp173Entity;
import com.scpmod.item.ScpDocumentItem;
import com.scpmod.scp.ScpDefinition;
import com.scpmod.scp.ScpRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * SCP 档案：左侧列表（使命宣言 + 各 SCP 同级条目），右侧详情；
 * 使命宣言全文支持平滑滚轮滚动，SCP-173 显示可用鼠标转向的 3D 模型预览。
 */
public class ScpArchiveScreen extends Screen {
	private static final Identifier LOGO = Identifier.fromNamespaceAndPath("scpmod", "scp_logo");
	private static final int LINE_H = 11;
	private static final int ROW_H = 20;

	private final List<ScpDefinition> scps = new ArrayList<>(ScpRegistry.all());
	private final List<String> recorded;
	private int selected; // 0 = 使命宣言，1..N = SCP
	private Scp173Entity previewEntity;
	private List<String> manifestLines = new ArrayList<>();
	private boolean draggingModel;
	private float previewYaw = 180.0F;
	private float previewPitch;
	private double lastDragX;
	private double lastDragY;
	private float scroll;
	private float targetScroll;

	public ScpArchiveScreen(ItemStack document) {
		super(Component.translatable("screen.scpmod.archive"));
		this.recorded = ScpDocumentItem.recorded(document);
	}

	@Override
	protected void init() {
		buildManifestLines();
	}

	private int panelW() {
		return Math.min(640, this.width - 40);
	}

	private int panelH() {
		return Math.min(370, this.height - 40);
	}

	private int panelX() {
		return (this.width - panelW()) / 2;
	}

	private int panelY() {
		return (this.height - panelH()) / 2;
	}

	private int leftW() {
		return Math.max(200, (int) (panelW() * 0.40D));
	}

	private int listX() {
		return panelX() + 10;
	}

	private int listTop() {
		return panelY() + 32;
	}

	private int detailX() {
		return panelX() + leftW() + 22;
	}

	private int detailY() {
		return panelY() + 32;
	}

	private int detailW() {
		return panelW() - leftW() - 34;
	}

	private int detailH() {
		return panelH() - 52;
	}

	private int maxScroll() {
		return Math.max(0, this.manifestLines.size() * LINE_H - (detailH() - 24));
	}

	private void buildManifestLines() {
		this.manifestLines = new ArrayList<>();
		String raw = Component.translatable("screen.scpmod.motto.full").getString();
		int maxW = detailW() - 10;
		for (String paragraph : raw.split("\n", -1)) {
			if (paragraph.isEmpty()) {
				this.manifestLines.add("");
				continue;
			}
			StringBuilder current = new StringBuilder();
			for (char c : paragraph.toCharArray()) {
				if (this.font.width(current.toString() + c) > maxW && current.length() > 0) {
					this.manifestLines.add(current.toString());
					current.setLength(0);
				}
				current.append(c);
			}
			if (current.length() > 0) {
				this.manifestLines.add(current.toString());
			}
		}
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

		// 标题：SCP [原图 logo] 档案
		String left = "SCP";
		String right = "档案";
		int logoW = 18;
		int logoH = 17;
		int titleW = this.font.width(left) + 5 + logoW + 5 + this.font.width(right);
		int tx = this.width / 2 - titleW / 2;
		int ty = y0 + 6;
		graphics.text(this.font, left, tx, ty + 5, 0xFFE8E8E0);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LOGO, tx + this.font.width(left) + 5, ty, logoW, logoH);
		graphics.text(this.font, right, tx + this.font.width(left) + 5 + logoW + 5, ty + 5, 0xFFE8E8E0);
		graphics.fill(x0 + 8, y0 + 28, x0 + w - 8, y0 + 29, 0xFF3A3F47);

		// 左侧列表：使命宣言 + SCP
		int rowY = listTop();
		for (int i = 0; i <= this.scps.size(); i++) {
			boolean isManifest = i == 0;
			String label;
			boolean known;
			if (isManifest) {
				label = Component.translatable("screen.scpmod.manifest.title").getString();
				known = true;
			} else {
				ScpDefinition definition = this.scps.get(i - 1);
				label = ScpRegistry.displayName(definition.id().getPath()).getString();
				known = this.recorded.contains(definition.id().getPath());
			}
			if (i == this.selected) {
				graphics.fill(listX() - 4, rowY - 2, listX() + leftW() - 14, rowY + ROW_H - 4, 0xFF343941);
			}
			graphics.text(this.font, Component.literal(label), listX(), rowY, known ? 0xFFE8E8E0 : 0xFF7A7A7A);
			if (!known) {
				graphics.text(this.font, Component.translatable("screen.scpmod.not_recorded"),
						listX() + leftW() - 78, rowY, 0xFF5E5E5E);
			}
			rowY += ROW_H;
		}

		// 右侧详情
		int detailX = detailX();
		int detailY = detailY();
		if (this.selected == 0) {
			// 平滑滚动（每帧逼近目标）
			this.scroll += (this.targetScroll - this.scroll) * 0.35F;
			if (Math.abs(this.targetScroll - this.scroll) < 0.5F) {
				this.scroll = this.targetScroll;
			}
			graphics.text(this.font, Component.translatable("screen.scpmod.manifest.title"),
					detailX, detailY, 0xFFFFFFFF);
			int top = detailY + 18;
			int viewH = detailH() - 24;
			graphics.enableScissor(detailX, top, detailX + detailW(), top + viewH);
			int lineY = top - (int) this.scroll;
			for (String line : this.manifestLines) {
				if (lineY + LINE_H >= top && lineY <= top + viewH) {
					graphics.text(this.font, Component.literal(line), detailX, lineY, 0xFFD0D0C8);
				}
				lineY += LINE_H;
			}
			graphics.disableScissor();
			// 滚动条
			int barX = detailX + detailW() + 4;
			graphics.fill(barX, top, barX + 3, top + viewH, 0xFF2A2E34);
			int barH = Math.max(16, (int) (viewH * (viewH / (double) Math.max(viewH, this.manifestLines.size() * LINE_H))));
			int barY = top + (int) ((viewH - barH) * (this.scroll / (double) Math.max(1, maxScroll())));
			graphics.fill(barX, barY, barX + 3, barY + barH, 0xFF6A7078);
		} else if (this.selected <= this.scps.size()) {
			ScpDefinition definition = this.scps.get(this.selected - 1);
			boolean known = this.recorded.contains(definition.id().getPath());
			graphics.text(this.font, ScpRegistry.displayName(definition.id().getPath()),
					detailX, detailY, 0xFFFFFFFF);
			if (known) {
				graphics.text(this.font, Component.literal("等级: " + definition.classification()),
						detailX, detailY + 16, 0xFF9FB4C8);
				int textX = detailX;
				int textY = detailY + 34;
				if ("scp_173".equals(definition.id().getPath())) {
					int modelX = detailX;
					int modelY = detailY + 34;
					int modelW = 92;
					int modelH = 118;
					try {
						float fakeMouseX = (float) (Math.sin(Math.toRadians(this.previewYaw)) * 40.0D);
						float fakeMouseY = (float) (-Math.sin(Math.toRadians(this.previewPitch)) * 40.0D);
						InventoryScreen.extractEntityInInventoryFollowsMouse(graphics,
								modelX, modelY, modelX + modelW, modelY + modelH, 32, 0.0F,
								fakeMouseX, fakeMouseY, preview173());
					} catch (Throwable throwable) {
						graphics.item(new ItemStack(ModItems.SCP_173_SPAWNER), modelX + 30, modelY + 40);
					}
					textX = detailX + modelW + 10;
				} else {
					graphics.item(preview(definition.id().getPath()), detailX, detailY + 40);
					textX = detailX + 26;
					textY = detailY + 36;
				}
				int textW = panelX() + panelW() - textX - 12;
				graphics.textWithWordWrap(this.font, Component.translatable(definition.descriptionKey()),
						textX, textY, textW, 0xFFD0D0C8);
			} else {
				graphics.textWithWordWrap(this.font, Component.translatable("screen.scpmod.unknown_hint"),
						detailX, detailY + 22, detailW(), 0xFF888888);
			}
		}
		graphics.centeredText(this.font, Component.translatable("screen.scpmod.close_hint"),
				this.width / 2, y0 + h - 14, 0xFF6A6A6A);
	}

	private Scp173Entity preview173() {
		if (this.previewEntity == null) {
			this.previewEntity = new Scp173Entity(ModEntities.SCP_173, Minecraft.getInstance().level);
			this.previewEntity.setYRot(180.0F);
			this.previewEntity.yBodyRot = 180.0F;
			this.previewEntity.setYHeadRot(180.0F);
		}
		return this.previewEntity;
	}

	private ItemStack preview(String path) {
		return switch (path) {
			case "scp_500" -> new ItemStack(ModItems.SCP_500);
			case "scp_005" -> new ItemStack(ModItems.SCP_005);
			default -> new ItemStack(ModItems.SCP_DOCUMENT);
		};
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (this.selected == 0) {
			if (mouseX >= detailX() && mouseX <= detailX() + detailW()
					&& mouseY >= detailY() && mouseY <= detailY() + detailH()) {
				this.targetScroll = Math.max(0, Math.min(maxScroll(), this.targetScroll - (float) scrollY * 22.0F));
				return true;
			}
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		if (event.button() == 0) {
			int rowY = listTop();
			for (int i = 0; i <= this.scps.size(); i++) {
				if (event.x() >= listX() - 4 && event.x() <= listX() + leftW() - 14
						&& event.y() >= rowY - 2 && event.y() <= rowY + ROW_H - 4) {
					this.selected = i;
					this.scroll = 0.0F;
					this.targetScroll = 0.0F;
					return true;
				}
				rowY += ROW_H;
			}
		}
		if (event.button() == 0 && this.selected > 0 && this.selected <= this.scps.size()
				&& "scp_173".equals(this.scps.get(this.selected - 1).id().getPath())) {
			int modelX = detailX();
			int modelY = detailY() + 34;
			if (event.x() >= modelX && event.x() <= modelX + 92
					&& event.y() >= modelY && event.y() <= modelY + 118) {
				this.draggingModel = true;
				this.lastDragX = event.x();
				this.lastDragY = event.y();
				return true;
			}
		}
		return super.mouseClicked(event, doubled);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (this.draggingModel) {
			this.previewYaw += (float) (event.x() - this.lastDragX) * 0.8F;
			this.previewPitch = Math.max(-60.0F, Math.min(60.0F,
					this.previewPitch + (float) (event.y() - this.lastDragY) * 0.8F));
			this.lastDragX = event.x();
			this.lastDragY = event.y();
			return true;
		}
		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		this.draggingModel = false;
		return super.mouseReleased(event);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
