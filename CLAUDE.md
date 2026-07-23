# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TopLogic is an open-source, model-based, no-code web application platform (declarative UML/BPMN modeling + WYSIWYG UI, extensible with Java components). Maven build; dual-licensed AGPL-3.0 / BOS-TopLogic-1.0.

## Build Commands

### Building the Project

```bash
# Build entire engine from root (tl-parent-all)
mvn install

# Build specific module (path-based -pl, from project root)
mvn install -pl com.top_logic.basic
```

Tests are skipped by default (`skipTests=true` in tl-parent-all), so `-DskipTests=true` is not needed.

**IMPORTANT**: Always build from the project root using `-pl <module-dir>`, never by `cd`-ing into a module directory. The build uses a workspace-local Maven repository; generate it once per workspace with `.claude/scripts/local-m2.sh`. Never use the global Maven repository (`~/.m2/repository`) and never pass `-Dmaven.repo.local=$HOME/.m2/repository` — each workspace has its own local repo.

### Refreshing the Workspace After a Branch Switch

Switching branches or pulling can leave stale jars in the local Maven repo (installed artifacts older than — or built from a different branch than — the current sources). A full rebuild is expensive; detect and rebuild only the stale modules:

```bash
.claude/scripts/rebuild-stale.sh            # detect + rebuild stale modules
.claude/scripts/rebuild-stale.sh --dry-run  # only list them
```

**Run it after every pull or branch switch, not just when something looks broken** — it is the cheap step that reconciles the local repo with the working tree, including newly registered modules (flagged `[no jar]`). The one case it cannot catch is a module present in the source tree but not yet registered in any aggregator's `<modules>` list. The script's header comment documents the staleness rules (source-, dependency-, and branch-state-stale) and why each is needed.

### Running Tests

Tests are **skipped by default** (`skipTests=true` in tl-parent-all). To run tests:

```bash
# Run tests in specific module (from project root)
mvn test -DskipTests=false -pl com.top_logic.basic

# Run single test class — -Dtest MUST be the fully qualified class name
mvn test -DskipTests=false -pl com.top_logic.basic -Dtest=test.com.top_logic.basic.SomeTest
```

The `-Dtest` value **must be the fully qualified class name** — a simple class name does not work because `tl-parent-core` configures Surefire with `<include>test/TestAll.java</include>`, which only an FQN overrides. Running a single method (`-Dtest=Class#method`) does **not** work for tests with a `suite()` method: the suite setup (XMLProperties init, service startup) is skipped and the test fails with "Cannot start configuration service".

**Important**: Tests require database configuration. Many tests use H2 in-memory databases, but some require specific database drivers (MySQL, Oracle, PostgreSQL, DB2, MS SQL Server).

**Stale dependency jars**: If compilation fails with "cannot find symbol" for classes that DO exist in the source tree, the local Maven repository has outdated jars. Fix: run `mvn install` on the dependency modules, then recompile the failing module.

**Build pitfalls**:
- **Use `mvn install`, not just `compile`**, when building dependent modules in sequence — downstream modules need the updated artifacts in the local Maven repository.
- **To force recompilation without clean**: `touch` the changed `.java` files, then run `mvn compile`.
- **Piping Maven output**: Always use `mvn -B` (batch mode) when piping output to `grep`, `tail`, etc. Without `-B`, Maven emits ANSI color codes that prevent text matching (e.g. `grep 'BUILD'` fails because the actual string is `[1;32mBUILD SUCCESS[m`).
- **Always preserve full build output**: Use `tee` to save output to a log file while still seeing it live: `mvn -B install -pl com.top_logic.basic 2>&1 | tee com.top_logic.basic/target/mvn-build.log`. This way you can search the full output afterwards without having to re-run the build.
- **GWT client → server WAR dependency**: `tl-react-flow-server` packages the GWT JavaScript from `tl-react-flow-client` as a WAR overlay. The Jetty server serves GWT files from the server's web-fragment.war, NOT from the client's. **Always rebuild `com.top_logic.react.flow.server` after changing `com.top_logic.react.flow.client`**, otherwise the app serves stale JavaScript. Full rebuild command: `mvn install -pl com.top_logic.react.flow.common,com.top_logic.react.flow.client,com.top_logic.react.flow.server,com.top_logic.demo`.
- **Never call `javac` directly** — always build through Maven (`mvn compile` / `mvn install`), which handles the classpath, dependency resolution, and source encoding.
- **Do not use the `-am` (also-make) flag** when verifying your own changes. List every module you modified explicitly on `-pl` (comma-separated, in dependency order). `-am` drags in the whole upstream dependency closure, hides which modules you actually touched, and slows every iteration down.

### Other Useful Commands

