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
# Pom-packaging modules (parents/aggregators) are tracked by their installed
# POM: out-of-reactor builds (e.g. the nested builds of the tl-archetype-*
# integration tests) resolve parent chains from the repository, so a stale
# installed parent POM breaks them. Stale POMs are reinstalled (near-free) but
# do not propagate staleness to inheriting modules.
#
# The reactor is enumerated by walking the <modules> lists of the aggregator
# POMs (including <modules> in active-by-default profiles), starting at the
# root POM. This deliberately avoids running a Maven goal per reactor module:
# any such goal (e.g. exec:exec) requires each module's dependencies to be
# resolvable, so a single missing reactor jar -- exactly the situation this
# script exists to repair -- would abort the enumeration. Maven is invoked
# only once (-N, no reactor) to obtain the local repository path.
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

EVAL_OUT="$(mktemp)"
MAPFILE="$(mktemp)"
trap 'rm -f "$EVAL_OUT" "$MAPFILE"' EXIT

echo ">>> Resolving local Maven repository path ..." >&2
if ! mvn -B -N -q help:evaluate -Dexpression=settings.localRepository -DforceStdout > "$EVAL_OUT" 2>&1; then
    echo "!!! 'mvn help:evaluate' failed; its output:" >&2
    cat "$EVAL_OUT" >&2
    exit 1
fi
# With -q -DforceStdout the value is the only payload line; [WARNING]-style
# log lines that may precede it are filtered out.
LOCAL_REPO="$(grep -v '^\[' "$EVAL_OUT" | tail -n 1)"
if [[ ! -d "$LOCAL_REPO" ]]; then
    echo "!!! Evaluated settings.localRepository is not a directory: '$LOCAL_REPO'" >&2
    cat "$EVAL_OUT" >&2
    exit 1
fi

# The enumeration and transitive staleness computation is graph work; do it in
# Python. It prints stale module paths (relative to ROOT), one per line, to
# stdout, and a human-readable summary to stderr. Any inconsistency (missing
# or unparsable POM, unresolvable coordinates) is a loud, fatal error.
ROOT="$ROOT" LOCAL_REPO="$LOCAL_REPO" python3 - > "$MAPFILE" <<'PY'
import os, sys, re, xml.etree.ElementTree as ET

root = os.environ["ROOT"]
repo = os.environ["LOCAL_REPO"]

NS = re.compile(r"\{.*\}")          # strip XML namespace

def tag(elem):
    return NS.sub("", elem.tag)

def child(elem, name):
    for ch in elem:
        if tag(ch) == name:
            return ch
    return None

def text(elem, name):
    ch = child(elem, name)
    if ch is None or ch.text is None:
        return None
    return ch.text.strip() or None

def fail(msg):
    print(f"!!! {msg}", file=sys.stderr)
    sys.exit(1)

def module_dirs(proj):
    """Directories listed in <modules> of the project and of its
    active-by-default profiles (matching Maven's default reactor)."""
    lists = []
    top = child(proj, "modules")
    if top is not None:
        lists.append(top)
    profiles = child(proj, "profiles")
    if profiles is not None:
        for prof in profiles:
            if tag(prof) != "profile":
                continue
            activation = child(prof, "activation")
            by_default = activation is not None and text(activation, "activeByDefault") == "true"
            mm = child(prof, "modules")
            if by_default and mm is not None:
                lists.append(mm)
    for mm in lists:
        for m in mm:
            if tag(m) == "module" and m.text and m.text.strip():
                yield m.text.strip()

