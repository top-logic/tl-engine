# Split `CommandScopeElement` — extract `CommandCarrierElement`

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Remove the structural smell in `ContextMenuElement` where it extends `CommandScopeElement` only to re-use command-configuration + lifecycle, overrides `createControl` to bypass the superclass template, and stubs `createChromeControl` to throw `UnsupportedOperationException`. Extract an abstract base `CommandCarrierElement` that holds commands + content + lifecycle. `CommandScopeElement` keeps scope + chrome + toolbar-sync on top of it. `ContextMenuElement` extends the new base directly — no scope, no chrome, no landmine.

**Architecture:**

`CommandCarrierElement` owns Phases 1/3/6 of the current `createControl` orchestration (model building, content merging, attach/detach lifecycle). `CommandScopeElement extends CommandCarrierElement` adds Phases 2/4/5 (scope creation + context derivation, abstract chrome hook, toolbar-placement sync + scope-listener). `ContextMenuElement extends CommandCarrierElement` directly, builds its own models, filters them for `PLACEMENT_CONTEXT_MENU`, builds the `ContextMenuContribution` + `ContextMenuRegionControl`, registers lifecycle — no scope dependency.

**Tech Stack:** Java 17 · TopLogic view + react layers · Maven. Branch: continue on `CWS/CWS_29108_react_context_menus`.

---

## Established facts (from prior recon — do not re-investigate)

- `CommandScopeElement` is abstract, extends `ContainerElement`. Six-phase `createControl`: (1) `buildCommandModels`, (2) scope + `withCommandScope`, (3) `createContent`, (4) `createChromeControl`, (5) toolbar-placement sync, (6) attach/detach lifecycle. File: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/CommandScopeElement.java:55`.
- `Config` has one property: `getCommands() : List<PolymorphicConfiguration<? extends ViewCommand>>`, `@Name(COMMANDS)`, `@EntryTag("command")`, `@TreeProperty`. JavaDoc currently says "available to child elements via the command scope" — scope-centric, wrong fit for a self-contained carrier.
- External contribution is real (`FormElement.java:430, 576, 442, 586` — `scope.addCommand`/`removeCommand`). A `<context-menu>` does not need this; its commands are its own.
- Subclasses: `WindowElement`, `PanelElement`, `ContextMenuElement`. First two have real chrome; third currently throws from `createChromeControl`.
- No dedicated `TestCommandScopeElement`. Coverage is via integration (WindowElement/PanelElement/ContextMenuElement tests).

---

## Files

**Create:**
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/CommandCarrierElement.java` — new abstract base.

**Modify:**
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/CommandScopeElement.java` — now `extends CommandCarrierElement`; keep only scope + chrome + toolbar-sync + scope-listener.
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/ContextMenuElement.java` — now `extends CommandCarrierElement`; drop `createChromeControl` override-and-throw; `createControl` becomes natural, not a workaround.

**No changes:** `WindowElement`, `PanelElement`, `FormElement` (uses `scope.addCommand` — still works because scope is only in CommandScopeElement and its chrome-bearing subclasses), `ContextMenuContribution`, `ContextMenuOpener`, `ContextMenuRegionControl`, tests.

---

## Notes

- Build from project root with `-pl`. Never `cd`.
- ISO-8859-1 for Java. `_` member prefix.
- Never amend. Commit format `Ticket #29108: <description>.` — no `Co-Authored-By`.
- The rename/extraction must preserve the externally observed behavior of `WindowElement`/`PanelElement` exactly. If any of their existing behavior changes, the refactor is wrong.

---

### Task 1: Create `CommandCarrierElement`

**Files:** Create `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/CommandCarrierElement.java`.

- [ ] **Step 1: Read the current `CommandScopeElement.java` fully**

Understand the exact current wiring. Note the fields and the ordering in the constructor (which loads configs into `_commandConfigs` or similar; verify via the file) and in `createControl`. Everything that is about "hold commands, build models, manage their attach/detach, merge children into one content control" moves into the new base. Everything about "scope" and "chrome" stays in `CommandScopeElement`.

- [ ] **Step 2: Write `CommandCarrierElement.java`**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;

/**
 * Abstract base for view elements that carry {@link ViewCommand} configurations.
 *
 * <p>Responsible for loading command configs, building {@link ViewCommandModel}s, merging
 * child elements into a single content {@link ReactControl}, and managing attach/detach
 * lifecycle of the command models.</p>
 *
 * <p>Does NOT create a {@code CommandScope} or any chrome. Subclasses decide what to do
 * with the resulting model list: {@link CommandScopeElement} wraps them in a shared scope
 * and adds toolbar chrome; {@code ContextMenuElement} uses them to populate a context-menu
 * region.</p>
 */
