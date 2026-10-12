"""Deletes whole Java methods (with preceding annotations/javadoc) whose declaration line contains a given text.

usage: drop_method.py File.java "text in declaration" ["more"...]
"""
import re
import sys


def drop(src, needle):
    start = src.find(needle)
    if start < 0:
        sys.exit("not found: " + needle)
    line_start = src.rfind("\n", 0, start) + 1
    # pull in annotations / javadoc directly above
    head = line_start
    while True:
        prev_end = head - 1
        prev_start = src.rfind("\n", 0, prev_end) + 1
        prev = src[prev_start:prev_end].strip()
        if prev.startswith("@") or prev.startswith("*") or prev.startswith("/**") or prev.endswith("*/"):
            head = prev_start
        else:
            break
    brace = src.find("{", start)
    depth = 0
    i = brace
    while True:
        c = src[i]
        if c == "{":
            depth += 1
        elif c == "}":
            depth -= 1
            if depth == 0:
                break
        i += 1
    end = src.find("\n", i) + 1
    # swallow one blank line after
    if src[end:end + 1] == "\n":
        end += 1
    return src[:head] + src[end:]


assert drop("class A {\n    @X\n    void f() {\n        if (a) { b(); }\n    }\n\n    void g() {}\n}\n", "void f()") == "class A {\n    void g() {}\n}\n"

if __name__ == "__main__" and len(sys.argv) > 2:
    path = sys.argv[1]
    text = open(path, encoding="utf-8").read()
    for n in sys.argv[2:]:
        text = drop(text, n)
    open(path, "w", encoding="utf-8", newline="\n").write(text)
    print("dropped", len(sys.argv) - 2)