# --- 1. Enumerate the reactor by walking the aggregator POM tree ----------
mods = {}            # artifactId -> dict(g,a,v,pkg,basedir,jar,src_mtime,jar_mtime)
trees = {}           # artifactId -> parsed POM
queue = [root]
seen = set()
while queue:
    basedir = os.path.normpath(queue.pop())
    if os.path.isfile(basedir):  # a <module> entry may point at the POM file
        basedir = os.path.dirname(basedir)
    key = os.path.realpath(basedir)
    if key in seen:
        continue
    seen.add(key)
    pom = os.path.join(basedir, "pom.xml")
    if not os.path.isfile(pom):
        fail(f"No pom.xml in referenced module directory: {basedir}")
    try:
        tree = ET.parse(pom)
    except ET.ParseError as ex:
        fail(f"Unparsable POM {pom}: {ex}")
    proj = tree.getroot()
    parent = child(proj, "parent")
    g = text(proj, "groupId") or (text(parent, "groupId") if parent is not None else None)
    v = text(proj, "version") or (text(parent, "version") if parent is not None else None)
    a = text(proj, "artifactId")
    pkg = text(proj, "packaging") or "jar"
    if not a or not g or not v:
        fail(f"Cannot determine coordinates of {pom} (groupId={g}, artifactId={a}, version={v})")
    if "${" in g or "${" in v:
        fail(f"Unresolved property in coordinates of {pom} (groupId={g}, version={v})")
    if a in mods:
        fail(f"Duplicate artifactId '{a}': {mods[a]['basedir']} vs. {basedir}")
    mods[a] = dict(g=g, a=a, v=v, pkg=pkg, basedir=basedir)
    trees[a] = tree
    for m in module_dirs(proj):
        queue.append(os.path.join(basedir, m))

print(f">>> Enumerated {len(mods)} reactor modules from the POM tree.", file=sys.stderr)

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

def installed_mtime(artifact):
    """Effective install time of a repository artifact, or None if absent.

    maven-install-plugin copies files into the repository PRESERVING the
    source file's (millisecond-truncated) timestamp, and maven-jar-plugin
    keeps an existing jar file when its contents are unchanged -- so the
    artifact's own mtime can predate the actual install and would flag the
    module stale forever (e.g. when the TLDoclet regenerates messages files
    under src/ after packaging). maven-metadata-local.xml in the same
    directory is rewritten at install time, so the newer of the two is the
    time the module was last installed."""
    if not os.path.isfile(artifact):
        return None
    mt = os.path.getmtime(artifact)
    meta = os.path.join(os.path.dirname(artifact), "maven-metadata-local.xml")
    if os.path.isfile(meta):
        mt = max(mt, os.path.getmtime(meta))
    return mt

# --- 2. Compute mtimes and parse direct reactor dependencies -------------
deps = {}                            # artifactId -> set(reactor artifactIds it depends on)

for a, m in mods.items():
    g, v, basedir = m["g"], m["v"], m["basedir"]
    m["src_mtime"] = newest_mtime(os.path.join(basedir, "pom.xml"),
                                  os.path.join(basedir, "src"))
    if m["pkg"] == "pom":
        # Parents/aggregators produce no jar, but their installed POM matters:
        # builds that resolve a reactor artifact's parent chain from the
        # repository (e.g. the nested out-of-reactor builds spawned by the
        # tl-archetype-* integration tests) see the INSTALLED parent POM, so a
        # stale one breaks them (e.g. a dependencyManagement entry missing).
        # Installing a POM is near-free, so track it like any artifact.
        m["jar"] = os.path.join(repo, *g.split("."), a, v, f"{a}-{v}.pom")
        m["jar_mtime"] = installed_mtime(m["jar"])
        continue
    m["jar"] = os.path.join(repo, *g.split("."), a, v, f"{a}-{v}.jar")
    m["jar_mtime"] = installed_mtime(m["jar"])

    edges = set()
    for dep in trees[a].iter():
        if tag(dep) != "dependency":
            continue
        aid = text(dep, "artifactId")
        # Match by artifactId: reactor artifactIds are unique, so this is
        # robust even when the dependency's groupId is a ${property}.
        if aid in reactor_artifacts:
            edges.add(aid)
    deps[a] = edges

jarred = [a for a, m in mods.items() if m["pkg"] != "pom"]
pom_only = [a for a, m in mods.items() if m["pkg"] == "pom"]

# --- 3. Seed source-stale set --------------------------------------------
stale = set()
reason = {}
for a in jarred:
    m = mods[a]
    if m["jar_mtime"] is None:
        stale.add(a); reason[a] = "no jar"
    elif m["src_mtime"] > m["jar_mtime"]:
        stale.add(a); reason[a] = "sources changed"

# Stale pom-packaging modules are reinstalled, but deliberately do NOT
# propagate over the dependency graph: a touched parent POM would otherwise
# force a rebuild of every inheriting module on each run.
for a in pom_only:
    m = mods[a]
    if m["jar_mtime"] is None:
        stale.add(a); reason[a] = "no installed pom"
    elif m["src_mtime"] > m["jar_mtime"]:
        stale.add(a); reason[a] = "pom changed"

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
    if basedir == root:
        return "."      # the root POM itself, as a -pl selector
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
