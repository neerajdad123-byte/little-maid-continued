"""Little Maid Continued porting helper: Block hook signatures 1.21.1 -> 26.x.

Usage: python tools/port/fix_blocks.py <java-src-dir>
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
from fix_gui import ensure_import  # noqa: E402

A = r"(?:@\w+ )?"


def process(t):
    o = t
    # updateShape(state, dir, neighbor, LevelAccessor, pos, neighborPos) -> 26.x order with ScheduledTickAccess
    m = re.search(r"public BlockState updateShape\(%sBlockState (\w+), %sDirection (\w+), %sBlockState (\w+),\s*%sLevelAccessor (\w+), %sBlockPos (\w+), %sBlockPos (\w+)\)" % (A, A, A, A, A, A), t)
    if m:
        s, d, n, lv, c, f = m.groups()
        t = t[:m.start()] + ("protected BlockState updateShape(BlockState %s, LevelReader %s, ScheduledTickAccess scheduledTicks, BlockPos %s, "
                             "Direction %s, BlockPos %s, BlockState %s, RandomSource random)" % (s, lv, c, d, f, n)) + t[m.end():]
        t = re.sub(r"\bsuper\.updateShape\(%s, %s, %s, %s, %s, %s\)" % (s, d, n, lv, c, f),
                   "super.updateShape(%s, %s, scheduledTicks, %s, %s, %s, %s, random)" % (s, lv, c, d, f, n), t)
        t = re.sub(r"\b%s\.scheduleTick\(" % lv, "scheduledTicks.scheduleTick(", t)
        for fqn in ["net.minecraft.world.level.LevelReader", "net.minecraft.world.level.ScheduledTickAccess", "net.minecraft.util.RandomSource"]:
            t = ensure_import(t, fqn)

    t = re.sub(r"public void onBlockExploded\(BlockState (\w+), Level (\w+), BlockPos (\w+), Explosion (\w+)\)",
               r"public void onBlockExploded(BlockState \1, ServerLevel \2, BlockPos \3, Explosion \4)", t)
    if "onBlockExploded(BlockState" in t and "ServerLevel " in t:
        t = ensure_import(t, "net.minecraft.server.level.ServerLevel")

    # pick block: NeoForge IBlockExtension#getCloneItemStack(LevelReader, BlockPos, BlockState, boolean, Player)
    m = re.search(r"public ItemStack getCloneItemStack\(%sBlockState (\w+), %sHitResult (\w+), %sLevelReader (\w+), %sBlockPos (\w+), %sPlayer (\w+)\)" % (A, A, A, A, A), t)
    if m:
        st, tg, lv, pos, pl = m.groups()
        t = t[:m.start()] + "public ItemStack getCloneItemStack(LevelReader %s, BlockPos %s, BlockState %s, boolean includeData, Player %s)" % (lv, pos, st, pl) + t[m.end():]
        t = re.sub(r"\bsuper\.getCloneItemStack\(%s, %s, %s, %s, %s\)" % (st, tg, lv, pos, pl), "super.getCloneItemStack(%s, %s, %s, includeData, %s)" % (lv, pos, st, pl), t)
        t = re.sub(r"\bsuper\.getCloneItemStack\((\w+), (\w+), (\w+)\)", r"super.getCloneItemStack(\1, \2, \3, includeData, %s)" % pl, t)

    t = re.sub(r"public void fallOn\(Level (\w+), BlockState (\w+), BlockPos (\w+), Entity (\w+), float (\w+)\)",
               r"public void fallOn(Level \1, BlockState \2, BlockPos \3, Entity \4, double \5)", t)
    t = re.sub(r"public void neighborChanged\(BlockState (\w+), Level (\w+), BlockPos (\w+), Block (\w+), BlockPos (\w+), boolean (\w+)\)",
               r"protected void neighborChanged(BlockState \1, Level \2, BlockPos \3, Block \4, @Nullable Orientation orientation, boolean \6)", t)
    if "@Nullable Orientation orientation" in t:
        t = ensure_import(t, "net.minecraft.world.level.redstone.Orientation")
    t = re.sub(r"public int getAnalogOutputSignal\(BlockState (\w+), Level (\w+), BlockPos (\w+)\)",
               r"protected int getAnalogOutputSignal(BlockState \1, Level \2, BlockPos \3, Direction direction)", t)
    if "Direction direction)" in t:
        t = ensure_import(t, "net.minecraft.core.Direction")
    t = re.sub(r"    @Override\n(    public void updateEntityAfterFallOn\()",
               r"    // TODO(Little Maid Continued): no longer a Block hook in MC 26.x (bed bounce)\n\1", t)
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
    print("blocks pass changed", n)


if __name__ == "__main__":
    main()
