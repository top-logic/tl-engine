# Command Display Design

## Context

The view system (Ticket #29108) needs structured command rendering in the React UI.
Currently, `PanelElement` creates flat `ReactButtonControl` instances for toolbar
commands with no clique grouping, no separators, no menu cliques, no button-bar
placement, and no icon support.

This document designs the command display system: toolbar and button-bar rendering
with clique grouping, inline/menu display modes, and icon support.

**Module:** `com.top_logic.layout.react` (React controls), `com.top_logic.layout.view` (view elements)

**Spec reference:** `docs/plans/TL-VIEWS-SPEC.md`, Section 9

**Scope:** Toolbar + button-bar. Context menu is deferred.

## Design Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Toolbar model | Structural container | Toolbar is a layout container for arbitrary child controls, not limited to buttons. Allows inputs, selects, or custom controls in toolbars. |
| Grouping | Java-side (Approach A) | Java computes full clique structure. React renders it. Business logic stays where config data lives. |
| React component | New TLToolbar | Dedicated component for toolbar rendering. TLPanel embeds it as child controls for toolbar and button-bar areas. |
| Button icons | Icon + label + icon-only | Support all three display variants. DisplayMode determined by config or toolbar density. |
| Context menu | Deferred | Not part of this design. |

## 1. Data Model (Java to React)

The toolbar is a `ReactToolbarControl` that sends structured state to `TLToolbar`:

```typescript
interface ToolbarState {
  groups: CliqueGroup[];
}

interface CliqueGroup {
  name: string;                 // Clique name (e.g., "create", "edit", "export")
  display: 'inline' | 'menu';  // How the group is rendered
  label?: string;               // Menu trigger label (display='menu' only)
  icon?: string;                // Menu trigger icon (display='menu' only)
  items: ChildDescriptor[];     // Arbitrary child controls (buttons, inputs, etc.)
  subGroups?: CliqueGroup[];    // Nested sub-groups (for menu cliques only)
}
```

Toolbar and button-bar share the same `ToolbarState` structure, filtered by
`CommandPlacement`. Both are embedded in `TLPanel` as child controls.

**Key insight:** `items` are `ChildDescriptor` references (the existing mechanism
for embedding child `ReactControl`s). This means any `ReactControl` can appear in
a toolbar group, not just buttons.

## 2. React Components

### TLToolbar (new)

A structural React component that renders clique groups with separators and
dropdown wrappers.

```
[InlineGroup] | [InlineGroup] | [MenuGroup v] [MenuGroup v]
```

- **InlineGroup**: renders child controls (`TLChild`) side by side
- **MenuGroup**: renders a dropdown trigger button; clicking opens a popup
  containing child controls with separator lines between sub-groups
- Empty groups (all items hidden) are suppressed
- Separators between adjacent empty groups are suppressed
- Single-command menu groups render as a direct button (no dropdown)

### TLButton (enhanced)

Add icon and display mode support:

```typescript
interface TLButtonProps {
  command?: string;
  label?: string;
  icon?: string;                                       // NEW
  disabled?: boolean;
  displayMode?: 'icon-only' | 'icon-label' | 'label-only'; // NEW
}
```

Rendering:
- `icon-only`: icon element + `aria-label` for accessibility
- `icon-label`: icon element + text label
- `label-only`: text label only (current behavior, fallback)

### TLPanel (modified)

Replace flat `toolbarButtons` array with structured toolbar and button-bar areas:

```
+-- header: title + toolbar (TLChild -> TLToolbar) ---+
|                                                      |
|  content (TLChild)                                   |
|                                                      |
+-- button-bar (TLChild -> TLToolbar) ----------------+
```

State changes:
- Remove: `toolbarButtons: ChildDescriptor[]`
- Add: `toolbar: ChildDescriptor` (a TLToolbar control, may be null)
- Add: `buttonBar: ChildDescriptor` (a TLToolbar control, may be null)

## 3. Java-Side Architecture

### New: ReactToolbarControl

`ReactControl` subclass mapping to `TLToolbar`. Manages clique groups and their
child controls.

```java
public class ReactToolbarControl extends ReactControl {
    private static final String REACT_MODULE = "TLToolbar";
    private static final String GROUPS = "groups";

    public void addGroup(String name, String display, String label,
                         String icon, List<ReactControl> items) { ... }
    public void addGroup(String name, String display, String label,
                         String icon, List<ReactControl> items,
                         List<CliqueGroupData> subGroups) { ... }
}
```

### New: ToolbarBuilder

Builds a `ReactToolbarControl` from a `CommandScope` and placement filter:

```java
public class ToolbarBuilder {
    public static ReactToolbarControl build(
            CommandScope scope,
            CommandPlacement placement,
            List<LocalCliqueConfig> localCliques,
            ViewContext context) {
        // 1. Filter commands by placement
        // 2. Group by clique
        // 3. Sort groups by clique order (standard + local cliques)
        // 4. For each group: create child controls, determine display mode
        // 5. Handle sub-groups for menu cliques
        // 6. Return ReactToolbarControl
    }
}
```

### New: CliqueRegistry

Provides standard clique definitions with ordering and display mode:

| Order | Name       | Display  | Default Label |
|-------|------------|----------|---------------|
| 1     | `create`   | inline   | --            |
| 2     | `edit`     | inline   | --            |
| 3     | `delete`   | inline   | --            |
| 4     | `commit`   | inline   | --            |
| 5     | `navigate` | inline   | --            |
| 6     | `view`     | menu     | "View"        |
| 7     | `export`   | menu     | "Export"       |
| 8     | `more`     | menu     | "More"         |

Local cliques use `after`/`before` to insert at the correct position.

### Modified: PanelElement

Replace manual flat button creation with `ToolbarBuilder`:

```java
@Override
public IReactControl createControl(ViewContext context) {
    // Phase 1-3: same as today (command models, scope, child controls)

    // Phase 4: Build structured toolbars
    ReactToolbarControl toolbar = ToolbarBuilder.build(
        scope, CommandPlacement.TOOLBAR, config.getCliques(), context);
    ReactToolbarControl buttonBar = ToolbarBuilder.build(
        scope, CommandPlacement.BUTTON_BAR, config.getCliques(), context);

    // Phase 5: Create panel with toolbar and button-bar
    ReactPanelControl panel = new ReactPanelControl(
        _title, content, toolbar, buttonBar, ...);
}
```

### Modified: ReactPanelControl

Replace `addToolbarButton(ReactControl)` with toolbar/button-bar child controls:

```java
public ReactPanelControl(String title, ReactControl content,
        ReactToolbarControl toolbar, ReactToolbarControl buttonBar,
        boolean showMinimize, boolean showMaximize, boolean showPopOut) {
    // ...
    getReactState().put(TOOLBAR, toolbar);
    getReactState().put(BUTTON_BAR, buttonBar);
}
```

Remove `addToolbarButton()` and the flat `toolbarButtons` state.

### Modified: ReactButtonControl

Add icon and display mode:

```java
public void setIcon(String iconCssClass) {
    putState("icon", iconCssClass);
}

public void setDisplayMode(String mode) {
    putState("displayMode", mode);
}
```

## 4. Clique Behavior

### Ordering

Groups are rendered left-to-right in clique order. Within a group, commands appear
in declaration order (explicit) then registration order (implicit).

### Local cliques

```xml
<panel toolbar="true">
  <cliques>
    <clique name="workflow" after="edit" display="inline"/>
  </cliques>
```

Inserts `workflow` between `edit` (order 2) and `delete` (order 3).

### Menu clique rendering

1. Trigger button shows clique label + dropdown chevron icon
2. Click opens a popup containing the group's child controls
3. Sub-groups within the popup are separated by `<hr>` lines
4. Single-command menu group renders as direct button (no dropdown)

### Empty group handling

Groups where all items are hidden (executability `NOT_EXEC_HIDDEN`) are not
rendered. Adjacent separators collapse.

## 5. Implicit Command Updates

When implicit commands change (added/removed via `CommandScope`):

1. `CommandScope` fires change listeners
2. `PanelElement`'s toolbar listener rebuilds the toolbar structure
3. Updated `ReactToolbarControl` state is pushed via SSE
4. `TLToolbar` re-renders with new groups

## 6. Rendering Examples

### Toolbar with inline and menu cliques

```
Toolbar:  [+ New] [Import]  |  [Edit]  |  [Delete]  |  [View v]  [Export v]
           ---- create ----     - edit -    - delete -    - view -    - export -
```

### Button bar

```
                                                    [Save]  [Cancel]
                                                    ------- commit --------
```

### Menu clique expanded

```
Toolbar:  ...  [Export v]
                 |- Excel
                 |- PDF
                 |--------
                 |- Print labels
```

### Local clique

```
Toolbar:  [Edit]  |  [Approve] [Reject] [Escalate]  |  [Delete]
          - edit -   ---------- workflow -----------    - delete -
```
