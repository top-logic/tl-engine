# React Context Menus — Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Establish a clean, reusable context-menu mechanism for React controls that builds on the existing `ViewCommand`/`CommandScope`/`ReactMenuControl` stack — no legacy `DisplayContext`/`CommandModel` coupling — and enable it for explicit `<context-menu>` regions in views. Table/tree/implicit-region integration is out of scope for this plan.

**Architecture:**

Three new server-side runtime abstractions work together: **`ContextMenuContribution`** holds one target `ViewChannel` + a list of `ViewCommandModel`s whose `input` is that channel. **`ContextMenuOpener`** (one per frame, exposed on `ViewContext`) owns the frame's single `ReactMenuControl`; `open(x, y, List<Targeted>)` writes each contribution's target into its channel, collects executable commands, assembles one flat `MenuEntry` list ordered by contribution then by `clique`, and dispatches selections back to the right `ViewCommandModel`. **`ContextMenuElement`** is the new `<context-menu>` view element that declares commands declaratively, produces one `Contribution`, and creates a `ContextMenuRegionControl` that handles `contextmenu` DOM events in its subtree and calls the opener.

Client side: a new `TLContextMenuRegion` React component wraps its children, installs an `onContextMenu` handler, `preventDefault()`s, and sends the `openContextMenu` command to the server with coordinates. The existing `TLMenu` (`ReactMenuControl`) renders the popup; no new overlay primitive is needed.

**Tech Stack:**

Java 17 · TopLogic `com.top_logic.layout.view` + `com.top_logic.layout.react` · TypeScript + React via `tl-react-bridge` · JUnit 4 for server tests · Maven (build from project root with `-pl`) · Playwright for manual verification.

---

## File Structure

**New files — server:**
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/ContextMenuContribution.java` — runtime data holder (target channel + command models)
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/ContextMenuOpener.java` — composes and dispatches menus for one frame
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/ContextMenuElement.java` — `<context-menu>` view element
- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/overlay/ContextMenuRegionControl.java` — wrapper ReactControl for a right-click region

**New files — client:**
- `com.top_logic.layout.react/react-src/controls/TLContextMenuRegion.tsx` — renders children + catches `contextmenu` DOM event

**New files — tests:**
- `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/command/TestContextMenuContribution.java`
- `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/command/TestContextMenuOpener.java`

**Modified files — server:**
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewContext.java` — add `getContextMenuOpener()` accessor + `withContextMenuOpener(...)` derivation
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewElement.java` — initialize a `ContextMenuOpener` at the root context
- Module-registry files (e.g. `module.xml`, test setup) — register `<context-menu>` tag via `@TagName`; usually no extra registration needed because polymorphic config discovers it, but verify

**Modified files — client:**
- `com.top_logic.layout.react/react-src/bridge/tl-react-bridge.ts` — register `TLContextMenuRegion` module (the discovery mechanism in `mount`)

**Modified files — demo:**
- `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/` — add one demo view (new file `context-menu-demo.view.xml`) that shows a panel with a context menu

---

## Notes for the engineer

1. **Build from the project root** with `-pl <module>`. Never `cd` into a module.
2. **Tests are skipped by default**; pass `-DskipTests=false` to run them.
3. **Source encoding is ISO-8859-1** — no non-ASCII in Java without care.
4. **Member variables start with `_`** (`_commands`, `_opener`).
5. **`@Label` annotations** on config getters drive UI labels; messages regenerate during `mvn install`.
6. **React builds run during `mvn install`** via `frontend-maven-plugin`. Do not run `npx vite build`.
7. **Verify current API before copy-pasting** code in this plan. Signatures shown match state on branch `CWS/CWS_29108_react_context_menus` at plan time; if a refactor landed since, adapt.
8. **Commit after each task** with the format `Ticket #29108: <description>.` — never amend, never add `Co-Authored-By` trailers.

---

### Task 1: Create `ContextMenuContribution`

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/ContextMenuContribution.java`
- Test: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/command/TestContextMenuContribution.java`

- [ ] **Step 1: Write the failing test**

```java
package test.com.top_logic.layout.view.command;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.layout.view.channel.SimpleViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ContextMenuContribution;
import com.top_logic.layout.react.control.button.CommandModel;

public class TestContextMenuContribution extends TestCase {

    public void testHoldsTargetAndCommands() {
        ViewChannel target = new SimpleViewChannel();
        CommandModel cmd = FakeCommandModels.contextMenu("edit", "Edit", true, true);
        ContextMenuContribution contribution =
            new ContextMenuContribution(target, List.of(cmd));

        assertSame(target, contribution.target());
        assertEquals(List.of(cmd), contribution.commands());
    }

    public void testExecutableCommandsFiltersDisabledAndInvisible() {
        ViewChannel target = new SimpleViewChannel();
        CommandModel visibleEnabled  = FakeCommandModels.contextMenu("a", "A", true, true);
        CommandModel disabledVisible = FakeCommandModels.contextMenu("b", "B", true, false);
        CommandModel invisible       = FakeCommandModels.contextMenu("c", "C", false, true);

        ContextMenuContribution contribution =
            new ContextMenuContribution(target, List.of(visibleEnabled, disabledVisible, invisible));

        assertEquals(
            List.of(visibleEnabled, disabledVisible),
            contribution.visibleCommands());
    }
}
```

