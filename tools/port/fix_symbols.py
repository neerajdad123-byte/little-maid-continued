"""Little Maid Continued porting helper: line-targeted fixes keyed on the compiler's missing-symbol text.

Usage: python tools/port/fix_symbols.py <build-log>
"""
import re
import sys
from collections import defaultdict

PORT_UTIL = "com.github.tartaricacid.touhoulittlemaid.util.PortUtil"

# missing symbol (as javac prints it after "symbol:") -> list of (regex on the error line, replacement)
# keys ending with "(" are prefix rules that match any argument list
RULES = {
    "method location()": [(r"\.location\(\)", ".identifier()")],
    "method getYRot()": [(r"\.getYRot\(\)", ".yRot()")],
    "method getXRot()": [(r"\.getXRot\(\)", ".xRot()")],
    "method getTimer()": [(r"Minecraft\.getInstance\(\)\.getTimer\(\)", "Minecraft.getInstance().getDeltaTracker()"),
                          (r"\b(\w+)\.getTimer\(\)", r"\1.getDeltaTracker()")],
    "method isOnRenderThreadOrInit()": [(r"RenderSystem\.isOnRenderThreadOrInit\(\)", "RenderSystem.isOnRenderThread()")],
    "method getServer()": [(r"\b(\w+)\.getServer\(\)", r"\1.level().getServer()")],
    "method serverLevel()": [(r"\.serverLevel\(\)", ".level()")],
    "method is(TagKey<EntityType<?>>)": [(r"\.getType\(\)\.is\(", ".getType().builtInRegistryHolder().is("),
                                          (r"\b(\w+)\.is\(", r"\1.builtInRegistryHolder().is(")],
    "variable Serializer": [(r"Component\.Serializer\.toJson\(", "PortUtil.componentToJson("),
                            (r"Component\.Serializer\.fromJson\(", "PortUtil.componentFromJson(")],
    "method serializeNBT(":[(r"\b([\w.]+(?:\(\))?)\.serializeNBT\(([^()]*(?:\(\))?)\)", r"PortUtil.serializeItems(\1, \2)")],
    "method deserializeNBT(": [(r"\b([\w.]+(?:\(\))?)\.deserializeNBT\(([^,]+), (.+)\);", r"PortUtil.deserializeItems(\1, \2, \3);")],
}


def main():
    log = sys.argv[1]
    lines = open(log, encoding="utf-8", errors="ignore").read().split("\n")
    hits = defaultdict(set)
    for i, l in enumerate(lines):
        m = re.match(r"\s*(.+\.java):(\d+): error: (.*)", l)
        if not m:
            continue
        for j in range(i + 1, min(i + 5, len(lines))):
            if "symbol:" in lines[j]:
                sym = re.sub(r"\s+", " ", lines[j].split("symbol:", 1)[1].strip())
                key = sym if sym in RULES else next((k for k in RULES if k.endswith("(") and sym.startswith(k)), None)
                if key:
                    hits[m.group(1)].add((int(m.group(2)), key))
                break
    n = 0
    for path, items in hits.items():
        src = open(path, encoding="utf-8").read().split("\n")
        for ln, sym in items:
            line = src[ln - 1]
            for a, b in RULES[sym]:
                new = re.sub(a, b, line, count=1)
                if new != line:
                    src[ln - 1] = new
                    n += 1
                    break
        text = "\n".join(src)
        if "PortUtil." in text and ("import %s;" % PORT_UTIL) not in text and not path.endswith("PortUtil.java"):
            text = re.sub(r"^(package [\w.]+;\n)", r"\1\nimport %s;" % PORT_UTIL, text, count=1, flags=re.M)
        open(path, "w", encoding="utf-8", newline="\n").write(text)
    print("symbol fixes", n)


if __name__ == "__main__":
    main()