public abstract class CommandCarrierElement extends ContainerElement {

    /**
     * Configuration of a {@link CommandCarrierElement}.
     */
    public interface Config extends ContainerElement.Config {

        /** Property name constant for {@link #getCommands()}. */
        String COMMANDS = "commands";

        /**
         * The commands declared on this element.
         */
        @Name(COMMANDS)
        @EntryTag("command")
        @TreeProperty
        List<PolymorphicConfiguration<? extends ViewCommand>> getCommands();
    }

    private final List<PolymorphicConfiguration<? extends ViewCommand>> _commandConfigs;

    /**
     * Creates a {@link CommandCarrierElement}.
     */
    protected CommandCarrierElement(InstantiationContext context, Config config) {
        super(context, config);
        _commandConfigs = new ArrayList<>(config.getCommands());
    }

    /**
     * Builds {@link ViewCommandModel}s from the configured commands, resolving their
     * input channels, executability rules, and confirmation strategies against the
     * given {@link ViewContext}.
     */
    protected List<ViewCommandModel> buildCommandModels(ViewContext context) {
        // Copy the body of the current CommandScopeElement.buildCommandModels(ViewContext)
        // here verbatim. Do NOT leave it in both places.
        // <insert current implementation — adapted only if imports differ>
        throw new UnsupportedOperationException("move body here during Task 1 Step 2");
    }

    /**
     * Registers attach/detach hooks for the given command models so they
     * re-evaluate executability when input channels change.
     */
    protected void registerLifecycle(List<ViewCommandModel> models, ViewContext context) {
        // Copy the attach/detach lifecycle logic (Phase 6) out of the current
        // CommandScopeElement.createControl — the part that wires beforeWriteAction
        // and cleanupAction.
        throw new UnsupportedOperationException("move body here during Task 1 Step 2");
    }

    /**
     * Merges all child content elements into a single {@link ReactControl},
     * wrapping multiples in a {@link ReactStackControl}.
     */
    protected ReactControl createContent(ViewContext context) {
        List<ReactControl> children = getContent().stream()
            .map(c -> (ReactControl) c.createControl(context))
            .collect(Collectors.toList());
        if (children.size() == 1) {
            return children.get(0);
        }
        return new ReactStackControl(context, children);
    }
}
```

**The two `throw new UnsupportedOperationException` placeholders are for you to replace with the actual code copied from `CommandScopeElement.java`** — do this now in the same step by reading the relevant regions. The plan would be dishonest if it pretended to know the exact current implementation of `buildCommandModels` and the lifecycle wiring verbatim. Copy them as-is; only change imports.

- [ ] **Step 3: Verify it compiles standalone**

```bash
mvn -B install -DskipTests=true -pl com.top_logic.layout.view 2>&1 | tail -30
```

Expect: compile errors in `CommandScopeElement` and `ContextMenuElement` because the methods still exist there. That's fine — Tasks 2 and 3 remove them. But the new class itself must be compilable; if it isn't, fix imports/access levels.

If `mvn install` fails too globally to tell, compile just the new file:
```bash
javac -d /tmp/carrier-check -cp "$(mvn -q -pl com.top_logic.layout.view dependency:build-classpath -Dmdep.outputFile=/dev/stdout 2>/dev/null | tail -1)" com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/CommandCarrierElement.java
```
(Optional — only if the Maven error is unreadable.)

- [ ] **Step 4: Do not commit yet**

The codebase is broken after this step (duplicate methods in `CommandCarrierElement` and `CommandScopeElement`). Commit happens after Task 2 removes the duplicates.

---

### Task 2: Make `CommandScopeElement` extend `CommandCarrierElement`

**Files:** Modify `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/CommandScopeElement.java`.

- [ ] **Step 1: Change the class declaration**

```java
public abstract class CommandScopeElement extends CommandCarrierElement
```

The nested `Config` stays, but change its extension:

```java
public interface Config extends CommandCarrierElement.Config {
    // no additional properties here — they're inherited from the base
}
```

If `Config` currently declares `getCommands()` itself (with the scope-centric JavaDoc), **remove** the declaration here — it's inherited from the base with the cleaner JavaDoc. If subclasses override the getter docstring, that stays in the subclass.

- [ ] **Step 2: Remove the methods/fields now in the base**

Remove from `CommandScopeElement`:
- The `_commandConfigs` field (or whatever name holds the command configs).
- The constructor body that populates it (the super-call will handle it now via the base class).
- `buildCommandModels(ViewContext)` (moved).
- The content-merging logic (moved to `createContent`).
- The attach/detach lifecycle wiring (moved to `registerLifecycle`).

Keep:
- The `createControl(ViewContext)` orchestration, now simplified to:
  ```java
  @Override
  public IReactControl createControl(ViewContext context) {
      List<ViewCommandModel> models = buildCommandModels(context);
      CommandScope scope = new CommandScope(models);
      ViewContext derived = context.withCommandScope(scope);
      ReactControl content = createContent(derived);
      ToolbarControl chrome = createChromeControl(derived, content);
      syncToolbarButtons(chrome, derived, scope, new ArrayList<>());
      scope.addListener(() -> syncToolbarButtons(chrome, derived, scope, /* current buttons */));
      registerLifecycle(models, derived);
      return chrome;
  }
  ```
  **Verify the exact current signatures** of `syncToolbarButtons` and `scope.addListener` before editing — keep the current behavior byte-for-byte. The point is to call into the new base helpers, not to change semantics.
- `createChromeControl(ViewContext, ReactControl)` abstract hook.
- The private `syncToolbarButtons(...)` helper.

- [ ] **Step 3: Build the view module**

```bash
mvn -B install -DskipTests=true -pl com.top_logic.layout.view 2>&1 | tail -40
```

Expected: BUILD SUCCESS, pending Task 3 (ContextMenuElement still has `createChromeControl` override-and-throw — that compiles fine, it's just still ugly).

- [ ] **Step 4: Quick check — existing subclasses don't see any change in signature**

Verify `WindowElement` and `PanelElement` still compile without modification. They override only `createChromeControl`, which is still declared on `CommandScopeElement`. No changes expected.

If they don't compile: something was removed that was actually public/protected API. Fix by re-exposing (moving back to base or re-adding to scope).

- [ ] **Step 5: Do not commit yet**

Wait until Task 3 finishes — one atomic commit for the whole split.

---

### Task 3: Make `ContextMenuElement` extend `CommandCarrierElement`

**Files:** Modify `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/ContextMenuElement.java`.

- [ ] **Step 1: Change the class declaration and `Config`**

```java
@TagName("context-menu")
public class ContextMenuElement extends CommandCarrierElement {