```bash
# Check for dependency issues
mvn dependency:tree

# Check for plugin updates (from tl-parent-all)
mvn -N versions:display-plugin-updates

# Run SpotBugs static analysis
mvn clean verify spotbugs:spotbugs

# Check for security vulnerabilities
mvn dependency-check:check

# Generate JavaDoc
mvn javadoc:javadoc
```

## Architecture & Module Layout

TopLogic is a layered Maven reactor of 110+ modules. Name prefixes:

- **`com.top_logic.*`** — framework and feature modules
- **`tl-*`** — build tooling, parent POMs, infrastructure
- **`ext.*`** — repackaged external libraries (CKEditor, Font Awesome, BPMN.js, …)
- **`test.*`** — test-only modules

Most modules are library-JAR *fragments* composed into applications: fragments use parent `tl-parent-core`, runnable apps use `tl-parent-app`.

**Dependency direction is strictly bottom-up and must stay acyclic:** `com.top_logic.basic` (utilities, no other TL deps) → `basic.db` → `dob` / `dsa` (data access) → `com.top_logic` (knowledge base, model, layout) → feature modules → application modules. In particular, `basic.*` must never depend on a higher layer.

To find the module that owns a class or feature, use the `tl-mcp` tools (`module_of`) or the "Repository Structure" section below.

## File Locations

Standard Maven layout, plus these TopLogic conventions:

- **Tests** live under `src/test/java/test/com/top_logic/...` — note the extra `test.` package prefix.
- **Webapp config** (web modules) under `src/main/webapp/WEB-INF/`: `layouts/` (layout XML), `model/` (model XML), `conf/` (app config), `kbase/` (kbase definitions).
- Model types are also declared in `*.type.xml`.

## Development Patterns

### Coding Conventions

**Member Variable Naming:**
- All member variables (instance fields) must start with an underscore `_` prefix
- This convention makes using `this.` unnecessary and clearly distinguishes member variables from local variables and parameters

```java
public class MyClass {
    private final int _widthMm;
    private String _name;

    public MyClass(int widthMm, String name) {
        _widthMm = widthMm;  // No need for this.widthMm
        _name = name;         // No need for this.name
    }

    public int getWidth() {
        return _widthMm;      // Clearly a member variable
    }
}
```

### Configuration Framework

TopLogic uses a typed configuration system: config classes are annotated `ConfigurationItem` / `PolymorphicConfiguration` interfaces (properties declared via `@Name`, `@Mandatory`, …), loaded from XML via `InstantiationContext`.

**TypedConfiguration pitfalls:**
- `@ClassDefault(MyClass.class)` is REQUIRED on Config interfaces extending `PolymorphicConfiguration<T>` — otherwise the framework tries to instantiate the base interface.
- `@TagName("name")` only works in **list-typed** `PolymorphicConfiguration` properties. For single-valued properties, tag names do NOT work — the parser interprets child elements as property names. Fix: use a `List<>` property and extract the first element, or use `@DefaultContainer`.
- `@Mandatory` is at `com.top_logic.basic.config.annotation.Mandatory` (not `com.top_logic.basic.config`).
- Tests using TypedConfiguration need `TypeIndex.Module.INSTANCE` in their service setup.

### Knowledge Base Access

Objects are read/written through `PersistencyLayer.getKnowledgeBase()`. Wrap every mutation in a transaction: `kb.beginTransaction()`, perform the changes, `tx.commit()`, with `tx.rollback()` in a `finally` (a no-op after a successful commit).

### Layout Components

UI is assembled declaratively in `*.layout.xml` files under `WEB-INF/layouts/`. See the `tl-layout` skill for the template-call pattern, channel binding, and the component catalog.

### React Controls (`com.top_logic.layout.react`)

React controls MUST import `React` from `'tl-react-bridge'`, NEVER from `'react'` directly — importing from `'react'` bundles a duplicate React copy, causing "useState is null" runtime errors. The JS/TS build runs via `frontend-maven-plugin` during `mvn compile`; do not run `npx vite build` directly. For setting up a React control module (vite / tsconfig / shims / wiring), see [docs/faq/new-react-module.md](docs/faq/new-react-module.md).

### Model Definitions

Application data types are defined in `*.model.xml` files under `WEB-INF/model/`. See the `tl-model` skill for types, attributes, references, derived attributes, and wrapper generation.

### UI Labels and I18N

- English `messages_en.properties` (in `src/main/java/META-INF/`) are **generated during `mvn install`** from config-property names and JavaDoc — **never edit them by hand**; the next build overwrites them.
- German `messages_de.properties` are **hand-maintained**: the build seeds new keys via DeepL but never overwrites existing entries, so correcting a translation directly is the normal workflow.
- **Never pass `-Dmaven.javadoc.skip=true` when adding or renaming `I18NConstants`** — the `TLDoclet` runs in the javadoc lifecycle, so skipping it leaves the `messages_*.properties` stale.
- To change a label, add a `@Label` annotation (tooltip = getter JavaDoc), run `mvn install`, and commit both the Java change and the regenerated `messages_*.properties`.

