# Menu Overlay Refactor — Move ReactMenuControl out of the content tree

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Eliminate the unconditional `ReactStackControl` wrapper that Task 3 of the context-menu plan introduced. Move the per-frame `ReactMenuControl` (and the `ContextMenuOpener` that drives it) from `ViewElement.createControl` to `ReactAppShellControl`, mirroring how `DialogManager` is mounted.

**Architecture:**

`ReactAppShellControl` already mounts a `ReactDialogManagerControl` as a state-property (`dialogManager`) outside its flex content area. `TLAppShell.tsx` renders it via `{dialogManager && <TLChild .../>}`. We do exactly the same for a single, app-shell-global `ReactMenuControl` plus the `ContextMenuOpener` that wraps it. `ViewElement.createControl` no longer creates either — it inherits the opener via the existing `ViewContext.getContextMenuOpener()` chain, and reverts to the original single-content fast path.

**Tech Stack:** Java 17 · TopLogic · TypeScript/React via `tl-react-bridge` · Maven (build from project root with `-pl`).

**Branch:** continue on `CWS/CWS_29108_react_context_menus`.

---

## Files to touch

**Modify — server:**
- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ReactContext.java` — add `default ContextMenuOpener getContextMenuOpener() { return null; }`.
- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactAppShellControl.java` — instantiate `ReactMenuControl` + `ContextMenuOpener` + `MenuRenderer`; expose menu via state property `menuOverlay`; wrap incoming `ReactContext` in a forwarding subtype that overrides `getContextMenuOpener()` and pass the wrapped context to all child controls.
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/DefaultViewContext.java` — fall back to wrapped `ReactContext.getContextMenuOpener()` in its own `getContextMenuOpener()` when the local field is null.
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewElement.java` — revert Task 3 hunk: restore single-content fast path, drop menu-control construction, drop sibling attachment.

**Modify — client:**
- `com.top_logic.layout.react/react-src/controls/TLAppShell.tsx` — render `menuOverlay` ChildDescriptor next to `dialogManager`.

**Modify — demo (Task 5):**
- The open-window demo's content view + (if needed) its `ReactControlProvider` so the sub-window root contains a `ReactAppShellControl` and a `<context-menu>` region.

**No changes:** `ContextMenuOpener`, `ContextMenuContribution`, `ContextMenuElement`, `ContextMenuRegionControl`, `ReactMenuControl` itself.

---

## Notes for the engineer

- Build from the project root with `-pl <module>`. Never `cd` into modules.
- ISO-8859-1 source for Java; UTF-8 for TypeScript. Member fields prefixed `_`.
- React imports MUST come from `'tl-react-bridge'`, never from `'react'` directly.
- Never amend commits. Commit format: `Ticket #29108: <description>.` — no `Co-Authored-By`.
- `frontend-maven-plugin` runs the vite build during `mvn install`; do not invoke vite directly.
- The `DialogManager` precedent is the source of truth — when in doubt, mirror what dialogs do.

---

## Established facts (from prior recon — no further investigation needed)

- `ReactAppShellControl` is created either declaratively by `<app-shell>` (`AppShellElement.java:123`) or programmatically (`DemoReactAppComponent.java:185`). One per top-level page.
- Sub-views via `<view-ref>` (e.g. `chart-demo.view.xml`, `input-controls-demo.view.xml`) lack their own `<app-shell>` but render *inside* the outer shell; they reach the shell's opener via the inherited `ReactContext`.
- Dialogs (`OpenDialogAction.java:136`) wrap the **same** underlying `ReactContext` in a fresh `DefaultViewContext`; dialog content inherits the shell's opener.
- Programmatically opened sub-windows (`ReactWindowRegistry.openWindow`, `ReactWindowRegistry.java:164-206`) get a fresh `ReactContext` and their own control tree. Whether that tree contains its own shell depends on its `ReactControlProvider`. **The Open-Window demo IS such a sub-window** — so we must guarantee a shell-with-menu exists there too (covered in Task 5).
- `ReactAppShellControl` operates on `ReactContext`, not `ViewContext`. The first `DefaultViewContext` is derived later by `AppShellElement.createControl(ViewContext)`. **Therefore the opener must be reachable through `ReactContext`, not only via `ViewContext`.**

