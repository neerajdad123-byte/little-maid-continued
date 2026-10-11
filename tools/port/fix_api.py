"""Little Maid Continued porting helper: common game API changes 1.21.1 -> 26.x.

Usage: python tools/port/fix_api.py <java-src-dir> [build-log]
"""
import os
import re
import sys
from collections import defaultdict

sys.path.insert(0, os.path.dirname(__file__))
from fix_gui import split_args, ensure_import  # noqa: E402

PORT_UTIL = "com.github.tartaricacid.touhoulittlemaid.util.PortUtil"

GLOBAL = [
    (r"\bInteractionResult\.sidedSuccess\(\s*([^,()]+(?:\([^()]*\))?)\s*,\s*[^()]+(?:\([^()]*\))?\s*\)",
     r"InteractionResult.SUCCESS.heldItemTransformedTo(\1)"),
    (r"\bInteractionResult\.sidedSuccess\((?:[^()]|\([^()]*\))*\)", "InteractionResult.SUCCESS"),
    (r"\bInteractionResult\.success\(([^;]+?)\)(?=;)", r"InteractionResult.SUCCESS.heldItemTransformedTo(\1)"),
    (r"\bInteractionResult\.consume\(([^;]+?)\)(?=;)", r"InteractionResult.CONSUME.heldItemTransformedTo(\1)"),
    (r"\bInteractionResult\.pass\([^;]*?\)(?=;)", "InteractionResult.PASS"),
    (r"\bInteractionResult\.fail\([^;]*?\)(?=;)", "InteractionResult.FAIL"),
    (r"\bInteractionResult\.SKIP_DEFAULT_BLOCK_INTERACTION\b", "InteractionResult.PASS"),
    (r"\bInteractionResult\.PASS_TO_DEFAULT_BLOCK_INTERACTION\b", "InteractionResult.TRY_WITH_EMPTY_HAND"),
    (r"\bInteractionResult\.CONSUME_PARTIAL\b", "InteractionResult.CONSUME"),
    (r"\bInteractionResult<ItemStack>", "InteractionResult"),
    (r"\.getAllKeys\(\)", ".keySet()"),
    (r"\.contains\(([^,()]+(?:\([^()]*\))?), Tag\.TAG_\w+\)", r".contains(\1)"),
    (r"\.readResourceLocation\(\)", ".readIdentifier()"),
    (r"\.writeResourceLocation\(", ".writeIdentifier("),
    (r"\.getMinBuildHeight\(\)", ".getMinY()"),
    (r"\.getMaxBuildHeight\(\)", ".getMaxY() + 1"),
    (r"\.getDayTime\(\)", ".getOverworldClockTime()"),
    (r"(?:[\w.]+(?:\(\))?\.)+getProfiler\(\)", "Profiler.get()"),
    (r"\bRenderType\.entityCutoutNoCull\(", "RenderTypes.entityCutout("),
    (r"\bRenderTypes\.entityCutoutNoCull\(", "RenderTypes.entityCutout("),
    (r"\bChatFormatting\.(\w+)\.getColor\(\)", r"PortUtil.color(ChatFormatting.\1)"),
    (r"\bNbtUtils\.writeBlockPos\(", "PortUtil.writeBlockPos("),
    (r"\bNbtUtils\.readBlockPos\((\w+(?:\.\w+\(\))?), ([^()]+?)\)", r"\1.read(\2, BlockPos.CODEC)"),
    (r"\.putUUID\(([^,()]+), ([^;]+)\);", r".store(\1, UUIDUtil.CODEC, \2);"),
    (r"(?<![\w.])Screen\.hasShiftDown\(\)", "Minecraft.getInstance().hasShiftDown()"),
    (r"(?<![\w.])Screen\.hasControlDown\(\)", "Minecraft.getInstance().hasControlDown()"),
    (r"(?<![\w.])Screen\.hasAltDown\(\)", "Minecraft.getInstance().hasAltDown()"),
    (r"(?<![\w.])hasShiftDown\(\)", "Minecraft.getInstance().hasShiftDown()"),
    (r"\bFMLEnvironment\.dist\b", "FMLEnvironment.getDist()"),
    (r"\bFMLEnvironment\.production\b", "FMLEnvironment.isProduction()"),
    (r"\bsoundEvent\.getLocation\(\)", "soundEvent.location()"),
    (r"\.getLocation\(\)(?=\.(?:getPath|getNamespace|toString)\(\))", ".location()"),
]

IPN = re.compile(r"^\s*@IPN\w+(\([^)]*\))?\s*\n|^import org\.anti_ad\.mc\.ipn\.api\.[\w.]+;\n", re.M)