See [docs/faq/i18n.md](docs/faq/i18n.md) for the full workflow and examples.

### Exception Handling

Throw `com.top_logic.util.error.TopLogicException` for user-visible errors that need an internationalized message; use a plain `RuntimeException` for internal programming errors users cannot act on. User-facing messages are `ResKey` constants declared in an `I18NConstants` class with an `@en` JavaDoc default, named `ERROR_<DESCRIPTION>__<PARAM1>_<PARAM2>` (`ResKey`, `ResKey1`, … chosen by parameter count).

See [docs/faq/i18n.md](docs/faq/i18n.md) for the `I18NConstants` pattern and a full example.

### JavaDoc Conventions

**Reference members, methods, and types with `{@link}`, not `{@code}`.** A `{@link #getScrollX()}`
is checked by the compiler/doclet, so a later rename breaks the build instead of silently leaving
stale documentation (`{@code scrollX}` would rot unnoticed). Reserve `{@code ...}` for things that
are not resolvable symbols: literals (`{@code null}`, `{@code true}`), expressions
(`{@code end >= start}`), method parameter names (JavaDoc has no link syntax for parameters), and
external/JS identifiers.

- In `*.proto` files, reference a field by its **proto field name**, not the generated getter:
  `{@link #strokeColor}` (same message) or `{@link OtherMessage#fieldName}` (another message). The
  msgbuf generator rewrites field links to the correct getter — do NOT guess `getStrokeColor()`.
- The `TLDoclet` "Invalid camel case word" warning flags bare `camelCase` words in JavaDoc — wrap
  them in a `{@link}` (preferred) or, only for non-symbols, `{@code}`.

## Testing Conventions

- Test classes use the `test.` package prefix (e.g., `test.com.top_logic.basic.GenericTest`)
- Many base test classes exist for common scenarios (see `Abstract*Test.java` patterns)
- Tests requiring a knowledge base extend `AbstractDBKnowledgeBaseTest`
- Tests are JUnit 4 based

### Manual Verification with Playwright

After implementing a UI feature or fix, always verify it manually in a running application using Playwright before reporting the work as done:

1. **Ensure the feature is accessible in `com.top_logic.demo`** — if it is not already wired into the demo app, add the necessary configuration (layouts, views, model entries) so it can be reached in the browser.
2. **Start the demo app** using the `tl-app` skill.
3. **Verify the feature** by navigating to it, interacting with it, and checking it behaves as expected. **Delegate the browser interaction loop to a sub-agent** so its hundreds of round-trips don't re-bill the main thread's context on every turn — see the `tl-app` skill's "Verifying in the browser" section for the sub-agent brief and the snapshot-vs-screenshot guidance.
4. **Only then report the work as complete.**

## Important Notes

### Demo App Credentials

The demo application's default developer login is `root` / `root1234`.

### Version Management

- Central version is defined in `tl-parent-all` — read it from there when needed.
- Use `${tl.version}` property for internal dependencies
- External dependencies versioned via properties (e.g., `${jetty.version}`)

### New Module Setup

When creating a new TopLogic module:
- Use `tl-parent-core-internal` as the parent POM.
- Register the module in `tl-parent-engine/pom.xml`.
- `web-fragment.xml` goes in `src/main/java/META-INF/`.
- Configuration files go in `src/main/webapp/WEB-INF/conf/`.

### Resource File Normalization

`.properties` files under `WEB-INF/conf/resources/` must be normalized. See the `tl-model` skill ("Normalizing resource files") for the command and the mandatory `-N` flag.


### Theme / CSS Reloading

To reload CSS changes at runtime: Use sidebar menu "Entwickleroptionen" (Developer Options) "Theme neu laden" (Reload Theme), then **reload the page** in the browser. The theme reload alone doesn't update the browser.

### View Configuration Reloading

Changes to `.view.xml` files take effect after a logout/login — no application restart is needed. Views are loaded per session, not cached globally at startup.


### Layout Normalization

XML layout files must be normalized using:

```bash
mvn exec:java@normalize-layouts
```

This runs `com.top_logic.basic.xml.XMLPrettyPrinter` on layout directories.

### Migration Tools

For layout migrations after API changes:

```bash
# Example: Migrate confirm messages (Ticket #28336)
mvn exec:java@migrate-ticket28336
```

## Repository Structure

