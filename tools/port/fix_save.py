"""Little Maid Continued porting helper: CompoundTag save/load -> ValueInput/ValueOutput (26.x).

Keeps the original method bodies by bridging through PortUtil.readRoot / PortUtil.writeRoot.
Usage: python tools/port/fix_save.py <java-src-dir>
"""
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
from fix_gui import ensure_import  # noqa: E402


def body_span(t, brace):
    depth, i = 0, brace
    while i < len(t):
        if t[i] == "{":
            depth += 1
        elif t[i] == "}":
            depth -= 1
            if depth == 0:
                return i
        i += 1
    raise ValueError


def indent_body(body):
    return "\n".join(("    " + l) if l.strip() else l for l in body.split("\n"))


def rewrite(t, pat, make):
    while True:
        m = pat.search(t)
        if not m:
            return t
        brace = t.index("{", m.end() - 1)
        end = body_span(t, brace)
        new = make(m, t[brace + 1:end])
        t = t[:m.start()] + new + t[end + 1:]


def process(t):
    o = t
    # Entity#addAdditionalSaveData(CompoundTag)
    def save_entity(m, body):
        ind, tag = m.group("i"), m.group("tag")
        body = re.sub(r"\bsuper\.addAdditionalSaveData\(%s\)" % tag, "super.addAdditionalSaveData(valueOutput)", body)
        return ("%s%s void addAdditionalSaveData(ValueOutput valueOutput) {\n%s    CompoundTag %s = new CompoundTag();\n%s    try {%s\n%s    } finally {\n"
                "%s        PortUtil.writeRoot(valueOutput, %s);\n%s    }\n%s}"
                % (ind, m.group("mod"), ind, tag, ind, indent_body(body.rstrip()), ind, ind, tag, ind, ind))
    t = rewrite(t, re.compile(r"(?P<i>[ \t]*)(?P<mod>public|protected) void addAdditionalSaveData\(CompoundTag (?P<tag>\w+)\)\s*\{"), save_entity)

    def load_entity(m, body):
        ind, tag = m.group("i"), m.group("tag")
        body = re.sub(r"\bsuper\.readAdditionalSaveData\(%s\)" % tag, "super.readAdditionalSaveData(valueInput)", body)
        return ("%s%s void readAdditionalSaveData(ValueInput valueInput) {\n%s    CompoundTag %s = PortUtil.readRoot(valueInput);%s}"
                % (ind, m.group("mod"), ind, tag, body))
    t = rewrite(t, re.compile(r"(?P<i>[ \t]*)(?P<mod>public|protected) void readAdditionalSaveData\(CompoundTag (?P<tag>\w+)\)\s*\{"), load_entity)

    # BlockEntity#saveAdditional(CompoundTag, HolderLookup.Provider)
    def save_be(m, body):
        ind, tag, prov = m.group("i"), m.group("tag"), m.group("prov")
        body = re.sub(r"\bsuper\.saveAdditional\(%s, %s\)" % (tag, prov), "super.saveAdditional(valueOutput)", body)
        return ("%sprotected void saveAdditional(ValueOutput valueOutput) {\n%s    HolderLookup.Provider %s = PortUtil.registries();\n"
                "%s    CompoundTag %s = new CompoundTag();\n%s    try {%s\n%s    } finally {\n%s        PortUtil.writeRoot(valueOutput, %s);\n%s    }\n%s}"
                % (ind, ind, prov, ind, tag, ind, indent_body(body.rstrip()), ind, ind, tag, ind, ind))
    t = rewrite(t, re.compile(r"(?P<i>[ \t]*)(?:public|protected) void saveAdditional\(CompoundTag (?P<tag>\w+), (?:HolderLookup\.)?Provider (?P<prov>\w+)\)\s*\{"), save_be)

    def load_be(m, body):
        ind, tag, prov = m.group("i"), m.group("tag"), m.group("prov")
        body = re.sub(r"\bsuper\.loadAdditional\(%s, %s\)" % (tag, prov), "super.loadAdditional(valueInput)", body)
        return ("%sprotected void loadAdditional(ValueInput valueInput) {\n%s    HolderLookup.Provider %s = valueInput.lookup();\n"
                "%s    CompoundTag %s = PortUtil.readRoot(valueInput);%s}" % (ind, ind, prov, ind, tag, body))
    t = rewrite(t, re.compile(r"(?P<i>[ \t]*)(?:public|protected) void loadAdditional\(CompoundTag (?P<tag>\w+), (?:HolderLookup\.)?Provider (?P<prov>\w+)\)\s*\{"), load_be)

    if t != o:
        t = ensure_import(t, "com.github.tartaricacid.touhoulittlemaid.util.PortUtil")
        t = ensure_import(t, "net.minecraft.nbt.CompoundTag")
        if "ValueOutput" in t:
            t = ensure_import(t, "net.minecraft.world.level.storage.ValueOutput")
        if "ValueInput" in t:
            t = ensure_import(t, "net.minecraft.world.level.storage.ValueInput")
        if "HolderLookup.Provider" in t:
            t = ensure_import(t, "net.minecraft.core.HolderLookup")
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
    print("save pass changed", n)


if __name__ == "__main__":
    main()