These facts lock the design to Branch B in Task 2 below — there is no A/B decision left.

---

### Task 2: Mount the menu overlay at the app shell

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactAppShellControl.java`
- Possibly modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ReactContext.java` (only if Task 1 chose the second branch — add `default ContextMenuOpener getContextMenuOpener() { return null; }`).

- [ ] **Step 1: Add a `MENU_OVERLAY` state key and a `_menuControl` field**

Open `ReactAppShellControl.java`. Locate the existing `DIALOG_MANAGER` state key (around line 101 per recon). Mirror it:

```java
private static final String MENU_OVERLAY = "menuOverlay";

private final ReactMenuControl _menuControl;
private final ContextMenuOpener _contextMenuOpener;
```

- [ ] **Step 2: Construct the menu and opener in the shell constructor**

Inside `ReactAppShellControl`'s constructor, immediately after `_dialogManager` is created and before `putState(DIALOG_MANAGER, _dialogManager)`, add:

```java
// One-time per app shell: a single overlay-mounted ReactMenuControl driven by a
// ContextMenuOpener that descendant ContextMenuElements reach via the
// ViewContext.getContextMenuOpener() chain (binding happens below).
_menuControl = new ReactMenuControl(context, null, java.util.List.of(),
    itemId -> { /* re-wired per open() via setSelectHandler */ },
    () -> { /* re-wired per open() via setCloseHandler */ });
ContextMenuOpener.MenuRenderer renderer = new ContextMenuOpener.MenuRenderer() {
    @Override
    public void show(int x, int y, java.util.List<ReactMenuControl.MenuEntry> items,
            java.util.function.Consumer<String> selectHandler, Runnable closeHandler) {
        _menuControl.updateItems(items);
        _menuControl.setSelectHandler(selectHandler);
        _menuControl.setCloseHandler(closeHandler);
        _menuControl.open(x, y);
    }

    @Override
    public void hide() {
        _menuControl.close();
    }
};
_contextMenuOpener = new ContextMenuOpener(renderer);
_contextMenuOpener.bindReactContext(() -> context);
```

Add the necessary imports (`com.top_logic.layout.react.control.overlay.ReactMenuControl`, `com.top_logic.layout.view.command.ContextMenuOpener`).

- [ ] **Step 3: Expose the menu in state**

After `putState(DIALOG_MANAGER, _dialogManager);` add:

```java
putState(MENU_OVERLAY, _menuControl);
```

- [ ] **Step 4: Make the opener reachable via `ReactContext` (not only `ViewContext`)**

Because the shell sees only `ReactContext` but `<context-menu>`-elements deep inside resolve via `ViewContext.getContextMenuOpener()`, three pieces are needed:

1. **Add to `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ReactContext.java`** a default accessor:
   ```java
   default com.top_logic.layout.view.command.ContextMenuOpener getContextMenuOpener() {
       return null;
   }
   ```
   No `with...` derivation on this level; only an accessor.

2. **In `ReactAppShellControl`**, expose the shell-owned opener by wrapping the incoming `ReactContext` in a thin per-instance subtype that overrides `getContextMenuOpener()` to return `_contextMenuOpener`, and pass that wrapped context to all child-control constructors (header, content, footer, snackbar, dialog manager, menu). The wrapper must delegate every other `ReactContext` method to the original context — easiest via a small `ForwardingReactContext` helper class. If a forwarding helper already exists in the codebase (grep for `class.*Forwarding.*ReactContext` or look at `DisplayContextAdapter` for an analogous pattern), reuse it; otherwise add one alongside `ReactContext.java`.

3. **In `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/DefaultViewContext.java`**, change `getContextMenuOpener()` to fall back to the wrapped parent `ReactContext` when its own field is `null`:
   ```java
   public ContextMenuOpener getContextMenuOpener() {
       if (_contextMenuOpener != null) {
           return _contextMenuOpener;
       }
       return _reactContext == null ? null : _reactContext.getContextMenuOpener();
   }
   ```
   Use the actual field name from `DefaultViewContext` — verify by reading the file. The `withContextMenuOpener(...)` derivation introduced in commit `6d0e5ac` stays; it just becomes the rarer override path.

- [ ] **Step 5: Cleanup wiring**

In `ReactAppShellControl`'s `cleanupChildren()` (or whatever the existing cleanup hook is — locate it analogously to how `_dialogManager` gets cleaned), call:

