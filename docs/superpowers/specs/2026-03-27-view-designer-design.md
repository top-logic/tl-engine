# View Designer: Unified UI Editor for React-based Views

**Date**: 2026-03-27
**Ticket**: #29108
**Status**: Draft

## Problem

The legacy TopLogic UI has separate Layout Editor and Form Editor, both integrated into
burger menus. This approach requires navigating to the exact location you want to edit,
which is cumbersome -- especially when a misconfiguration has broken the very UI you need
to navigate through.

The new React-based View/UIElement configuration layer unifies the UI into a single
ReactControl tree, eliminating the distinction between layout and form. A new, unified
editor is needed.

## Solution Overview

A **View Designer** -- an independent browser window that shows the complete application
structure as a tree of UIElement configurations, paired with a configuration form for the
selected element. Changes are written back to `.view.xml` files and applied live to the
running application.

### Key Design Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Editor scope | Structure, properties, channels, commands | Model editor stays separate |
| Persistence | Direct to `.view.xml` files | Simplest first step; no runtime personalization layer needed |
| Window model | Separate browser window (`window.open`) | Full screen for both app and editor; dual-monitor friendly |
| Navigation | Jump to View deferred to later iteration | Semantics unclear for composable views; needs more design work |
| Tree granularity | Full UIElement.Config hierarchy | Every UIElement is a node, including ReferenceElement and ViewElement |
| Adding elements | Context menu in tree | "Add child" / "Insert sibling" with type selection from list |
| Applying changes | Explicit "Apply" button | Avoids broken intermediate states from auto-apply |
| Data layer | UIElement.Config instances only | No UIElement instantiation needed for the design view |

## Architecture

The View Designer is itself a View (`.view.xml`), loaded via the standard ViewServlet at
`/view/designer`. It uses the same infrastructure as any other view: channels for reactive
data, ViewCommands for server-side logic, SSE for updates.

```
+---------------------------+         +---------------------------+
|   Main Window (App)       |         |  Designer Window          |
|                           |         |  /view/designer           |
|  Running View with        |         |                           |
|  IReactControl tree       |  <-SSE- |  UIElement.Config tree    |
|                           |         |  (left)                   |
|                           |         |  ConfigEditorControl      |
|  ViewServlet /view/*      |         |  (right)                  |
+---------------------------+         +---------------------------+
         ^                                    |
         |                                    |
         |  View reload          ViewCommand: |
         +--------- ViewLoader <-- Save XML   |
                    (cache invalidation)      |
```

### Data Flow: Apply

1. User clicks "Apply" in the designer
2. `SaveDesign` ViewCommand receives the modified configuration
3. Command determines which `.view.xml` file(s) are affected
4. Serializes changed UIElement.Config trees back to XML via `ConfigurationWriter`
   (re-serialization from the Config model; exact formatting preservation is not a goal)
5. Writes the file(s) to disk
6. ViewLoader cache invalidation happens automatically via timestamp-based invalidation:
   `ViewLoader.getOrLoadView()` checks `file.lastModified()` against the cached timestamp
   and re-parses if the file has changed. No explicit invalidation API needed.
7. Triggers a view reload in the main window via server-side control interaction
   (both windows share the same session and SSE channel)
8. Main window rebuilds its IReactControl tree from the fresh configuration

### Cross-Window Communication

The designer is opened as a window within the React UI (e.g., via `ReactWindowControl`).
Both windows share the same server-side session and SSE channel. Communication happens
through normal server-side ReactControl interaction — no window-name lookup or special
event types needed.

After `SaveDesign` writes the `.view.xml` and the ViewLoader cache is invalidated, the
command triggers a view reload in the main window by updating the relevant server-side
control state. The main window receives the update through the normal SSE mechanism.


### Config-Only Loading Path

`ViewLoader.getOrLoadView()` currently returns instantiated `ViewElement` objects, not
`ViewElement.Config`. The designer needs the Config layer only. Two options:

- **Preferred**: Add a `getOrLoadConfig()` method to `ViewLoader` that stops after XML
  parsing and returns the `ViewElement.Config` without calling `context.getInstance()`.
- **Alternative**: Parse the XML independently in the designer commands.

## Editor Layout

### SplitPanel Structure

