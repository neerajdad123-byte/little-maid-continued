"""One-off port patch: Block#onRemove (gone in MC 26.x) -> LegacyRemovalBlock#onRemoveLegacy + BlockEntity#preRemoveSideEffects."""
import re

B = "src/main/java/com/github/tartaricacid/touhoulittlemaid/"
PAIRS = {
    "BlockAltar": "TileEntityAltar",
    "BlockGarageKit": "TileEntityGarageKit",
    "BlockJoy": "TileEntityJoy",
    "BlockMaidBeacon": "TileEntityMaidBeacon",
    "BlockModelSwitcher": "TileEntityModelSwitcher",
    "BlockShrine": "TileEntityShrine",
}
SIG = re.compile(r"    @Override\n    public void onRemove\(BlockState state, Level (\w+), BlockPos pos, BlockState newState, boolean isMoving\) \{\n")


def imp(t, fqn):
    if "import %s;" % fqn in t:
        return t
    return re.sub(r"^(package [\w.]+;\n)", r"\1\nimport %s;" % fqn, t, count=1, flags=re.M)


for block, be in PAIRS.items():
    p = B + "block/%s.java" % block
    t = open(p, encoding="utf-8").read()
    m = SIG.search(t)
    assert m, block
    level = m.group(1)
    t = t[:m.start()] + (
        "    @Override\n"
        "    public void onRemoveLegacy(BlockState state, Level %s, BlockPos pos) {\n"
        "        // only called when the block itself is replaced, never when moved by a piston\n"
        "        BlockState newState = Blocks.AIR.defaultBlockState();\n"
        "        boolean isMoving = false;\n" % level) + t[m.end():]
    t = re.sub(r"\n[ \t]*super\.onRemove\(state, \w+, pos, newState, isMoving\);", "", t)
    t = re.sub(r"(public (?:abstract )?class %s\b[^{]*?)(\s*\{)" % block,
               lambda mm: mm.group(1) + (", LegacyRemovalBlock" if "implements" in mm.group(1) else " implements LegacyRemovalBlock") + mm.group(2),
               t, count=1)
    t = imp(t, "net.minecraft.world.level.block.Blocks")
    open(p, "w", encoding="utf-8", newline="\n").write(t)

    p = B + "tileentity/%s.java" % be
    t = open(p, encoding="utf-8").read()
    i = t.rindex("}")
    t = t[:i].rstrip() + (
        "\n\n    @Override\n"
        "    public void preRemoveSideEffects(BlockPos pos, BlockState state) {\n"
        "        super.preRemoveSideEffects(pos, state);\n"
        "        LegacyRemovalBlock.notify(this.level, pos, state);\n"
        "    }\n}\n")
    for fqn in ["com.github.tartaricacid.touhoulittlemaid.block.LegacyRemovalBlock", "net.minecraft.core.BlockPos",
                "net.minecraft.world.level.block.state.BlockState"]:
        t = imp(t, fqn)
    open(p, "w", encoding="utf-8", newline="\n").write(t)

# snack cabinet is a vanilla container: vanilla already drops its contents
p = B + "block/BlockSnackCabinet.java"
t = open(p, encoding="utf-8").read()
t = re.sub(r"    @Override\n    public void onRemove\(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving\) \{\n.*?\n    \}\n\n", "", t, flags=re.S)
open(p, "w", encoding="utf-8", newline="\n").write(t)
print("block removal patched")