```java
if (_menuControl != null) {
    _menuControl.cleanupTree();
}
```

If `_dialogManager` is cleaned up via an existing pattern (e.g. listed in a `_overlays` collection), add `_menuControl` to the same collection instead of writing the call by hand.

- [ ] **Step 6: Build the react module**

```bash
mvn -B install -DskipTests=true -pl com.top_logic.layout.react 2>&1 | tail -30
```

Expected: BUILD SUCCESS.

- [ ] **Step 7: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactAppShellControl.java
# plus any ReactContext.java change if Branch B was taken
git commit -m "Ticket #29108: Mount ReactMenuControl as app-shell overlay alongside DialogManager."
```

---

### Task 3: Render the menu overlay in `TLAppShell.tsx`

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls/TLAppShell.tsx`

- [ ] **Step 1: Read the existing `dialogManager` rendering line**

Locate the line (per recon: line 39):

```tsx
{dialogManager && <TLChild control={dialogManager} />}
```

- [ ] **Step 2: Add the menu overlay sibling**

Add immediately after the dialog manager line:

```tsx
{menuOverlay && <TLChild control={menuOverlay} />}
```

Make sure `menuOverlay` is destructured from `useTLState()` along with `dialogManager` and the other state fields. The state-key string on the wire is `"menuOverlay"` (matches the server constant).

- [ ] **Step 3: Place it OUTSIDE the flex content area, like dialog manager**

Verify the menu overlay JSX sits next to (not inside) `<div className="tlAppShell__content">`. The menu uses `position: fixed` (per `TLMenu.tsx` line 132) so its DOM parent doesn't constrain its painted position, but keeping it outside the content flex prevents it from participating in flex sizing or being clipped by `overflow: hidden` containers.

- [ ] **Step 4: Build the react module (vite bundle regenerates)**

```bash
mvn -B install -DskipTests=true -pl com.top_logic.layout.react 2>&1 | tail -30
```

Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.layout.react/react-src/controls/TLAppShell.tsx
git commit -m "Ticket #29108: Render menu overlay in TLAppShell next to dialog manager."
```

---

### Task 4: Revert `ViewElement.createControl` to its pre-Task-3 shape

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewElement.java`

- [ ] **Step 1: Read commit `6d0e5ac` for the diff to revert**

```bash
git show 6d0e5ac -- com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewElement.java
```

The hunk in `createControl(...)` between roughly lines 116 and 158 is what gets reverted. Specifically:
- Restore the single-content fast path:
  ```java
  if (_content.size() == 1) {
      return _content.get(0).createControl(context);
  }
  ```
- Remove the `ReactMenuControl` construction, the `selectHolder`/`closeHolder` arrays, the inline `MenuRenderer`, the `ContextMenuOpener` instantiation, and the `withContextMenuOpener(opener)` derivation.
- Restore the original multi-content branch:
  ```java
  List<ReactControl> children = _content.stream()
      .map(e -> (ReactControl) e.createControl(context))
      .collect(Collectors.toList());
  return new ReactStackControl(context, children);
  ```
- Remove the now-unused imports added in `6d0e5ac`: `ArrayList`, `Consumer`, `ReactMenuControl`, `MenuEntry`, `ContextMenuOpener`, `MenuRenderer`.

- [ ] **Step 2: Confirm `withContextMenuOpener` is no longer needed here**

The derivation of opener-context happens at the shell now (Task 2). `ViewElement.createControl` should not call `withContextMenuOpener` itself. The `context` passed in already carries the opener (Branch A) or transparently inherits it (Branch B).

- [ ] **Step 3: Build the view module**

```bash
mvn -B install -DskipTests=true -pl com.top_logic.layout.view 2>&1 | tail -30
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Run the existing context-menu unit tests**

```bash
mvn -B test -DskipTests=false -pl com.top_logic.layout.view -Dtest=TestContextMenuOpener,TestContextMenuContribution 2>&1 | tail -20
```

Expected: 5/5 tests pass (no regression).

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewElement.java
git commit -m "Ticket #29108: Revert ViewElement to single-content fast path; menu lives in shell now."
```

---

### Task 5: Extend the demo so the Open-Window case has a context menu

