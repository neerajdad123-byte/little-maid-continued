"""Little Maid Continued porting helper: line-targeted fixes keyed on compiler error messages (not symbols).

Usage: python tools/port/fix_messages.py <build-log>
"""
import re
import sys
from collections import defaultdict

ESR = "net.minecraft.world.entity.EntitySpawnReason"

# message prefix -> (regex on the error line, replacement, import to add)
RULES = [
    ("no suitable method found for create(Level)", r"\.create\(([^(),]+(?:\(\))?)\)", r".create(\1, EntitySpawnReason.LOAD)", ESR),
    ("no suitable method found for create(ClientLevel)", r"\.create\(([^(),]+(?:\(\))?)\)", r".create(\1, EntitySpawnReason.LOAD)", ESR),
    ("no suitable method found for create(ServerLevel)", r"\.create\(([^(),]+(?:\(\))?)\)", r".create(\1, EntitySpawnReason.LOAD)", ESR),
    ("isClientSide has private access in Level", r"\.isClientSide\b(?!\()", ".isClientSide()", None),
    ("random has protected access in Level", r"\.random\b(?!\()", ".getRandom()", None),
    ("incompatible types: Optional<Reference<", r"\.get\(", ".getValue(", None),
    ("no suitable method found for text(Font,", r"\.text\((\w+), ([^,]+), ([^,]+), ([^,]+), ", r".text(\1, \2, (int) (\3), (int) (\4), ", None),
    ("incompatible types: Reference<SoundEvent> cannot be converted to SoundEvent", r"(SoundEvents\.\w+)(?!\.value\(\))", r"\1.value()", None),
]


def main():
    hits = defaultdict(list)
    for line in open(sys.argv[1], encoding="utf-8", errors="ignore"):
        m = re.match(r"\s*(.+\.java):(\d+): error: (.*)", line)
        if not m:
            continue
        for rule in RULES:
            if m.group(3).startswith(rule[0]):
                hits[m.group(1)].append((int(m.group(2)), rule))
    n = 0
    for path, items in hits.items():
        text = open(path, encoding="utf-8").read()
        lines = text.split("\n")
        imports = set()
        for ln, (_, a, b, imp) in set(items):
            new = re.sub(a, b, lines[ln - 1], count=1)
            if new != lines[ln - 1]:
                lines[ln - 1] = new
                n += 1
                if imp:
                    imports.add(imp)
        text = "\n".join(lines)
        for imp in imports:
            if "import %s;" % imp not in text:
                text = re.sub(r"^(package [\w.]+;\n)", r"\1\nimport %s;" % imp, text, count=1, flags=re.M)
        open(path, "w", encoding="utf-8", newline="\n").write(text)
    print("message fixes", n)


if __name__ == "__main__":
    main()