    public interface Config extends CommandCarrierElement.Config {
        @Name("input")
        @Nullable
        @Format(ChannelRefFormat.class)
        ChannelRef getInput();
    }
```

Remove any `extends CommandScopeElement.Config` in favor of `CommandCarrierElement.Config`.

- [ ] **Step 2: Replace `createControl` — drop the scope workaround**

Current implementation (Phase 1 Task 4 + cleanup) calls `super.createControl(context)` to get content + side-effect-build a scope, then reads `getCommandScope().getAllCommands()` and filters. Replace with direct use of `buildCommandModels`:

```java
@Override
public IReactControl createControl(ViewContext context) {
    List<ViewCommandModel> models = buildCommandModels(context);
    registerLifecycle(models, context);

    ViewChannel channel = resolveTargetChannel(context);
    Consumer<Object> setter = channel::set;
    Supplier<Object> supplier = channel::get;

    List<CommandModel> contextMenuCommands = filterByPlacement(models, CommandModel.PLACEMENT_CONTEXT_MENU);
    ContextMenuContribution contribution = new ContextMenuContribution(setter, contextMenuCommands);

    ReactControl content = createContent(context);

    ContextMenuOpener opener = context.getContextMenuOpener();
    if (opener == null) {
        throw new IllegalStateException(
            "<context-menu> requires an enclosing frame providing a ContextMenuOpener.");
    }
    return new ContextMenuRegionControl(context, content, contribution, supplier, opener);
}

private static List<CommandModel> filterByPlacement(List<ViewCommandModel> models, String placement) {
    List<CommandModel> result = new ArrayList<>();
    for (ViewCommandModel model : models) {
        if (placement.equals(model.getPlacement())) {
            result.add(model);
        }
    }
    return result;
}
```

Imports now required:
- `java.util.function.Consumer`, `java.util.function.Supplier`
- `com.top_logic.layout.react.control.overlay.ContextMenuContribution`
- `com.top_logic.layout.react.control.overlay.ContextMenuOpener`
- `com.top_logic.layout.react.control.button.CommandModel`
- `com.top_logic.layout.view.command.ViewCommandModel`
- `com.top_logic.layout.view.channel.ViewChannel`
- existing `ChannelRef`/`ChannelRefFormat` imports

Remove imports that were only for the scope workaround (e.g. `CommandScope` if imported).

- [ ] **Step 3: Remove `createChromeControl` override**

The `@Override public ToolbarControl createChromeControl(...) { throw new UnsupportedOperationException(...); }` block goes away entirely. There is no abstract method to override now — `ContextMenuElement` extends `CommandCarrierElement`, which has no chrome hook.

- [ ] **Step 4: `resolveTargetChannel` helper**

The existing private helper `resolveTargetChannel(ViewContext context)` stays. Verify it still compiles and returns a `ViewChannel`. No semantic change.

- [ ] **Step 5: Build the view module**

```bash
mvn -B install -DskipTests=true -pl com.top_logic.layout.view 2>&1 | tail -40
```

Expected: BUILD SUCCESS. Everything compiles cleanly; no `UnsupportedOperationException` stubs remain.

- [ ] **Step 6: Run the context-menu unit tests**

```bash
mvn -B test -DskipTests=false -pl com.top_logic.layout.view -Dtest=TestContextMenuOpener,TestContextMenuContribution 2>&1 | tail -20
```

Expected: 5/5 tests pass. These tests don't touch the element classes directly, so they should be unaffected. If they fail, a lifecycle- or contribution-wiring bug was introduced.

- [ ] **Step 7: Commit — one atomic commit for the whole split**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/CommandCarrierElement.java \
        com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/CommandScopeElement.java \
        com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/ContextMenuElement.java
git commit -m "Ticket #29108: Extract CommandCarrierElement; ContextMenuElement no longer abuses CommandScopeElement's chrome template."
```

---

### Task 4: Full rebuild + demo + smoke test

**Files:** none.

- [ ] **Step 1: Full-module rebuild**

```bash
mvn -B install -DskipTests=true -pl com.top_logic.layout.view 2>&1 | tail -20
mvn -B install -DskipTests=true -pl com.top_logic.layout.react 2>&1 | tail -20
mvn -B install -DskipTests=true -pl com.top_logic.demo 2>&1 | tail -20
```

(Last command may need `dangerouslyDisableSandbox: true` because of the `tl:app` plugin's use of `/tmp`, per prior plan.)

All BUILD SUCCESS.

- [ ] **Step 2: Run the whole `com.top_logic.layout.view` test suite (excluding known pre-existing failures)**

```bash
mvn -B test -DskipTests=false -pl com.top_logic.layout.view 2>&1 | tee /tmp/carrier-tests.log | tail -30
```

Expected: only the two previously-documented failures (`TestViewElement`, `TestViewReload`) — unrelated to this refactor. Any NEW failure is a regression; diagnose before claiming done.

- [ ] **Step 3: Playwright smoke — both scenarios from prior plan**

Re-run scenarios A + B from `docs/superpowers/plans/2026-04-14-menu-overlay-refactor.md` Task 6 (main window + sub-window). Pass criteria identical. Save screenshots to:
- `com.top_logic.demo/target/command-carrier-split-main.png`
- `com.top_logic.demo/target/command-carrier-split-subwindow.png`

Verification points:
1. Right-click opens menu with correct items + clique separator.
2. Items execute (server log shows both `Hello from context menu` and `Goodbye from context menu`).
3. Exactly one `TLMenu` instance per window.
4. No extra `tlReactStack` in the ancestor chain of `.tl-context-menu-region`.
5. `WindowElement`- and `PanelElement`-based views (e.g. "Dialog Demo") still open and their toolbars still work — any visible regression here is a show-stopper.

- [ ] **Step 4: If no regressions, no commit needed**

If a regression required a fix, commit: `Ticket #29108: <what was fixed after the split.>`.

---

## Out of scope

- Renaming `Config.COMMANDS` or adjusting the XML tag name `<command>` (both are public API).
- Splitting `CommandScope` into something scope-vs-listener. The scope concept stays unchanged; only the element hierarchy changes.
- Improving the JavaDoc on subclass `Config` interfaces beyond what's shown above.

## Self-review

- Spec coverage: the structural smell (ContextMenuElement bypassing a template that doesn't fit) is eliminated by extracting the template-free base. `UnsupportedOperationException` stub is gone. `WindowElement`/`PanelElement` paths are untouched semantically.
- Placeholders: there are two explicit "copy the current body here" markers in Task 1 Step 2 — they are honest pointers to code that must be moved verbatim, not placeholders hiding unknown design.
- Type consistency: `CommandCarrierElement`, `CommandScopeElement`, `ContextMenuElement`, `ContextMenuContribution`, `ContextMenuOpener`, `ContextMenuRegionControl` are used consistently across tasks and match the cleanup commit `e11ecc9` (Contribution in `com.top_logic.layout.react.control.overlay`).