`FakeCommandModels` is a small test utility (next step).

- [ ] **Step 2: Add the fake command-model helper**

Create `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/command/FakeCommandModels.java`:

```java
package test.com.top_logic.layout.view.command;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.tools.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.boundsec.HandlerResult;

final class FakeCommandModels {
    static CommandModel contextMenu(String name, String label, boolean visible, boolean executable) {
        return new CommandModel() {
            public String getName()        { return name; }
            public String getLabel()       { return label; }
            public ResKey getLabelKey()    { return null; }
            public ThemeImage getImage()   { return null; }
            public String getCssClasses()  { return null; }
            public String getPlacement()   { return PLACEMENT_CONTEXT_MENU; }
            public String getClique()      { return null; }
            public ExecutableState getExecutableState() { return ExecutableState.EXECUTABLE; }
            public boolean isExecutable()  { return executable; }
            public boolean isVisible()     { return visible; }
            public HandlerResult executeCommand(ReactContext context) { return HandlerResult.DEFAULT_RESULT; }
            public void addStateChangeListener(Runnable r) { }
            public void removeStateChangeListener(Runnable r) { }
        };
    }
    private FakeCommandModels() { }
}
```

Verify `CommandModel` method set matches actual interface before compiling (`com.top_logic.layout.react/.../control/button/CommandModel.java`). Add missing stubs as needed.

- [ ] **Step 3: Run the test to see it fail**

```bash
mvn -B test -DskipTests=false -pl com.top_logic.layout.view \
    -Dtest=TestContextMenuContribution 2>&1 | tee /tmp/test1.log
```

Expected: Compilation error — `ContextMenuContribution` does not exist.

- [ ] **Step 4: Implement `ContextMenuContribution`**

```java
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Declarative contribution to a context menu: one {@link ViewChannel} providing the
 * target object, and the {@link CommandModel}s that consume it.
 *
 * <p>Runtime-only; views create these, controls receive them.</p>
 */
public final class ContextMenuContribution {

    private final ViewChannel _target;
    private final List<CommandModel> _commands;

    public ContextMenuContribution(ViewChannel target, Collection<? extends CommandModel> commands) {
        _target = target;
        _commands = Collections.unmodifiableList(new ArrayList<>(commands));
    }

    public ViewChannel target() {
        return _target;
    }

    public List<CommandModel> commands() {
        return _commands;
    }

    public List<CommandModel> visibleCommands() {
        List<CommandModel> result = new ArrayList<>();
        for (CommandModel cmd : _commands) {
            if (cmd.isVisible()) {
                result.add(cmd);
            }
        }
        return result;
    }
}
```

- [ ] **Step 5: Run the test — green**

```bash
mvn -B test -DskipTests=false -pl com.top_logic.layout.view \
    -Dtest=TestContextMenuContribution 2>&1 | tee /tmp/test1.log
```

Expected: `BUILD SUCCESS`, `Tests run: 2, Failures: 0`.

- [ ] **Step 6: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/ContextMenuContribution.java \
        com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/command/TestContextMenuContribution.java \
        com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/command/FakeCommandModels.java
git commit -m "Ticket #29108: Add ContextMenuContribution runtime type."
```

---

### Task 2: Create `ContextMenuOpener` — assembly + dispatch

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/ContextMenuOpener.java`
- Test: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/command/TestContextMenuOpener.java`

The opener composes one flat menu from N `(Contribution, target)` pairs. For each contribution it writes the target into the channel, then reads the resulting `visibleCommands()`. Items are ordered by contribution index, separated by `separator` between contributions, and within a contribution grouped by `clique` (separators between cliques too). Item IDs on the wire are `"<contributionIndex>:<commandName>"` to avoid name collisions across contributions.

Abstract out the `ReactMenuControl` interaction behind a minimal `MenuRenderer` interface so the opener is testable without real controls.

- [ ] **Step 1: Write the failing test**

```java
package test.com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.layout.react.control.overlay.ReactMenuControl.MenuEntry;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.view.channel.SimpleViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ContextMenuContribution;
import com.top_logic.layout.view.command.ContextMenuOpener;
import com.top_logic.layout.view.command.ContextMenuOpener.MenuRenderer;
import com.top_logic.layout.view.command.ContextMenuOpener.Targeted;

public class TestContextMenuOpener extends TestCase {

