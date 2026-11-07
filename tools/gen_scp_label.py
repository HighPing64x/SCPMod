#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量生成 SCP 收容室标牌（风格统一的 512x512 PNG）。
用法:
    python gen_scp_label.py labels.json 输出目录
labels.json 示例:
[
  {"number": "SCP-173", "class": "EUCLID", "note": "有意识且暴力", "photo": null},
  {"number": "SCP-096", "class": "EUCLID", "note": "目视其面部即触发", "photo": "096.jpg"}
]
可用 photo 传入本地图片路径，自动裁切为正方形放进右下角。
"""
import json
import os
import sys
from PIL import Image, ImageDraw, ImageFont

W = H = 512
YELLOW = (255, 222, 23)
BLACK = (20, 20, 20)
WHITE = (236, 236, 236)
GREY = (150, 150, 150)
RED = (200, 40, 40)

FONT_CANDIDATES = [
    r"C:\Windows\Fonts\msyhbd.ttc",
    r"C:\Windows\Fonts\msyh.ttc",
    r"C:\Windows\Fonts\simhei.ttf",
    r"C:\Windows\Fonts\arialbd.ttf",
]

def font(size: int):
    for path in FONT_CANDIDATES:
        if os.path.exists(path):
            try:
                return ImageFont.truetype(path, size)
            except Exception:
                continue
    return ImageFont.load_default()

def hazard_stripes(draw, box, stripe=14):
    x0, y0, x1, y1 = box
    draw.rectangle(box, fill=YELLOW)
    draw.polygon([(x0, y1), (x0, y1 - stripe), (x1 - stripe, y0), (x1, y0)][:4], fill=BLACK) if False else None
    # 45 度黑黄斜条
    step = stripe
    for i in range(-H, W + H, step * 2):
        draw.polygon([(i, y0), (i + stripe, y0), (i + stripe - (y1 - y0), y1), (i - (y1 - y0), y1)], fill=BLACK)

def draw_header(img):
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, W, 92], fill=YELLOW)
    f_big = font(58)
    d.text((30, 2), "SCP", font=f_big, fill=BLACK)
    f_small = font(21)
    d.text((33, 62), "控制 · 收容 · 保护", font=f_small, fill=BLACK)
    # 右侧简易徽标：圆 + 三箭头缺口
    cx, cy, r = 448, 46, 34
    d.ellipse([cx - r, cy - r, cx + r, cy + r], outline=BLACK, width=6)
    for ang in range(0, 360, 120):
        pass
    d.ellipse([cx - 8, cy - 8, cx + 8, cy + 8], fill=BLACK)

def draw_body(img, item):
    d = ImageDraw.Draw(img)
    # 背景：做旧灰白
    d.rectangle([0, 92, W, H - 96], fill=WHITE)
    for i in range(0, W, 4):
        shade = 220 + (i * 7) % 26
        d.line([(i, 92), (i, H - 96)], fill=(shade, shade, shade))
    # 噪点
    import random
    random.seed(hash(item.get("number", "scp")) & 0xFFFF)
    for _ in range(2500):
        x = random.randint(0, W - 1); y = random.randint(92, H - 97)
        v = random.randint(120, 235)
        d.point((x, y), fill=(v, v, v))
    # 编号
    d.text((28, 108), item.get("number", "SCP-XXX"), font=font(56), fill=BLACK)
    # 等级
    d.text((30, 176), "项目等级：" + item.get("class", "EUCLID"), font=font(34), fill=BLACK)
    # 左下警示三角
    tri = [(96, 452), (26, 330), (166, 330)]
    d.polygon(tri, fill=YELLOW, outline=BLACK)
    d.line(tri + [tri[0]], fill=BLACK, width=6)
    # 三角里的“脑+眼”简笔示意
    d.ellipse([76, 360, 116, 396], outline=BLACK, width=4)
    d.ellipse([88, 370, 104, 386], fill=BLACK)
    d.line([(96, 396), (96, 420)], fill=BLACK, width=4)
    # 说明文字
    d.text((28, 458), item.get("note", ""), font=font(24), fill=BLACK)
    # 右下照片或占位
    box = [200, 320, 492, 470]
    photo = item.get("photo")
    if photo and os.path.exists(photo):
        p = Image.open(photo).convert("RGB")
        s = min(p.size)
        p = p.crop(((p.width - s) // 2, (p.height - s) // 2, (p.width + s) // 2, (p.height + s) // 2)).resize((box[2] - box[0], box[3] - box[1]), Image.LANCZOS)
        img.paste(p, (box[0], box[1]))
    else:
        d.rectangle(box, fill=(190, 190, 190), outline=BLACK, width=3)
        d.text((box[0] + 40, box[1] + 50), "无照片", font=font(26), fill=(90, 90, 90))

def draw_footer(img):
    d = ImageDraw.Draw(img)
    d.rectangle([0, H - 96, W, H], fill=(0, 0, 0))
    hazard_stripes(d, [0, H - 96, W, H - 76], stripe=12)
    d.rectangle([0, H - 76, W, H], fill=BLACK)
    d.text((18, H - 62), "未经授权禁止进入  /  AUTH. PERSONNEL ONLY", font=font(17), fill=YELLOW)

def make_label(item, out_dir):
    img = Image.new("RGBA", (W, H), WHITE)
    draw_header(img)
    draw_body(img, item)
    draw_footer(img)
    name = item.get("number", "SCP-XXX").replace(" ", "_") + ".png"
    path = os.path.join(out_dir, name)
    img.convert("RGB").save(path, optimize=True)
    return path

def main():
    if len(sys.argv) < 3:
        print(__doc__)
        return 2
    with open(sys.argv[1], "r", encoding="utf-8") as f:
        items = json.load(f)
    out_dir = sys.argv[2]
    os.makedirs(out_dir, exist_ok=True)
    for item in items:
        print("生成:", make_label(item, out_dir))
    return 0

if __name__ == "__main__":
    sys.exit(main())
