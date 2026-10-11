"""Shows unique compiler errors (message + symbol + source line) for files matching a path substring."""
import re, sys
log, needle = sys.argv[1], sys.argv[2]
lines = open(log, encoding="utf-8", errors="ignore").read().split("\n")
seen = set()
for i, l in enumerate(lines):
    m = re.match(r"\s*(.+\.java):(\d+): error: (.*)", l)
    if not m or needle.lower() not in m.group(1).lower():
        continue
    key = (m.group(1), m.group(2), m.group(3))
    if key in seen:
        continue
    seen.add(key)
    sym = ""
    for j in range(i + 1, min(i + 5, len(lines))):
        if "symbol:" in lines[j]:
            sym = lines[j].strip().replace("symbol:", "").strip()
            break
    src = open(m.group(1), encoding="utf-8").read().split("\n")[int(m.group(2)) - 1].strip()
    f = re.split(r"[\/]", m.group(1))[-1]
    print("%s:%s | %s %s\n    %s" % (f, m.group(2), m.group(3)[:90], ("[" + sym + "]") if sym else "", src[:170]))
