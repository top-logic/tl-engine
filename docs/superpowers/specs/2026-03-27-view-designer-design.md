# View Designer: Unified UI Editor for React-based Views

**Date**: 2026-03-27
**Ticket**: #29108
**Status**: Prototype working

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
structure as a tree of configurations, paired with a configuration form for the selected
element. Changes are written back to `.view.xml` files.

### Key Design Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Editor scope | Structure, properties, channels, commands | Model editor stays separate |
| Persistence | Direct to `.view.xml` files | Simplest first step; no runtime personalization layer needed |
| Window model | Separate browser window (via ReactWindowRegistry) | Full screen for both app and editor; dual-monitor friendly |
| Navigation | Jump to View deferred to later iteration | Semantics unclear for composable views; needs more design work |
| Tree granularity | Full ConfigurationItem hierarchy | Every config is a node, including non-UIElement configs (NavItemConfig etc.) |
| Tree grouping | Virtual property group nodes | Multi-property containers show `[header]`, `[content]`, `[footer]` groups |
| Adding elements | Context menu in tree | "Add child" / "Remove" / "Move up/down" |
| Applying changes | Explicit "Apply" button | Avoids broken intermediate states from auto-apply |
| Data layer | ConfigurationItem instances only | No UIElement instantiation needed for the design view |

## Architecture

The View Designer is itself a `.view.xml` (`designer.view.xml`), opened as a child window
via `ReactWindowRegistry.openWindow()`. It uses the same infrastructure as any other view:
channels for reactive data, ViewCommands for server-side logic, SSE for updates.

### Cross-Window Communication

The designer is opened as a window within the React UI via `ReactWindowRegistry`. Both
windows share the same server-side session and SSE channel. Communication happens through
normal server-side ReactControl interaction -- no window-name lookup or special event types
needed.

### Config-Only Loading Path (implemented)

`ViewLoader` was refactored to expose `getOrLoadConfig()` which returns `ViewElement.Config`
without UIElement instantiation. The existing `getOrLoadView()` delegates to
`getOrLoadConfig()` internally, so parsing and caching logic is shared without code
duplication. A separate `CONFIG_CACHE` stores parsed configs with timestamp-based
invalidation.

### Generic Config Tree Traversal (implemented)

The `DesignTreeBuilder` generically discovers all `PolymorphicConfiguration`-typed LIST and
ITEM properties via `PropertyDescriptor.getInstanceType()`. This includes not only
`UIElement.Config` but also intermediate config types (e.g. `NavItemConfig`,
`BottomBarItemConfig`) that contain UIElement children in their own properties.

Each container property always gets its own **virtual group node** (e.g. `[header]`,
`[content]`, `[items]`). No inlining optimization -- the tree always shows the full
structure. `ReferenceElement.Config` nodes eagerly load the referenced view's config.

### Data Flow: Apply (implemented)

1. User clicks "Apply" in the designer
2. `SaveDesignCommand` resolves the `designTree` channel to get the root `DesignTreeNode`
3. Command traverses the tree, collecting `ViewElement.Config` instances grouped by source file
4. Serializes each config back to XML via `ConfigurationWriter`
5. Writes file(s) to disk via `FileManager.getIDEFileOrNull()`
6. ViewLoader cache invalidation happens automatically via timestamp comparison

### Data Flow: Revert (implemented)

1. User clicks "Revert" in the designer
2. `RevertDesignCommand` rebuilds the `DesignTreeNode` tree from disk via `DesignTreeBuilder`
3. Sets the new root on the `designTree` channel, triggering `DesignerTreeElement` to rebuild
4. Clears the `selectedNode` channel

## Editor Layout

### SplitPanel Structure

```
+------------------------------------------------------------------+
| Toolbar: [View Designer]                    [Apply] [Revert]     |
+----------------------------+-------------------------------------+
|                            |                                     |
|  Application Tree          |  Configuration Form                 |
|                            |                                     |
|  v view                    |  Konfigurationstyp: SidebarElement  |
|    v [content]             |  Implementierung: SidebarElement     |
|      v app-shell           |  Personalisierungsschluessel:        |
|        > [footer]          |  Aktiver Artikel: dashboard          |
|        v [content]         |  Zusammengeklappt: [ ]               |
|          v sidebar     <-- |                                     |
|            v [items]       |                                     |
|              > nav-item    |                                     |
|                "dashboard" |                                     |
|              > nav-item    |                                     |
|                "tabs-demo" |                                     |
|              ...           |                                     |
|        > [header]          |                                     |
|                            |                                     |
+----------------------------+-------------------------------------+
```

