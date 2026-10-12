"""One-off port: 1.21.1 ingredient objects ({"item": x} / {"tag": x}) -> 1.21.2+ strings ("x" / "#x")."""
import json
import pathlib
import sys


def conv(ing, where):
    if isinstance(ing, list):
        out = [conv(i, where) for i in ing]
        if any(s.startswith("#") for s in out) and len(out) > 1:
            sys.exit("tag inside an ingredient list (not expressible in 26.x): %s" % where)
        return out
    if isinstance(ing, dict) and set(ing) == {"item"}:
        return ing["item"]
    if isinstance(ing, dict) and set(ing) == {"tag"}:
        return "#" + ing["tag"]
    if isinstance(ing, str):
        return ing
    sys.exit("unknown ingredient form in %s: %r" % (where, ing))


assert conv({"tag": "c:x"}, "t") == "#c:x" and conv([{"item": "a"}, {"item": "b"}], "t") == ["a", "b"]
n = 0
for root in sys.argv[1:]:
    for p in pathlib.Path(root).rglob("*.json"):
        d = json.loads(p.read_text(encoding="utf-8"))
        if not isinstance(d, dict) or not ("ingredients" in d or "key" in d) or "type" not in d:
            continue
        if "ingredients" in d:
            d["ingredients"] = [conv(i, p) for i in d["ingredients"]]
        if "key" in d:
            d["key"] = {k: conv(v, p) for k, v in d["key"].items()}
        p.write_text(json.dumps(d, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
        n += 1
print("converted", n)
