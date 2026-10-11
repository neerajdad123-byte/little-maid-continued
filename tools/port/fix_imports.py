"""Little Maid Continued porting helper.

Rewrites imports of Minecraft / NeoForge classes that moved packages, and applies
known class renames, using an index built from the decompiled target-version sources.

Usage: python tools/port/fix_imports.py <mc-sources-dir> <java-src-dir>
"""
import os
import re
import sys
from collections import defaultdict

# Old simple name -> new fully qualified name (class renamed, not only moved)
RENAMES = {
    "net.minecraft.resources.ResourceLocation": "net.minecraft.resources.Identifier",
    "net.minecraft.client.gui.GuiGraphics": "net.minecraft.client.gui.GuiGraphicsExtractor",
    "net.minecraft.world.ItemInteractionResult": "net.minecraft.world.InteractionResult",
    "net.minecraft.world.InteractionResultHolder": "net.minecraft.world.InteractionResult",
    "net.minecraft.world.item.UseAnim": "net.minecraft.world.item.ItemUseAnimation",
    "net.minecraft.world.entity.MobSpawnType": "net.minecraft.world.entity.EntitySpawnReason",
    "net.minecraft.client.renderer.LightTexture": "net.minecraft.util.LightCoordsUtil",
    "net.minecraft.util.FastColor": "net.minecraft.util.ARGB",
    "net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent":
        "net.neoforged.neoforge.client.event.AddClientReloadListenersEvent",
    "net.minecraft.client.gui.LayeredDraw": "net.neoforged.neoforge.client.gui.GuiLayer",
    "net.minecraft.world.level.block.state.properties.DirectionProperty":
        "net.minecraft.world.level.block.state.properties.EnumProperty",
    "net.minecraft.world.item.ItemNameBlockItem": "net.minecraft.world.item.BlockItem",
    "javax.annotation.ParametersAreNonnullByDefault": "org.jspecify.annotations.NullMarked",
}

ROOTS = ("net.minecraft.", "net.neoforged.", "com.mojang.")


def build_index(src_root):
    index = defaultdict(set)
    known = set()
    for dirpath, _, files in os.walk(src_root):
        for f in files:
            if not f.endswith(".java"):
                continue
            rel = os.path.relpath(os.path.join(dirpath, f), src_root)
            fqn = rel[:-5].replace(os.sep, ".")
            known.add(fqn)
            index[f[:-5]].add(fqn)
            # nested public types
            text = open(os.path.join(dirpath, f), encoding="utf-8", errors="ignore").read()
            for m in re.finditer(r"^\s+(?:public |protected )?(?:static )?(?:final )?(?:abstract )?(?:sealed )?"
                                 r"(?:class|interface|enum|record) (\w+)", text, re.M):
                nested = fqn + "." + m.group(1)
                known.add(nested)
    return index, known


def resolve(fqn, index, known):
    if fqn in known:
        return fqn
    if fqn in RENAMES:
        return RENAMES[fqn]
    parts = fqn.split(".")
    # nested class import: Outer.Inner
    for i in range(len(parts) - 1, 0, -1):
        outer = ".".join(parts[:i])
        if outer in known or outer in RENAMES:
            new_outer = resolve(outer, index, known)
            if new_outer:
                return new_outer + "." + ".".join(parts[i:])
            return None
    simple = parts[-1]
    cands = index.get(simple, set())
    if len(cands) == 1:
        return next(iter(cands))
    if len(cands) > 1:
        # prefer candidate sharing the longest package prefix
        def score(c):
            cp = c.split(".")
            n = 0
            for a, b in zip(cp, parts):
                if a != b:
                    break
                n += 1
            return n
        best = sorted(cands, key=score, reverse=True)
        if score(best[0]) > score(best[1]):
            return best[0]
    return None


def main():
    mc_src, java_src = sys.argv[1], sys.argv[2]
    index, known = build_index(mc_src)
    unresolved = defaultdict(int)
    changed_files = 0
    simple_renames = {old.rsplit(".", 1)[1]: new.rsplit(".", 1)[1]
                      for old, new in RENAMES.items() if old.rsplit(".", 1)[1] != new.rsplit(".", 1)[1]}
    for dirpath, _, files in os.walk(java_src):
        for f in files:
            if not f.endswith(".java"):
                continue
            path = os.path.join(dirpath, f)
            text = open(path, encoding="utf-8", newline="").read()
            orig = text
            renamed_here = set()

            def fix(m):
                static, fqn = m.group(1) or "", m.group(2)
                if not fqn.startswith(ROOTS) and fqn not in RENAMES:
                    return m.group(0)
                target = fqn
                member = ""
                if static:
                    target, member = fqn.rsplit(".", 1)
                    member = "." + member
                if target.endswith(".*"):
                    return m.group(0)
                new = resolve(target, index, known)
                if new is None:
                    unresolved[target] += 1
                    return m.group(0)
                if new != target:
                    old_simple, new_simple = target.rsplit(".", 1)[1], new.rsplit(".", 1)[1]
                    if old_simple != new_simple:
                        renamed_here.add(old_simple)
                return "import %s%s%s;" % (static, new, member)

            text = re.sub(r"import (static )?([\w.]+(?:\.\*)?);", fix, text)
            for old_simple in renamed_here:
                new_simple = simple_renames.get(old_simple)
                if new_simple:
                    text = re.sub(r"(?<![\w.])%s(?!\w)" % re.escape(old_simple), new_simple, text)
            # imports may now be duplicated after renames
            seen = set()
            lines = []
            for line in text.split("\n"):
                if line.startswith("import "):
                    if line in seen:
                        continue
                    seen.add(line)
                lines.append(line)
            text = "\n".join(lines)
            if text != orig:
                open(path, "w", encoding="utf-8", newline="").write(text)
                changed_files += 1
    print("changed files:", changed_files)
    for k, v in sorted(unresolved.items(), key=lambda kv: -kv[1]):
        print("UNRESOLVED %4d %s" % (v, k))


if __name__ == "__main__":
    main()
