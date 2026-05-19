#!/usr/bin/env python3
"""
Add explicit per-file UTF-8 overrides for every `.properties` file to each
Maven module's `.settings/org.eclipse.core.resources.prefs`.

Folder-level entries (`encoding//some/folder=UTF-8`) are NOT honoured by
Eclipse for `.properties` files: the JDT Properties content describer
returns ISO-8859-1, and that wins over any folder/project default. The
only reliable per-project remedy is a per-file entry:

    encoding//src/main/webapp/WEB-INF/conf/resources/foo.properties=UTF-8

For every Maven module (directory containing pom.xml), this script walks
the module tree, picks up every `.properties` file, and ensures a matching
entry exists in the module's resources.prefs. Existing entries with any
value are left untouched so manual overrides survive.

Usage:
    eclipse-properties-file-overrides.py [--apply] [--root DIR]
"""

from __future__ import annotations

import argparse
import os
import sys
from pathlib import Path


PRUNE_DIRS = {"target", "build", "node_modules", ".git", "bin", ".settings"}
PREFS_NAME = "org.eclipse.core.resources.prefs"


def find_module_dirs(root: Path) -> list[Path]:
    out: list[Path] = []
    for pom in root.rglob("pom.xml"):
        s = str(pom)
        if "/target/" in s or "/node_modules/" in s or "/.git/" in s:
            continue
        out.append(pom.parent)
    return out


def properties_in_module(module: Path) -> list[str]:
    """Return relative paths (with leading '/') of all .properties files under `module`,
    pruning nested Maven modules (subdirectories that contain their own pom.xml)."""
    result: list[str] = []
    module_resolved = module.resolve()
    for dirpath, dirs, files in os.walk(module):
        dirs[:] = [d for d in dirs if d not in PRUNE_DIRS]
        # Prune nested modules: any subdirectory that has its own pom.xml.
        dirs[:] = [d for d in dirs if not (Path(dirpath, d, "pom.xml").is_file())]
        for name in files:
            if name.endswith(".properties"):
                rel = Path(dirpath, name).resolve().relative_to(module_resolved)
                result.append("/" + rel.as_posix())
    return sorted(result)


def update_prefs(file: Path, entries: list[str], apply: bool) -> int:
    if not entries:
        return 0
    existing = (
        file.read_text(encoding="utf-8") if file.exists() else "eclipse.preferences.version=1\n"
    )
    lines = existing.splitlines()
    keys = {l.split("=", 1)[0] for l in lines if "=" in l}
    added = 0
    for path in entries:
        key = f"encoding/{path}"
        if key in keys:
            continue
        lines.append(f"{key}=UTF-8")
        added += 1
    if added == 0:
        return 0
    new_text = "\n".join(lines)
    if not new_text.endswith("\n"):
        new_text += "\n"
    if apply:
        file.parent.mkdir(parents=True, exist_ok=True)
        file.write_text(new_text, encoding="utf-8")
    return added


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--apply", action="store_true")
    ap.add_argument("--root", type=Path, default=Path.cwd())
    args = ap.parse_args()

    modules = find_module_dirs(args.root)
    touched = 0
    total_entries = 0
    for module in modules:
        entries = properties_in_module(module)
        if not entries:
            continue
        prefs = module / ".settings" / PREFS_NAME
        added = update_prefs(prefs, entries, args.apply)
        if added:
            touched += 1
            total_entries += added
            print(f"{prefs.relative_to(args.root)}: +{added}")

    print()
    print(f"Modules scanned: {len(modules)}")
    print(f"Modules touched: {touched}")
    print(f"Entries added:   {total_entries} ({'written' if args.apply else 'dry run'})")
    if total_entries and not args.apply:
        print("Re-run with --apply to write the changes above.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
