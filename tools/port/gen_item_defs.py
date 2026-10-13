"""One-off port: writes MC 26.x item definitions (assets/<ns>/items/<id>.json) from the 1.21.1 item models.

- plain models            -> minecraft:model
- neoforge:separate_transforms -> minecraft:select on display_context (2D model for the listed perspectives)
- builtin/entity models   -> minecraft:special with the renderer registered in InitSpecialModelRenderers
- totem / XP bottle       -> minecraft:condition on touhou_little_maid:config (VanillaConfig texture swaps)
"""
import json
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parents[2]
NS = "touhou_little_maid"
MAIN = ROOT / "src/main/resources/assets" / NS
GEN = ROOT / "src/generated/resources/assets" / NS
SPECIAL = {"chair", "garage_kit", "picnic_basket", "entity_placeholder"}
# models the entity placeholder renders by recipe name (were standalone models in 1.21.1)
EXTRA = ["spawn_box", "reborn_maid", "spawn_lightning_bolt"]


def model_file(name):
    for base in (MAIN, GEN):
        p = base / "models/item" / (name + ".json")
        if p.exists():
            return p
    return None


def model(ref):
    return {"type": "minecraft:model", "model": ref}


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8", newline="\n")


def definition(name):
    p = model_file(name)
    if p is None:
        raise SystemExit("no item model for " + name)
    data = json.loads(p.read_text(encoding="utf-8"))
    ref = "%s:item/%s" % (NS, name)
    if data.get("loader") == "neoforge:separate_transforms":
        # 26.x picks models per display context in the item definition; split the old loader model
        cases = []
        for perspective, flat in data["perspectives"].items():
            flat_name = "%s_%s" % (name, perspective)
            write(p.parent / (flat_name + ".json"), flat)
            cases.append({"when": perspective, "model": model("%s:item/%s" % (NS, flat_name))})
        base = data["base"]["parent"]
        p.unlink()
        return {"model": {"type": "minecraft:select", "property": "minecraft:display_context",
                          "cases": cases, "fallback": model(base)}}
    if data.get("parent") == "builtin/entity":
        # builtin/entity is gone; the model keeps only its display transforms
        del data["parent"]
        write(p, data)
        return {"model": {"type": "minecraft:special", "base": ref, "model": {"type": "%s:%s" % (NS, name)}}}
    return {"model": model(ref)}


assert definition.__name__ == "definition"
items = re.findall(r'register\("([a-z0-9_]+)"', (ROOT / "src/main/java/com/github/tartaricacid/touhoulittlemaid/init/InitItems.java").read_text(encoding="utf-8"))
for name in items + EXTRA:
    d = definition(name)
    if name in SPECIAL:
        assert d["model"]["type"] == "minecraft:special", name
    write(MAIN / "items" / (name + ".json"), d)

for vanilla, replacement, option in [("totem_of_undying", "life_point", "replace_totem"),
                                     ("experience_bottle", "point_item", "replace_xp_bottle")]:
    write(ROOT / "src/main/resources/assets/minecraft/items" / (vanilla + ".json"), {"model": {
        "type": "minecraft:condition", "property": "%s:config" % NS, "option": option,
        "on_true": model("%s:item/%s" % (NS, replacement)),
        "on_false": model("minecraft:item/" + vanilla)}})
print("item definitions:", len(items) + len(EXTRA))
