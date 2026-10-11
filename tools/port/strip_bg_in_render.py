"""Removes super.extractBackground(...) calls located inside extractRenderState methods (Screen draws it itself now)."""
import os, re, sys
n = 0
for d, _, fs in os.walk(sys.argv[1]):
    for f in fs:
        if not f.endswith(".java"):
            continue
        p = os.path.join(d, f); t = open(p, encoding="utf-8").read()
        out, cur = [], None
        for line in t.split("\n"):
            m = re.search(r"\bvoid (\w+)\(", line)
            if m and re.match(r"\s+(public|protected|private)", line):
                cur = m.group(1)
            if cur == "extractRenderState" and re.match(r"\s*super\.extractBackground\([^;]*\);\s*$", line):
                n += 1
                continue
            out.append(line)
        nt = "\n".join(out)
        if nt != t:
            open(p, "w", encoding="utf-8", newline="\n").write(nt)
print("removed", n)