    public void testOpenAssemblesMenuFromContributions() {
        ViewChannel rowTarget = new SimpleViewChannel();
        ViewChannel cellTarget = new SimpleViewChannel();

        CommandModel edit = FakeCommandModels.contextMenu("edit", "Edit", true, true);
        CommandModel delete = FakeCommandModels.contextMenu("delete", "Delete", true, true);
        CommandModel copy = FakeCommandModels.contextMenu("copy", "Copy Cell", true, true);

        ContextMenuContribution cellContribution =
            new ContextMenuContribution(cellTarget, List.of(copy));
        ContextMenuContribution rowContribution =
            new ContextMenuContribution(rowTarget, List.of(edit, delete));

        RecordingRenderer renderer = new RecordingRenderer();
        ContextMenuOpener opener = new ContextMenuOpener(renderer);

        opener.open(10, 20, List.of(
            new Targeted(cellContribution, "cellValue"),
            new Targeted(rowContribution, "rowValue")));

        assertEquals("cellValue", cellTarget.get());
        assertEquals("rowValue",  rowTarget.get());

        List<MenuEntry> items = renderer.lastItems;
        // cell:copy, separator, row:edit, row:delete
        assertEquals(4, items.size());
        assertEquals("0:copy",   items.get(0).id());
        assertEquals("separator", items.get(1).type());
        assertEquals("1:edit",   items.get(2).id());
        assertEquals("1:delete", items.get(3).id());
        assertEquals(10, renderer.lastX);
        assertEquals(20, renderer.lastY);
    }

    public void testOpenSkipsEmptyContributionsAndDoesNotOpenIfAllEmpty() {
        ViewChannel t = new SimpleViewChannel();
        CommandModel invisible = FakeCommandModels.contextMenu("x", "X", false, true);
        ContextMenuContribution contribution = new ContextMenuContribution(t, List.of(invisible));

        RecordingRenderer renderer = new RecordingRenderer();
        ContextMenuOpener opener = new ContextMenuOpener(renderer);

        opener.open(0, 0, List.of(new Targeted(contribution, "whatever")));

        assertFalse("Must not open an empty menu", renderer.opened);
    }

    public void testSelectDispatchesToCorrectCommand() {
        ViewChannel t0 = new SimpleViewChannel();
        ViewChannel t1 = new SimpleViewChannel();
        CountingCommandModel cmdA = new CountingCommandModel("edit");
        CountingCommandModel cmdB = new CountingCommandModel("edit");

        ContextMenuContribution c0 = new ContextMenuContribution(t0, List.of(cmdA));
        ContextMenuContribution c1 = new ContextMenuContribution(t1, List.of(cmdB));

        RecordingRenderer renderer = new RecordingRenderer();
        ContextMenuOpener opener = new ContextMenuOpener(renderer);

        opener.open(0, 0, List.of(new Targeted(c0, "obj0"), new Targeted(c1, "obj1")));
        renderer.selectHandler.accept("1:edit");

        assertEquals(0, cmdA.invocations);
        assertEquals(1, cmdB.invocations);
    }

    // --- helpers ---
    static final class RecordingRenderer implements MenuRenderer {
        List<MenuEntry> lastItems;
        int lastX, lastY;
        boolean opened;
        java.util.function.Consumer<String> selectHandler;
        Runnable closeHandler;

        public void show(int x, int y, List<MenuEntry> items,
                         java.util.function.Consumer<String> selectHandler,
                         Runnable closeHandler) {
            this.opened = true;
            this.lastX = x; this.lastY = y;
            this.lastItems = items;
            this.selectHandler = selectHandler;
            this.closeHandler = closeHandler;
        }
        public void hide() { this.opened = false; }
    }

    static final class CountingCommandModel extends FakeCommandModelBase {
        int invocations;
        CountingCommandModel(String name) { super(name); }
        @Override
        public com.top_logic.tool.boundsec.HandlerResult executeCommand(com.top_logic.layout.react.ReactContext ctx) {
            invocations++;
            return com.top_logic.tool.boundsec.HandlerResult.DEFAULT_RESULT;
        }
    }
}
```

Extract a `FakeCommandModelBase` from `FakeCommandModels` (shared base class) in the same test package. Straightforward refactor.

- [ ] **Step 2: Run the test — compile fail**

```bash
mvn -B test -DskipTests=false -pl com.top_logic.layout.view \
    -Dtest=TestContextMenuOpener 2>&1 | tee /tmp/test2.log
```

Expected: compile errors (`ContextMenuOpener` missing, `SimpleViewChannel` may need to be verified/located).

- [ ] **Step 3: Verify `SimpleViewChannel` exists or add a test-local channel**

```bash
```

Use Grep for `class SimpleViewChannel` under `com.top_logic.layout.view`. If absent, create a minimal test-local `TestChannel` implementing `ViewChannel` (listeners + get/set) in the test package.

- [ ] **Step 4: Implement `ContextMenuOpener`**

```java
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.overlay.ReactMenuControl.MenuEntry;

