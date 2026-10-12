"""Little Maid Continued porting helper: brain behaviour factories, food components and misc AI API changes.

Usage: python tools/port/fix_tasks.py <java-src-dir>
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
from fix_gui import ensure_import, split_args  # noqa: E402


def lift(arg, name):
    """Turns a one-argument predicate / function into the 26.x (level, x) -> form."""
    arg = arg.strip()
    m = re.fullmatch(r"this::(\w+)", arg)
    if m:
        return "(level, %s) -> this.%s(%s)" % (name, m.group(1), name)
    m = re.fullmatch(r"([A-Z]\w*)::(\w+)", arg)
    if m:
        return "(level, %s) -> %s.%s(%s)" % (name, m.group(1), m.group(2), name)
    m = re.fullmatch(r"\(?(\w+)\)?\s*->\s*(.*)", arg, re.S)
    if m:
        return "(level, %s) -> %s" % (m.group(1), m.group(2))
    return None


def rewrite(t, call, names):
    out, pos = [], 0
    for m in list(re.finditer(r"\b%s\(" % re.escape(call), t)):
        if m.start() < pos:
            continue
        try:
            args, end = split_args(t, m.end() - 1)
        except ValueError:
            continue
        if len(args) != len(names) or any(a.startswith("(level") for a in args):
            continue
        lifted = [lift(a, n) for a, n in zip(args, names)]
        if None in lifted:
            continue
        out.append(t[pos:m.start()])
        out.append("%s(%s)" % (call, ", ".join(lifted)))
        pos = end + 1
    out.append(t[pos:])
    return "".join(out)


def process(t):
    o = t
    t = rewrite(t, "StartAttacking.create", ["body", "body"])
    t = rewrite(t, "StopAttackingIfTargetInvalid.create", ["target"])
    t = rewrite(t, "StartAttacking.create", ["body"])
    # food is a data component
    t = re.sub(r"\b([\w.]+(?:\(\))?)\.getItem\(\)\.getFoodProperties\(\1, [\w.]+\)", r"\1.get(DataComponents.FOOD)", t)
    t = re.sub(r"\b([\w.]+(?:\(\))?)\.getFoodProperties\((?:null|[\w.]+)\)", r"\1.get(DataComponents.FOOD)", t)
    t = re.sub(r"\b([\w.]+(?:\(\))?)\.getDrinkingSound\(\)", r"PortUtil.consumeSound(\1)", t)
    # vanilla milk clears every effect, NeoForge 26.x has no cure registry
    t = re.sub(r"\b\w+\.getCures\(\)\.contains\(EffectCures\.MILK\)", "true", t)
    t = t.replace("import net.neoforged.neoforge.common.EffectCures;\n", "")
    t = re.sub(r"\.coldEnoughToSnow\((\w+)\)", r".coldEnoughToSnow(\1, maid.level().getSeaLevel())", t)
    t = re.sub(r"\bItemNameBlockItem\b", "BlockItem", t)
    t = t.replace("import net.minecraft.world.item.BlockItem;\nimport net.minecraft.world.item.BlockItem;", "import net.minecraft.world.item.BlockItem;")
    if t != o:
        if "DataComponents.FOOD" in t:
            t = ensure_import(t, "net.minecraft.core.component.DataComponents")
        if "PortUtil." in t:
            t = ensure_import(t, "com.github.tartaricacid.touhoulittlemaid.util.PortUtil")
        if re.search(r"\bBlockItem\b", t):
            t = ensure_import(t, "net.minecraft.world.item.BlockItem")
    return t


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
    print("tasks pass changed", n)


if __name__ == "__main__":
    main()
