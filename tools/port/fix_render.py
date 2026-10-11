"""Little Maid Continued porting helper: moves mod renderers/models onto the legacy render bridge.

Usage: python tools/port/fix_render.py <java-src-dir>
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
from fix_gui import ensure_import  # noqa: E402

L = "com.github.tartaricacid.touhoulittlemaid.client.renderer.legacy."

# vanilla import -> (legacy simple name, legacy fqn)
SWAPS = {
    "net.minecraft.client.model.EntityModel": ("EntityModel", "LegacyEntityModel"),
    "net.minecraft.client.model.Model": ("Model", "LegacyModel"),
    "net.minecraft.client.renderer.entity.MobRenderer": ("MobRenderer", "LegacyMobRenderer"),
    "net.minecraft.client.renderer.entity.LivingEntityRenderer": ("LivingEntityRenderer", "LegacyLivingEntityRenderer"),
    "net.minecraft.client.renderer.entity.EntityRenderer": ("EntityRenderer", "LegacyEntityRenderer"),
    "net.minecraft.client.renderer.entity.layers.RenderLayer": ("RenderLayer", "LegacyRenderLayer"),
    "net.minecraft.client.renderer.entity.RenderLayerParent": ("RenderLayerParent", "LegacyRenderLayerParent"),
}

RENAMED_FACTORIES = {
    "entityCutoutNoCull": "entityCutout",
    "entityCutoutNoCullZOffset": "entityCutoutZOffset",
    "itemEntityTranslucentCull": "entityTranslucentCullItemTarget",
    "entityTranslucentCull": "entityTranslucentCullItemTarget",
}


def process(t):
    o = t
    for fqn, (old, new) in SWAPS.items():
        imp = "import %s;" % fqn
        if imp not in t:
            continue
        t = t.replace(imp, "import %s%s;" % (L, new))
        # only rename the simple name where it is used as a type (followed by < or whitespace + identifier/{)
        t = re.sub(r"(?<![\w.])%s(?=\s*<|\s+\w|\s*\{|\s*\)|\s*,|\s*>|::)" % old, new, t)
    # RenderType static factories moved to RenderTypes in 26.x
    if re.search(r"(?<![\w.])RenderType(\.|::)[a-z]", t):
        def fac(m):
            name = RENAMED_FACTORIES.get(m.group(2), m.group(2))
            return "RenderTypes%s%s" % (m.group(1), name)
        t = re.sub(r"(?<![\w.])RenderType(\.|::)([a-z]\w*)", fac, t)
        t = ensure_import(t, "net.minecraft.client.renderer.rendertype.RenderTypes")
    t = re.sub(r"(?<![\w.])RenderTypes(\.|::)(entityCutoutNoCull|itemEntityTranslucentCull|entityTranslucentCull)\b",
               lambda m: "RenderTypes%s%s" % (m.group(1), RENAMED_FACTORIES[m.group(2)]), t)
    return t


def main():
    n = 0
    for d, _, fs in os.walk(sys.argv[1]):
        if "renderer" + os.sep + "legacy" in d or "renderer" + os.sep + "bridge" in d:
            continue
        for f in fs:
            if f.endswith(".java"):
                p = os.path.join(d, f)
                t = open(p, encoding="utf-8").read()
                nt = process(t)
                if nt != t:
                    open(p, "w", encoding="utf-8", newline="\n").write(nt)
                    n += 1
    print("render pass changed", n)


if __name__ == "__main__":
    main()
