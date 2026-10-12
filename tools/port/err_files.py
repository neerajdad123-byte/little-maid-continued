"""Unique compiler errors per file, most first."""
import collections
import re
import sys

lines = set(l.strip() for l in open(sys.argv[1], encoding="utf-8", errors="ignore") if re.match(r"^C:.*: error:", l))
c = collections.Counter(re.sub(r":\d+: error:.*", "", l).split("touhoulittlemaid\\")[-1] for l in lines)
print(sum(c.values()), "errors in", len(c), "files")
for k, v in c.most_common(int(sys.argv[2]) if len(sys.argv) > 2 else 80):
    print(v, k)