### Tree (Left Panel)

- Root is the application's root `ViewElement.Config` (displayed as "view")
- Every `ConfigurationItem` is a node -- including non-UIElement configs like `NavItemConfig`
- Container properties with `PolymorphicConfiguration` children get virtual group nodes
  (displayed in brackets, e.g. `[content]`, `[items]`, `[header]`)
- `ReferenceElement` nodes eagerly load and display referenced view children
- Node label: `@TagName` value + identifying attribute (e.g., `nav-item "dashboard"`)
- Label heuristic checks properties: "name", "title-key", "attribute", "view", "id"
- Each node tracks its source `.view.xml` file path
- **Context menu** on right-click:
  - "Add child" -- creates a default `StackElement.Config`
  - "Remove" -- removes from parent's children list
  - "Move up" / "Move down" -- reorders within parent

### Configuration Form (Right Panel)

- `ConfigEditorControl` renders scalar TypedConfiguration properties (PLAIN, REF, ITEM)
- Wrapped in `EditorWrapperControl` that swaps the editor when selection changes
- Virtual group nodes show no form (empty right panel)
- Changes are written directly to the `ConfigurationItem` instance via `ConfigFieldModel`
- Structural list properties (children, channels) are NOT shown -- edited via tree only

### Toolbar

- **Apply**: Writes changes to `.view.xml` file(s)
- **Revert**: Discards all in-memory modifications, reloads tree from disk

## Implementation

### New/Modified Files

**`com.top_logic.layout.view` module:**
- `ViewLoader.java` -- refactored: `getOrLoadConfig()`, `CONFIG_CACHE`, shared helpers
- `designer/DesignTreeNode.java` -- tree node: config or virtual group, with parent/children
- `designer/DesignTreeBuilder.java` -- generic config traversal, virtual groups, ref resolution
- `designer/DesignerTreeElement.java` -- UIElement building ReactTreeControl from DesignTreeNode
- `designer/ConfigEditorElement.java` -- UIElement wrapping ConfigEditorControl with selection swap
- `designer/OpenDesignerCommand.java` -- opens designer window via ReactWindowRegistry
- `designer/SaveDesignCommand.java` -- serializes configs back to .view.xml files
- `designer/RevertDesignCommand.java` -- reloads tree from disk
- `designer/AddChildCommand.java` -- adds StackElement child to selected node
- `designer/RemoveElementCommand.java` -- removes node from parent
- `designer/MoveElementCommand.java` -- moves node up/down in parent's children
- `views/designer.view.xml` -- the designer's own view definition
- `element/AppBarElement.java` -- extended with `<commands>` section for toolbar buttons
- `I18NConstants.java` -- error keys for save failures
- `pom.xml` -- added `tl-layout-configedit` dependency

**`com.top_logic.layout.react` module:**
- `ReactCompositeControl.java` -- `addChild()` now triggers SSE patch via `putState()`
- `ReactAppBarControl.java` -- extends `ToolbarControl` for command button support
- `ReactTreeControl.java` -- added context menu support (openContextMenu, @ReactCommand handlers)
- `ToolbarControl.java` -- base class for controls with toolbar buttons

**`com.top_logic.demo` module:**
- `app.view.xml` -- added `<open-designer>` command to app-bar

## Current Status (2026-03-30)

### Working
- Tree shows complete application hierarchy (all config types, all depths)
- Virtual group nodes for multi-property containers
- ReferenceElement resolution (view-ref -> referenced .view.xml)
- Config form appears on selection with editable scalar properties
- Property changes propagate to ConfigurationItem instances
- Apply writes modified configs back to .view.xml files
- Context menu infrastructure (Add/Remove/Move commands wired)

### Known Issues
- XML formatting lost on save (ConfigurationWriter normalizes to single line)
- Main window reload after Apply not yet implemented (TODO in SaveDesignCommand)
- I18N resources for error keys not yet generated (startup warnings)
- Revert and context menu not yet tested end-to-end

## Out of Scope (for this iteration)

- **Model editor integration**: Type/attribute definitions stay in the separate model editor
- **Runtime personalization**: No per-user/per-role overlays; changes go directly to files
- **Drag & drop**: Element reordering via context menu only
- **Jump to View**: Semantics unclear for composable views; needs more design work
- **Inspect mode in app**: No click-to-select in the main window
- **Undo/Redo**: Not in first iteration; "Revert" discards all changes
- **Multi-user conflict handling**: Single-writer assumption for now
- **LIST property editing in ConfigEditorControl**: Structural properties edited via tree only
- **XML formatting preservation**: Re-serialization normalizes the XML
