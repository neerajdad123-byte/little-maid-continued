"""Moves datagen out of the runtime build and keeps constants-only copies of the key classes runtime code uses.

The full providers stay in src/datagen_disabled until data generation is ported to MC 26.x.
"""
import os
import re
import shutil
import subprocess

SRC = "src/main/java/com/github/tartaricacid/touhoulittlemaid/datagen"
DST = "src/datagen_disabled/java/com/github/tartaricacid/touhoulittlemaid/datagen"
KEEP = ["EnchantmentKeys.java", "tag/TagBlock.java", "tag/TagEntity.java", "tag/TagItem.java"]

os.makedirs(os.path.dirname(DST), exist_ok=True)
subprocess.check_call(["git", "mv", SRC, DST])

for rel in KEEP:
    text = open(os.path.join(DST, rel), encoding="utf-8").read()
    pkg = re.search(r"^package [\w.]+;", text, re.M).group(0)
    imports = re.findall(r"^import [\w.*]+;", text, re.M)
    cls = re.search(r"public (?:final )?class (\w+)", text).group(1)
    # every top-level static final field, possibly spanning lines
    fields = re.findall(r"^    public static final [^;]+;", text, re.M | re.S)
    body = "\n".join(fields)
    used = [i for i in imports if re.search(r"\b%s\b" % re.escape(i.split(".")[-1].rstrip(";")), body)]
    out = "%s\n\n%s\n\n/**\n * Little Maid Continued: key constants used at runtime. The data generator for these lives in\n * src/datagen_disabled until it is ported to MC 26.x.\n */\npublic final class %s {\n%s\n\n    private %s() {\n    }\n}\n" % (
        pkg, "\n".join(used), cls, body, cls)
    dst = os.path.join(SRC, rel)
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    open(dst, "w", encoding="utf-8", newline="\n").write(out)
    print(rel, len(fields), "constants")
