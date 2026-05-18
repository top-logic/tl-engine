#!/usr/bin/env python3
"""
Convert Java source files from ISO-8859-1 to UTF-8.

Strategy per file:
  1. Read raw bytes.
  2. If pure ASCII (all bytes < 0x80): no conversion needed; skip silently.
  3. Else try to decode strictly as UTF-8:
       - If it succeeds AND the file contains non-ASCII bytes, the file is
         already UTF-8 (or accidentally contains UTF-8 sequences). Skip
         and report under "already-utf8".
       - If decoding as UTF-8 fails, the file is genuine ISO-8859-1.
         Decode as ISO-8859-1 (cannot fail) and rewrite as UTF-8.

By default this is a dry run. Pass --apply to actually rewrite files.

Usage:
    convert-java-to-utf8.py [--apply] [--root DIR] [PATH ...]

If no PATHs are given, the script walks --root (default: CWD) and
processes every *.java file under it. Otherwise each PATH is treated
as either a single file or a directory to walk.
"""

from __future__ import annotations

import argparse
import os
import sys
from pathlib import Path


def classify(data: bytes) -> str:
    """Return one of: 'ascii', 'already-utf8', 'iso8859-1'."""
    if all(b < 0x80 for b in data):
        return "ascii"
    try:
        data.decode("utf-8")
    except UnicodeDecodeError:
        return "iso8859-1"
    return "already-utf8"


def iter_java_files(paths: list[Path]) -> list[Path]:
    out: list[Path] = []
    for p in paths:
        if p.is_file():
            if p.suffix == ".java":
                out.append(p)
        elif p.is_dir():
            for root, dirs, files in os.walk(p):
                # Prune common build/output dirs.
                dirs[:] = [d for d in dirs if d not in {
                    "target", "build", "node_modules", ".git", "bin",
                }]
                for name in files:
                    if name.endswith(".java"):
                        out.append(Path(root) / name)
    return out


def convert(file: Path, apply: bool) -> str:
    data = file.read_bytes()
    kind = classify(data)
    if kind == "ascii" or kind == "already-utf8":
        return kind
    text = data.decode("iso-8859-1")
    encoded = text.encode("utf-8")
    if apply:
        file.write_bytes(encoded)
    return "converted"


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--apply", action="store_true",
                    help="actually rewrite files (default: dry run)")
    ap.add_argument("--root", type=Path, default=Path.cwd(),
                    help="root directory to walk if no PATHs are given")
    ap.add_argument("--verbose", "-v", action="store_true",
                    help="list every classified file")
    ap.add_argument("paths", nargs="*", type=Path,
                    help="files or directories to process")
    args = ap.parse_args()

    targets = args.paths if args.paths else [args.root]
    files = iter_java_files(targets)

    counts = {"ascii": 0, "already-utf8": 0, "converted": 0}
    already_utf8: list[Path] = []
    converted: list[Path] = []

    for f in files:
        kind = convert(f, args.apply)
        counts[kind] += 1
        if kind == "already-utf8":
            already_utf8.append(f)
        elif kind == "converted":
            converted.append(f)
        if args.verbose:
            print(f"{kind:14s} {f}")

    print()
    print(f"Scanned:        {len(files)} .java files")
    print(f"Pure ASCII:     {counts['ascii']}")
    print(f"Already UTF-8:  {counts['already-utf8']}  (skipped)")
    print(f"Converted:      {counts['converted']}  ({'written' if args.apply else 'dry run'})")

    if already_utf8:
        print()
        print("Files already containing UTF-8 byte sequences (skipped):")
        for f in already_utf8:
            print(f"  {f}")

    if converted and not args.apply:
        print()
        print("Re-run with --apply to rewrite the files above.")

    return 0


if __name__ == "__main__":
    sys.exit(main())
