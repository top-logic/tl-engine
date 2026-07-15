#!/usr/bin/env bash
#
# Find all reactor modules whose installed artifact in the local Maven
# repository is stale, then rebuild just those modules (and everything that
# transitively depends on them) in the correct order via `mvn install -pl ...`.
#
# A module is considered stale when EITHER:
#   (a) its own sources (src/ + pom.xml) are newer than its installed jar, or
#       the jar is missing  -- "source-stale"; or
#   (b) any reactor module it depends on is stale, or has a newer jar than
#       this module's jar   -- "dependency-stale" (cross-module API drift).
#
# (b) is what a naive per-module mtime check misses: e.g. model.search's
# sources change and it gets rebuilt, but service.openapi.server -- whose own
# sources are untouched -- is still compiled against the OLD SearchExpression
# API. Its jar is newer than its own sources, so a local check calls it fresh,
# yet it must be rebuilt against the new model.search jar.
#
# Usage:
#   ./rebuild-stale.sh            # detect and rebuild
#   ./rebuild-stale.sh --dry-run  # only list stale modules
#
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

DRY_RUN=0
[[ "${1:-}" == "--dry-run" ]] && DRY_RUN=1

OUT="$(mktemp)"
trap 'rm -f "$OUT"' EXIT

echo ">>> Enumerating reactor modules via Maven ..." >&2
# One exec:exec call at the root walks the entire reactor (including nested
# aggregators like tl-parent-engine). Each reactor module prints one line.
# The @@MOD prefix lets us grep it out reliably from any surrounding noise.
mvn -B -q exec:exec \
    -Dexec.executable=echo \
    -Dexec.args='@@MOD ${project.groupId} ${project.artifactId} ${project.version} ${project.packaging} ${settings.localRepository} ${project.basedir}' \
    > "$OUT"

# The transitive staleness computation is graph work; do it in Python.
# It prints stale module paths (relative to ROOT), one per line, to stdout,
# and a human-readable summary to stderr.
MAPFILE="$(mktemp)"
trap 'rm -f "$OUT" "$MAPFILE"' EXIT

ROOT="$ROOT" python3 - "$OUT" > "$MAPFILE" <<'PY'
import os, sys, re, xml.etree.ElementTree as ET

root = os.environ["ROOT"]
out = sys.argv[1]

# --- 1. Parse the reactor enumeration ------------------------------------
mods = {}            # artifactId -> dict(g,a,v,pkg,repo,basedir,jar,src_mtime,jar_mtime)
for line in open(out):
    p = line.split()
    if len(p) < 7 or p[0] != "@@MOD":
        continue
    _, g, a, v, pkg, repo, basedir = p[:7]
    mods[a] = dict(g=g, a=a, v=v, pkg=pkg, repo=repo, basedir=basedir)

reactor_artifacts = set(mods)

def newest_mtime(*paths):
    m = 0.0
    for base in paths:
        if os.path.isfile(base):
            m = max(m, os.path.getmtime(base))
        for dirpath, _dirs, files in os.walk(base):
            for f in files:
                try:
                    m = max(m, os.path.getmtime(os.path.join(dirpath, f)))
                except OSError:
                    pass
    return m

# --- 2. Compute mtimes and parse direct reactor dependencies -------------
NS = re.compile(r"\{.*\}")          # strip XML namespace
deps = {}                            # artifactId -> set(reactor artifactIds it depends on)

for a, m in mods.items():
    if m["pkg"] == "pom":            # aggregators produce no jar
        continue
    g, v, repo, basedir = m["g"], m["v"], m["repo"], m["basedir"]
    m["jar"] = os.path.join(repo, *g.split("."), a, v, f"{a}-{v}.jar")
    m["src_mtime"] = newest_mtime(os.path.join(basedir, "pom.xml"),
                                  os.path.join(basedir, "src"))
    m["jar_mtime"] = os.path.getmtime(m["jar"]) if os.path.isfile(m["jar"]) else None

    edges = set()
    pom = os.path.join(basedir, "pom.xml")
    try:
        tree = ET.parse(pom)
        for dep in tree.iter():
            if NS.sub("", dep.tag) != "dependency":
                continue
            aid = None
            for ch in dep:
                if NS.sub("", ch.tag) == "artifactId":
                    aid = (ch.text or "").strip()
            # Match by artifactId: reactor artifactIds are unique, so this is
            # robust even when the dependency's groupId is a ${property}.
            if aid in reactor_artifacts:
                edges.add(aid)
    except ET.ParseError:
        pass
    deps[a] = edges

jarred = [a for a, m in mods.items() if m["pkg"] != "pom"]

# --- 3. Seed source-stale set --------------------------------------------
stale = set()
reason = {}
for a in jarred:
    m = mods[a]
    if m["jar_mtime"] is None:
        stale.add(a); reason[a] = "no jar"
    elif m["src_mtime"] > m["jar_mtime"]:
        stale.add(a); reason[a] = "sources changed"

# --- 4. Propagate to a fixpoint over the dependency graph ----------------
# M is stale if a dependency D is stale, OR D's jar is newer than M's jar
# (D was rebuilt after M -> M links against a stale D).
changed = True
while changed:
    changed = False
    for a in jarred:
        if a in stale:
            continue
        m = mods[a]
        for d in deps.get(a, ()):
            dm = mods.get(d)
            if dm is None or dm["pkg"] == "pom":
                continue
            newer_dep = (dm["jar_mtime"] is not None and m["jar_mtime"] is not None
                         and dm["jar_mtime"] > m["jar_mtime"])
            if d in stale or newer_dep:
                stale.add(a)
                reason[a] = f"dependency {d} " + ("rebuilt" if d in stale else "newer")
                changed = True
                break

# --- 5. Emit --------------------------------------------------------------
def rel(basedir):
    return basedir[len(root) + 1:] if basedir.startswith(root + os.sep) else basedir

if not stale:
    print(f">>> Checked {len(jarred)} jar-producing modules. All up to date.", file=sys.stderr)
    sys.exit(0)

# Order: source-stale first in stderr summary, but -pl order is irrelevant
# (Maven always builds in reactor/topological order).
print(f">>> Checked {len(jarred)} jar-producing modules.", file=sys.stderr)
print(f">>> Stale modules ({len(stale)}):", file=sys.stderr)
for a in sorted(stale):
    print(f"    - {a}  [{reason[a]}]", file=sys.stderr)

for a in sorted(stale):
    print(rel(mods[a]["basedir"]))
PY

mapfile -t stale_paths < "$MAPFILE"

if [[ ${#stale_paths[@]} -eq 0 ]]; then
    exit 0
fi

if [[ $DRY_RUN -eq 1 ]]; then
    exit 0
fi

IFS=','; list="${stale_paths[*]}"; unset IFS

echo ">>> Running: mvn -B install -pl <stale>" >&2
exec mvn -B install -pl "$list"
