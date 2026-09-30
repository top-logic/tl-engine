#!/usr/bin/env python3
"""
Remove obsolete ISO-8859-1 encoding overrides from Eclipse
.settings/org.eclipse.core.resources.prefs files.

After the project default has been flipped to UTF-8 (encoding/<project>=UTF-8),
the remaining per-path `=ISO-8859-1` overrides are stale: they tell Eclipse to
treat individual files (config snippets, factorypath, .gitignore, the JDT
prefs files, resource folders, *.launch files, metaConf.txt) as ISO-8859-1
even though the project as a whole is now UTF-8.

This script removes those obsolete entries so Eclipse falls back to the project
default. Lines that are still meaningful (e.g. unrelated `=UTF-8` entries) are
preserved verbatim.

Usage:
    eclipse-encoding-cleanup.py [--apply] [PATH ...]

If no PATHs are given, walks the current directory tree.
"""

from __future__ import annotations

import argparse
import os
import re
import sys
from pathlib import Path


_LINE = re.compile(r"^encoding/.+=ISO-8859-1\s*$")


def iter_prefs(paths: list[Path]) -> list[Path]:
    out: list[Path] = []
    for p in paths:
        if p.is_file() and p.name == "org.eclipse.core.resources.prefs":
            out.append(p)
        elif p.is_dir():
            for root, dirs, files in os.walk(p):
                dirs[:] = [d for d in dirs if d not in {"target", "build", "node_modules", ".git"}]
                for name in files:
                    if name == "org.eclipse.core.resources.prefs":
                        out.append(Path(root) / name)
    return out


def clean(file: Path, apply: bool) -> tuple[int, list[str]]:
    text = file.read_text(encoding="utf-8")
    removed: list[str] = []
    kept_lines: list[str] = []
    for line in text.splitlines():
        if _LINE.match(line):
            removed.append(line)
        else:
            kept_lines.append(line)
    if not removed:
        return 0, []
    new_text = "\n".join(kept_lines)
    if text.endswith("\n"):
        new_text += "\n"
    if apply:
        file.write_text(new_text, encoding="utf-8")
    return len(removed), removed


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--apply", action="store_true", help="rewrite files (default: dry run)")
    ap.add_argument("paths", nargs="*", type=Path)
    args = ap.parse_args()

    targets = args.paths if args.paths else [Path.cwd()]
    files = iter_prefs(targets)

    total_removed = 0
    touched = 0
    for f in files:
        n, removed = clean(f, args.apply)
        if n:
            touched += 1
            total_removed += n
            print(f"{f}: -{n}")
            for line in removed:
                print(f"   {line}")

    print()
    print(f"Files scanned:  {len(files)}")
    print(f"Files touched:  {touched}")
    print(f"Entries removed: {total_removed} ({'written' if args.apply else 'dry run'})")
    if total_removed and not args.apply:
        print("Re-run with --apply to write the changes above.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
