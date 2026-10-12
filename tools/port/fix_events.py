"""Little Maid Continued porting helper: input events, buttons, tooltips, resize (1.21.1 -> 26.x).

Rewrites method signatures and declares the old parameter names as locals so method bodies keep working.
Usage: python tools/port/fix_events.py <java-src-dir>
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
from fix_gui import ensure_import, split_args  # noqa: E402

IMPORTS = {
    "MouseButtonEvent": "net.minecraft.client.input.MouseButtonEvent",
    "MouseButtonInfo": "net.minecraft.client.input.MouseButtonInfo",
    "KeyEvent": "net.minecraft.client.input.KeyEvent",
    "CharacterEvent": "net.minecraft.client.input.CharacterEvent",
    "InputWithModifiers": "net.minecraft.client.input.InputWithModifiers",
    "TooltipDisplay": "net.minecraft.world.item.component.TooltipDisplay",
    "Consumer<": "java.util.function.Consumer",
    "ArrayList<": "java.util.ArrayList",
}


def header(text, pattern, new_sig, locals_fmt):
    """Replace a method header (pattern must capture param names and end at '{') and inject locals."""
    def rep(m):
        indent = re.match(r"\s*", text[text.rfind("\n", 0, m.start()) + 1:]).group(0) + "    "
        g = m.groupdict()
        loc = "".join("\n%s%s" % (indent, l.format(**g)) for l in locals_fmt)
        return new_sig.format(**g) + " {" + loc
    return re.sub(pattern, rep, text)


def process(t, is_button):
    o = t
    # mouseClicked(double, double, int)
    t = header(t, r"public boolean mouseClicked\(double (?P<x>\w+), double (?P<y>\w+), int (?P<b>\w+)\)\s*\{",
               "public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)",
               ["double {x} = event.x();", "double {y} = event.y();", "int {b} = event.button();"])
    t = header(t, r"public boolean mouseReleased\(double (?P<x>\w+), double (?P<y>\w+), int (?P<b>\w+)\)\s*\{",
               "public boolean mouseReleased(MouseButtonEvent event)",
               ["double {x} = event.x();", "double {y} = event.y();", "int {b} = event.button();"])
    t = header(t, r"public boolean mouseDragged\(double (?P<x>\w+), double (?P<y>\w+), int (?P<b>\w+), double (?P<dx>\w+), double (?P<dy>\w+)\)\s*\{",
               "public boolean mouseDragged(MouseButtonEvent event, double {dx}, double {dy})",
               ["double {x} = event.x();", "double {y} = event.y();", "int {b} = event.button();"])
    t = header(t, r"public boolean keyPressed\(int (?P<k>\w+), int (?P<s>\w+), int (?P<m>\w+)\)\s*\{",
               "public boolean keyPressed(KeyEvent event)",
               ["int {k} = event.key();", "int {s} = event.scancode();", "int {m} = event.modifiers();"])
    t = header(t, r"public boolean keyReleased\(int (?P<k>\w+), int (?P<s>\w+), int (?P<m>\w+)\)\s*\{",
               "public boolean keyReleased(KeyEvent event)",
               ["int {k} = event.key();", "int {s} = event.scancode();", "int {m} = event.modifiers();"])
    t = header(t, r"public boolean charTyped\(char (?P<c>\w+), int (?P<m>\w+)\)\s*\{",
               "public boolean charTyped(CharacterEvent event)",
               ["char {c} = (char) event.codepoint();", "int {m} = 0;"])
    t = header(t, r"public void onClick\(double (?P<x>\w+), double (?P<y>\w+)\)\s*\{",
               "public void onClick(MouseButtonEvent event, boolean doubleClick)",
               ["double {x} = event.x();", "double {y} = event.y();"])
    t = header(t, r"public void onRelease\(double (?P<x>\w+), double (?P<y>\w+)\)\s*\{",
               "public void onRelease(MouseButtonEvent event)",
               ["double {x} = event.x();", "double {y} = event.y();"])
    t = header(t, r"protected void onDrag\(double (?P<x>\w+), double (?P<y>\w+), double (?P<dx>\w+), double (?P<dy>\w+)\)\s*\{",
               "protected void onDrag(MouseButtonEvent event, double {dx}, double {dy})",
               ["double {x} = event.x();", "double {y} = event.y();"])
    t = re.sub(r"public void onPress\(\)\s*\{", "public void onPress(InputWithModifiers input) {", t)
    t = re.sub(r"\bsuper\.onPress\(\)", "super.onPress(input)", t)

    # super calls inside the rewritten methods
    t = re.sub(r"\bsuper\.mouseClicked\([^;]*?\)(?=[;)\s|&])", "super.mouseClicked(event, doubleClick)", t)
    t = re.sub(r"\bsuper\.mouseReleased\([^;]*?\)(?=[;)\s|&])", "super.mouseReleased(event)", t)
    t = re.sub(r"\bsuper\.mouseDragged\(\w+, \w+, \w+, (\w+), (\w+)\)", r"super.mouseDragged(event, \1, \2)", t)
    t = re.sub(r"\bsuper\.keyPressed\(\w+, \w+, \w+\)", "super.keyPressed(event)", t)
    t = re.sub(r"\bsuper\.keyReleased\(\w+, \w+, \w+\)", "super.keyReleased(event)", t)
    t = re.sub(r"\bsuper\.charTyped\(\w+, \w+\)", "super.charTyped(event)", t)
    t = re.sub(r"\bsuper\.onClick\(\w+, \w+\)", "super.onClick(event, doubleClick)", t)

    # forwarding to children: child.mouseClicked(x, y, b) -> child.mouseClicked(event, doubleClick)
    if "MouseButtonEvent event, boolean doubleClick" in t:
        t = re.sub(r"(\w+(?:\.\w+\(\))?)\.mouseClicked\(\w+, \w+, \w+\)", r"\1.mouseClicked(event, doubleClick)", t)
    if "mouseReleased(MouseButtonEvent event)" in t:
        t = re.sub(r"(\w+(?:\.\w+\(\))?)\.mouseReleased\(\w+, \w+, \w+\)", r"\1.mouseReleased(event)", t)
    if "keyPressed(KeyEvent event)" in t:
        t = re.sub(r"(\w+(?:\.\w+\(\))?)\.keyPressed\(\w+, \w+, \w+\)", r"\1.keyPressed(event)", t)
    if "charTyped(CharacterEvent event)" in t:
        t = re.sub(r"(\w+(?:\.\w+\(\))?)\.charTyped\(\w+, \w+\)", r"\1.charTyped(event)", t)

    # resize(Minecraft, int, int) -> resize(int, int)
    t = re.sub(r"public void resize\(Minecraft \w+, int (\w+), int (\w+)\)", r"public void resize(int \1, int \2)", t)
    t = re.sub(r"\bsuper\.resize\(\w+, (\w+), (\w+)\)", r"super.resize(\1, \2)", t)
    t = re.sub(r"\bthis\.resize\((?:this\.)?minecraft, (\w+), (\w+)\)", r"this.resize(\1, \2)", t)

    # buttons: AbstractButton.extractWidgetRenderState is final -> extractContents
    if is_button:
        t = re.sub(r"protected void extractWidgetRenderState\(GuiGraphicsExtractor", "protected void extractContents(GuiGraphicsExtractor", t)
        t = t.replace("super.extractWidgetRenderState(", "super.extractContents(")

    # Item#appendHoverText(stack, ctx, List<Component>, flag) -> (stack, ctx, display, Consumer<Component>, flag)
    def tooltip(m):
        indent = m.group("indent")
        return ("%spublic void appendHoverText(ItemStack %s, %s %s, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag %s) {\n"
                "%s    List<Component> %s = new ArrayList<>();\n%s    try {"
                % (indent, m.group("s"), m.group("ct"), m.group("c"), m.group("f"), indent, m.group("l"), indent))
    pat = re.compile(r"(?P<indent>[ \t]*)public void appendHoverText\((?:@\w+ )?ItemStack (?P<s>\w+), (?:@\w+ )?(?P<ct>(?:Item\.)?TooltipContext) (?P<c>\w+), (?:@\w+ )?List<Component> (?P<l>\w+), (?:@\w+ )?TooltipFlag (?P<f>\w+)\)\s*\{")
    while True:
        m = pat.search(t)
        if not m:
            break
        # find matching closing brace of the method body
        start = t.index("{", m.end() - 1)
        depth, i = 0, start
        while i < len(t):
            if t[i] == "{":
                depth += 1
            elif t[i] == "}":
                depth -= 1
                if depth == 0:
                    break
            i += 1
        body = t[start + 1:i]
        indent = m.group("indent")
        body = "\n".join(("    " + l) if l.strip() else l for l in body.split("\n"))
        new = (tooltip(m) + body.rstrip() + "\n%s    } finally {\n%s        %s.forEach(tooltipAdder);\n%s    }\n%s}"
               % (indent, indent, m.group("l"), indent, indent))
        t = t[:m.start()] + new + t[i + 1:]
    t = re.sub(r"\bsuper\.appendHoverText\((\w+), (\w+), (\w+), (\w+)\)", r"super.appendHoverText(\1, \2, tooltipDisplay, \3::add, \4)", t)

    if t != o:
        for k, v in IMPORTS.items():
            if k in t:
                t = ensure_import(t, v)
    return t


def main():
    n = 0
    for d, _, fs in os.walk(sys.argv[1]):
        for f in fs:
            if not f.endswith(".java"):
                continue
            p = os.path.join(d, f)
            t = open(p, encoding="utf-8").read()
            is_button = bool(re.search(r"extends (Abstract)?Button\b|extends \w*Button\b", t))
            nt = process(t, is_button)
            if nt != t:
                open(p, "w", encoding="utf-8", newline="\n").write(nt)
                n += 1
    print("events pass changed", n)


if __name__ == "__main__":
    main()
