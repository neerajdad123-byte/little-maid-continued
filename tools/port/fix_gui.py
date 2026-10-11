"""Little Maid Continued porting helper: GuiGraphics (1.21.1) -> GuiGraphicsExtractor (26.x).

Usage: python tools/port/fix_gui.py <java-src-dir>
"""
import os
import re
import sys

PIPE = "RenderPipelines.GUI_TEXTURED"
PIPE_IMPORT = "net.minecraft.client.renderer.RenderPipelines"


def split_args(s, start):
    """s[start] == '('. Returns (args, end_index_of_closing_paren)."""
    depth = 0
    args, cur = [], []
    i = start
    in_str = None
    while i < len(s):
        c = s[i]
        if in_str:
            cur.append(c)
            if c == "\\":
                cur.append(s[i + 1])
                i += 2
                continue
            if c == in_str:
                in_str = None
        elif c in "\"'":
            in_str = c
            cur.append(c)
        elif c in "([{":
            if depth > 0:
                cur.append(c)
            depth += 1
        elif c in ")]}":
            depth -= 1
            if depth == 0:
                args.append("".join(cur).strip())
                return args, i
            cur.append(c)
        elif c == "," and depth == 1:
            args.append("".join(cur).strip())
            cur = []
        else:
            cur.append(c)
        i += 1
    raise ValueError("unbalanced")


def rewrite_calls(text, var, method, fn):
    pat = re.compile(r"\b%s\.%s\(" % (re.escape(var), method))
    out, pos = [], 0
    for m in list(pat.finditer(text)):
        if m.start() < pos:
            continue
        try:
            args, end = split_args(text, m.end() - 1)
        except ValueError:
            continue
        new = fn(args)
        if new is None:
            continue
        out.append(text[pos:m.start()])
        out.append(new)
        pos = end + 1
    out.append(text[pos:])
    return "".join(out)


def looks_like_texture(arg):
    return not re.match(r"^(RenderPipelines\.|RenderType|[A-Z_]+_PIPELINE)", arg) and not arg.startswith("RenderPipeline")


def fix_color(arg):
    # 1.21.6+ text/fill colors are ARGB; a 6-digit literal now means alpha 0 (invisible)
    m = re.fullmatch(r"0x([0-9a-fA-F]{6})", arg)
    if m:
        return "0xFF" + m.group(1).upper()
    if arg in ("16777215",):
        return "0xFFFFFFFF"
    return arg


def ensure_import(text, fqn):
    if ("import %s;" % fqn) in text:
        return text
    m = re.search(r"^package [\w.]+;\n", text, re.M)
    return text[:m.end()] + "\nimport %s;" % fqn + text[m.end():] if m else text