- **./** - Root aggregator POM
- **tl-parent-engine/** - Aggregates all runtime modules (103 modules)
- **com.top_logic/** - Core engine (tl-core artifact)
- **com.top_logic.basic/** - Foundation utilities
- **com.top_logic.element/** - Element management
- **com.top_logic.demo/** - Legacy demo application (`tl-demo`). The classic
  layout UI plus a React view layer mounted at `/view/`. Depends directly on the
  legacy `graphic.blocks.*` diagram stack, so it also serves those modules'
  webapp resources (e.g. `/style/tl-flow-core.css`).
- **com.top_logic.demo.react/** - React-only demo application (`tl-demo-react`),
  built purely on the `com.top_logic.layout.view` `.view.xml` layer and served at
  `/view/`. This is the successor test bed for React UI features (see its
  `TRIAGE.md`). It does **not** depend on the legacy `graphic.blocks.*` modules;
  the React flow diagram is provided by the copied, self-contained
  `com.top_logic.react.flow.*` modules. When verifying a React UI feature, run
  this app, not `tl-demo` — a feature that works under `tl-demo` may still be
  broken here if it relies on a legacy webapp resource that only `tl-demo`
  happens to serve.
- **test-migrate-apps/** - Test applications for migration scenarios
- **bos-settings/** - Build settings and configuration
- **tl-doc/** - Documentation and JavaDoc output
- **docs/faq/** - FAQ articles for common development tasks and pitfalls

## Trac Ticket System

This project uses **Trac** for issue tracking. Tickets are referenced using the format `#<number>` (e.g., `#29053`).

### MCP Server Integration

A Trac MCP server (installed once per machine from the `tl-dev-mcp` package and registered in Claude Code at user scope) lets Claude Code interact with tickets at `http://tl/trac/`. See `mcp-servers/README.md` for the one-step setup.

**When discussing "tickets" in this project, always use the Trac MCP server tools to retrieve ticket information.**

**Issue tracking is maintained in Trac, PR review comments are maintained in Gitea.** When asked for "issue comments" for a PR, automatically derive the ticket number from the PR title and look it up in Trac (e.g. via `get_ticket_changelog`) — do not use Gitea issue APIs for this.

Available tools include:
- `get_ticket` - Get complete ticket details
- `search_tickets` - Search for tickets using Trac query syntax
- `get_ticket_changelog` - Get ticket change history
- `create_ticket` / `update_ticket` - Create or update tickets

**Important**: Trac uses WikiFormatting syntax (not Markdown) for ticket descriptions and comments. Key syntax:
- Headings: `= Title =`, `== Subtitle ==`
- Bold: `'''text'''`
- Code blocks: `{{{` ... `}}}` with optional syntax highlighting `{{{#!java`

See `mcp-servers/README.md` for setup instructions.

## VCS

TopLogic uses Git as Version Control System

### Git Commit Message Convention

Commit messages in this project must follow a specific format:

- **Format**: `Ticket #<number>: <description>`
- **Example**: `Ticket #28934: Add data URI SVG support to SVGReplacedElementFactory.`
- **Important**: Do NOT include "Generated with Claude Code", "Co-Authored-By: Claude", or any AI-generation attribution lines
- **Never amend commits** unless explicitly asked to do so. Always create new commits.
- Keep the message plain and focused on describing the change.
- **Commit completed work at the end of each turn** without waiting to be asked, so every step of
  work lands as its own commit. Group unrelated changes into separate commits.

### Git PR Conventions

**Branch naming:**
- Format: `CWS/CWS_<ticket-number>_<short-description>` (e.g., `CWS/CWS_28934_svg_data_uri`)
- The short description is optional but recommended

**PR title:**
- Must start with `Ticket #<number>: <description>` (same format as commit messages)
- Example: `Ticket #28934: Add data URI SVG support to SVGReplacedElementFactory.`
- To get the correct issue/ticket for a PR it must be determined from the PR title. In rare cases, when the title is wrong, the correct issue can be determined from the commit messages.

**CI:** Jenkins automatically builds each PR and posts the result as a review (APPROVED on success, REQUEST_CHANGES on failure).

**After merge:** The source branch is deleted automatically.

## msgbuf Library

The project uses the [msgbuf](https://github.com/msgbuf/msgbuf) library for type-safe protocol message generation from `.proto` files. For the `JsonWriter` writer-type pitfall and the generator-plugin invocation, see [docs/faq/msgbuf.md](docs/faq/msgbuf.md).


## Additional Resources

- **Main Repository**: https://github.com/top-logic/tl-engine
- **Developer Guide**: https://dev.top-logic.com/tl/content/docs/DeveloperGuide/
- **Tutorial**: https://dev.top-logic.com/tl/content/docs/Tutorial/
- **Discord Server**: https://discord.gg/XsNq9JXTe9
- **Organization**: Business Operation Systems GmbH (https://top-logic.com)
