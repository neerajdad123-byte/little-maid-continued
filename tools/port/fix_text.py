"""Little Maid Continued porting helper: regex-level source fixes.

Usage: python tools/port/fix_text.py <java-src-dir> [build-log]
With a build log, also applies line-targeted fixes for specific compiler errors.
"""
import os
import re
import sys
from collections import defaultdict

GLOBAL = [
    # annotation removed in 26.x, @NullMarked (jspecify) covers both params and returns
    (r"[ \t]*@MethodsReturnNonnullByDefault[ \t]*\r?\n", ""),
    (r"@MethodsReturnNonnullByDefault\s*", ""),
    (r"import net\.minecraft\.MethodsReturnNonnullByDefault;\r?\n", ""),
    (r"@ParametersAreNonnullByDefault", "@NullMarked"),
    # EventBusSubscriber no longer takes a bus
    (r"bus\s*=\s*EventBusSubscriber\.Bus\.(?:MOD|GAME)\s*,\s*", ""),
    (r"\s*,\s*bus\s*=\s*EventBusSubscriber\.Bus\.(?:MOD|GAME)", ""),
    (r"\(\s*bus\s*=\s*EventBusSubscriber\.Bus\.(?:MOD|GAME)\s*\)", ""),
    (r"\bPacketDistributor\.sendToServer\b", "ClientPacketDistributor.sendToServer"),
    (r"\bnet\.minecraft\.advancements\.critereon\b", "net.minecraft.advancements.criterion"),
]

# compiler message -> (regex on the offending line, replacement)
TARGETED = {
    "isClientSide has private access in Level": (r"\.isClientSide\b(?!\()", ".isClientSide()"),
    "random has protected access in Level": (r"\.random\b(?!\()", ".getRandom()"),
}


def ensure_import(text, fqn):
    if re.search(r"^import %s;" % re.escape(fqn), text, re.M):
        return text
    m = re.search(r"^package [\w.]+;\r?\n", text, re.M)
    if not m:
        return text
    nl = "\r\n" if "\r\n" in text else "\n"
    return text[:m.end()] + nl + "import %s;" % fqn + text[m.end():]


def main():
    src = sys.argv[1]
    log = sys.argv[2] if len(sys.argv) > 2 else None
    changed = 0
    for dirpath, _, files in os.walk(src):
        for f in files:
            if not f.endswith(".java"):
                continue
            p = os.path.join(dirpath, f)
            t = open(p, encoding="utf-8", newline="").read()
            o = t
            for a, b in GLOBAL:
                t = re.sub(a, b, t)
            if "@NullMarked" in t:
                t = t.replace("import javax.annotation.ParametersAreNonnullByDefault;", "")
                t = ensure_import(t, "org.jspecify.annotations.NullMarked")
            if "ClientPacketDistributor." in t:
                t = ensure_import(t, "net.neoforged.neoforge.client.network.ClientPacketDistributor")
            if t != o:
                open(p, "w", encoding="utf-8", newline="").write(t)
                changed += 1
    print("global pass changed", changed)

    if not log:
        return
    hits = defaultdict(set)
    for line in open(log, encoding="utf-8", errors="ignore"):
        m = re.match(r"(.+\.java):(\d+): error: (.*)", line.strip())
        if not m:
            continue
        for msg in TARGETED:
            if m.group(3).startswith(msg):
                hits[m.group(1)].add((int(m.group(2)), msg))
    n = 0
    for path, items in hits.items():
        lines = open(path, encoding="utf-8", newline="").read().split("\n")
        for ln, msg in items:
            a, b = TARGETED[msg]
            new = re.sub(a, b, lines[ln - 1])
            if new != lines[ln - 1]:
                lines[ln - 1] = new
                n += 1
        open(path, "w", encoding="utf-8", newline="").write("\n".join(lines))
    print("targeted fixes", n)


if __name__ == "__main__":
    main()