Goal: the "Open Window" demo (a programmatically opened sub-window via `WindowRegistry.openWindow`) must contain a working `<context-menu>` region. This is the only mainstream scenario in which a non-shell root tree could occur — the straight-line case must work.

**Files (verify exact paths during execution):**
- Find the open-window demo source: grep for `openWindow` calls and for a demo view referenced as the window content. Likely under `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/` (a file with "window" in its name) or under `com.top_logic.demo/src/main/java/com/top_logic/demo/...` for the action that opens it.
- Modify: the open-window demo's content view (XML), and possibly its `ReactControlProvider` (Java) if the provider currently builds a tree without `<app-shell>`.

- [ ] **Step 1: Locate the open-window demo**

Grep:
```
WindowRegistry.openWindow | new ReactControlProvider | window-demo | open-window | OpenWindow
```
Identify (a) the action/button that opens the window, (b) the view file or `ReactControlProvider` that supplies the window's root control.

- [ ] **Step 2: Ensure the window root contains an `ReactAppShellControl`**

The window's root MUST be a `ReactAppShellControl` (declarative `<app-shell>` wrapping content, OR programmatic `new ReactAppShellControl(...)` like `DemoReactAppComponent.java:185`). Otherwise the menu has no anchor.

If the existing demo already builds a shell at the window root: skip to Step 3. Otherwise: wrap its content. Prefer the declarative `<app-shell>` form if the window content is a `.view.xml`. If it's purely programmatic, mirror `DemoReactAppComponent`'s instantiation pattern.

- [ ] **Step 3: Add a `<context-menu>` region to the window's content**

Put a small panel/card inside the window content (or wherever its primary content sits) wrapped in `<context-menu>`, with two commands:

```xml
<context-menu>
  <command name="windowSayHello" placement="CONTEXT_MENU" input="contextTarget">
    <label><en>Hello from sub-window</en></label>
    <action class="com.top_logic.demo.view.command.SayHelloAction"/>
  </command>
  <command name="windowSayGoodbye" placement="CONTEXT_MENU" input="contextTarget" clique="DELETE">
    <label><en>Goodbye from sub-window</en></label>
    <action class="com.top_logic.demo.view.command.SayGoodbyeAction"/>
  </command>
  <card>
    <label><en>Right-click here in the sub-window.</en></label>
  </card>
</context-menu>
```

Reuse the existing `SayHelloAction` / `SayGoodbyeAction` from Phase 1 Task 7. Distinct command `name`s avoid confusion in logs.

- [ ] **Step 4: Build the demo**