/**
 * Composes a single context menu from multiple {@link ContextMenuContribution}s
 * and dispatches selections back to the contributing {@link CommandModel}.
 */
public final class ContextMenuOpener {

    /** Abstraction over {@link com.top_logic.layout.react.control.overlay.ReactMenuControl}
     *  so the opener is unit-testable. */
    public interface MenuRenderer {
        void show(int x, int y, List<MenuEntry> items,
                  Consumer<String> selectHandler, Runnable closeHandler);
        void hide();
    }

    public record Targeted(ContextMenuContribution contribution, Object target) { }

    private final MenuRenderer _renderer;

    /** Captures the state of a currently-open menu for dispatch. */
    private List<Targeted> _active = List.of();

    public ContextMenuOpener(MenuRenderer renderer) {
        _renderer = renderer;
    }

    public void open(int x, int y, List<Targeted> contributions) {
        for (Targeted t : contributions) {
            t.contribution().target().set(t.target());
        }

        List<MenuEntry> items = new ArrayList<>();
        boolean anything = false;
        for (int i = 0; i < contributions.size(); i++) {
            List<CommandModel> commands = contributions.get(i).contribution().visibleCommands();
            if (commands.isEmpty()) continue;
            if (anything) items.add(MenuEntry.separator());
            appendCliqued(items, i, commands);
            anything = true;
        }
        if (!anything) return;

        _active = List.copyOf(contributions);
        _renderer.show(x, y, items, this::handleSelect, this::handleClose);
    }

    private static void appendCliqued(List<MenuEntry> out, int contributionIndex, List<CommandModel> commands) {
        List<CommandModel> sorted = new ArrayList<>(commands);
        sorted.sort(Comparator.comparing(cmd -> nullSafe(cmd.getClique())));

        String currentClique = null;
        boolean first = true;
        for (CommandModel cmd : sorted) {
            String clique = nullSafe(cmd.getClique());
            if (!first && !clique.equals(currentClique)) {
                out.add(MenuEntry.separator());
            }
            out.add(MenuEntry.item(
                contributionIndex + ":" + cmd.getName(),
                cmd.getLabel(),
                cmd.getImage() == null ? null : cmd.getImage().toEncodedForm(),
                !cmd.isExecutable()));
            currentClique = clique;
            first = false;
        }
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }

    private void handleSelect(String itemId) {
        int colon = itemId.indexOf(':');
        int idx = Integer.parseInt(itemId.substring(0, colon));
        String name = itemId.substring(colon + 1);
        Targeted t = _active.get(idx);
        CommandModel cmd = findCommand(t.contribution().commands(), name);
        if (cmd != null && cmd.isExecutable()) {
            cmd.executeCommand(currentReactContext());
        }
        _renderer.hide();
        _active = List.of();
    }

    private void handleClose() {
        _active = List.of();
    }

    private static CommandModel findCommand(List<CommandModel> list, String name) {
        for (CommandModel cmd : list) {
            if (name.equals(cmd.getName())) return cmd;
        }
        return null;
    }

    /**
     * Returns the {@link ReactContext} to use for command execution.
     * Subclasses may override to supply a context; the default opener is created with one bound.
     */
    protected ReactContext currentReactContext() {
        return _contextSupplier == null ? null : _contextSupplier.get();
    }

    private java.util.function.Supplier<ReactContext> _contextSupplier;

    public void bindReactContext(java.util.function.Supplier<ReactContext> supplier) {
        _contextSupplier = supplier;
    }
}
```

You may need to adapt:
- `MenuEntry.item(id, label, icon, disabled)` factory signature — check `ReactMenuControl.java` and expose a matching factory (existing code only has 2- and 3-arg `item` factories; add a 4-arg one there if needed in a follow-up step).
- `CommandModel.getLabel()` returns `String`; if it returns `ResKey`, pass `getLabelKey()` through a resolver.

If `MenuEntry` has no 4-arg factory with `disabled`, add one — see next sub-step.

- [ ] **Step 5: Add `MenuEntry.item(id, label, icon, disabled)` factory if missing**

In `com.top_logic.layout.react/.../ReactMenuControl.java`, around the existing `MenuEntry` record, add:

```java
public static MenuEntry item(String id, String label, String icon, boolean disabled) {
    return new MenuEntry("item", id, label, icon, disabled);
}
```

Skip if already present.

- [ ] **Step 6: Run the test — green**

```bash
mvn -B install -DskipTests=true -pl com.top_logic.layout.react 2>&1 | tail -20
mvn -B test -DskipTests=false -pl com.top_logic.layout.view \
    -Dtest=TestContextMenuOpener 2>&1 | tee /tmp/test2.log