def process(text):
    if "GuiGraphicsExtractor" not in text:
        return text
    names = set(re.findall(r"GuiGraphicsExtractor\s+(\w+)", text))
    names.discard("graphics_unused")
    need_pipe = False

    # --- method definitions -------------------------------------------------
    text = re.sub(r"\b(public|protected) void render\(GuiGraphicsExtractor (\w+), int (\w+), int (\w+), float (\w+)\)",
                  r"public void extractRenderState(GuiGraphicsExtractor \2, int \3, int \4, float \5)", text)
    text = re.sub(r"\b(public|protected) void renderWidget\(GuiGraphicsExtractor",
                  r"protected void extractWidgetRenderState(GuiGraphicsExtractor", text)
    text = re.sub(r"\bsuper\.renderWidget\(", "super.extractWidgetRenderState(", text)
    text = re.sub(r"\b(public|protected) void renderLabels\(GuiGraphicsExtractor", r"protected void extractLabels(GuiGraphicsExtractor", text)
    text = re.sub(r"\bsuper\.renderLabels\(", "super.extractLabels(", text)

    def bg(m):
        g, pt, mx, my = m.group(2), m.group(3), m.group(4), m.group(5)
        return ("public void extractBackground(GuiGraphicsExtractor %s, int %s, int %s, float %s) {\n"
                "%ssuper.extractBackground(%s, %s, %s, %s);\n%s" % (g, mx, my, pt, m.group(6), g, mx, my, pt, m.group(6)))
    text = re.sub(r"\b(public|protected) void renderBg\(GuiGraphicsExtractor (\w+), float (\w+), int (\w+), int (\w+)\)\s*\{\n(\s*)", bg, text)
    text = re.sub(r"\b(public|protected) void renderBackground\(GuiGraphicsExtractor (\w+), int (\w+), int (\w+), float (\w+)\)",
                  r"public void extractBackground(GuiGraphicsExtractor \2, int \3, int \4, float \5)", text)
    text = re.sub(r"\bsuper\.renderBackground\(", "super.extractBackground(", text)
    # explicit background calls inside render() are now done by Screen itself
    text = re.sub(r"\n[ \t]*(?:this\.)?renderBackground\([^;]*\);[ \t]*", "", text)
    text = re.sub(r"\b(public|protected) void renderTooltip\(GuiGraphicsExtractor (\w+), int (\w+), int (\w+)\)",
                  r"protected void extractTooltip(GuiGraphicsExtractor \2, int \3, int \4)", text)
    text = re.sub(r"\bsuper\.renderTooltip\((\w+), (\w+), (\w+)\)", r"super.extractTooltip(\1, \2, \3)", text)
    text = re.sub(r"\b(?:this\.)?renderTooltip\((\w+), (\w+), (\w+)\);", r"this.extractTooltip(\1, \2, \3);", text)
    text = re.sub(r"\bsuper\.render\((\w+), (\w+), (\w+), (\w+)\)", r"super.extractRenderState(\1, \2, \3, \4)", text)
    text = re.sub(r"\.render\((\w+), (\w+), (\w+), (\w+)\);", lambda m: (".extractRenderState(%s, %s, %s, %s);" % m.groups())
                  if m.group(1) in names else m.group(0), text)

    # --- calls on the graphics object ----------------------------------------
    simple = {
        "drawString": "text", "drawCenteredString": "centeredText", "drawWordWrap": "textWithWordWrap",
        "renderItem": "item", "renderFakeItem": "fakeItem", "renderItemDecorations": "itemDecorations",
        "renderComponentTooltip": "setComponentTooltipForNextFrame", "renderTooltip": "setTooltipForNextFrame",
        "renderOutline": "outline", "hLine": "horizontalLine", "vLine": "verticalLine",
        "drawStringWithBackdrop": "textWithBackdrop",
    }
    for g in names:
        for old, new in simple.items():
            text = re.sub(r"\b%s\.%s\(" % (re.escape(g), old), "%s.%s(" % (g, new), text)

        for m in ("text", "centeredText"):
            text = rewrite_calls(text, g, m, lambda a, m=m: "%s.%s(%s)" % (g, m, ", ".join(a[:4] + [fix_color(a[4])] + a[5:])) if len(a) >= 5 else None)
        text = rewrite_calls(text, g, "fill", lambda a: "%s.fill(%s)" % (g, ", ".join(a[:4] + [fix_color(a[4])] + a[5:])) if len(a) == 5 else None)
        text = rewrite_calls(text, g, "fillGradient", lambda a: "%s.fillGradient(%s)" % (g, ", ".join(a[:4] + [fix_color(a[4]), fix_color(a[5])] + a[6:])) if len(a) == 6 else None)

        def blit(a):
            nonlocal need_pipe
            if not a or not looks_like_texture(a[0]):
                return None
            need_pipe = True
            if len(a) == 7:      # tex, x, y, u, v, w, h  (old default 256x256 atlas)
                a = a + ["256", "256"]
            elif len(a) == 9:    # tex, x, y, u, v, w, h, texW, texH
                pass
            elif len(a) == 11:   # tex, x, y, w, h, u, v, srcW, srcH, texW, texH (old order)
                tex, x, y, w, h, u, v, sw, sh, tw, th = a
                a = [tex, x, y, u, v, w, h, sw, sh, tw, th]
            elif len(a) == 10:   # tex, x, y, z(blitOffset), u, v, w, h, texW, texH
                tex, x, y, _z, u, v, w, h, tw, th = a
                a = [tex, x, y, u, v, w, h, tw, th]
            else:
                return None
            return "%s.blit(%s, %s)" % (g, PIPE, ", ".join(a))
        text = rewrite_calls(text, g, "blit", blit)

        def blit_sprite(a):
            nonlocal need_pipe
            if not a or not looks_like_texture(a[0]):
                return None
            need_pipe = True
            return "%s.blitSprite(%s, %s)" % (g, PIPE, ", ".join(a))
        text = rewrite_calls(text, g, "blitSprite", blit_sprite)

        p = r"%s\.pose\(\)" % re.escape(g)
        text = re.sub(p + r"\.pushPose\(\)", "%s.pose().pushMatrix()" % g, text)
        text = re.sub(p + r"\.popPose\(\)", "%s.pose().popMatrix()" % g, text)
        text = rewrite_calls(text, g + r".pose()", "translate", lambda a: "%s.pose().translate(%s)" % (g, ", ".join(a[:2])) if len(a) == 3 else None)
        text = rewrite_calls(text, g + r".pose()", "scale", lambda a: "%s.pose().scale(%s)" % (g, ", ".join(a[:2])) if len(a) == 3 else None)
        text = re.sub(p + r"\.mulPose\(Axis\.ZP\.rotationDegrees\(([^;]*?)\)\);", r"%s.pose().rotate((float) Math.toRadians(\1));" % g, text)
        text = re.sub(r"\n[ \t]*%s\.flush\(\);" % re.escape(g), "", text)

    # GL state is no longer set by GUI code
    text = re.sub(r"\n[ \t]*RenderSystem\.(enableBlend|disableBlend|defaultBlendFunc|enableDepthTest|disableDepthTest|setShader\(GameRenderer::getPositionTexShader\))\(\);", "", text)
    if need_pipe:
        text = ensure_import(text, PIPE_IMPORT)
    return text


def main():
    n = 0
    for d, _, fs in os.walk(sys.argv[1]):
        for f in fs:
            if f.endswith(".java"):
                p = os.path.join(d, f)
                t = open(p, encoding="utf-8").read()
                nt = process(t)
                if nt != t:
                    open(p, "w", encoding="utf-8", newline="\n").write(nt)
                    n += 1
    print("gui pass changed", n)


if __name__ == "__main__":
    main()
