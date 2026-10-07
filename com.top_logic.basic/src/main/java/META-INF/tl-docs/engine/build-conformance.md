# FAQ: Build conformance & CI gates

A green local `mvn install` does **not** prove a branch will pass CI. Tests are skipped by default locally, and several conformance checks run only per-module via `TestAll` (plus TLDoclet warnings that make the Jenkins build UNSTABLE). Before pushing a branch that adds or edits source or layout files, check the following.

## New `.java` files: SPDX header + class comment

Every new `.java` file needs an SPDX copyright header **and** a class-level `/** … */` doc comment, or `test.com.top_logic.basic.TestComment.testSourceFiles` fails with *"Could not find class comment."* Its `classPattern` matches `class` / `interface` / `enum` / `record` / `@interface`.

## New / edited layouts: must be normalized

Every new or edited `*.xml` layout must be byte-identical to `XMLPrettyPrinter` output — in particular **no trailing newline** after the root element — or `TestLayoutsNormalized.testLayouts` fails with *"… : Not normalized."*

Normalize a **single file** with the generic exec goal:

```bash
mvn -B exec:java -pl com.top_logic.basic \
    -Dexec.mainClass=com.top_logic.basic.xml.XMLPrettyPrinter \
    -Dexec.args="<abs-path-to-file-or-dir>"
```

It prints `Updating: <file>` when it actually rewrites. The named `exec:java@normalize-layouts` execution ignores a `-Dtl.layoutDir` override (the argument is bound from the POM), so it silently no-ops on an arbitrary path — use it only for the whole-tree normalize (`mvn exec:java@normalize-layouts`).

## `-DskipTests=true` also skips test *compilation*

Skipping tests in this reactor also skips compiling the test sources, so a green `mvn install -DskipTests=true` can hide broken test code — an API change (e.g. changing a config-getter's type, deleting a class) can leave test files that no longer compile while the build still reports BUILD SUCCESS.

Skipping the *execution* of the multi-hour core suites (`com.top_logic`, …) is fine, but after touching a module's public API, run at least a test-compile for that module before calling it done:

```bash
mvn test -DskipTests=false -pl <module>     # small/new module with a fast suite
mvn test-compile -pl <module>               # at minimum, prove the test code compiles
```