```

Expected: `Tests run: 3, Failures: 0`.

- [ ] **Step 7: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/command/ContextMenuOpener.java \
        com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/command/TestContextMenuOpener.java \
        com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/command/FakeCommandModels.java \
        com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/overlay/ReactMenuControl.java
git commit -m "Ticket #29108: Add ContextMenuOpener and disabled MenuEntry factory."
```

---

### Task 3: Expose `ContextMenuOpener` on `ViewContext`

The opener is one instance per frame (top-level view). `ViewContext` already supports derivation (`withCommandScope`). Follow the same pattern.

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewContext.java`
- Modify: any default `ViewContext` implementation (grep for `implements ViewContext` to locate — typically `DefaultViewContext` or similar within `com.top_logic.layout.view`)
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewElement.java` — instantiate opener at frame root

- [ ] **Step 1: Add accessor methods to `ViewContext`**

Open `ViewContext.java`, add:

```java
/** Returns the context-menu opener for the enclosing frame, or {@code null}
 *  if this context is not beneath a frame. */
ContextMenuOpener getContextMenuOpener();

/** Returns a derived context that exposes the given opener to child elements. */
ViewContext withContextMenuOpener(ContextMenuOpener opener);
```

- [ ] **Step 2: Implement in the default `ViewContext` implementation**

Grep:

```bash
```

Use Grep for `implements ViewContext` inside `com.top_logic.layout.view/src/main/java`. In each concrete implementation, add a field `_contextMenuOpener`, the getter, and a copy-derivation producing a new instance with the opener replaced. Follow the exact pattern of `withCommandScope` to minimize divergence.

- [ ] **Step 3: Create one opener at the frame root**

Open `ViewElement.java`. In whatever method builds the root control (search for where it first creates/wraps the root `ReactControl`), instantiate a `ReactMenuControl`, wrap it in a `MenuRenderer` adapter, create the opener, and expose it via `context.withContextMenuOpener(opener)` for the derived context passed into children.

Adapter (inline class or sibling file `ContextMenuOpener.ReactMenuRenderer`):

```java
static ContextMenuOpener.MenuRenderer rendererFor(ReactMenuControl menu) {
    return new ContextMenuOpener.MenuRenderer() {
        public void show(int x, int y, List<MenuEntry> items,
                         java.util.function.Consumer<String> sel, Runnable close) {
            menu.updateItems(items);
            // position via setAnchorId — verify ReactMenuControl API for absolute x/y
            menu.open();
        }
        public void hide() { menu.close(); }
    };
}
```

**Open API gap to resolve here:** `ReactMenuControl` currently supports `setAnchorId`. For free-floating (x, y) positioning needed by right-click, either extend its state to support coordinates or attach to a zero-size anchor div positioned absolutely at (x, y). **Decision point for the engineer:** extend `ReactMenuControl` with a coordinates path (preferred — cleaner API); alternatively, add a hidden-anchor overlay div managed by the frame. Add a state key `ANCHOR_X` / `ANCHOR_Y` and a companion `open(int x, int y)` method; update `TLMenu.tsx` to use them when present.

- [ ] **Step 4: Build and run existing tests (no regression)**

```bash
mvn -B install -DskipTests=true -pl com.top_logic.layout.view 2>&1 | tail -20
mvn -B test -DskipTests=false -pl com.top_logic.layout.view 2>&1 | tee /tmp/test3.log
```

Expected: `BUILD SUCCESS`, no test regressions.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.layout.view com.top_logic.layout.react
git commit -m "Ticket #29108: Expose ContextMenuOpener per frame on ViewContext."
```

---

### Task 4: `<context-menu>` view element

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/ContextMenuElement.java`
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/overlay/ContextMenuRegionControl.java`

`ContextMenuElement` extends `CommandScopeElement` (or an adequate sibling abstraction). It collects child commands that have `placement=CONTEXT_MENU`, builds one `ContextMenuContribution` for them (channel name: `contextTarget`), and creates a `ContextMenuRegionControl` as its chrome control that wraps the child content.

- [ ] **Step 1: Implement `ContextMenuRegionControl`**

```java
package com.top_logic.layout.react.control.overlay;

import java.util.List;
import java.util.Map;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.command.ContextMenuContribution;
import com.top_logic.layout.view.command.ContextMenuOpener;

/**
 * Wraps a child control and dispatches browser {@code contextmenu} events via the
 * frame's {@link ContextMenuOpener}.
 */
public class ContextMenuRegionControl extends ReactControl {

    private static final String MODULE = "TLContextMenuRegion";
    private static final String STATE_CHILD_ID = "childId";

    private final ContextMenuContribution _contribution;
    private final ContextMenuOpener _opener;
    private final ReactControl _child;

