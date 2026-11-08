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
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import org.joml.Quaternionf;
import org.joml.Vector3f;

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
	private float scroll;
	private float modelAngle;
	private EntityRenderState previewState;
	private int lastWidth;
	private int lastHeight;
	private long lastFrameNanos;
	private List<String> descLines = new ArrayList<>();
	private float descScroll;
	private float descTargetScroll;
	private int descFor = -1;
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
		return panelY() + 56;
	}

	private int detailX() {
		return panelX() + leftW() + 22;
	}

	private int detailY() {
		return panelY() + 56;
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
		if (this.lastWidth != this.width || this.lastHeight != this.height) {
			this.lastWidth = this.width;
			this.lastHeight = this.height;
			this.buildManifestLines();
			this.descFor = -1;
		}
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

		// 标题：SCP [原图 logo] 档案（1.5 倍字号）
		String left = "SCP";
		String right = "档案";
		float titleScale = 1.5F;
		int leftW = (int) (this.font.width(left) * titleScale);
		int rightW = (int) (this.font.width(right) * titleScale);
		int logoW = 27;
		int logoH = 26;
		int titleW = leftW + 6 + logoW + 6 + rightW;
		int tx = this.width / 2 - titleW / 2;
		int ty = y0 + 10;
		this.bigText(graphics, Component.literal(left), tx, ty + 6, titleScale, 0xFFE8E8E0);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LOGO, tx + leftW + 6, ty, logoW, logoH);
		this.bigText(graphics, Component.literal(right), tx + leftW + 6 + logoW + 6, ty + 6, titleScale, 0xFFE8E8E0);
		graphics.fill(x0 + 8, y0 + 46, x0 + w - 8, y0 + 47, 0xFF3A3F47);

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
			this.bigText(graphics, Component.translatable("screen.scpmod.manifest.title"),
					detailX, detailY, 1.5F, 0xFFFFFFFF);
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
			this.bigText(graphics, ScpRegistry.displayName(definition.id().getPath()),
					detailX, detailY, 1.5F, 0xFFFFFFFF);
			if (known) {
				graphics.text(this.font, Component.literal("等级: " + definition.classification()),
						detailX, detailY + 16, 0xFF9FB4C8);
				int textX = detailX;
				int textY = detailY + 34;
				if ("scp_173".equals(definition.id().getPath())) {
					int modelX = detailX();
					int modelY = detailY() + 34;
					int modelW = 92;
					int modelH = 118;
					try {
						if (this.previewState == null) {
							EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
							this.previewState = dispatcher.extractEntity(preview173(), 1.0F);
							if (this.previewState instanceof LivingEntityRenderState living) {
								living.bodyRot = 0.0F;
								living.yRot = 0.0F;
								living.xRot = 0.0F;
								living.scale = 1.0F;
							}
						}
						EntityRenderState state = this.previewState;
						// 俯视 + 顺时针自动旋转：固定每帧小步进（约 60fps 时 24°/秒），
						// 不依赖帧时间，避免帧率波动导致的旋转步进抖动。
						this.modelAngle = (this.modelAngle + 0.4F) % 360.0F;
						float angle = this.modelAngle;
						Quaternionf base = new Quaternionf()
								.rotateY((float) Math.toRadians(angle))
								.rotateZ((float) Math.PI);
						Quaternionf tilt = new Quaternionf().rotateX((float) Math.toRadians(-15.0F));
						Vector3f pivot = new Vector3f(0.0F, state.boundingBoxHeight / 2.0F, 0.0F);
						graphics.entity(state, 32.0F, pivot, base, tilt,
								modelX, modelY, modelX + modelW, modelY + modelH);
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
				this.renderDescription(graphics, definition, textX, textY, textW);
			} else {
				graphics.textWithWordWrap(this.font, Component.translatable("screen.scpmod.unknown_hint"),
						detailX, detailY + 22, detailW(), 0xFF888888);
			}
		}
		graphics.centeredText(this.font, Component.translatable("screen.scpmod.close_hint"),
				this.width / 2, y0 + h + 6, 0xFF6A6A6A);
	}

	/** 放大绘制文字（GUI 栏目标题用）。 */
	private void bigText(GuiGraphicsExtractor graphics, Component text, int x, int y, float scale, int color) {
		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);
		graphics.pose().scale(scale, scale);
		graphics.text(this.font, text, 0, 0, color);
		graphics.pose().popMatrix();
	}

	private void renderDescription(GuiGraphicsExtractor graphics, ScpDefinition definition,
			int textX, int textY, int textW) {
		if (this.descFor != this.selected) {
			this.rebuildDescLines(definition, textW);
		}
		int bottom = panelY() + panelH() - 30;
		int viewH = bottom - textY;
		if (viewH <= 0) {
			return;
		}
		this.descScroll += (this.descTargetScroll - this.descScroll) * 0.35F;
		if (Math.abs(this.descTargetScroll - this.descScroll) < 0.5F) {
			this.descScroll = this.descTargetScroll;
		}
		graphics.enableScissor(textX, textY, textX + textW, textY + viewH);
		int lineY = textY - (int) this.descScroll;
		for (String line : this.descLines) {
			if (lineY + LINE_H >= textY && lineY <= textY + viewH) {
				graphics.text(this.font, Component.literal(line), textX, lineY, 0xFFD0D0C8);
			}
			lineY += LINE_H;
		}
		graphics.disableScissor();
		int totalH = this.descLines.size() * LINE_H;
		if (totalH > viewH) {
			int barX = textX + textW + 4;
			graphics.fill(barX, textY, barX + 3, textY + viewH, 0xFF2A2E34);
			int barH = Math.max(16, (int) (viewH * (viewH / (double) totalH)));
			int maxScroll = Math.max(1, totalH - viewH);
			int barY = textY + (int) ((viewH - barH) * (this.descScroll / (double) maxScroll));
			graphics.fill(barX, barY, barX + 3, barY + barH, 0xFF6A7078);
		}
	}

	private void rebuildDescLines(ScpDefinition definition, int textW) {
		this.descLines = new ArrayList<>();
		String raw = Component.translatable(definition.descriptionKey()).getString();
		int maxW = Math.max(40, textW);
		for (String paragraph : raw.split("\n", -1)) {
			if (paragraph.isEmpty()) {
				this.descLines.add("");
				continue;
			}
			StringBuilder current = new StringBuilder();
			for (char c : paragraph.toCharArray()) {
				if (this.font.width(current.toString() + c) > maxW && current.length() > 0) {
					this.descLines.add(current.toString());
					current.setLength(0);
				}
				current.append(c);
			}
			if (current.length() > 0) {
				this.descLines.add(current.toString());
			}
		}
		this.descFor = this.selected;
		this.descScroll = 0.0F;
		this.descTargetScroll = 0.0F;
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
			case "scp_006" -> new ItemStack(ModItems.SCP_006_BUCKET);
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
		if (this.selected > 0 && this.selected <= this.scps.size()) {
			if (mouseX >= detailX() && mouseX <= detailX() + detailW()
					&& mouseY >= detailY() && mouseY <= detailY() + detailH()) {
				int viewH = panelY() + panelH() - 30 - (detailY() + 36);
				int maxScroll = Math.max(0, this.descLines.size() * LINE_H - viewH);
				this.descTargetScroll = Math.max(0.0F,
						Math.min(maxScroll, this.descTargetScroll - (float) scrollY * 22.0F));
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
		return super.mouseClicked(event, doubled);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
