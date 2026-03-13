---
name: run-scripted-test
description: Run a TopLogic scripted test (.script.xml). Examples - "run TestTokenEditor", "run the scripted test config/editors/TestTokenEditor".
allowed-tools: Bash, Read, Glob, Grep
---

# Run Scripted Test

Runs a TopLogic scripted test (`.script.xml`) using the `test.TestAll` runner.

## How It Works

Scripted tests are XML action recordings that replay UI interactions against a running test application. They are executed by the generic `test.TestAll` runner with a `-DTestAll.target=<path>` system property.

## Arguments

The argument is the test name or path. Examples:
- `TestTokenEditor` — searches for matching `.script.xml`
- `config/editors/TestTokenEditor` — partial path match
- Full absolute path to a `.script.xml` file

## Steps

1. **Find the test file**: Search for the `.script.xml` file matching the argument.
   ```bash
   # Use Glob to find matching files
   ```
   If multiple matches, ask the user which one. If none found, report and stop.

2. **Determine the app module**: The test file lives under a module's `src/test/java/` directory. Extract the module from the path (e.g. `com.top_logic.demo`).

3. **Ensure dependencies are installed**: Run `mvn install -DskipTests=true` in the app module if there are uncommitted changes or if a previous test run failed with `ClassNotFoundException` / `NoClassDefFoundError`. Skip this step if the user says "just run".

4. **Run the test** from the app module directory:
   ```bash
   cd <app-module-path> && mvn test -DskipTests=false -Dtest=test.TestAll "-DTestAll.target=<absolute-path-to-script.xml>" -DTestAll.recursive=true 2>&1 | tail -80
   ```
   Use a timeout of 600000ms (10 minutes) since scripted tests boot a full application.

5. **Report results**:
   - **Success**: `Tests run: N, Failures: 0, Errors: 0` + `BUILD SUCCESS`
   - **Failure**: Show the failure message. The key information is usually in the `Failures:` or `Errors:` summary near the end of the output. Common issues:
     - `Form member not found: ... label="X"` — a UI label changed; compare expected vs actual labels
     - `ClassNotFoundException` / `NoClassDefFoundError` — stale dependency jars; re-run `mvn install -DskipTests=true` on the dependency module
     - `AbstractMethodError` — binary incompatibility; full `mvn install -DskipTests=true` from root needed

## Notes

- The test boots a full TopLogic application in-process — expect ~45-60s runtime.
- Scripted tests use **German locale** labels by default (the recorder captures DE labels).
- The `.script.xml` files reference components by layout path and form members by label — any label rename breaks the test.