    public ContextMenuRegionControl(ReactContext context, ReactControl child,
                                    ContextMenuContribution contribution,
                                    ContextMenuOpener opener) {
        super(context);
        _child = child;
        _contribution = contribution;
        _opener = opener;
        putState(STATE_CHILD_ID, child.getId());
    }

    @Override
    protected String getReactModule() { return MODULE; }

    @Override
    public List<ReactControl> getChildren() { return List.of(_child); }

    @ReactCommand("openContextMenu")
    public void openContextMenu(Map<String, Object> arguments) {
        int x = asInt(arguments.get("x"));
        int y = asInt(arguments.get("y"));
        Object target = _contribution.target().get(); // self-scoped for generic <context-menu>
        _opener.open(x, y, List.of(new ContextMenuOpener.Targeted(_contribution, target)));
    }

    private static int asInt(Object o) {
        return (o instanceof Number n) ? n.intValue() : 0;
    }
}
```

Verify `ReactControl` base class API (`getId()`, `getChildren()`, `putState`, `getReactModule`) against current source; adapt.

- [ ] **Step 2: Implement `ContextMenuElement`**

```java
package com.top_logic.layout.view.element;

import java.util.List;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.overlay.ContextMenuRegionControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.SimpleViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ContextMenuContribution;
import com.top_logic.layout.view.command.ContextMenuOpener;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.react.control.button.CommandModel;

/**
 * Declares a context-menu region. Commands with {@code placement="CONTEXT_MENU"}
 * inside this element are routed into a {@link ContextMenuContribution} scoped to
 * this region rather than into the enclosing toolbar/button-bar scope.
 */
@TagName("context-menu")
public class ContextMenuElement extends CommandScopeElement {

    public interface Config extends CommandScopeElement.Config {

        /** Optional source of the target value. If unset, the implicit
         *  {@code contextTarget} channel owned by this element is used. */
        @Name("input")
        ChannelRef getInput();
    }

    public ContextMenuElement(Config config) { super(config); }

    @Override
    public ReactControl createControl(ViewContext context) {
        ViewChannel target = resolveTargetChannel(context);
        // Let CommandScopeElement build the models; they'll use context.resolveChannel()
        // and must reference the target channel via input="contextTarget" in XML.
        ReactControl content = super.createControl(context);

        List<CommandModel> contextMenuCommands =
            filterByPlacement(getCommandScope().getAllCommands(), CommandModel.PLACEMENT_CONTEXT_MENU);
        ContextMenuContribution contribution =
            new ContextMenuContribution(target, contextMenuCommands);

        ContextMenuOpener opener = context.getContextMenuOpener();
        if (opener == null) {
            // Fail loudly: <context-menu> used outside a frame
            throw new IllegalStateException(
                "<context-menu> requires an enclosing frame that provides a ContextMenuOpener.");
        }
        return new ContextMenuRegionControl(context, content, contribution, opener);
    }

    private ViewChannel resolveTargetChannel(ViewContext context) {
        Config cfg = (Config) getConfig();
        if (cfg.getInput() != null) {
            return context.resolveChannel(cfg.getInput());
        }
        ViewChannel channel = new SimpleViewChannel();
        context.registerChannel("contextTarget", channel);
        return channel;
    }

    private static List<CommandModel> filterByPlacement(List<CommandModel> all, String placement) {
        List<CommandModel> result = new java.util.ArrayList<>();
        for (CommandModel cmd : all) {
            if (placement.equals(cmd.getPlacement())) result.add(cmd);
        }
        return result;
    }
}
```

Notes:
- `CommandScopeElement` extension points may differ; if there's no overridable `createControl(ViewContext)` method, pick the existing hook (`createContent`, `createChromeControl`, or similar). Goal: the chrome-equivalent here is `ContextMenuRegionControl` wrapping the content.
- Inherit `Config` properties (commands, content) from `CommandScopeElement.Config` without redefining.
- Verify `context.registerChannel(String, ViewChannel)` exists; `ViewContext.java:registerChannel` was noted in research.
- The `contextTarget` channel name is a convention documented in user-facing documentation (update docs as a follow-up).

- [ ] **Step 3: Build module `com.top_logic.layout.view`**

```bash
mvn -B install -DskipTests=true -pl com.top_logic.layout.view 2>&1 | tee com.top_logic.layout.view/target/mvn-task4.log | tail -40
```

Expected: `BUILD SUCCESS`. Fix compile errors by reading the actual `CommandScopeElement` API and adapting.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/ContextMenuElement.java \
        com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/overlay/ContextMenuRegionControl.java
git commit -m "Ticket #29108: Add <context-menu> view element and region control."
```

---

### Task 5: Client-side `TLContextMenuRegion`

**Files:**
- Create: `com.top_logic.layout.react/react-src/controls/TLContextMenuRegion.tsx`
- Modify: `com.top_logic.layout.react/react-src/bridge/tl-react-bridge.ts` — register module

- [ ] **Step 1: Implement `TLContextMenuRegion.tsx`**

