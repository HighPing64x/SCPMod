#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""SCPMod 收容室标牌生成器。

用法：
    python gen_labels.py labels.json 输出目录 [--theme A|B|C|D]
    python gen_labels.py labels.json 输出目录 --size 768
    python gen_labels.py labels.json 输出目录 --width 640 --height 512

脚本只依赖 Pillow，不下载网络素材。输入中的 photo 可以是相对于
labels.json 的本地图片路径；缺图会生成带有压角、纹理和“图像受限”字样的占位框。
"""
from __future__ import annotations

import argparse
import hashlib
import json
import math
import os
import random
import re
import sys
from pathlib import Path
from typing import Any, Dict, Iterable, List, Optional, Sequence, Tuple

from PIL import Image, ImageDraw, ImageEnhance, ImageFilter, ImageFont, ImageOps

RGB = Tuple[int, int, int]
Box = Tuple[float, float, float, float]

# ---------------------------------------------------------------------------
# 主题配置：所有可扩展的色彩和文案放在这里，新增主题只需增加一个 renderer。
# ---------------------------------------------------------------------------
THEME_NAMES = ("A", "B", "C", "D")
CLASS_COLORS: Dict[str, RGB] = {
    "SAFE": (45, 142, 103),
    "EUCLID": (224, 150, 36),
    "KETER": (202, 54, 45),
    "THAUMIEL": (117, 73, 182),
    "NEUTRALIZED": (73, 111, 143),
    "APOLLYON": (169, 37, 91),
    "ESOTERIC": (38, 133, 143),
    "PENDING": (119, 125, 130),
}

DEFAULT_ITEM = {
    "number": "SCP-XXX",
    "class": "EUCLID",
    "note": "行为特征待确认",
    "photo": None,
    "theme": "A",
    "warning_cn": "未经授权禁止进入",
    "warning_en": "UNAUTHORIZED ENTRY PROHIBITED",
    "site": "SITE-19",
    "record": "CONTAINMENT RECORD",
    "timestamp": "SITE-19 // 03:17:42",
}

FONT_CANDIDATES = {
    "bold": [
        r"C:\Windows\Fonts\msyhbd.ttc",
        r"C:\Windows\Fonts\simhei.ttf",
        r"C:\Windows\Fonts\Dengb.ttf",
        r"C:\Windows\Fonts\arialbd.ttf",
        "/usr/share/fonts/opentype/noto/NotoSansCJK-Bold.ttc",
        "/usr/share/fonts/opentype/noto/NotoSansCJK-Bold.otf",
        "/usr/share/fonts/truetype/noto/NotoSansCJK-Bold.ttc",
        "/usr/share/fonts/truetype/noto/NotoSansCJK-Bold.otf",
    ],
    "regular": [
        r"C:\Windows\Fonts\msyh.ttc",
        r"C:\Windows\Fonts\simsun.ttc",
        r"C:\Windows\Fonts\simfang.ttf",
        r"C:\Windows\Fonts\simhei.ttf",
        r"C:\Windows\Fonts\arial.ttf",
        "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
        "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.otf",
        "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc",
        "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.otf",
    ],
}
_FONT_PATH_CACHE: Dict[bool, Optional[str]] = {}
_FONT_CACHE: Dict[Tuple[bool, int], ImageFont.FreeTypeFont] = {}


def _find_font(bold: bool) -> Optional[str]:
    if bold in _FONT_PATH_CACHE:
        return _FONT_PATH_CACHE[bold]
    candidates = FONT_CANDIDATES["bold" if bold else "regular"]
    for path in candidates:
        if os.path.isfile(path):
            _FONT_PATH_CACHE[bold] = path
            return path
    # 不同 Linux 发行版路径不同；只扫常见字体目录，不触碰网络。
    roots = [
        "/usr/share/fonts",
        "/usr/local/share/fonts",
        os.path.expanduser("~/.fonts"),
        r"C:\Windows\Fonts",
    ]
    keywords = ("msyh", "simhei", "simsun", "noto", "sourcehans", "deng")
    for root in roots:
        if not os.path.isdir(root):
            continue
        try:
            for current, _dirs, files in os.walk(root):
                for name in files:
                    low = name.lower()
                    if any(k in low for k in keywords) and low.endswith((".ttf", ".ttc", ".otf")):
                        if bold and not any(k in low for k in ("bold", "bd", "hei", "heavy")):
                            continue
                        _FONT_PATH_CACHE[bold] = os.path.join(current, name)
                        return _FONT_PATH_CACHE[bold]
        except OSError:
            continue
    _FONT_PATH_CACHE[bold] = None
    return None


def _font(size: int, bold: bool = False) -> ImageFont.ImageFont:
    size = max(4, int(size))
    key = (bold, size)
    if key in _FONT_CACHE:
        return _FONT_CACHE[key]
    path = _find_font(bold) or _find_font(False)
    try:
        value: ImageFont.ImageFont = ImageFont.truetype(path, size) if path else ImageFont.load_default()
    except (OSError, TypeError):
        value = ImageFont.load_default()
    _FONT_CACHE[key] = value  # type: ignore[assignment]
    return value


def _rgb(value: Sequence[int]) -> RGB:
    return (int(value[0]), int(value[1]), int(value[2]))


def _clamp(v: int) -> int:
    return max(0, min(255, int(v)))


def _mix(a: RGB, b: RGB, t: float) -> RGB:
    return tuple(_clamp(round(x + (y - x) * t)) for x, y in zip(a, b))  # type: ignore[return-value]


def _darken(c: RGB, amount: int) -> RGB:
    return tuple(_clamp(v - amount) for v in c)  # type: ignore[return-value]


def _lighten(c: RGB, amount: int) -> RGB:
    return tuple(_clamp(v + amount) for v in c)  # type: ignore[return-value]


def _stable_seed(item: Dict[str, Any], theme: str, base_seed: int) -> int:
    raw = f"{base_seed}|{theme}|{item.get('number', '')}|{item.get('class', '')}"
    return int(hashlib.sha256(raw.encode("utf-8")).hexdigest()[:16], 16)


class Art:
    """以 512×512 设计坐标绘图，并用 3 倍超采样保证缩放后的字和线条清晰。"""

    def __init__(self, width: int, height: int, supersample: int = 3) -> None:
        self.out_width = max(64, int(width))
        self.out_height = max(64, int(height))
        self.ss = max(1, int(supersample))
        self.width = self.out_width * self.ss
        self.height = self.out_height * self.ss
        self.sx = self.out_width / 512.0
        self.sy = self.out_height / 512.0
        self.image = Image.new("RGB", (self.width, self.height), (220, 220, 220))
        self.d = ImageDraw.Draw(self.image)

    def xy(self, x: float, y: float) -> Tuple[int, int]:
        return (round(x * self.sx * self.ss), round(y * self.sy * self.ss))

    def box(self, box: Box) -> Tuple[int, int, int, int]:
        x0, y0, x1, y1 = box
        return (*self.xy(x0, y0), *self.xy(x1, y1))

    def size(self, w: float, h: float) -> Tuple[int, int]:
        return (max(1, round(w * self.sx * self.ss)), max(1, round(h * self.sy * self.ss)))

    def font(self, size: float, bold: bool = False) -> ImageFont.ImageFont:
        return _font(round(size * min(self.sx, self.sy) * self.ss), bold)

    def refresh(self) -> None:
        self.d = ImageDraw.Draw(self.image)

    def resize_output(self) -> Image.Image:
        resampling = getattr(Image, "Resampling", Image)
        return self.image.resize((self.out_width, self.out_height), resampling.LANCZOS)


def _fill_texture(art: Art, base: RGB, rng: random.Random, mode: str = "paper") -> None:
    """生成纸张、涂层或金属纹理；不使用任何外部素材。"""
    noise_amount = {"paper": 25, "metal": 16, "terminal": 12}.get(mode, 20)
    noise = Image.effect_noise((art.width, art.height), noise_amount).convert("L")
    if mode == "metal":
        # 横向低频拉丝：多条半透明线叠加在细噪声之上。
        noise = noise.filter(ImageFilter.GaussianBlur(radius=max(0.5, art.ss * 0.45)))
    lo = _darken(base, 22 if mode != "terminal" else 16)
    hi = _lighten(base, 23 if mode != "terminal" else 13)
    grain = ImageOps.colorize(noise, black=lo, white=hi).convert("RGB")
    art.image = grain
    art.refresh()
    d = art.d
    if mode == "metal":
        for y in range(5, 512, 4):
            alpha = rng.randint(10, 34)
            color = _mix(base, (255, 255, 255), alpha / 255.0)
            d.line([art.xy(0, y), art.xy(512, y + rng.choice([-0.3, 0, 0.3]))], fill=color, width=max(1, art.ss))
    elif mode == "paper":
        # 纸纤维与少量老化细线，让大面积底色不显得像纯色几何块。
        for _ in range(130):
            x = rng.uniform(0, 512)
            y = rng.uniform(0, 512)
            length = rng.uniform(5, 34)
            color = _mix(base, (110, 104, 95), rng.uniform(0.08, 0.24))
            d.line([art.xy(x, y), art.xy(x + length, y + rng.uniform(-1, 1))], fill=color, width=max(1, art.ss // 2))
    else:
        for y in range(0, 512, 8):
            d.line([art.xy(0, y), art.xy(512, y)], fill=_mix(base, (255, 255, 255), 0.05), width=max(1, art.ss // 2))


def _add_wear(art: Art, rng: random.Random, dark: RGB = (35, 35, 32), light: RGB = (245, 245, 230), amount: int = 130) -> None:
    d = art.d
    for _ in range(amount):
        x = rng.uniform(5, 507)
        y = rng.uniform(5, 507)
        if rng.random() < 0.72:
            length = rng.uniform(1, 15)
            color = dark if rng.random() < 0.58 else light
            d.line([art.xy(x, y), art.xy(x + length, y + rng.uniform(-1.4, 1.4))], fill=color, width=max(1, round(rng.uniform(0.4, 1.5) * art.ss)))
        else:
            r = rng.uniform(0.4, 2.4)
            d.ellipse(art.box((x - r, y - r, x + r, y + r)), fill=dark if rng.random() < 0.5 else light)
    # 边角磨损：内外两层不规则短线。
    for _ in range(32):
        side = rng.choice((0, 1, 2, 3))
        p = rng.uniform(8, 504)
        if side == 0:
            box = (p, rng.uniform(3, 13), p + rng.uniform(3, 14), rng.uniform(3, 13))
        elif side == 1:
            box = (p, rng.uniform(499, 509), p + rng.uniform(3, 14), rng.uniform(499, 509))
        elif side == 2:
            box = (rng.uniform(3, 13), p, rng.uniform(3, 13), p + rng.uniform(3, 14))
        else:
            box = (rng.uniform(499, 509), p, rng.uniform(499, 509), p + rng.uniform(3, 14))
        d.line([art.xy(box[0], box[1]), art.xy(box[2], box[3])], fill=dark, width=max(1, art.ss))


def _vignette(art: Art, strength: int = 38) -> None:
    overlay = Image.new("RGBA", art.image.size, (0, 0, 0, 0))
    od = ImageDraw.Draw(overlay)
    for i in range(22, 0, -1):
        alpha = round(strength * (23 - i) / 23)
        inset = i * art.ss
        od.rectangle((inset, inset, art.width - inset, art.height - inset), outline=(0, 0, 0, alpha), width=max(1, art.ss))
    art.image = Image.alpha_composite(art.image.convert("RGBA"), overlay).convert("RGB")
    art.refresh()


def _panel(art: Art, box: Box, fill: RGB, outline: Optional[RGB] = None, radius: float = 5, shadow: bool = True, width: int = 2) -> None:
    x0, y0, x1, y1 = box
    d = art.d
    if shadow:
        shadow_overlay = Image.new("RGBA", art.image.size, (0, 0, 0, 0))
        sd = ImageDraw.Draw(shadow_overlay)
        sb = art.box((x0 + 3, y0 + 4, x1 + 3, y1 + 4))
        sd.rounded_rectangle(sb, radius=round(radius * art.ss), fill=(0, 0, 0, 70))
        art.image = Image.alpha_composite(art.image.convert("RGBA"), shadow_overlay).convert("RGB")
        art.refresh()
        d = art.d
    b = art.box(box)
    d.rounded_rectangle(b, radius=round(radius * art.ss), fill=fill, outline=outline, width=max(1, round(width * art.ss)) if outline else 1)


def _text_size(art: Art, text: str, font: ImageFont.ImageFont, spacing: int = 3) -> Tuple[int, int]:
    return art.d.multiline_textbbox((0, 0), text, font=font, spacing=round(spacing * art.ss), stroke_width=0)[2:]


def _wrap_text(art: Art, text: str, font: ImageFont.ImageFont, max_width: float) -> str:
    """中英文混排按字符折行；显式换行优先，避免中文变成溢出或压字。"""
    if not text:
        return ""
    limit = max_width * art.sx * art.ss
    lines: List[str] = []
    for paragraph in str(text).replace("\r", "").split("\n"):
        current = ""
        for char in paragraph:
            candidate = current + char
            if current and art.d.textlength(candidate, font=font) > limit:
                lines.append(current)
                current = char
            else:
                current = candidate
        lines.append(current)
    return "\n".join(lines)


def _fit_text(art: Art, text: str, max_width: float, max_height: float, size: float, bold: bool = False, min_size: float = 8.0) -> Tuple[ImageFont.ImageFont, str]:
    size = max(size, min_size)
    while size >= min_size:
        f = art.font(size, bold)
        wrapped = _wrap_text(art, str(text), f, max_width)
        bbox = art.d.multiline_textbbox((0, 0), wrapped, font=f, spacing=round(size * 0.13 * art.ss))
        if bbox[2] - bbox[0] <= max_width * art.sx * art.ss and bbox[3] - bbox[1] <= max_height * art.sy * art.ss:
            return f, wrapped
        size -= 1
    f = art.font(min_size, bold)
    return f, _wrap_text(art, str(text), f, max_width)


def _draw_fit(art: Art, box: Box, text: str, size: float, fill: RGB, bold: bool = False, min_size: float = 8.0, align: str = "left", anchor: str = "lt", spacing: int = 3) -> None:
    x0, y0, x1, y1 = box
    f, wrapped = _fit_text(art, text, x1 - x0, y1 - y0, size, bold, min_size)
    bbox = art.d.multiline_textbbox((0, 0), wrapped, font=f, spacing=round(spacing * art.ss))
    tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]
    if align == "center":
        x = x0 + ((x1 - x0) - tw / (art.sx * art.ss)) / 2
    elif align == "right":
        x = x1 - tw / (art.sx * art.ss)
    else:
        x = x0
    y = y0 + ((y1 - y0) - th / (art.sy * art.ss)) / 2
    # Pillow 的 multiline_text 不支持 anchor；这里使用已计算出的左上坐标，保证可控边界。
    art.d.multiline_text(art.xy(x, y), wrapped, font=f, fill=fill, spacing=round(spacing * art.ss), align=align)


def _draw_scp_logo(art: Art, center: Tuple[float, float], radius: float, color: RGB, line: Optional[RGB] = None, width: int = 3) -> None:
    cx, cy = center
    d = art.d
    line = line or color
    d.ellipse(art.box((cx - radius, cy - radius, cx + radius, cy + radius)), outline=line, width=max(1, round(width * art.ss)))
    d.ellipse(art.box((cx - radius * 0.20, cy - radius * 0.20, cx + radius * 0.20, cy + radius * 0.20)), fill=color)
    for angle in (-90, 30, 150):
        a = math.radians(angle)
        a1, a2 = math.radians(angle - 22), math.radians(angle + 22)
        p1 = (cx + math.cos(a1) * radius * 0.88, cy + math.sin(a1) * radius * 0.88)
        p2 = (cx + math.cos(a2) * radius * 0.88, cy + math.sin(a2) * radius * 0.88)
        q = (cx + math.cos(a) * radius * 0.30, cy + math.sin(a) * radius * 0.30)
        d.polygon([art.xy(*p1), art.xy(*p2), art.xy(*q)], fill=color)


def _draw_brand(art: Art, x: float, y: float, color: RGB, sub_color: Optional[RGB] = None, radius: float = 20, dark: bool = False) -> None:
    _draw_scp_logo(art, (x + radius, y + radius), radius, color, color, 2)
    _draw_fit(art, (x + radius * 2 + 9, y + 1, x + radius * 2 + 164, y + 28), "SCP FOUNDATION", 19, color, True, 10)
    _draw_fit(art, (x + radius * 2 + 9, y + 28, x + radius * 2 + 190, y + 53), "控制 · 收容 · 保护", 14, sub_color or color, False, 8)


def _draw_hazard_stripes(art: Art, box: Box, yellow: RGB = (239, 192, 40), dark: RGB = (26, 27, 25), stripe: float = 13) -> None:
    x0, y0, x1, y1 = box
    d = art.d
    d.rectangle(art.box(box), fill=yellow)
    height = y1 - y0
    step = stripe * 2
    i = x0 - height
    while i < x1 + height:
        d.polygon([art.xy(i, y0), art.xy(i + stripe, y0), art.xy(i + stripe - height, y1), art.xy(i - height, y1)], fill=dark)
        i += step


def _draw_triangle(art: Art, center_x: float, top: float, width: float, height: float, fill: RGB, stroke: RGB = (25, 25, 23)) -> None:
    d = art.d
    points = [(center_x, top), (center_x - width / 2, top + height), (center_x + width / 2, top + height)]
    d.polygon([art.xy(*p) for p in points], fill=fill)
    d.line([art.xy(*points[i]) for i in (0, 1, 2, 0)], fill=stroke, width=max(1, round(3 * art.ss)), joint="curve")
    d.ellipse(art.box((center_x - 3, top + height * 0.42, center_x + 3, top + height * 0.58)), fill=stroke)
    d.rectangle(art.box((center_x - 3, top + height * 0.66, center_x + 3, top + height * 0.73)), fill=stroke)


def _draw_screws(art: Art, color: RGB = (50, 52, 51), highlight: RGB = (215, 216, 210), inset: float = 14) -> None:
    d = art.d
    for x, y in ((inset, inset), (512 - inset, inset), (inset, 512 - inset), (512 - inset, 512 - inset)):
        r = 7
        d.ellipse(art.box((x - r, y - r, x + r, y + r)), fill=color, outline=highlight, width=max(1, art.ss))
        d.line([art.xy(x - 3.7, y - 3.7), art.xy(x + 3.7, y + 3.7)], fill=_darken(color, 25), width=max(1, 2 * art.ss))
        d.line([art.xy(x - 3.7, y - 3.7), art.xy(x + 2.3, y - 4.5)], fill=highlight, width=max(1, art.ss))


def _photo_image(path: Optional[str], size: Tuple[int, int]) -> Optional[Image.Image]:
    if not path or not os.path.isfile(path):
        return None
    try:
        with Image.open(path) as source:
            return ImageOps.fit(source.convert("RGB"), size, method=getattr(Image, "Resampling", Image).LANCZOS, centering=(0.5, 0.45))
    except (OSError, ValueError):
        return None


def _draw_photo(art: Art, box: Box, photo_path: Optional[str], rng: random.Random, style: str = "paper", label: str = "图像受限") -> None:
    x0, y0, x1, y1 = box
    d = art.d
    # 框架阴影和内衬，先留出压角空间。
    _panel(art, (x0 - 4, y0 - 4, x1 + 4, y1 + 4), (44, 45, 42) if style in ("terminal", "metal") else (72, 70, 62), None, 2, True)
    inner = (x0 + 7, y0 + 7, x1 - 7, y1 - 7)
    image = _photo_image(photo_path, art.size(x1 - x0 - 14, y1 - y0 - 14))
    if image is not None:
        # 可控的轻微色调，照片不会破坏四种主题的统一气质。
        if style == "terminal":
            image = ImageEnhance.Color(image).enhance(0.25)
            image = ImageEnhance.Contrast(image).enhance(1.3)
            tint = Image.new("RGB", image.size, (30, 110, 80))
            image = Image.blend(image, tint, 0.23)
        elif style == "metal":
            image = ImageEnhance.Contrast(image).enhance(1.15)
            image = ImageEnhance.Color(image).enhance(0.7)
        art.image.paste(image, (art.xy(inner[0], inner[1])))
        art.refresh()
    else:
        psize = art.size(inner[2] - inner[0], inner[3] - inner[1])
        placeholder_base = (30, 42, 39) if style == "terminal" else (99, 101, 96) if style == "metal" else (180, 174, 155)
        placeholder = Image.new("RGB", psize, placeholder_base)
        pd = ImageDraw.Draw(placeholder)
        for i in range(-psize[1], psize[0] + psize[1], max(6, 12 * art.ss)):
            pd.line([(i, psize[1]), (i + psize[1], 0)], fill=_mix(placeholder_base, (230, 230, 205), 0.24), width=max(1, art.ss))
        # 受限图像的靶心/取景框，避免使用刺眼的纯灰块。
        pd.rectangle((psize[0] * 0.14, psize[1] * 0.18, psize[0] * 0.86, psize[1] * 0.82), outline=_mix(placeholder_base, (220, 230, 206), 0.4), width=max(1, art.ss))
        pd.ellipse((psize[0] * 0.38, psize[1] * 0.30, psize[0] * 0.62, psize[1] * 0.70), outline=_mix(placeholder_base, (220, 230, 206), 0.42), width=max(1, art.ss))
        art.image.paste(placeholder, art.xy(inner[0], inner[1]))
        art.refresh()
        _draw_fit(art, (x0 + 10, y0 + (y1 - y0) * 0.42, x1 - 10, y0 + (y1 - y0) * 0.64), label, 18, (235, 230, 199) if style != "paper" else (57, 53, 44), True, 9, "center")
    # 四个压角/固定片，明确照片区边界。
    corner_color = (235, 194, 62) if style == "terminal" else (28, 29, 27)
    for cx, cy, sx, sy in ((x0 + 10, y0 + 10, 1, 1), (x1 - 10, y0 + 10, -1, 1), (x0 + 10, y1 - 10, 1, -1), (x1 - 10, y1 - 10, -1, -1)):
        d.polygon([art.xy(cx, cy), art.xy(cx + sx * 18, cy), art.xy(cx, cy + sy * 6)], fill=corner_color)
    d.rectangle(art.box(box), outline=(220, 211, 177) if style == "paper" else (175, 181, 164), width=max(1, 2 * art.ss))


def _class_name(item: Dict[str, Any]) -> str:
    return str(item.get("class") or DEFAULT_ITEM["class"]).upper().strip()


def _class_color(item: Dict[str, Any]) -> RGB:
    name = _class_name(item)
    return CLASS_COLORS.get(name, (111, 121, 127))


def _warning(item: Dict[str, Any]) -> Tuple[str, str]:
    cn = str(item.get("warning_cn") or DEFAULT_ITEM["warning_cn"])
    en = str(item.get("warning_en") or DEFAULT_ITEM["warning_en"])
    return cn, en


def _draw_text_shadow(art: Art, xy: Tuple[float, float], text: str, f: ImageFont.ImageFont, fill: RGB, shadow: RGB, offset: Tuple[float, float] = (1.5, 1.5), stroke: int = 0) -> None:
    x, y = xy
    art.d.text(art.xy(x + offset[0], y + offset[1]), text, font=f, fill=shadow, stroke_width=max(0, round(stroke * art.ss)))
    art.d.text(art.xy(x, y), text, font=f, fill=fill, stroke_width=max(0, round(stroke * art.ss)))


def _draw_glow_text(art: Art, xy: Tuple[float, float], text: str, f: ImageFont.ImageFont, color: RGB, glow: RGB, stroke: int = 0) -> None:
    layer = Image.new("RGBA", art.image.size, (0, 0, 0, 0))
    ld = ImageDraw.Draw(layer)
    pos = art.xy(*xy)
    ld.text(pos, text, font=f, fill=(*glow, 150), stroke_width=max(0, round(stroke * art.ss)), stroke_fill=(*glow, 100))
    blur = layer.filter(ImageFilter.GaussianBlur(radius=max(1, 3 * art.ss)))
    art.image = Image.alpha_composite(art.image.convert("RGBA"), blur).convert("RGB")
    art.refresh()
    art.d.text(pos, text, font=f, fill=color, stroke_width=max(0, round(stroke * art.ss)), stroke_fill=color)


def render_a(item: Dict[str, Any], width: int, height: int, seed: int) -> Image.Image:
    art = Art(width, height)
    rng = random.Random(seed)
    base = (207, 201, 181)
    _fill_texture(art, base, rng, "paper")
    _add_wear(art, rng, amount=145)
    d = art.d
    black = (25, 26, 24)
    yellow = (238, 190, 35)
    accent = _class_color(item)
    # 工业卡边缘、内阴影、顶部警示带。
    d.rectangle(art.box((7, 7, 505, 505)), outline=black, width=round(5 * art.ss))
    d.rectangle(art.box((16, 16, 496, 496)), outline=_mix(base, black, 0.35), width=max(1, art.ss))
    d.rectangle(art.box((0, 0, 512, 92)), fill=yellow)
    _draw_hazard_stripes(art, (347, 0, 512, 18), yellow=yellow, dark=black, stripe=13)
    _draw_brand(art, 28, 16, black, black, 20)
    _draw_fit(art, (353, 33, 486, 71), "ACCESS // 07", 16, black, True, 10, "right")
    # 编号信息块：占据主视觉，但不压住等级和备注。
    d.rectangle(art.box((25, 108, 487, 220)), fill=_mix(base, (255, 255, 255), 0.25), outline=black, width=round(3 * art.ss))
    d.rectangle(art.box((25, 108, 36, 220)), fill=accent)
    _draw_fit(art, (48, 116, 475, 204), str(item.get("number", DEFAULT_ITEM["number"])), 68, black, True, 22)
    _draw_fit(art, (51, 196, 470, 215), "CONTAINMENT IDENTIFICATION / 收容识别", 11, _darken(black, 0), True, 8)
    # 等级条和中文项目等级。
    _panel(art, (27, 236, 244, 292), accent, _darken(accent, 35), 3, True, 2)
    _draw_fit(art, (39, 241, 232, 259), "项目等级 / CLASS", 12, (250, 245, 225), True, 8)
    _draw_fit(art, (39, 257, 232, 286), _class_name(item), 25, (255, 255, 247), True, 12)
    d.line([art.xy(263, 236), art.xy(263, 444)], fill=_mix(base, black, 0.22), width=max(1, art.ss))
    # 左侧备注 + 警示三角，右侧照片。
    _panel(art, (27, 307, 268, 390), _mix(base, (255, 255, 245), 0.18), _mix(base, black, 0.35), 3, False, 2)
    _draw_fit(art, (39, 315, 255, 334), "RISK / 行为备注", 13, black, True, 9)
    _draw_fit(art, (39, 337, 255, 380), str(item.get("note", DEFAULT_ITEM["note"])), 22, black, False, 11)
    _draw_triangle(art, 100, 402, 116, 57, yellow, black)
    _draw_fit(art, (171, 406, 255, 436), "HIGH RISK\n高风险处置", 14, black, True, 9)
    _draw_photo(art, (287, 307, 485, 445), item.get("photo"), rng, "paper")
    # 底部粗警示条和双语门禁警示。
    _draw_hazard_stripes(art, (0, 461, 512, 484), yellow, black, 14)
    d.rectangle(art.box((0, 484, 512, 512)), fill=black)
    cn, en = _warning(item)
    _draw_fit(art, (18, 486, 303, 507), cn, 17, yellow, True, 10)
    _draw_fit(art, (305, 488, 496, 507), en, 11, (242, 236, 205), True, 8, "right")
    _vignette(art, 32)
    return art.resize_output()


def render_b(item: Dict[str, Any], width: int, height: int, seed: int) -> Image.Image:
    art = Art(width, height)
    rng = random.Random(seed)
    base = (226, 216, 188)
    _fill_texture(art, base, rng, "paper")
    _add_wear(art, rng, dark=(120, 83, 70), light=(248, 238, 208), amount=110)
    d = art.d
    ink = (46, 44, 39)
    red = (145, 43, 43)
    accent = _class_color(item)
    d.rectangle(art.box((13, 13, 499, 499)), outline=red, width=round(3 * art.ss))
    d.rectangle(art.box((21, 21, 491, 491)), outline=_mix(base, ink, 0.25), width=max(1, art.ss))
    _draw_brand(art, 30, 29, red, ink, 17)
    _draw_fit(art, (322, 30, 477, 57), "ARCHIVE / 公文档案", 14, red, True, 9, "right")
    d.line([art.xy(28, 92), art.xy(484, 92)], fill=red, width=round(2 * art.ss))
    # 档案表头。
    fields = [("SITE", str(item.get("site", "SITE-19"))), ("RECORD", str(item.get("record", "CONTAINMENT RECORD"))), ("STATUS", "ACTIVE / 现行")]
    x = 29
    for label, value in fields:
        w = 145 if label != "RECORD" else 184
        d.rectangle(art.box((x, 108, x + w, 152)), outline=_mix(base, ink, 0.35), width=max(1, art.ss))
        _draw_fit(art, (x + 8, 112, x + w - 7, 126), label, 10, red, True, 7)
        _draw_fit(art, (x + 8, 127, x + w - 7, 146), value, 13, ink, True, 8)
        x += w + 8
    # 编号与分类色签。
    _draw_fit(art, (31, 174, 340, 255), str(item.get("number", DEFAULT_ITEM["number"])), 62, ink, True, 22)
    _draw_fit(art, (34, 248, 300, 267), "ITEM IDENTIFICATION / 项目编号", 11, red, True, 8)
    d.rectangle(art.box((31, 278, 296, 308)), fill=accent)
    _draw_fit(art, (43, 282, 284, 304), "项目等级 / " + _class_name(item), 17, (255, 250, 234), True, 10)
    # 备注表格，右侧照片避免和编号重叠。
    d.rectangle(art.box((31, 326, 296, 423)), outline=_mix(base, ink, 0.40), width=round(2 * art.ss))
    d.line([art.xy(31, 351), art.xy(296, 351)], fill=_mix(base, ink, 0.32), width=max(1, art.ss))
    _draw_fit(art, (42, 332, 280, 348), "OBSERVATION NOTE / 行为备注", 11, red, True, 8)
    _draw_fit(art, (42, 360, 284, 411), str(item.get("note", DEFAULT_ITEM["note"])), 20, ink, False, 11)
    _draw_photo(art, (315, 278, 478, 423), item.get("photo"), rng, "paper")
    # 红色印章不遮挡主编号，体现公文风而不是几何拼贴。
    stamp = Image.new("RGBA", art.size(106, 73), (0, 0, 0, 0))
    sd = ImageDraw.Draw(stamp)
    sf = _font(round(15 * min(art.sx, art.sy) * art.ss), True)
    sd.rounded_rectangle((2 * art.ss, 2 * art.ss, stamp.width - 2 * art.ss, stamp.height - 2 * art.ss), radius=6 * art.ss, outline=(151, 38, 38, 192), width=2 * art.ss)
    sd.text((stamp.width // 2, 19 * art.ss), "已收容", font=sf, fill=(151, 38, 38, 205), anchor="ma")
    sd.text((stamp.width // 2, 43 * art.ss), "CONTAINED", font=_font(round(9 * min(art.sx, art.sy) * art.ss), True), fill=(151, 38, 38, 205), anchor="ma")
    stamp = stamp.rotate(12, resample=getattr(Image, "Resampling", Image).BICUBIC, expand=True)
    art.image.paste(stamp, art.xy(369, 184), stamp)
    art.refresh()
    # 页脚签批线与门禁双语。
    d.line([art.xy(30, 442), art.xy(482, 442)], fill=red, width=round(2 * art.ss))
    cn, en = _warning(item)
    _draw_fit(art, (30, 449, 240, 469), cn, 15, red, True, 9)
    _draw_fit(art, (244, 449, 482, 469), en, 10, ink, True, 8, "right")
    _draw_fit(art, (30, 476, 482, 492), "FOUNDATION PROPERTY // 未经授权不得复制或进入", 10, _mix(base, ink, 0.48), False, 7, "center")
    _vignette(art, 27)
    return art.resize_output()


def render_c(item: Dict[str, Any], width: int, height: int, seed: int) -> Image.Image:
    art = Art(width, height)
    rng = random.Random(seed)
    base = (18, 28, 27)
    _fill_texture(art, base, rng, "terminal")
    d = art.d
    green = (109, 238, 151)
    amber = (241, 184, 64)
    red = (241, 84, 74)
    accent = _class_color(item)
    # 监控终端网格与边框。
    for x in range(26, 512, 34):
        d.line([art.xy(x, 22), art.xy(x, 491)], fill=(26, 65, 53), width=max(1, art.ss // 2))
    for y in range(22, 492, 29):
        d.line([art.xy(22, y), art.xy(490, y)], fill=(26, 65, 53), width=max(1, art.ss // 2))
    d.rectangle(art.box((15, 15, 497, 497)), outline=(68, 174, 113), width=round(2 * art.ss))
    d.rectangle(art.box((23, 23, 489, 489)), outline=(33, 95, 72), width=max(1, art.ss))
    _draw_brand(art, 30, 31, green, (103, 193, 132), 16)
    _draw_fit(art, (310, 28, 475, 49), str(item.get("timestamp", DEFAULT_ITEM["timestamp"])), 11, amber, True, 8, "right")
    d.line([art.xy(29, 87), art.xy(483, 87)], fill=green, width=round(2 * art.ss))
    _draw_fit(art, (31, 105, 475, 190), str(item.get("number", DEFAULT_ITEM["number"])), 70, green, True, 23)
    _draw_fit(art, (33, 184, 290, 203), "ANOMALY FEED / 异常识别", 12, amber, True, 8)
    # 状态灯和等级色条。
    d.rectangle(art.box((316, 111, 475, 177)), outline=(57, 150, 102), width=max(1, art.ss))
    d.ellipse(art.box((332, 128, 347, 143)), fill=accent)
    _draw_fit(art, (356, 120, 466, 143), "STATUS", 10, (126, 203, 152), True, 7)
    _draw_fit(art, (356, 141, 466, 163), _class_name(item), 20, accent, True, 10, "right")
    # 备注/照片两栏。
    _panel(art, (29, 224, 274, 359), (20, 40, 34), (48, 142, 94), 2, False, 1)
    _draw_fit(art, (41, 236, 261, 255), "BEHAVIOR LOG / 行为备注", 12, amber, True, 8)
    _draw_fit(art, (41, 264, 260, 330), str(item.get("note", DEFAULT_ITEM["note"])), 21, (196, 244, 208), False, 11)
    _draw_photo(art, (303, 224, 478, 359), item.get("photo"), rng, "terminal", "IMAGE RESTRICTED")
    # 终端数据行，提供层级细节。
    d.rectangle(art.box((29, 383, 478, 430)), outline=(49, 135, 91), width=max(1, art.ss))
    _draw_fit(art, (40, 389, 180, 403), "CONTAINMENT", 10, (119, 202, 142), True, 7)
    _draw_fit(art, (190, 389, 270, 403), "LOCKED", 11, green, True, 7)
    _draw_fit(art, (287, 389, 466, 403), "REMOTE OVERRIDE: DENIED", 10, red, True, 7, "right")
    d.line([art.xy(40, 414), art.xy(465, 414)], fill=(45, 118, 79), width=max(1, art.ss))
    _draw_fit(art, (40, 416, 465, 427), "LOG // AUTHORIZATION CHECK REQUIRED", 9, (109, 179, 126), False, 7)
    # 扫描线只加在内容层上，保持文字仍可读。
    for y in range(92, 474, 4):
        d.line([art.xy(24, y), art.xy(488, y)], fill=(77, 158, 110), width=max(1, art.ss // 2))
    cn, en = _warning(item)
    d.rectangle(art.box((24, 454, 488, 488)), fill=(15, 24, 22), outline=amber, width=round(2 * art.ss))
    _draw_fit(art, (37, 459, 253, 481), cn, 15, amber, True, 9)
    _draw_fit(art, (262, 460, 475, 481), en, 10, (177, 234, 190), True, 7, "right")
    _vignette(art, 42)
    return art.resize_output()


def render_d(item: Dict[str, Any], width: int, height: int, seed: int) -> Image.Image:
    art = Art(width, height)
    rng = random.Random(seed)
    base = (147, 151, 147)
    _fill_texture(art, base, rng, "metal")
    _add_wear(art, rng, dark=(69, 72, 69), light=(218, 220, 207), amount=95)
    d = art.d
    black = (34, 37, 35)
    steel = (192, 194, 185)
    accent = _class_color(item)
    d.rectangle(art.box((8, 8, 504, 504)), fill=_mix(base, black, 0.13), outline=black, width=round(4 * art.ss))
    d.rectangle(art.box((17, 17, 495, 495)), outline=_lighten(base, 45), width=round(2 * art.ss))
    d.rectangle(art.box((24, 24, 488, 488)), outline=_darken(base, 45), width=max(1, art.ss))
    d.rectangle(art.box((24, 24, 38, 488)), fill=accent)
    _draw_screws(art, black, _lighten(base, 72), 14)
    # 深钢色顶栏，配合压印式基金会标识。
    d.rectangle(art.box((43, 36, 478, 91)), fill=(55, 60, 58), outline=(212, 213, 201), width=max(1, art.ss))
    _draw_brand(art, 58, 42, steel, (169, 174, 162), 17)
    _draw_fit(art, (335, 49, 464, 74), "METAL PLATE", 13, steel, True, 8, "right")
    # 雕刻编号：高光偏移 + 深色压痕，不用大面积纯黑。
    number = str(item.get("number", DEFAULT_ITEM["number"]))
    nf, nwrapped = _fit_text(art, number, 430, 94, 68, True, 24)
    _draw_text_shadow(art, (48, 120), nwrapped, nf, _lighten(base, 73), _darken(base, 72), (2, 2))
    _draw_fit(art, (51, 198, 290, 216), "IDENTIFICATION / 识别铭文", 11, _darken(base, 78), True, 8)
    # 等级垂直彩条 + 芯片。
    d.rectangle(art.box((45, 235, 296, 273)), fill=accent, outline=black, width=round(2 * art.ss))
    _draw_fit(art, (57, 240, 286, 267), "CLASS / 项目等级  " + _class_name(item), 16, (246, 243, 224), True, 9)
    d.line([art.xy(45, 285), art.xy(293, 285)], fill=_lighten(base, 45), width=max(1, art.ss))
    # 左备注区、右固定照片区明显分栏。
    _panel(art, (45, 300, 293, 414), _mix(base, steel, 0.36), _darken(base, 58), 2, True, 2)
    _draw_fit(art, (58, 312, 279, 331), "RISK PROFILE / 行为备注", 12, black, True, 8)
    _draw_fit(art, (58, 340, 280, 398), str(item.get("note", DEFAULT_ITEM["note"])), 21, black, False, 11)
    _draw_photo(art, (316, 254, 474, 414), item.get("photo"), rng, "metal")
    # 金属铭牌底部警示，英文和中文留出独立区域。
    _draw_hazard_stripes(art, (43, 438, 478, 459), (225, 178, 41), black, 13)
    d.rectangle(art.box((43, 459, 478, 488)), fill=black)
    cn, en = _warning(item)
    _draw_fit(art, (53, 463, 270, 484), cn, 15, (239, 199, 74), True, 9)
    _draw_fit(art, (280, 465, 465, 482), en, 9, (221, 221, 205), True, 7, "right")
    _vignette(art, 34)
    return art.resize_output()


RENDERERS = {"A": render_a, "B": render_b, "C": render_c, "D": render_d}


def _normalize_item(raw: Any) -> Dict[str, Any]:
    value = dict(DEFAULT_ITEM)
    if isinstance(raw, dict):
        value.update(raw)
    # 清理用户可能写入的 null / 空字符串；保留 photo=None 表示占位框。
    for key in ("number", "class", "note", "theme"):
        if value.get(key) in (None, ""):
            value[key] = DEFAULT_ITEM[key]
    return value


def _load_items(path: Path) -> List[Dict[str, Any]]:
    with path.open("r", encoding="utf-8-sig") as handle:
        data = json.load(handle)
    if isinstance(data, dict) and isinstance(data.get("labels"), list):
        data = data["labels"]
    if not isinstance(data, list):
        raise ValueError("labels.json 必须是对象数组，或 {\"labels\": [...]}。")
    return [_normalize_item(item) for item in data]


def _slug(value: str) -> str:
    value = re.sub(r"[^0-9A-Za-z一-龥._-]+", "_", value.strip())
    return value.strip("._") or "SCP-XXX"


def generate(items: List[Dict[str, Any]], input_path: Path, output_dir: Path, theme_override: Optional[str], width: int, height: int, base_seed: int) -> List[Path]:
    output_dir.mkdir(parents=True, exist_ok=True)
    used: Dict[str, int] = {}
    outputs: List[Path] = []
    for index, item in enumerate(items, start=1):
        theme = (theme_override or str(item.get("theme") or "A")).upper().strip()
        if theme not in RENDERERS:
            print(f"警告：第 {index} 条 theme={theme!r} 无效，改用 A。", file=sys.stderr)
            theme = "A"
        resolved = dict(item)
        photo = resolved.get("photo")
        if photo:
            photo_path = Path(str(photo))
            if not photo_path.is_absolute():
                photo_path = (input_path.parent / photo_path).resolve()
            resolved["photo"] = str(photo_path)
        image = RENDERERS[theme](resolved, width, height, _stable_seed(resolved, theme, base_seed))
        stem = f"{_slug(str(resolved.get('number', 'SCP-XXX')))}_{theme}"
        used[stem] = used.get(stem, 0) + 1
        if used[stem] > 1:
            stem = f"{stem}_{used[stem]}"
        destination = output_dir / f"{stem}.png"
        image.save(destination, format="PNG", optimize=True)
        outputs.append(destination)
        print(f"生成 [{theme}] {resolved.get('number', 'SCP-XXX')} -> {destination}")
    return outputs


def _parse_args(argv: Optional[Sequence[str]] = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="批量生成 SCPMod 四主题收容室标牌 PNG")
    parser.add_argument("labels", type=Path, help="标签 JSON 文件")
    parser.add_argument("output", type=Path, help="PNG 输出目录")
    parser.add_argument("--theme", choices=THEME_NAMES, type=str.upper, help="覆盖 JSON 中的 theme，仅生成指定主题")
    parser.add_argument("--size", type=int, default=512, help="正方形输出尺寸，默认 512")
    parser.add_argument("--width", type=int, help="输出宽度；与 --height 一起使用可生成非正方形贴图")
    parser.add_argument("--height", type=int, help="输出高度；与 --width 一起使用可生成非正方形贴图")
    parser.add_argument("--seed", type=int, default=173, help="纹理随机种子，默认 173；相同输入可稳定复现")
    return parser.parse_args(argv)


def main(argv: Optional[Sequence[str]] = None) -> int:
    args = _parse_args(argv)
    if not args.labels.is_file():
        print(f"错误：找不到 labels 文件：{args.labels}", file=sys.stderr)
        return 2
    width = args.width or args.size
    height = args.height or args.size
    if width < 64 or height < 64:
        print("错误：尺寸至少为 64×64。", file=sys.stderr)
        return 2
    try:
        items = _load_items(args.labels)
        if not items:
            print("提示：labels.json 为空，没有生成文件。")
            return 0
        outputs = generate(items, args.labels.resolve(), args.output, args.theme, width, height, args.seed)
    except (OSError, ValueError, json.JSONDecodeError) as exc:
        print(f"错误：{exc}", file=sys.stderr)
        return 1
    font_path = _find_font(False)
    if font_path is None:
        print("警告：未找到中文系统字体；已使用 Pillow 内置字体，请安装/提供 msyhbd.ttc 或 simhei.ttf 以避免中文缺字。", file=sys.stderr)
    print(f"完成：{len(outputs)} 张，尺寸 {width}×{height}，输出目录 {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