```
+------------------------------------------------------------------+
| Toolbar: [Jump to View] [Apply] [Revert]                        |
+----------------------------+-------------------------------------+
|                            |                                     |
|  Application Tree          |  Configuration Form                 |
|                            |                                     |
|  v AppShell                |  TagName: Panel                     |
|    v AppBar                |  title-key: contacts.panel.title    |
|      TabBar                |  css-class: my-panel                |
|    v Panel "Kontakte"  <-- |  collapsible: true                  |
|      v Form                |  ...                                |
|        Field "name"        |                                     |
|        Field "email"       |                                     |
|      Table                 |                                     |
|    > ReferenceElement      |                                     |
|      > Panel "Settings"    |                                     |
|                            |                                     |
+----------------------------+-------------------------------------+
```

### Tree (Left Panel)

- Root is the application's root view
- Every UIElement.Config is a node -- including `ViewElement`, `ReferenceElement`, etc.
- A `ReferenceElement` shows the referenced view's children beneath it
  (referenced `.view.xml` files are eagerly loaded and parsed at design time)
- Node label: TagName + identifying attribute (e.g., `Panel "Kontakte"`, `Field attribute="name"`)
- Nodes indicate which `.view.xml` file they originate from (e.g., via tooltip)
- When editing an element from a referenced view, the change is written to that
  element's source `.view.xml` file (not the file containing the ReferenceElement)
- **Context menu** on right-click:
  - "Add child" -- type selection from available UIElement types
  - "Insert sibling before/after" -- type selection
  - "Remove"
  - "Move up" / "Move down"

### Configuration Form (Right Panel)

- `ConfigEditorControl` renders the **scalar** TypedConfiguration properties of the
  selected UIElement.Config (PLAIN, REF, ITEM properties)
- **Structural list properties** (children, channels, command lists) are NOT shown in the
  config form -- they are edited via the tree and context menu instead. This avoids the
  need to extend ConfigEditorControl for LIST/MAP/ARRAY properties.
- Uniform editing regardless of element type
- Properties include: name, labels, CSS classes, channel references, etc.

### Toolbar

- **Apply**: Writes changes to `.view.xml` file(s), triggers reload in main window
- **Revert**: Discards all in-memory modifications across all loaded views, reloads from disk

## Server-Side Components

### ViewCommands

| Command | Purpose |
|---|---|
| `OpenDesigner` | Opens the designer as a new window (via `ReactWindowControl`). Available as a toolbar/menu action in the main application. |
| `LoadDesignTree` | Reads the root view's UIElement.Config tree recursively via ViewLoader's new config-only path, eagerly resolving ReferenceElement children by loading referenced `.view.xml` files. Returns tree model for the designer. |
| `LoadElementConfig` | Returns the ConfigurationItem for a selected tree node, to be rendered by ConfigEditorControl. |
| `SaveDesign` | Receives modified configuration, determines affected `.view.xml` file(s) based on each element's source file, serializes to XML via ConfigurationWriter, writes files, sends SSE reload event to main window. |

### Key Integration Points

- **ViewLoader**: Needs a new `getOrLoadConfig()` method that returns `ViewElement.Config`
  without UIElement instantiation. The existing `getOrLoadView()` should be refactored to
  use `getOrLoadConfig()` internally, so that parsing and caching logic is shared without
  code duplication. `getOrLoadView()` then just adds the instantiation step on top.
- **ConfigurationWriter**: Used to serialize Config trees back to XML. Re-serializes from
  the model; original formatting is not preserved.
- **ConfigEditorControl**: Renders scalar TypedConfiguration properties as forms -- direct
  reuse for PLAIN, REF, and ITEM properties. No extension needed for LIST properties.
- **ReactWindowControl**: Designer opens as a window within the React UI; both windows
  share the same session and SSE channel. Cross-window communication via server-side
  control state — no special event types or window registries needed.

## Out of Scope (for this iteration)

- **Model editor integration**: Type/attribute definitions stay in the separate model editor
- **Runtime personalization**: No per-user/per-role overlays; changes go directly to files
- **Drag & drop**: Element reordering via context menu only
- **Jump to View**: Semantics unclear for composable views (which "level" is active?); needs more design work
- **Inspect mode in app**: No click-to-select in the main window
- **Undo/Redo**: Not in first iteration; "Revert" discards all changes
- **Multi-user conflict handling**: Single-writer assumption for now
- **LIST property editing in ConfigEditorControl**: Structural properties edited via tree only
- **XML formatting preservation**: Re-serialization normalizes the XML
