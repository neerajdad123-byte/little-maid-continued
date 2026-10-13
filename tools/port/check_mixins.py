"""Flags mixin targets (methods, shadows, accessors, INVOKE targets) whose names no longer exist in the 26.2 sources.

usage: check_mixins.py <mixin dir> <mc-src dir>
A name hit is only a heuristic (overloads and descriptors are not compared), so read every flagged mixin.
"""
import pathlib
import re
import sys

mixin_dir, src_root = pathlib.Path(sys.argv[1]), pathlib.Path(sys.argv[2])


def source_of(fqn):
    p = src_root / (fqn.split("$")[0].replace(".", "/") + ".java")
    return p.read_text(encoding="utf-8", errors="ignore") if p.exists() else None


bad = 0
for f in sorted(mixin_dir.rglob("*.java")):
    t = f.read_text(encoding="utf-8")
    m = re.search(r"@Mixin\((?:value\s*=\s*)?(\w+(?:\.\w+)*)\.class", t)
    if not m:
        continue
    simple = m.group(1).split(".")[-1]
    imp = re.search(r"import ([\w.]+\.%s);" % re.escape(simple), t)
    fqn = imp.group(1) if imp else None
    src = source_of(fqn) if fqn else None
    if src is None:
        print("%s: target %s source not found" % (f.name, fqn or simple))
        bad += 1
        continue
    names = set()
    names |= {n for n in re.findall(r'method\s*=\s*"([\w<>$]+)', t) if not n.startswith("lambda$") and n != "<init>"}
    names |= set(re.findall(r'@(?:Accessor|Invoker)\((?:value\s*=\s*)?"(\w+)"', t))
    names |= set(re.findall(r"@Shadow[^;{]*?\s(\w+)\s*[;(]", t, flags=re.S))
    for n in sorted(names):
        if not re.search(r"\b%s\b" % re.escape(n), src):
            print("%s: %s.%s missing" % (f.name, simple, n))
            bad += 1
    for owner, n in re.findall(r'target\s*=\s*"L([\w/$]+);(\w+)\(', t):
        osrc = source_of(owner.replace("/", "."))
        if osrc is not None and not re.search(r"\b%s\b" % re.escape(n), osrc):
            print("%s: INVOKE %s.%s missing" % (f.name, owner.split("/")[-1], n))
            bad += 1
print("flagged", bad)
