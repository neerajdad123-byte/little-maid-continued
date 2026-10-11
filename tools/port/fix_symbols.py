"""Little Maid Continued porting helper: line-targeted fixes keyed on the compiler's missing-symbol text.

Usage: python tools/port/fix_symbols.py <build-log>
"""
import re
import sys
from collections import defaultdict

# missing symbol (as javac prints it after "symbol:") -> list of (regex on the error line, replacement)
RULES = {
    "method location()": [(r"\.location\(\)", ".identifier()")],
    "method getYRot()": [(r"\.getYRot\(\)", ".yRot()")],
    "method getXRot()": [(r"\.getXRot\(\)", ".xRot()")],
    "method getTimer()": [(r"Minecraft\.getInstance\(\)\.getTimer\(\)", "Minecraft.getInstance().getDeltaTracker()"),
                          (r"\b(\w+)\.getTimer\(\)", r"\1.getDeltaTracker()")],
    "method isOnRenderThreadOrInit()": [(r"RenderSystem\.isOnRenderThreadOrInit\(\)", "RenderSystem.isOnRenderThread()")],
    "method getServer()": [(r"\b(\w+)\.getServer\(\)", r"\1.level().getServer()")],
    "method serverLevel()": [(r"\.serverLevel\(\)", ".level()")],
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
                if sym in RULES:
                    hits[m.group(1)].add((int(m.group(2)), sym))
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
        open(path, "w", encoding="utf-8", newline="\n").write("\n".join(src))
    print("symbol fixes", n)


if __name__ == "__main__":
    main()