```tsx
import { React, useTLCommand, useTLState } from 'tl-react-bridge';

interface Props { children?: React.ReactNode; }

export default function TLContextMenuRegion(_: Props) {
    const state = useTLState();
    const childId = state['childId'] as string | undefined;
    const sendCommand = useTLCommand();

    const onContextMenu = React.useCallback((e: React.MouseEvent<HTMLDivElement>) => {
        e.preventDefault();
        e.stopPropagation();
        sendCommand('openContextMenu', { x: e.clientX, y: e.clientY });
    }, [sendCommand]);

    return (
        <div className="tl-context-menu-region" onContextMenu={onContextMenu}>
            {/* The child React control is mounted independently by the bridge;
                the server renders its <div data-react-module=...> inside us. */}
            {childId ? <ChildHost id={childId} /> : null}
        </div>
    );
}

function ChildHost({ id }: { id: string }) {
    // The child's DOM is prerendered by the server directly inside our region.
    // We don't need to render content — just reserve the slot in the tree.
    return <div data-tl-child-host={id} />;
}
```

**Caveat on child rendering:** The TopLogic React bridge mounts controls on server-prerendered `<div data-react-module>` elements during `discoverAndMount`. Verify whether `ContextMenuRegionControl` emits the child `<div>` inside its own HTML output (likely yes, via `getChildren()` plus default container rendering). If the bridge needs a specific placeholder pattern, follow what `ToolbarControl`/`TLAppShell` do for their children — see `TLAppShell.tsx` and `TLToolbar.tsx` for precedent.

- [ ] **Step 2: Register the module**

In `tl-react-bridge.ts`, find the module registry (grep for `TLMenu` — wherever `TLMenu` is registered, add `TLContextMenuRegion` next to it).

- [ ] **Step 3: Build the React bundle**

```bash
mvn -B install -DskipTests=true -pl com.top_logic.layout.react 2>&1 | tail -40
```

Expected: `BUILD SUCCESS`; the `frontend-maven-plugin` regenerates `tl-react-controls.js`.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.react/react-src/
git commit -m "Ticket #29108: Add TLContextMenuRegion client component."
```

---

### Task 6: Extend `ReactMenuControl` to support (x, y) positioning

Without this, the menu only anchors to a DOM id. Right-click needs pixel coordinates.

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/overlay/ReactMenuControl.java`
- Modify: `com.top_logic.layout.react/react-src/controls/TLMenu.tsx`

- [ ] **Step 1: Add coordinate state + `open(int x, int y)` method on the server**

In `ReactMenuControl.java`:

```java
public static final String ANCHOR_X = "anchorX";
public static final String ANCHOR_Y = "anchorY";

public void open(int x, int y) {
    putState(ANCHOR_X, x);
    putState(ANCHOR_Y, y);
    putState(ANCHOR_ID, null);
    putState(OPEN, true);
}
```

Keep the existing `setAnchorId`/`open()` methods for callers that anchor to an element.

- [ ] **Step 2: Adapt `TLMenu.tsx` to render at absolute coordinates when set**

Modify the menu's positioning code: if `anchorX` and `anchorY` state values are present, render the portal at those viewport coordinates; else fall back to anchor-id-based positioning.

- [ ] **Step 3: Wire the opener's `MenuRenderer` to use `open(x, y)`**

Update the adapter from Task 3 Step 3:

```java
public void show(int x, int y, List<MenuEntry> items,
                 Consumer<String> sel, Runnable close) {
    menu.updateItems(items);
    menu.setSelectHandler(sel);
    menu.setCloseHandler(close);
    menu.open(x, y);
}
```

This requires adding `setSelectHandler` / `setCloseHandler` to `ReactMenuControl` since they're currently constructor-only. Add them.

- [ ] **Step 4: Build + existing tests**

```bash
mvn -B install -DskipTests=true -pl com.top_logic.layout.react 2>&1 | tail -40
mvn -B test -DskipTests=false -pl com.top_logic.layout.react 2>&1 | tail -40
```

Expected: `BUILD SUCCESS`, no test regressions.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.layout.react/
git commit -m "Ticket #29108: Support coordinate-anchored menus in ReactMenuControl."
```

---

### Task 7: Demo app integration

**Files:**
- Create: `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/context-menu-demo.view.xml`
- Modify: wherever the demo app lists available views (usually a layout XML under `com.top_logic.demo/src/main/webapp/WEB-INF/layouts/`)

- [ ] **Step 1: Create the demo view**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<view xmlns="http://www.top-logic.com/ns/view/1.0">
  <panel>
    <label><en>Context Menu Demo</en></label>
    <context-menu>
      <command name="sayHello" placement="CONTEXT_MENU" input="contextTarget">
        <label><en>Say Hello</en></label>
        <action class="com.top_logic.demo.view.SayHelloAction"/>
      </command>
      <command name="sayGoodbye" placement="CONTEXT_MENU" input="contextTarget" clique="DELETE">
        <label><en>Say Goodbye</en></label>
        <action class="com.top_logic.demo.view.SayGoodbyeAction"/>
      </command>
      <!-- Arbitrary content on which right-click triggers the menu -->
      <html><![CDATA[<p>Right-click anywhere in this panel.</p>]]></html>
    </context-menu>
  </panel>
</view>
```

