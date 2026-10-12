"""Little Maid Continued porting helper: menu / slot / recipe API changes 1.21.1 -> 26.x.

Usage: python tools/port/fix_containers.py <java-src-dir>
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
from fix_gui import ensure_import  # noqa: E402


def process(t):
    o = t
    # Slot icons are GUI sprites now (no atlas pair)
    t = re.sub(r"public Pair<Identifier, Identifier> getNoItemIcon\(\)", "public Identifier getNoItemIcon()", t)
    t = re.sub(r"return Pair\.of\((?:InventoryMenu\.)?BLOCK_ATLAS, ([^;]+)\);", r"return \1;", t)
    t = t.replace("import static net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS;\n", "")
    t = re.sub(r"\bClickType\b", "ContainerInput", t)
    t = t.replace("import net.minecraft.world.inventory.ContainerInput;\nimport net.minecraft.world.inventory.ContainerInput;", "import net.minecraft.world.inventory.ContainerInput;")
    t = re.sub(r"\.getInventory\(\)\.selected\b", ".getInventory().getSelectedSlot()", t)
    t = re.sub(r"\.canInteractWithEntity\(", ".isWithinEntityInteractionRange(", t)
    t = re.sub(r"\.onCraftedBy\((\w+), (?:level|\w+Level|world), (\w+)\)", r".onCraftedBy(\1, \2)", t)
    t = re.sub(r"\.setRecipeUsed\((?:level|world), (\w+), (\w+)\)", r".setRecipeUsed(\1, \2)", t)
    t = re.sub(r"\.assemble\((\w+), [\w.]+\.registryAccess\(\)\)", r".assemble(\1)", t)
    t = re.sub(r"\.getBurnTime\((RecipeType\.\w+)\)", r".getBurnTime(\1, PortUtil.fuelValues())", t)
    # LivingEntity no longer exposes its last-equipment cache; vanilla detects the change itself
    t = re.sub(r"\n[ \t]*\w+\.setLast(?:Armor|Hand)Item\([^;]*\);", "", t)
    if t != o:
        if "ContainerInput" in t:
            t = ensure_import(t, "net.minecraft.world.inventory.ContainerInput")
        if "PortUtil." in t:
            t = ensure_import(t, "com.github.tartaricacid.touhoulittlemaid.util.PortUtil")
        if "Pair." not in t and "Pair<" not in t:
            t = t.replace("import com.mojang.datafixers.util.Pair;\n", "")
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
    print("containers pass changed", n)


if __name__ == "__main__":
    main()
