#!/usr/bin/env python3
"""Report unused and duplicate Java imports.

Eclipse's "Organize Imports" keeps hand-edited sources free of unused imports, so an
unused import reaches a pull request only through an edit made outside the IDE. The Maven
build does not catch it either: javac has no unused-import diagnostic, so such a branch
builds green and the finding surfaces first in a human code review.

Usage:
  check-unused-imports.py [PATH ...]   Check the given files or directories (default: .).
                                       Exits 1 when something is reported.
  check-unused-imports.py --hook       Read a Claude Code hook event from stdin and check
                                       the edited file. Exits 2 when something is reported,
                                       so the report is fed back to the model.

An import counts as used when its simple name appears in code or in a JavaDoc comment,
matching what the IDE reports. Occurrences in a line comment, in a non-JavaDoc block
comment, or inside a string literal do not count - that is the case a text edit
introduces when it deletes the last real use but leaves the type named in a comment.

An occurrence as part of a qualified name does not count either: a file that writes
`java.util.Map<...>` in full does not need an import of `java.util.Map`, and neither does
a JavaDoc that links `{@link java.util.Map}`. The same holds for a nested type imported as
`a.b.Outer.Inner` but always written `Outer.Inner`, and for a static import whose member is
always addressed through its class.

Only sources in a Maven `src` tree are checked. That leaves out generated code (the
compiled JSPs under an application's `tmp`, anything under `target`) and, because they are
repackaged external libraries, the sources of an `ext.*` module.
"""

import json
import os
import re
import sys

# A string or character literal, including escapes.
LITERAL = re.compile(r'"(?:\\.|[^"\\\n])*"|\'(?:\\.|[^\'\\\n])*\'')

# A single-type import, optionally static. On-demand imports (ending in `.*`) are skipped.
IMPORT = re.compile(r'^import\s+(static\s+)?([\w.]+)\s*;', re.M)

# A use of an imported simple name: the name on its own, not as a segment of a qualified
# name. The leading `.` in the look-behind is what distinguishes `Map` from `java.util.Map`;
# `$` is legal in Java identifiers and is excluded on both sides for the same reason as `\w`.
USE = r'(?<![\w.$])%s(?![\w$])'

# Directories not descended into when a whole tree is checked.
SKIPPED_DIRS = {'target', 'tmp', 'bin', '.git', 'node_modules'}


def strip_uncounted(src):
    """Replace string literals, line comments and non-JavaDoc block comments by blanks.

    JavaDoc is retained: a `{@link}` counts as a use of the imported type.
    """
    out = []
    pos = 0
    end = len(src)
    while pos < end:
        char = src[pos]
        if char in '"\'':
            literal = LITERAL.match(src, pos)
            if literal:
                out.append(' ')
                pos = literal.end()
            else:
                # An unterminated literal: keep the character and move on.
                out.append(char)
                pos += 1
        elif src.startswith('//', pos):
            newline = src.find('\n', pos)
            pos = end if newline < 0 else newline
            out.append(' ')
        elif src.startswith('/*', pos):
            close = src.find('*/', pos + 2)
            close = end if close < 0 else close + 2
            out.append(src[pos:close] if src.startswith('/**', pos) else ' ')
            pos = close
        else:
            out.append(char)
            pos += 1
    return ''.join(out)


def findings(path):
    """The unused and duplicate imports of the given Java source file."""
    try:
        with open(path, encoding='utf-8', errors='replace') as file:
            src = file.read()
    except OSError as ex:
        print('%s: cannot read: %s' % (path, ex), file=sys.stderr)
        return []

    # The declarations the imported names may be used from: everything but the imports
    # themselves, with uncounted regions blanked out.
    body = IMPORT.sub(' ', strip_uncounted(src))

    result = []
    declared = {}
    for match in IMPORT.finditer(src):
        static, name = match.group(1), match.group(2)
        if name.endswith('.*'):
            continue
        line = src.count('\n', 0, match.start()) + 1
        if name in declared:
            result.append((line, name, 'duplicate import (first at line %d)' % declared[name]))
            continue
        declared[name] = line
        simple = name.rsplit('.', 1)[-1]
        if not re.search(USE % re.escape(simple), body):
            result.append((line, ('static ' if static else '') + name, 'unused import'))
    return result


def checked(path):
    """Whether the given Java source file is one of the sources to check."""
    parts = os.path.abspath(path).split(os.sep)
    if any(part.startswith('ext.') for part in parts):
        return False
    # Everything hand-written lives in a Maven source tree; the generated code does not -
    # neither the compiled JSPs under an application's `tmp` nor anything under `target`.
    return 'src' in parts


def java_files(paths):
    """The Java source files at or below the given paths."""
    for path in paths:
        if os.path.isfile(path):
            if path.endswith('.java') and checked(path):
                yield path
            continue
        for dirpath, dirnames, filenames in os.walk(path):
            dirnames[:] = [dir for dir in dirnames if dir not in SKIPPED_DIRS]
            for filename in sorted(filenames):
                file = os.path.join(dirpath, filename)
                if filename.endswith('.java') and checked(file):
                    yield file


def report(paths, out):
    """Print the findings for the given paths, and answer how many there were."""
    count = 0
    for file in java_files(paths):
        for line, name, kind in findings(file):
            print('%s:%d: %s: %s' % (file, line, kind, name), file=out)
            count += 1
    return count


def hook():
    """Check the file an Edit or Write tool call has just modified."""
    try:
        event = json.load(sys.stdin)
    except ValueError:
        return 0
    input = event.get('tool_input') or {}
    response = event.get('tool_response') or {}
    path = response.get('filePath') or input.get('file_path') or ''
    if not path.endswith('.java') or not os.path.isfile(path) or not checked(path):
        return 0

    found = findings(path)
    if not found:
        return 0
    print('Unused imports in %s - remove these lines:' % path, file=sys.stderr)
    for line, name, kind in found:
        print('  line %d: %s: %s' % (line, kind, name), file=sys.stderr)
    print('(A type named only in a // comment does not count as a use.)', file=sys.stderr)
    # Exit code 2 feeds stderr back to the model.
    return 2


def main(argv):
    if '--hook' in argv:
        return hook()
    paths = [arg for arg in argv if not arg.startswith('-')] or ['.']
    for path in paths:
        # Say so when a file named on the command line is not checked at all. Skipping is
        # silent while walking a tree, where it is the point; for an explicit file it would
        # look like a clean result.
        if path.endswith('.java') and os.path.isfile(path) and not checked(path):
            print('%s: not checked: no Maven `src` tree in its path.' % path, file=sys.stderr)
    return 1 if report(paths, sys.stdout) else 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