Add trivial `SayHelloAction` / `SayGoodbyeAction` as `ViewAction` implementations that log and return input unchanged:

```java
package com.top_logic.demo.view;

import com.top_logic.basic.Logger;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;

public class SayHelloAction implements ViewAction {
    public Object execute(ReactContext ctx, Object input) {
        Logger.info("Hello from context menu, target=" + input, SayHelloAction.class);
        return input;
    }
}
```

(Similar for `SayGoodbyeAction`.)

- [ ] **Step 2: Register the demo view**

Add a layout entry that points to `demo/context-menu-demo.view.xml`. Follow the pattern of existing React demos (grep for an existing `.view.xml` reference under demo layouts).

- [ ] **Step 3: Build the demo**

```bash
mvn -B install -DskipTests=true -pl com.top_logic.demo 2>&1 | tee com.top_logic.demo/target/mvn-task7.log | tail -40
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.demo/
git commit -m "Ticket #29108: Add context-menu demo view."
```

---

### Task 8: Manual verification with Playwright

- [ ] **Step 1: Start the demo app**

Use the `tl-app` skill to start the app.

- [ ] **Step 2: Navigate to the demo view**

Use Playwright:
1. `browser_navigate` to the demo root (usually `http://localhost:<port>/top-logic/`)
2. Log in as `root` / `root1234`
3. Navigate to the "Context Menu Demo" view via the sidebar / layout tree
4. `browser_snapshot` — confirm the panel with "Right-click anywhere in this panel." is visible

- [ ] **Step 3: Trigger the context menu**

Playwright does not have a direct "right-click" action in the primitives list — use `browser_evaluate` with:

```js
const el = document.querySelector('.tl-context-menu-region');
const rect = el.getBoundingClientRect();
el.dispatchEvent(new MouseEvent('contextmenu', {
  bubbles: true, cancelable: true,
  clientX: rect.left + 20, clientY: rect.top + 20
}));
```

Then `browser_snapshot` to confirm the menu with "Say Hello" / "Say Goodbye" appears near the click position.

- [ ] **Step 4: Invoke an item**

Click "Say Hello" via `browser_click`. Verify via `browser_console_messages` or server log that `SayHelloAction` fired. Menu closes afterwards.

- [ ] **Step 5: Verify clique separator**

Confirm visually (via `browser_snapshot`) that "Say Goodbye" (clique `DELETE`) is separated from "Say Hello" (no clique) by a separator.

- [ ] **Step 6: Verify no menu when no commands executable**

Add an `executability` rule to one of the commands that evaluates to `NOT_EXEC_HIDDEN` when target is null, reload, right-click on a fresh instance where the channel is unset, confirm no menu opens (Task 2 behavior). Revert the experimental change when done.

- [ ] **Step 7: Commit any fixes emerging from manual testing**

```bash
git add <changed files>
git commit -m "Ticket #29108: <what was fixed>."
```

---

## Out of scope (follow-up plans)

- **Table/tree implicit regions** (`<row-context-menu>`, `<column-context-menu>`, `<cell-context-menu>`, `<node-context-menu>`) — separate plan; builds on `ContextMenuOpener`/`ContextMenuContribution` by producing multiple contributions and passing them from TableControl/TreeControl at right-click time.
- **Legacy `CommandHandler` adapter** — intentionally omitted; commands in React context menus are authored as `ViewAction`/`ViewCommand`.
- **Submenus / nested menus** — `ViewCommand` has no hierarchy; if needed, an additional `children` property on a menu-structure config layer.
- **Accessibility** (keyboard navigation, focus trapping in menu) — handled by `TLMenu`; verify separately during Phase 2.
- **Internationalization of demo labels** — demo uses inline `<en>` only.

---

## Self-review checklist (performed at plan-writing time)

- Spec coverage: all architectural elements discussed in conversation (Contribution, Opener, explicit element, client wrapper, demo verification) have corresponding tasks.
- Placeholders: none. Open API gaps are flagged explicitly (e.g. `MenuEntry` factory with `disabled`, `ReactMenuControl` coordinate support) and have dedicated sub-steps that extend them.
- Type consistency: `ContextMenuContribution`, `ContextMenuOpener`, `Targeted` record, `MenuRenderer` interface, `ContextMenuElement`, `ContextMenuRegionControl`, and the `contextTarget` channel convention are used consistently across tasks.