OPT_DEFAULTS = {
    "Optional<Integer> cannot be converted to int": ("getInt", "getIntOr", "0"),
    "Optional<String> cannot be converted to String": ("getString", "getStringOr", '""'),
    "Optional<Boolean> cannot be converted to boolean": ("getBoolean", "getBooleanOr", "false"),
    "Optional<Long> cannot be converted to long": ("getLong", "getLongOr", "0L"),
    "Optional<Float> cannot be converted to float": ("getFloat", "getFloatOr", "0F"),
    "Optional<Double> cannot be converted to double": ("getDouble", "getDoubleOr", "0D"),
    "Optional<Byte> cannot be converted to byte": ("getByte", "getByteOr", "(byte) 0"),
    "Optional<Short> cannot be converted to short": ("getShort", "getShortOr", "(short) 0"),
    "Optional<CompoundTag> cannot be converted to CompoundTag": ("getCompound", "getCompoundOrEmpty", None),
    "method getList in class CompoundTag cannot be applied to given types": ("getList", "getListOrEmpty", "DROP2"),
    "Optional<int[]> cannot be converted to int[]": ("getIntArray", "getIntArray", "ORELSE_INTARR"),
    "Optional<long[]> cannot be converted to long[]": ("getLongArray", "getLongArray", "ORELSE_LONGARR"),
}


def rewrite_line_calls(line, old, new, default):
    pat = re.compile(r"\.%s\(" % old)
    out, pos = [], 0
    for m in list(pat.finditer(line)):
        if m.start() < pos:
            continue
        try:
            args, end = split_args(line, m.end() - 1)
        except ValueError:
            continue
        if default is None:
            rep = ".%s(%s)" % (new, args[0])
        elif default == "DROP2":
            rep = ".%s(%s)" % (new, args[0])
        elif default == "ORELSE_INTARR":
            rep = ".%s(%s).orElse(new int[0])" % (new, args[0])
        elif default == "ORELSE_LONGARR":
            rep = ".%s(%s).orElse(new long[0])" % (new, args[0])
        else:
            if len(args) != 1:
                continue
            rep = ".%s(%s, %s)" % (new, args[0], default)
        out.append(line[pos:m.start()])
        out.append(rep)
        pos = end + 1
    out.append(line[pos:])
    return "".join(out)


def global_pass(src):
    n = 0
    for d, _, fs in os.walk(src):
        for f in fs:
            if not f.endswith(".java"):
                continue
            p = os.path.join(d, f)
            t = open(p, encoding="utf-8").read()
            o = t
            t = IPN.sub("", t)
            for a, b in GLOBAL:
                t = re.sub(a, b, t)
            t = re.sub(r"\.getUUID\((\"[^\"]+\"|[A-Z_]+)\)", r".read(\1, UUIDUtil.CODEC).orElseThrow()", t)
            t = re.sub(r"\.hasUUID\((\"[^\"]+\"|[A-Z_]+)\)", r".read(\1, UUIDUtil.CODEC).isPresent()", t)
            if t != o:
                if "PortUtil." in t:
                    t = ensure_import(t, PORT_UTIL)
                if "Profiler.get()" in t:
                    t = ensure_import(t, "net.minecraft.util.profiling.Profiler")
                if "RenderTypes." in t:
                    t = ensure_import(t, "net.minecraft.client.renderer.rendertype.RenderTypes")
                if "UUIDUtil.CODEC" in t:
                    t = ensure_import(t, "net.minecraft.core.UUIDUtil")
                if "BlockPos.CODEC" in t:
                    t = ensure_import(t, "net.minecraft.core.BlockPos")
                if "Minecraft.getInstance()" in t:
                    t = ensure_import(t, "net.minecraft.client.Minecraft")
                open(p, "w", encoding="utf-8", newline="\n").write(t)
                n += 1
    print("api global pass changed", n)


def targeted_pass(log):
    hits = defaultdict(set)
    for line in open(log, encoding="utf-8", errors="ignore"):
        m = re.match(r"(.+\.java):(\d+): error: (.*)", line.strip())
        if not m:
            continue
        for msg in OPT_DEFAULTS:
            if msg in m.group(3):
                hits[m.group(1)].add((int(m.group(2)), msg))
    n = 0
    for path, items in hits.items():
        lines = open(path, encoding="utf-8").read().split("\n")
        for ln, msg in items:
            old, new, default = OPT_DEFAULTS[msg]
            nl = rewrite_line_calls(lines[ln - 1], old, new, default)
            if nl != lines[ln - 1]:
                lines[ln - 1] = nl
                n += 1
        open(path, "w", encoding="utf-8", newline="\n").write("\n".join(lines))
    print("api targeted fixes", n)


if __name__ == "__main__":
    if len(sys.argv) > 2:
        targeted_pass(sys.argv[2])
    else:
        global_pass(sys.argv[1])
