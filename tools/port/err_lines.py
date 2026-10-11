"""Prints the source line for each compiler error matching a message substring, grouped by a regex capture."""
import re, sys
from collections import Counter
log, needle = sys.argv[1], sys.argv[2]
cap = re.compile(sys.argv[3]) if len(sys.argv) > 3 else None
seen, cnt, ex = set(), Counter(), {}
for line in open(log, encoding="utf-8", errors="ignore"):
    m = re.match(r"(.+\.java):(\d+): error: (.*)", line.strip())
    if not m or needle not in m.group(3) or (m.group(1), m.group(2)) in seen:
        continue
    seen.add((m.group(1), m.group(2)))
    src = open(m.group(1), encoding="utf-8").read().split("\n")
    # the @Override error points at the annotation; look at the next non-annotation line
    i = int(m.group(2)) - 1
    while i < len(src) - 1 and src[i].strip().startswith("@"):
        i += 1
    s = src[i].strip()
    key = (cap.search(s).group(1) if cap and cap.search(s) else s[:120])
    cnt[key] += 1
    ex.setdefault(key, "%s:%d" % (m.group(1).split("touhoulittlemaid\\")[-1], i + 1))
for k, v in cnt.most_common(int(sys.argv[4]) if len(sys.argv) > 4 else 80):
    print("%4d  %-60s %s" % (v, k, ex[k]))
