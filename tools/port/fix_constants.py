"""Little Maid Continued porting helper: vanilla constants renamed or regrouped between MC 1.21.1 and 26.x.

Usage: python tools/port/fix_constants.py <mc-sources-dir> <java-src-dir>
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
from fix_gui import ensure_import  # noqa: E402

COLORS = ["WHITE", "ORANGE", "MAGENTA", "LIGHT_BLUE", "YELLOW", "LIME", "PINK", "GRAY", "LIGHT_GRAY",
          "CYAN", "PURPLE", "BLUE", "BROWN", "GREEN", "RED", "BLACK"]

SIMPLE = {
    # game rules
    "GameRules.RULE_KEEPINVENTORY": "GameRules.KEEP_INVENTORY",
    "GameRules.RULE_SHOWDEATHMESSAGES": "GameRules.SHOW_DEATH_MESSAGES",
    "GameRules.RULE_DOENTITYDROPS": "GameRules.ENTITY_DROPS",
    "GameRules.RULE_DOMOBLOOT": "GameRules.MOB_DROPS",
    "GameRules.RULE_MOBGRIEFING": "GameRules.MOB_GRIEFING",
    # mob effects (renamed in 1.21.5)
    "MobEffects.MOVEMENT_SPEED": "MobEffects.SPEED",
    "MobEffects.MOVEMENT_SLOWDOWN": "MobEffects.SLOWNESS",
    "MobEffects.DAMAGE_BOOST": "MobEffects.STRENGTH",
    "MobEffects.DAMAGE_RESISTANCE": "MobEffects.RESISTANCE",
    "MobEffects.HEAL": "MobEffects.INSTANT_HEALTH",
    "MobEffects.HARM": "MobEffects.INSTANT_DAMAGE",
    "MobEffects.JUMP": "MobEffects.JUMP_BOOST",
    "MobEffects.CONFUSION": "MobEffects.NAUSEA",
    "MobEffects.DIG_SPEED": "MobEffects.HASTE",
    "MobEffects.DIG_SLOWDOWN": "MobEffects.MINING_FATIGUE",
    # path types
    "PathType.DANGER_FIRE": "PathType.FIRE_IN_NEIGHBOR",
    "PathType.DAMAGE_FIRE": "PathType.FIRE",
    "PathType.DANGER_OTHER": "PathType.DAMAGING_IN_NEIGHBOR",
    "PathType.DAMAGE_OTHER": "PathType.DAMAGING",
    "PathType.DANGER_POWDER_SNOW": "PathType.ON_TOP_OF_POWDER_SNOW",
    "PathType.DANGER_TRAPDOOR": "PathType.ON_TOP_OF_TRAPDOOR",
    # misc
    "EntitySpawnReason.SPAWN_EGG": "EntitySpawnReason.SPAWN_ITEM_USE",
    "MobSpawnType.": "EntitySpawnReason.",
    "RenderShape.ENTITYBLOCK_ANIMATED": "RenderShape.INVISIBLE",
    "InventoryMenu.BLOCK_ATLAS": "TextureAtlas.LOCATION_BLOCKS",
    "EntityEvent.TALISMAN_ACTIVATE": "EntityEvent.PROTECTED_FROM_DEATH",
}

COLOR_COLLECTIONS = None


def collections(mc_src, cls):
    path = os.path.join(mc_src, "net", "minecraft", "world", *(["level", "block"] if cls == "Blocks" else ["item"]), cls + ".java")
    text = open(path, encoding="utf-8").read()
    return set(re.findall(r"public static final ColorCollection<\w+> (\w+) =", text))


def main():
    mc_src, src = sys.argv[1], sys.argv[2]
    entity_types = set(re.findall(r"public static final EntityType<[^>]*> (\w+) =",
                                  open(os.path.join(mc_src, "net/minecraft/world/entity/EntityTypes.java"), encoding="utf-8").read()))
    cols = {"Blocks": collections(mc_src, "Blocks"), "Items": collections(mc_src, "Items")}
    color_re = re.compile(r"\b(Blocks|Items)\.(%s)_(\w+)\b" % "|".join(sorted(COLORS, key=len, reverse=True)))
    n = 0
    for d, _, fs in os.walk(src):
        for f in fs:
            if not f.endswith(".java"):
                continue
            p = os.path.join(d, f)
            t = open(p, encoding="utf-8").read()
            o = t
            for a, b in SIMPLE.items():
                t = re.sub(r"(?<![\w.])" + re.escape(a) + (r"\b" if not a.endswith(".") else ""), b, t)

            def color(m):
                cls, c, rest = m.groups()
                if rest in cols[cls]:
                    return "%s.%s.pick(DyeColor.%s)" % (cls, rest, c)
                return m.group(0)
            t = color_re.sub(color, t)
            t = re.sub(r"(?<![\w.])EntityType\.([A-Z][A-Z0-9_]+)\b",
                       lambda m: "EntityTypes." + m.group(1) if m.group(1) in entity_types else m.group(0), t)
            if t != o:
                if "DyeColor." in t:
                    t = ensure_import(t, "net.minecraft.world.item.DyeColor")
                if "EntityTypes." in t:
                    t = ensure_import(t, "net.minecraft.world.entity.EntityTypes")
                if "TextureAtlas.LOCATION_BLOCKS" in t:
                    t = ensure_import(t, "net.minecraft.client.renderer.texture.TextureAtlas")
                if "EntitySpawnReason." in t:
                    t = ensure_import(t, "net.minecraft.world.entity.EntitySpawnReason")
                open(p, "w", encoding="utf-8", newline="\n").write(t)
                n += 1
    print("constants pass changed", n)


if __name__ == "__main__":
    main()