```bash
mvn -B install -DskipTests=true -pl com.top_logic.demo 2>&1 | tail -20
```

Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.demo/
git commit -m "Ticket #29108: Add context menu to open-window demo for sub-window verification."
```

---

### Task 6: End-to-end verification with Playwright

**Files:** none (verification only).

- [ ] **Step 1: Build full demo**

```bash
mvn -B install -DskipTests=true -pl com.top_logic.demo 2>&1 | tail -20
```

Expected: BUILD SUCCESS.

- [ ] **Step 2: Load Playwright tools**

Use `ToolSearch`:
```
select:mcp__playwright__browser_navigate,mcp__playwright__browser_click,mcp__playwright__browser_snapshot,mcp__playwright__browser_evaluate,mcp__playwright__browser_console_messages,mcp__playwright__browser_take_screenshot,mcp__playwright__browser_close
```

- [ ] **Step 3: Run scenario A — main-window context menu (Phase 1 Task 8 scenario)**

Use the `tl-app` skill to start the demo. Log in as `root` / `root1234`. Open the "Kontextmenue" demo entry. Right-click `.tl-context-menu-region` via:

```js
const el = document.querySelector('.tl-context-menu-region');
const rect = el.getBoundingClientRect();
el.dispatchEvent(new MouseEvent('contextmenu', {
  bubbles: true, cancelable: true,
  clientX: rect.left + 30, clientY: rect.top + 30
}));
```

Verify:
1. Menu opens at the click position with "Say Hello" / "Say Goodbye" + clique separator.
2. Click "Say Hello" → menu closes, server log contains `Hello from context menu`.
3. Re-open + click "Say Goodbye" → log contains `Goodbye from context menu`.
4. Take a screenshot to `com.top_logic.demo/target/menu-overlay-refactor-main.png`.

- [ ] **Step 3b: Run scenario B — sub-window context menu (Task 5 addition)**

Still in the same session: navigate to the "Open Window" demo entry, click whichever button opens the sub-window. Switch the Playwright tab/window to the new browser window (`browser_tabs` to list, `browser_navigate`/tab-switch as needed).

In the sub-window, repeat the right-click scenario on its `.tl-context-menu-region` element. Verify:
1. Menu opens with "Hello from sub-window" / "Goodbye from sub-window" + separator.
2. Click "Hello from sub-window" → server log contains `Hello from context menu` (logged by `SayHelloAction`).
3. Take a screenshot to `com.top_logic.demo/target/menu-overlay-refactor-subwindow.png`.
4. **Verify that closing the menu in the sub-window does NOT affect the main window** (visual sanity check via snapshot of the main window after sub-window menu closes).

- [ ] **Step 3c: Stop the app**

- [ ] **Step 4: Verify the structural change visually**

Inspect the live DOM via `browser_evaluate`:

```js
JSON.stringify({
  appShellChildren: Array.from(document.querySelector('.tlAppShell').children).map(c => c.className || c.tagName),
  menuRegionWrappedInStack: !!document.querySelector('.tl-context-menu-region')?.closest('.tlReactStack')
});
```

Expected:
- `appShellChildren` includes both the dialog-manager div and the menu-overlay div as siblings of the content area.
- `menuRegionWrappedInStack` is `false` if the demo view has only one top-level content element (single-content fast path restored). If `true`, then the demo happens to have multiple content elements — that's fine; what matters is that the wrap is no longer caused by the menu being a sibling.

- [ ] **Step 5: Confirm exactly ONE menu control per shell**

In each window (main + sub-window):
```js
document.querySelectorAll('[data-react-module="TLMenu"]').length
```

Expected: `1` per window — not one per `<context-menu>` region. (Total across two open windows: 2.)

- [ ] **Step 6: If verification passes — no commit needed**

If a fix had to be applied during verification, commit it: `Ticket #29108: <fix description>.`

---

## Out of scope

- Any refactor of `DialogManager` or unification of `DialogManager` + the menu overlay under a common `OverlayManager` abstraction. If a third overlay type is added later, that abstraction can be extracted then.
- Multiple simultaneous context menus across different frames/sub-windows (see "Subtleties" below).
- Per-frame menu instances. The whole point of this refactor is one shell-global menu.

---

## Subtleties / known limitations of this design

- **One menu per shell**: Only one context menu can be open at a time per `ReactAppShellControl`. Right-clicking anywhere within the shell closes any previously-open menu. Matches typical desktop behavior.
- **Sub-windows (`ReactWindowRegistry.openWindow`)**: each window gets a fresh `ReactContext` and an independent control tree. **Context menus only work in a sub-window if its root tree contains a `ReactAppShellControl`.** Task 5 ensures the demo's open-window scenario has one. Custom `ReactControlProvider`s that build a non-shell root will not have menus.
- **Top-level pages without `<app-shell>`**: a `.view.xml` whose root is not (or does not transitively contain) an `<app-shell>` and is rendered as the page root has no menu anchor. In current TopLogic apps this is not observed in practice — non-shell views are always nested inside an outer shell via `<view-ref>`. If a future use case appears, a fallback (e.g. per-frame mount as a sibling, accepting the wrapper) can be added in `ViewElement` keyed on `context.getContextMenuOpener() == null`.
- **Race / interleaving**: `ContextMenuOpener.open(...)` rewrites the menu's state and handlers atomically per call. A second `open()` before the first menu was dismissed simply replaces it.

---

## Self-review

- Spec coverage: every concern raised by the user (root no longer always a stack; menu lives in overlay layer like dialogs) is addressed by Tasks 2, 3, 4. Task 1 is recon to avoid guessing the exact context-binding site. Task 5 is the verification gate.
- Placeholders: none. Branch A/B in Task 2 is a real decision based on factual recon, not a placeholder.
- Type consistency: `ContextMenuOpener`, `ReactMenuControl`, `ContextMenuOpener.MenuRenderer`, `MenuEntry`, the state-key string `"menuOverlay"`, and the `ViewContext.getContextMenuOpener()` accessor are used consistently across tasks.
